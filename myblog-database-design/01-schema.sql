-- myblog 数据库设计 v1.0 | 2026-09-12
-- 前端依据：muggle893/myblog @ bb92a53e0041fa12ae1d1c97c9e7323107e91232
-- MySQL >= 8.0.16（或 8.4）；InnoDB；时间统一存 UTC。
-- 在新数据库执行一次；没有 DROP/TRUNCATE，不覆盖已有表。
-- CREATE DATABASE 的 IF NOT EXISTS 不表示后续建表可重复运行。
-- 用有建库/建表权限的迁移账号执行，应用运行账号无需 DDL 权限。
SET NAMES utf8mb4;
SET time_zone = '+00:00';
CREATE DATABASE IF NOT EXISTS myblog
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE myblog;

CREATE TABLE blog_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号ID；Java Long；JSON对前端以字符串传输',
  username VARCHAR(50) NOT NULL COMMENT '登录账号；后端统一转小写并去首尾空白',
  password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '带算法标识的密码哈希；禁用账号可为空，禁止明文密码',
  email VARCHAR(254) NULL COMMENT '可选邮箱；不填写时存NULL，未来用于邮箱验证码登录',
  phone VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '可选手机号；统一国家码格式，不填写时存NULL',
  role_code VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'AUTHOR' COMMENT 'ADMIN站点管理、AUTHOR写作、READER仅阅读；当前只创建一个ADMIN',
  status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0禁用、1启用；启用前必须设置有效密码哈希',
  last_login_at DATETIME(3) NULL COMMENT '最近一次成功登录时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_email (email),
  UNIQUE KEY uk_user_phone (phone),
  CONSTRAINT ck_user_username CHECK (CHAR_LENGTH(TRIM(username)) BETWEEN 1 AND 50),
  CONSTRAINT ck_user_email CHECK (email IS NULL OR CHAR_LENGTH(TRIM(email)) > 0),
  CONSTRAINT ck_user_phone CHECK (phone IS NULL OR CHAR_LENGTH(TRIM(phone)) > 0),
  CONSTRAINT ck_user_role CHECK (role_code IN ('ADMIN','AUTHOR','READER')),
  CONSTRAINT ck_user_status CHECK (status IN (0,1)),
  CONSTRAINT ck_user_password CHECK (status = 0 OR (password_hash IS NOT NULL AND CHAR_LENGTH(password_hash) > 0))
) ENGINE=InnoDB COMMENT='登录账号；不承载公开个人资料';

CREATE TABLE blog_asset (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件内部ID',
  public_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '后端生成UUID，组成稳定下载地址；UUID不能代替权限校验',
  uploader_id BIGINT NOT NULL COMMENT '上传者账号ID',
  original_name VARCHAR(255) NOT NULL COMMENT '原文件名；仅用于展示和下载，不直接拼接磁盘路径',
  media_type VARCHAR(127) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '服务端验证后的MIME类型',
  asset_kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'IMAGE图片或FILE附件',
  size_bytes BIGINT UNSIGNED NOT NULL COMMENT '文件字节数；应用当前限制单文件20MiB',
  storage_provider VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'LOCAL' COMMENT 'LOCAL、OSS或S3等；业务代码通过存储适配器读取',
  bucket VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT '' COMMENT '对象存储桶；本地存储为空字符串',
  object_key VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '后端生成的ASCII相对存储键；不存服务器IP或临时签名URL',
  sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '可选内容摘要；仅作查重线索，不设唯一约束',
  status VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UPLOADING' COMMENT 'UPLOADING、READY、DELETING、DELETED',
  row_version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件绑定/清理协调版本号',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传记录创建时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '状态修改时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_asset_public_id (public_id),
  UNIQUE KEY uk_asset_storage_key (storage_provider, bucket, object_key),
  KEY idx_asset_uploader_created (uploader_id, created_at, id),
  KEY idx_asset_cleanup (status, updated_at, id),
  KEY idx_asset_uploader_hash (uploader_id, sha256),
  CONSTRAINT fk_asset_uploader FOREIGN KEY (uploader_id) REFERENCES blog_user(id) ON DELETE RESTRICT,
  CONSTRAINT ck_asset_kind CHECK (asset_kind IN ('IMAGE','FILE')),
  CONSTRAINT ck_asset_status CHECK (status IN ('UPLOADING','READY','DELETING','DELETED'))
) ENGINE=InnoDB COMMENT='图片和附件元数据；文件二进制存磁盘或对象存储';

