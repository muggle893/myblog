-- 只读查询示例，可在建库后执行。SQL不修改业务数据。
-- 应用中把@变量改成MyBatis #{参数}，禁止使用${}拼接用户输入。
-- @actor_id必须取自服务端已验证会话；游客为NULL，不能接受前端自报身份。
USE myblog;
SET @actor_id = NULL;
SET @article_id = 1;
SET @category_id = 1;

-- 1. 游客首页：列表不查询正文；正式发布、公开、未删除。
SELECT a.id, a.title, a.summary, a.author_id, p.nickname,
       a.category_id, c.name AS category_name,
       a.published_at, a.reading_minutes, a.visibility
FROM blog_article a
JOIN blog_user_profile p ON p.user_id = a.author_id
LEFT JOIN blog_category c ON c.id = a.category_id
WHERE a.deleted_at IS NULL AND a.status = 'PUBLISHED' AND a.visibility = 'PUBLIC'
ORDER BY a.published_at DESC, a.id DESC
LIMIT 10 OFFSET 0;
-- 标签应按本页article_id批量查询并在Service中组装，避免一篇文章查一次。

-- 2. 文章详情：仅公开文章或作者本人；不存在/无权访问均可返回404。
-- 当前不给ADMIN自动读取他人私有文章的权限。
SELECT a.*, p.nickname AS author_nickname
FROM blog_article a
JOIN blog_user_profile p ON p.user_id = a.author_id
WHERE a.id = @article_id AND a.deleted_at IS NULL AND a.status = 'PUBLISHED'
  AND (a.visibility = 'PUBLIC' OR a.author_id = @actor_id);

-- 3. 首页分类计数：不可把私有文章数量泄露给游客。
SELECT c.id, c.name, COUNT(a.id) AS article_count
FROM blog_category c
JOIN blog_article a ON a.category_id = c.id
WHERE a.deleted_at IS NULL AND a.status = 'PUBLISHED'
  AND (a.visibility = 'PUBLIC' OR a.author_id = @actor_id)
GROUP BY c.id, c.name, c.sort_order
ORDER BY c.sort_order, c.id;

-- 4. 作者草稿箱：只查自己的ACTIVE记录；其他状态对页面隐藏。
SELECT id, article_id, title, category_id, visibility, word_count, saved_at, row_version,
       CASE WHEN article_id IS NULL THEN 'new' ELSE 'edit' END AS kind
FROM blog_draft
WHERE owner_id = @actor_id AND status = 'ACTIVE'
ORDER BY saved_at DESC, id DESC;

-- 5. “最近在关注”与标签计数必须从可见文章关联出发，不直接统计整个词典。
SELECT t.id, t.name, COUNT(DISTINCT a.id) AS article_count
FROM blog_tag t
JOIN blog_article_tag atg ON atg.tag_id = t.id
JOIN blog_article a ON a.id = atg.article_id
WHERE a.deleted_at IS NULL AND a.status = 'PUBLISHED'
  AND (a.visibility = 'PUBLIC' OR a.author_id = @actor_id)
GROUP BY t.id, t.name
ORDER BY article_count DESC, t.id DESC;

-- 6. 获取公开站点资料；不能向游客返回blog_user的密码、邮箱、手机号。
SELECT s.blog_title, s.hero_title, s.hero_subtitle, s.owner_user_id,
       p.nickname, p.signature, p.about_title, p.about_body,
       p.cover_position, avatar.public_id AS avatar_public_id,
       cover.public_id AS cover_public_id
FROM blog_site s
JOIN blog_user_profile p ON p.user_id = s.owner_user_id
LEFT JOIN blog_asset avatar ON avatar.id = p.avatar_asset_id AND avatar.status = 'READY'
LEFT JOIN blog_asset cover ON cover.id = p.cover_asset_id AND cover.status = 'READY'
WHERE s.id = 1;
-- profile.row_version/site.row_version只在有权编辑时返回给编辑接口，分别用于并发检查。
