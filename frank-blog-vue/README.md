# 拾光手记 · Vue 3 版本

这是从前一版纯 HTML 原型迁移得到的 **Vue 3 + Vite + Vue Router** 前端项目。

## 2026-09-12：编辑个人主页封面与自我介绍

登录后打开“关于我”→“编辑资料”，可以：

- 更换个人主页封面，预览图片，调整画面上下位置，或恢复默认日系封面。
- 更换或移除头像，头像继续同步到导航、首页与个人主页。
- 编辑自我介绍标题、正文和标签；正文保留换行与空行分段。
- 点击“保存修改”后生效；取消或按 Esc 不会改变已保存的资料。

原有昵称、首页文案和签名继续保留。封面、自我介绍、头像目前保存于当前浏览器，尚未上传服务器；其他设备和访客不会自动看到这些修改，后续需接入你实现的 SSM 资料保存与图片上传接口。

本次基于 GitHub `myblog` 仓库提交 `e51bb635ccf23fdc82f07a9882da94a6dda95dc7` 修改，保留仓库已有样式。功能改动涉及：

- `src/views/ProfileView.vue`
- `src/services/state.js`
- `public/assets/style.css`

如果本地已有其他修改，请对照以上三个文件合并，不要直接覆盖整个项目。包内 `dist/` 为本次编译好的静态网站，源码未包含 `node_modules/`。自行构建时在前端目录执行 `npm install` 和 `npm run build`；部署时将 `dist/` 中的内容更新到 Nginx 当前的网站根目录，保留现有 Vue 路由回退配置。网页刷新后如仍显示旧页面，可按 Ctrl+F5。

验证：Vite 生产构建通过；组件交互检查覆盖旧资料默认值、保存/取消、刷新后恢复、图片和存储失败回滚、标签限制、恢复封面与访客编辑入口。未连接真实后端。

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
