-- 可选初始化数据：在01-schema.sql成功执行之后，在空表中执行一次。
-- 不生成示例文章，不导入浏览器里的文章、头像、附件。
-- 默认创建禁用的frank账号，防止出现通用默认密码。
-- 如果准备立即登录：把下一行NULL替换为你用后端PasswordEncoder生成的完整哈希字符串。
-- 例如使用DelegatingPasswordEncoder时，完整值应含{bcrypt}等算法前缀。
-- 不要把真实明文密码或真实哈希提交到公开Git仓库。
USE myblog;
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET @owner_password_hash = NULL;
START TRANSACTION;
INSERT INTO blog_user(username, password_hash, role_code, status)
VALUES ('frank', @owner_password_hash, 'ADMIN', IF(@owner_password_hash IS NULL, 0, 1));
SET @owner_id = LAST_INSERT_ID();
INSERT INTO blog_user_profile(user_id, nickname, about_body)
VALUES (@owner_id, 'Frank', CONCAT(
  '这里是我的学习笔记，也是一个慢慢生长的个人空间。', CHAR(10),
  '喜欢把复杂的问题拆开，把学过的知识重新讲清楚。', CHAR(10), CHAR(10),
  '正在学习 Java 后端开发，也尝试用 Vue 做一些自己的小东西。希望每一次动手，都能让理解更深一点。'
));
INSERT INTO blog_profile_tag(user_id, tag_name, sort_order) VALUES
  (@owner_id, 'Java', 1), (@owner_id, 'SSM', 2), (@owner_id, 'Vue', 3), (@owner_id, '持续学习', 4);
INSERT INTO blog_site(id, owner_user_id) VALUES (1, @owner_id);
INSERT INTO blog_category(name, sort_order) VALUES
  ('Java 学习', 1), ('学习笔记', 2), ('生活日常', 3);
COMMIT;
-- 如果执行中报错，请ROLLBACK并排查；不要在客户端勾选“出错后继续”。
