# MyBlog 数据库设计包

基于2026-09-12最新版Vue前端，覆盖封面/头像/个人介绍、公开与私有文章、多个草稿、Markdown图片和附件。

## 建议阅读顺序

1. 打开 `myblog-er.png` 看总体E-R关系；放大查看使用 `myblog-er.svg`。
2. 阅读 `database-design.md` 理解表结构、发布事务、权限和扩展方式。
3. 对照 `field-dictionary.md` 理解每个字段。
4. 在新的MySQL开发数据库环境运行 `01-schema.sql`，再按需运行 `02-initial-data.sql`。
5. 用 `03-query-examples.sql` 学习查询写法。

最低要求：MySQL8.0.16，亦按MySQL8.4语法兼容设计。已使用真实MySQL8.0.46执行校验，结果见 `validation.md`。

初始化账号默认禁用，没有通用默认密码。先通过后端PasswordEncoder生成密码哈希，再设置哈希并启用账号。正式登录与资源权限必须由后端实现。

SQL用于初次建库，不是现有数据库升级脚本，也不会导入浏览器本地文章或图片。没有执行到你的服务器。

图源提供Mermaid `.mmd` 和Graphviz `.dot`；3张分图用于看清个人资料、写作及附件关系。数据字典和图由交付DDL派生。
