# 拾光手记 · Vue 3 版本

这是从前一版纯 HTML 原型迁移得到的 **Vue 3 + Vite + Vue Router** 前端项目。

## 目前已经迁移的功能

- 首页日系书桌插画与原视觉样式
- 个人主页 / 登录页统一日系插画
- 游客只能查看公开文章
- 作者会话登录
- 公开 / 私有文章
- 文章详情页编辑按钮
- EasyMDE Markdown 编辑器
- Markdown 实时预览、代码高亮、KaTeX
- 自由添加文章标签
- 图片粘贴 / 拖拽、附件、Markdown 导入导出
- 头像上传与个人资料编辑
- 多草稿草稿箱
- 新文章草稿 / 已发布文章修改草稿
- 明暗主题

## 技术结构

```text
frank-blog-vue/
├─ public/
│  └─ assets/              # 日系插画、EasyMDE、KaTeX 等静态资源
├─ src/
│  ├─ components/          # 顶部导航、头像、Markdown 等公共组件
│  ├─ data/                # 演示文章
│  ├─ router/              # Vue Router
│  ├─ services/            # 登录、文章、草稿、localStorage 等逻辑
│  ├─ views/               # 首页、登录、个人主页、文章、编辑器、草稿箱
│  ├─ App.vue
│  └─ main.js
├─ deploy/                 # Linux + Nginx 部署示例
├─ index.html
├─ package.json
└─ vite.config.js
```

## 本地运行

推荐 Node.js 24 LTS。

Windows 可以直接双击：

```text
start-vue.bat
```

或者在终端中运行：

```bash
npm install
npm run dev
```

浏览器打开：

```text
http://localhost:5173/
```

生产构建：

```bash
npm run build
```

构建结果在：

```text
dist/
```

## 非常重要：当前仍然没有后端

这版已经是 Vue 项目，但**文章、草稿、头像、登录状态仍是浏览器本地模拟**：

- 文章 / 草稿：localStorage
- 头像 / 文章图片 / 附件：IndexedDB
- 登录：sessionStorage

因此，把前端部署到公网服务器后：

> 你在自己电脑浏览器里发布的文章，只存在于你这个浏览器中，其他游客的浏览器不会自动看到这些文章。

目前部署到服务器的意义主要是：

1. 验证 Vue 页面和交互；
2. 验证域名、Nginx、HTTPS 等前端部署流程；
3. 为后续接 Java/SSM 后端做好结构准备。

等后端和数据库完成后，需要把 `src/services/posts.js`、`drafts.js`、登录逻辑和图片存储替换成真正的 HTTP API。

Linux 部署请看：

**`DEPLOY_LINUX.md`**
