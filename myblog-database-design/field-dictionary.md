# 字段字典

与 `01-schema.sql` 同源生成；PK=主键，FK=外键。详细唯一索引、CHECK约束及删除规则请查看SQL。

## blog_user

登录账号；不承载公开个人资料

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 账号ID；Java Long；JSON对前端以字符串传输 |
| `username` | `VARCHAR(50) NOT NULL` |  | 登录账号；后端统一转小写并去首尾空白 |
| `password_hash` | `VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NULL` |  | 带算法标识的密码哈希；禁用账号可为空，禁止明文密码 |
| `email` | `VARCHAR(254) NULL` |  | 可选邮箱；不填写时存NULL，未来用于邮箱验证码登录 |
| `phone` | `VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NULL` |  | 可选手机号；统一国家码格式，不填写时存NULL |
| `role_code` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'AUTHOR'` |  | ADMIN站点管理、AUTHOR写作、READER仅阅读；当前只创建一个ADMIN |
| `status` | `TINYINT UNSIGNED NOT NULL DEFAULT 0` |  | 0禁用、1启用；启用前必须设置有效密码哈希 |
| `last_login_at` | `DATETIME(3) NULL` |  | 最近一次成功登录时间 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 创建时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 修改时间UTC |

## blog_asset

图片和附件元数据；文件二进制存磁盘或对象存储

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 文件内部ID |
| `public_id` | `CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` |  | 后端生成UUID，组成稳定下载地址；UUID不能代替权限校验 |
| `uploader_id` | `BIGINT NOT NULL` | FK | 上传者账号ID |
| `original_name` | `VARCHAR(255) NOT NULL` |  | 原文件名；仅用于展示和下载，不直接拼接磁盘路径 |
| `media_type` | `VARCHAR(127) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` |  | 服务端验证后的MIME类型 |
| `asset_kind` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` |  | IMAGE图片或FILE附件 |
| `size_bytes` | `BIGINT UNSIGNED NOT NULL` |  | 文件字节数；应用当前限制单文件20MiB |
| `storage_provider` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'LOCAL'` |  | LOCAL、OSS或S3等；业务代码通过存储适配器读取 |
| `bucket` | `VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT ''` |  | 对象存储桶；本地存储为空字符串 |
| `object_key` | `VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` |  | 后端生成的ASCII相对存储键；不存服务器IP或临时签名URL |
| `sha256` | `CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL` |  | 可选内容摘要；仅作查重线索，不设唯一约束 |
| `status` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'UPLOADING'` |  | UPLOADING、READY、DELETING、DELETED |
| `row_version` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 文件绑定/清理协调版本号 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 上传记录创建时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 状态修改时间UTC |

## blog_user_profile

公开个人资料；与登录账号及站点设置分离

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `user_id` | `BIGINT NOT NULL` | PK, FK | 账号ID，同时是主键；每个账号最多一份公开资料 |
| `nickname` | `VARCHAR(20) NOT NULL DEFAULT 'Frank'` |  | 昵称；修改后通过关联查询同步显示 |
| `signature` | `VARCHAR(50) NOT NULL DEFAULT '慢慢积累，每一篇都算数。'` |  | 个人签名，可留空 |
| `about_title` | `VARCHAR(60) NOT NULL DEFAULT '你好，很高兴认识你'` |  | 自我介绍标题 |
| `about_body` | `TEXT NOT NULL` |  | 自我介绍纯文本，保留换行；应用限制3000字 |
| `avatar_asset_id` | `BIGINT NULL` | FK | 头像文件ID；NULL时显示昵称首字 |
| `cover_asset_id` | `BIGINT NULL` | FK | 个人主页封面文件ID；NULL时使用前端默认插画 |
| `cover_position` | `TINYINT UNSIGNED NOT NULL DEFAULT 48` |  | 个人封面竖直位置0到100，对应CSS百分比 |
| `row_version` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 乐观锁；防止多个编辑页面覆盖资料 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 创建时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 修改时间UTC |

## blog_profile_tag

个人介绍标签；应用限制每人最多12个

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `user_id` | `BIGINT NOT NULL` | PK, FK | 个人资料所属账号ID |
| `tag_name` | `VARCHAR(20) NOT NULL` | PK | 个人兴趣/技能标签；与文章标签分开 |
| `sort_order` | `SMALLINT UNSIGNED NOT NULL DEFAULT 0` |  | 显示顺序，数值越小越靠前 |

## blog_site

全站设置；与某个作者的个人资料区分

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `TINYINT UNSIGNED NOT NULL DEFAULT 1` | PK | 当前为单站点配置，固定1；不是多租户站点表 |
| `owner_user_id` | `BIGINT NOT NULL` | FK | 站长账号；首页展示此人的公开资料 |
| `blog_title` | `VARCHAR(24) NOT NULL DEFAULT '拾光手记'` |  | 导航、网页标题、页脚中的博客名称 |
| `hero_title` | `VARCHAR(50) NOT NULL DEFAULT '把学到的，写成自己的。'` |  | 首页横幅标题 |
| `hero_subtitle` | `VARCHAR(100) NOT NULL DEFAULT '代码、阅读、日常，以及一些正在发生的思考。'` |  | 首页横幅介绍，可留空 |
| `row_version` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 站点设置乐观锁版本号 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 创建时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 修改时间UTC |

## blog_category

全站文章分类；当前一篇文章最多属于一个分类

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 分类ID |
| `name` | `VARCHAR(50) NOT NULL` |  | 分类名称；当前对应Java学习、学习笔记、生活日常 |
| `sort_order` | `SMALLINT UNSIGNED NOT NULL DEFAULT 0` |  | 分类排序 |
| `enabled` | `TINYINT UNSIGNED NOT NULL DEFAULT 1` |  | 是否允许新文章选择；禁用不隐藏已有公开文章 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 创建时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 修改时间UTC |