CREATE TABLE blog_user_profile (
  user_id BIGINT NOT NULL COMMENT '账号ID，同时是主键；每个账号最多一份公开资料',
  nickname VARCHAR(20) NOT NULL DEFAULT 'Frank' COMMENT '昵称；修改后通过关联查询同步显示',
  signature VARCHAR(50) NOT NULL DEFAULT '慢慢积累，每一篇都算数。' COMMENT '个人签名，可留空',
  about_title VARCHAR(60) NOT NULL DEFAULT '你好，很高兴认识你' COMMENT '自我介绍标题',
  about_body TEXT NOT NULL COMMENT '自我介绍纯文本，保留换行；应用限制3000字',
  avatar_asset_id BIGINT NULL COMMENT '头像文件ID；NULL时显示昵称首字',
  cover_asset_id BIGINT NULL COMMENT '个人主页封面文件ID；NULL时使用前端默认插画',
  cover_position TINYINT UNSIGNED NOT NULL DEFAULT 48 COMMENT '个人封面竖直位置0到100，对应CSS百分比',
  row_version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁；防止多个编辑页面覆盖资料',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间UTC',
  PRIMARY KEY (user_id),
  KEY idx_profile_avatar (avatar_asset_id),
  KEY idx_profile_cover (cover_asset_id),
  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES blog_user(id) ON DELETE RESTRICT,
  CONSTRAINT fk_profile_avatar FOREIGN KEY (avatar_asset_id) REFERENCES blog_asset(id) ON DELETE RESTRICT,
  CONSTRAINT fk_profile_cover FOREIGN KEY (cover_asset_id) REFERENCES blog_asset(id) ON DELETE RESTRICT,
  CONSTRAINT ck_profile_nickname CHECK (CHAR_LENGTH(TRIM(nickname)) > 0),
  CONSTRAINT ck_profile_about_title CHECK (CHAR_LENGTH(TRIM(about_title)) > 0),
  CONSTRAINT ck_profile_position CHECK (cover_position <= 100)
) ENGINE=InnoDB COMMENT='公开个人资料；与登录账号及站点设置分离';

CREATE TABLE blog_profile_tag (
  user_id BIGINT NOT NULL COMMENT '个人资料所属账号ID',
  tag_name VARCHAR(20) NOT NULL COMMENT '个人兴趣/技能标签；与文章标签分开',
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
  PRIMARY KEY (user_id, tag_name),
  KEY idx_profile_tag_order (user_id, sort_order),
  CONSTRAINT fk_profile_tag_user FOREIGN KEY (user_id) REFERENCES blog_user_profile(user_id) ON DELETE CASCADE,
  CONSTRAINT ck_profile_tag_name CHECK (CHAR_LENGTH(TRIM(tag_name)) > 0)
) ENGINE=InnoDB COMMENT='个人介绍标签；应用限制每人最多12个';

CREATE TABLE blog_site (
  id TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '当前为单站点配置，固定1；不是多租户站点表',
  owner_user_id BIGINT NOT NULL COMMENT '站长账号；首页展示此人的公开资料',
  blog_title VARCHAR(24) NOT NULL DEFAULT '拾光手记' COMMENT '导航、网页标题、页脚中的博客名称',
  hero_title VARCHAR(50) NOT NULL DEFAULT '把学到的，写成自己的。' COMMENT '首页横幅标题',
  hero_subtitle VARCHAR(100) NOT NULL DEFAULT '代码、阅读、日常，以及一些正在发生的思考。' COMMENT '首页横幅介绍，可留空',
  row_version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '站点设置乐观锁版本号',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间UTC',
  PRIMARY KEY (id),
  KEY idx_site_owner (owner_user_id),
  CONSTRAINT fk_site_owner FOREIGN KEY (owner_user_id) REFERENCES blog_user(id) ON DELETE RESTRICT,
  CONSTRAINT ck_site_singleton CHECK (id = 1),
  CONSTRAINT ck_site_title CHECK (CHAR_LENGTH(TRIM(blog_title)) > 0),
  CONSTRAINT ck_site_hero_title CHECK (CHAR_LENGTH(TRIM(hero_title)) > 0)
) ENGINE=InnoDB COMMENT='全站设置；与某个作者的个人资料区分';

CREATE TABLE blog_category (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称；当前对应Java学习、学习笔记、生活日常',
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分类排序',
  enabled TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否允许新文章选择；禁用不隐藏已有公开文章',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_name (name),
  CONSTRAINT ck_category_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
  CONSTRAINT ck_category_enabled CHECK (enabled IN (0,1))
) ENGINE=InnoDB COMMENT='全站文章分类；当前一篇文章最多属于一个分类';

CREATE TABLE blog_tag (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章标签ID',
  name VARCHAR(20) NOT NULL COMMENT '标签名称；去首尾空白，大小写不敏感去重',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name (name),
  CONSTRAINT ck_tag_name CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB COMMENT='文章/草稿共享标签词典，不放个人兴趣标签';

CREATE TABLE blog_article (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '正式文章ID；API返回字符串',
  author_id BIGINT NOT NULL COMMENT '真实作者；所有写接口使用登录身份校验',
  category_id BIGINT NULL COMMENT '分类ID；NULL代表未分类',
  slug VARCHAR(160) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '可选稳定URL别名；初期可为NULL并使用ID路由',
  title VARCHAR(200) NOT NULL COMMENT '文章标题；正式发布不能为空',
  summary VARCHAR(500) NOT NULL DEFAULT '' COMMENT '列表摘要；后端发布时提取纯文本，可扩展手动摘要',
  content_markdown MEDIUMTEXT NOT NULL COMMENT 'Markdown原文；图片附件使用稳定资源地址',
  visibility VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PRIVATE' COMMENT 'PUBLIC公开、PRIVATE仅作者；默认不公开',
  status VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED已发布、ARCHIVED已下架；草稿单独存储',
  word_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '后端计算的内容字数，用于显示和阅读时间估算',
  reading_minutes INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '阅读分钟数；返回前端时格式化为N分钟',
  published_at DATETIME(3) NOT NULL COMMENT '首次发布时间UTC；更新文章不重置',
  row_version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号；每次修改正式文章加1',
  deleted_at DATETIME(3) NULL COMMENT '软删除时间；NULL表示未删除',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次创建正式文章时间UTC',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_slug (slug),
  UNIQUE KEY uk_article_id_author (id, author_id),
  KEY idx_article_public_feed (deleted_at, status, visibility, published_at DESC, id DESC),
  KEY idx_article_category_feed (category_id, deleted_at, status, visibility, published_at DESC, id DESC),
  KEY idx_article_author_feed (author_id, deleted_at, status, published_at DESC, id DESC),
  CONSTRAINT fk_article_author FOREIGN KEY (author_id) REFERENCES blog_user(id) ON DELETE RESTRICT,
  CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES blog_category(id) ON DELETE RESTRICT,
  CONSTRAINT ck_article_title CHECK (CHAR_LENGTH(TRIM(title)) > 0),
  CONSTRAINT ck_article_body CHECK (CHAR_LENGTH(TRIM(content_markdown)) > 0),
  CONSTRAINT ck_article_visibility CHECK (visibility IN ('PUBLIC','PRIVATE')),
  CONSTRAINT ck_article_status CHECK (status IN ('PUBLISHED','ARCHIVED')),
  CONSTRAINT ck_article_minutes CHECK (reading_minutes >= 1)
) ENGINE=InnoDB COMMENT='正式文章快照；自动保存不得直接覆盖此表';

CREATE TABLE blog_article_tag (
  article_id BIGINT NOT NULL COMMENT '正式文章ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文章内标签展示顺序',
  PRIMARY KEY (article_id, tag_id),
  KEY idx_article_tag_reverse (tag_id, article_id),
  CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
  CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id) REFERENCES blog_tag(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='文章与标签多对多；应用限制每篇最多10个';

CREATE TABLE blog_draft (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '草稿ID；用于自动保存和幂等发布',
  owner_id BIGINT NOT NULL COMMENT '草稿所有者；任何草稿均不对游客开放',
  article_id BIGINT NULL COMMENT '被修改文章ID；NULL表示新文章草稿',
  base_article_version INT UNSIGNED NULL COMMENT '开始修改时的正式文章版本；发布时检测并发冲突',
  published_article_id BIGINT NULL COMMENT '发布成功对应的文章ID；重复发布同一草稿时返回这个结果',
  status VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE编辑中、PUBLISHED已发布、DISCARDED已丢弃',
  active_edit_article_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN article_id ELSE NULL END) STORED COMMENT '只用于限制每个作者对一篇文章最多一个活跃编辑草稿；禁止手动写入',
  category_id BIGINT NULL COMMENT '草稿分类，可为空',
  title VARCHAR(200) NOT NULL DEFAULT '' COMMENT '草稿标题可以为空；显示为无标题草稿',
  content_markdown MEDIUMTEXT NOT NULL COMMENT '草稿正文可以为空；保存时仍校验大小和资源引用',
  visibility VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PRIVATE' COMMENT '期望的发布可见性；不代表草稿本身可公开访问',
  word_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '草稿字数，后端随保存更新',
  row_version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '自动保存乐观锁；避免多标签页较旧内容覆盖新内容',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '草稿创建时间UTC',
  saved_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后保存/状态变更时间UTC',
  PRIMARY KEY (id),
  UNIQUE KEY uk_draft_active_edit (owner_id, active_edit_article_id),
  KEY idx_draft_owner_list (owner_id, status, saved_at DESC, id DESC),
  KEY idx_draft_article_owner (article_id, owner_id),
  KEY idx_draft_result_owner (published_article_id, owner_id),
  KEY idx_draft_category (category_id),
  KEY idx_draft_cleanup (status, saved_at, id),
  CONSTRAINT fk_draft_owner FOREIGN KEY (owner_id) REFERENCES blog_user(id) ON DELETE RESTRICT,
  CONSTRAINT fk_draft_article_owner FOREIGN KEY (article_id, owner_id) REFERENCES blog_article(id, author_id) ON DELETE RESTRICT,
  CONSTRAINT fk_draft_result_owner FOREIGN KEY (published_article_id, owner_id) REFERENCES blog_article(id, author_id) ON DELETE RESTRICT,
  CONSTRAINT fk_draft_category FOREIGN KEY (category_id) REFERENCES blog_category(id) ON DELETE RESTRICT,
  CONSTRAINT ck_draft_status CHECK (status IN ('ACTIVE','PUBLISHED','DISCARDED')),
  CONSTRAINT ck_draft_visibility CHECK (visibility IN ('PUBLIC','PRIVATE')),
  CONSTRAINT ck_draft_base CHECK ((article_id IS NULL AND base_article_version IS NULL) OR (article_id IS NOT NULL AND base_article_version IS NOT NULL)),
  CONSTRAINT ck_draft_result CHECK ((status = 'PUBLISHED' AND published_article_id IS NOT NULL) OR (status IN ('ACTIVE','DISCARDED') AND published_article_id IS NULL)),
  CONSTRAINT ck_draft_edit_result CHECK (article_id IS NULL OR published_article_id IS NULL OR article_id = published_article_id)
) ENGINE=InnoDB COMMENT='新文章及已发布文章的修改草稿；发布后保留幂等结果，草稿箱只列ACTIVE';

CREATE TABLE blog_draft_tag (
  draft_id BIGINT NOT NULL COMMENT '草稿ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '草稿内标签展示顺序',
  PRIMARY KEY (draft_id, tag_id),
  KEY idx_draft_tag_reverse (tag_id, draft_id),
  CONSTRAINT fk_draft_tag_draft FOREIGN KEY (draft_id) REFERENCES blog_draft(id) ON DELETE CASCADE,
  CONSTRAINT fk_draft_tag_tag FOREIGN KEY (tag_id) REFERENCES blog_tag(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='草稿标签独立于正式文章，保存草稿不影响线上标签';

CREATE TABLE blog_article_asset (
  article_id BIGINT NOT NULL COMMENT '正式文章ID',
  asset_id BIGINT NOT NULL COMMENT '文件ID',
  usage_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'INLINE_IMAGE正文图片、ATTACHMENT下载附件',
  PRIMARY KEY (article_id, asset_id, usage_type),
  KEY idx_article_asset_reverse (asset_id, article_id),
  CONSTRAINT fk_article_asset_article FOREIGN KEY (article_id) REFERENCES blog_article(id) ON DELETE CASCADE,
  CONSTRAINT fk_article_asset_asset FOREIGN KEY (asset_id) REFERENCES blog_asset(id) ON DELETE RESTRICT,
  CONSTRAINT ck_article_asset_usage CHECK (usage_type IN ('INLINE_IMAGE','ATTACHMENT'))
) ENGINE=InnoDB COMMENT='已发布正文引用的文件；同一文件可多处使用';

CREATE TABLE blog_draft_asset (
  draft_id BIGINT NOT NULL COMMENT '草稿ID',
  asset_id BIGINT NOT NULL COMMENT '文件ID',
  usage_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'INLINE_IMAGE正文图片、ATTACHMENT下载附件',
  PRIMARY KEY (draft_id, asset_id, usage_type),
  KEY idx_draft_asset_reverse (asset_id, draft_id),
  CONSTRAINT fk_draft_asset_draft FOREIGN KEY (draft_id) REFERENCES blog_draft(id) ON DELETE CASCADE,
  CONSTRAINT fk_draft_asset_asset FOREIGN KEY (asset_id) REFERENCES blog_asset(id) ON DELETE RESTRICT,
  CONSTRAINT ck_draft_asset_usage CHECK (usage_type IN ('INLINE_IMAGE','ATTACHMENT'))
) ENGINE=InnoDB COMMENT='草稿引用文件；不因保存草稿而改变正式文章文件权限';