## blog_tag

文章/草稿共享标签词典，不放个人兴趣标签

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 文章标签ID |
| `name` | `VARCHAR(20) NOT NULL` |  | 标签名称；去首尾空白，大小写不敏感去重 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 创建时间UTC |

## blog_article

正式文章快照；自动保存不得直接覆盖此表

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 正式文章ID；API返回字符串 |
| `author_id` | `BIGINT NOT NULL` | FK | 真实作者；所有写接口使用登录身份校验 |
| `category_id` | `BIGINT NULL` | FK | 分类ID；NULL代表未分类 |
| `slug` | `VARCHAR(160) CHARACTER SET ascii COLLATE ascii_bin NULL` |  | 可选稳定URL别名；初期可为NULL并使用ID路由 |
| `title` | `VARCHAR(200) NOT NULL` |  | 文章标题；正式发布不能为空 |
| `summary` | `VARCHAR(500) NOT NULL DEFAULT ''` |  | 列表摘要；后端发布时提取纯文本，可扩展手动摘要 |
| `content_markdown` | `MEDIUMTEXT NOT NULL` |  | Markdown原文；图片附件使用稳定资源地址 |
| `visibility` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PRIVATE'` |  | PUBLIC公开、PRIVATE仅作者；默认不公开 |
| `status` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PUBLISHED'` |  | PUBLISHED已发布、ARCHIVED已下架；草稿单独存储 |
| `word_count` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 后端计算的内容字数，用于显示和阅读时间估算 |
| `reading_minutes` | `INT UNSIGNED NOT NULL DEFAULT 1` |  | 阅读分钟数；返回前端时格式化为N分钟 |
| `published_at` | `DATETIME(3) NOT NULL` |  | 首次发布时间UTC；更新文章不重置 |
| `row_version` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 乐观锁版本号；每次修改正式文章加1 |
| `deleted_at` | `DATETIME(3) NULL` |  | 软删除时间；NULL表示未删除 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 首次创建正式文章时间UTC |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 最后修改时间UTC |

## blog_article_tag

文章与标签多对多；应用限制每篇最多10个

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `article_id` | `BIGINT NOT NULL` | PK, FK | 正式文章ID |
| `tag_id` | `BIGINT NOT NULL` | PK, FK | 标签ID |
| `sort_order` | `SMALLINT UNSIGNED NOT NULL DEFAULT 0` |  | 文章内标签展示顺序 |

## blog_draft

新文章及已发布文章的修改草稿；发布后保留幂等结果，草稿箱只列ACTIVE

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | PK | 草稿ID；用于自动保存和幂等发布 |
| `owner_id` | `BIGINT NOT NULL` | FK | 草稿所有者；任何草稿均不对游客开放 |
| `article_id` | `BIGINT NULL` | FK | 被修改文章ID；NULL表示新文章草稿 |
| `base_article_version` | `INT UNSIGNED NULL` |  | 开始修改时的正式文章版本；发布时检测并发冲突 |
| `published_article_id` | `BIGINT NULL` | FK | 发布成功对应的文章ID；重复发布同一草稿时返回这个结果 |
| `status` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'ACTIVE'` |  | ACTIVE编辑中、PUBLISHED已发布、DISCARDED已丢弃 |
| `active_edit_article_id` | `BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN article_id ELSE NULL END) STORED` |  | 只用于限制每个作者对一篇文章最多一个活跃编辑草稿；禁止手动写入 |
| `category_id` | `BIGINT NULL` | FK | 草稿分类，可为空 |
| `title` | `VARCHAR(200) NOT NULL DEFAULT ''` |  | 草稿标题可以为空；显示为无标题草稿 |
| `content_markdown` | `MEDIUMTEXT NOT NULL` |  | 草稿正文可以为空；保存时仍校验大小和资源引用 |
| `visibility` | `VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'PRIVATE'` |  | 期望的发布可见性；不代表草稿本身可公开访问 |
| `word_count` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 草稿字数，后端随保存更新 |
| `row_version` | `INT UNSIGNED NOT NULL DEFAULT 0` |  | 自动保存乐观锁；避免多标签页较旧内容覆盖新内容 |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |  | 草稿创建时间UTC |
| `saved_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` |  | 最后保存/状态变更时间UTC |

## blog_draft_tag

草稿标签独立于正式文章，保存草稿不影响线上标签

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `draft_id` | `BIGINT NOT NULL` | PK, FK | 草稿ID |
| `tag_id` | `BIGINT NOT NULL` | PK, FK | 标签ID |
| `sort_order` | `SMALLINT UNSIGNED NOT NULL DEFAULT 0` |  | 草稿内标签展示顺序 |

## blog_article_asset

已发布正文引用的文件；同一文件可多处使用

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `article_id` | `BIGINT NOT NULL` | PK, FK | 正式文章ID |
| `asset_id` | `BIGINT NOT NULL` | PK, FK | 文件ID |
| `usage_type` | `VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` | PK | INLINE_IMAGE正文图片、ATTACHMENT下载附件 |

## blog_draft_asset

草稿引用文件；不因保存草稿而改变正式文章文件权限

| 字段 | SQL类型与默认值 | 键 | 说明 |
|---|---|---|---|
| `draft_id` | `BIGINT NOT NULL` | PK, FK | 草稿ID |
| `asset_id` | `BIGINT NOT NULL` | PK, FK | 文件ID |
| `usage_type` | `VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL` | PK | INLINE_IMAGE正文图片、ATTACHMENT下载附件 |
