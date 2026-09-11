# 拾光手记 Vue 前端 · Linux 服务器部署教程

下面按照“一台几乎什么都没有的 Linux 云服务器”来讲。

---

## 0. 先理解最终结构

Vue 项目不是直接把 `.vue` 文件交给浏览器运行。

开发阶段：

```text
Vue 源码
  ↓ npm run dev
Vite 开发服务器
```

正式部署：

```text
Vue 源码
  ↓ npm run build
生成 dist/
  ↓
Nginx 对外提供 dist 中的 HTML / JS / CSS / 图片
```

所以：

- Node.js / npm：主要负责安装依赖和构建；
- Nginx：负责真正对公网提供网页；
- 正式运行时不需要长期运行 `npm run dev`；
- 不要把 `vite preview` 当生产服务器。

本项目推荐：**Node.js 24 LTS + Nginx**。

---

# 第一部分：确认服务器

## 1. 登录 Linux 服务器

在 Windows PowerShell 中：

```powershell
ssh 你的用户名@服务器公网IP
```

常见用户名：

```text
root
ubuntu
centos
rocky
```

例如：

```powershell
ssh root@123.45.67.89
```

如果云厂商提供私钥：

```powershell
ssh -i C:\Users\你的用户名\.ssh\your-key.pem ubuntu@123.45.67.89
```

---

## 2. 查看 Linux 发行版

登录服务器后执行：

```bash
cat /etc/os-release
```

如果看到：

```text
Ubuntu
Debian
```

后面使用 `apt`。

如果看到：

```text
Rocky Linux
AlmaLinux
CentOS Stream
```

后面使用 `dnf`。

---

# 第二部分：安装基础工具和 Nginx

## 3A. Ubuntu / Debian

```bash
sudo apt update
sudo apt install -y nginx curl unzip git
```

启动 Nginx：

```bash
sudo systemctl enable --now nginx
```

检查状态：

```bash
sudo systemctl status nginx
```

看到：

```text
active (running)
```

即可。

---

## 3B. Rocky / AlmaLinux / CentOS Stream

```bash
sudo dnf install -y nginx curl unzip git
```

启动 Nginx：

```bash
sudo systemctl enable --now nginx
```

检查：

```bash
sudo systemctl status nginx
```

---

## 4. 开放服务器端口

云服务器一般有两层防火墙。

### 第一层：云厂商安全组

进入腾讯云 / 阿里云 / 华为云等控制台，为服务器放行：

```text
TCP 22   SSH
TCP 80   HTTP
TCP 443  HTTPS（以后域名和证书会用）
```

### 第二层：Linux 防火墙

Ubuntu 如果启用了 UFW：

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw status
```

Rocky / CentOS 如果启用了 firewalld：

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

这时在自己电脑浏览器打开：

```text
http://服务器公网IP
```

如果能看到 Nginx 默认欢迎页，说明网络和 Nginx 已经打通。

---

# 第三部分：安装 Node.js

## 5. 为什么推荐 NVM

不要急着：

```bash
apt install nodejs
```

Linux 仓库自带 Node.js 有时版本较旧。

这里使用 NVM 管理 Node 版本。

---

## 6. 安装 NVM

执行：

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.7/install.sh | bash
```

然后重新加载 Shell：

```bash
source ~/.bashrc
```

如果你使用 zsh：

```bash
source ~/.zshrc
```

检查：

```bash
nvm --version
```

---

## 7. 安装 Node.js 24 LTS

```bash
nvm install 24
nvm use 24
nvm alias default 24
```

检查：

```bash
node -v
npm -v
```

应该看到类似：

```text
v24.x.x
11.x.x
```

版本小号可能不同，这是正常的。

---

# 第四部分：把 Vue 项目传到服务器

## 8. 先在 Windows 解压项目

你拿到：

```text
frank-blog-vue.zip
```

既可以直接把 ZIP 上传服务器，也可以解压以后通过 Git 上传。

第一次部署最简单的方法：**直接传 ZIP**。

---

## 9. Windows PowerShell 上传 ZIP

先进入 ZIP 所在文件夹：

```powershell
cd D:\Downloads
```

然后：

```powershell
scp .\frank-blog-vue.zip root@服务器IP:/root/
```

如果服务器用户名是 ubuntu：

```powershell
scp .\frank-blog-vue.zip ubuntu@服务器IP:/home/ubuntu/
```

如果 `scp` 不方便，也可以使用 WinSCP 图形化上传。

---

# 第五部分：在服务器构建 Vue

## 10. 解压

以 root 用户为例：

```bash
cd /root
unzip frank-blog-vue.zip
cd frank-blog-vue
```

查看文件：

```bash
ls
```

应该看到：

```text
package.json
src
public
vite.config.js
index.html
```

---

## 11. 安装依赖

```bash
npm install
```

第一次会下载 Vue、Vite、Vue Router 等依赖。

安装结束后会多出：

```text
node_modules/
```

---

## 12. 先构建

```bash
npm run build
```

成功后会看到：

```text
dist/
```

检查：

```bash
ls dist
```

应该至少有：

```text
index.html
assets
```

这一步成功，说明 Vue 代码已经编译为可以上线的静态网页。

---

# 第六部分：发布 dist 到 Nginx

## 13. 创建网站目录

```bash
sudo mkdir -p /var/www/frank-blog
```

复制生产文件：

```bash
sudo rm -rf /var/www/frank-blog/*
sudo cp -a dist/. /var/www/frank-blog/
sudo chmod -R a+rX /var/www/frank-blog
```

检查：

```bash
ls -la /var/www/frank-blog
```

---

# 第七部分：配置 Nginx

## 14. 创建博客配置

```bash
sudo nano /etc/nginx/conf.d/frank-blog.conf
```

填入：

```nginx
server {
    listen 80;
    listen [::]:80;

    server_name 你的服务器公网IP;

    root /var/www/frank-blog;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /assets/build/ {
        try_files $uri =404;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location /assets/ {
        try_files $uri =404;
        add_header Cache-Control "no-cache";
    }
}
```

例如服务器 IP 是：

```text
123.45.67.89
```

则写：

```nginx
server_name 123.45.67.89;
```

保存 nano：

```text
Ctrl + O
Enter
Ctrl + X
```

项目里也已经准备了一份：

```text
deploy/nginx-frank-blog.conf.example
```

---

## 15. 为什么一定要 `try_files`

Vue Router 使用 history 模式。

例如用户访问：

```text
/profile
/article/ssm
/drafts
```

这些其实都由同一个：

```text
index.html
```

接管。

因此必须有：

```nginx
try_files $uri $uri/ /index.html;
```

否则：首页能打开，但是直接刷新 `/profile` 会出现 Nginx 404。

---

## 16. 检查 Nginx 配置

```bash
sudo nginx -t
```

如果看到：

```text
syntax is ok
test is successful
```

再执行：

```bash
sudo systemctl reload nginx
```

---

# 第八部分：访问博客

在自己的电脑浏览器打开：

```text
http://服务器公网IP
```

然后重点测试：

```text
首页
关于我
作者登录
写文章
草稿箱
文章详情
文章编辑
```

还要专门测试：

```text
http://服务器IP/profile
```

然后按 `F5` 刷新。

如果刷新后仍然正常，说明 Vue Router + Nginx 配置正确。

---

# 第九部分：以后怎么更新前端

以后我给你改了 Vue 代码，你重新把新源码放到服务器，然后：

```bash
cd /root/frank-blog-vue
npm install
npm run build
sudo rm -rf /var/www/frank-blog/*
sudo cp -a dist/. /var/www/frank-blog/
```

仅仅替换网页文件时，其实不需要重启 Nginx。

如果你修改了 Nginx 配置，再：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

项目里还准备了一个辅助脚本：

```bash
bash deploy/build-and-publish.sh
```

---

# 第十部分：一个更推荐的生产更新方式

实际上，Node.js **不是网站运行时的必需品**。

也可以在你的 Windows 电脑上：

```bash
npm install
npm run build
```

只把：

```text
dist/
```

上传到服务器。

这种情况下服务器甚至可以完全不安装 Node.js，只安装 Nginx。

也就是说：

```text
开发/构建电脑：Node.js + npm
服务器：Nginx
```

也是完全正常的部署方式。

你现在为了学习完整流程，可以先按照前面的“服务器安装 Node 并构建”方式做。

---

# 第十一部分：域名和 HTTPS（有域名后再做）

先把域名 A 记录解析到服务器公网 IP。

Nginx 中把：

```nginx
server_name 123.45.67.89;
```

改成：

```nginx
server_name blog.example.com;
```

Ubuntu / Debian 可以安装：

```bash
sudo apt install -y certbot python3-certbot-nginx
```

然后：

```bash
sudo certbot --nginx -d blog.example.com
```

Certbot 会帮助配置 HTTPS。

---

# 第十二部分：常见错误

## 1. 浏览器访问服务器 IP 超时

先检查：

```bash
sudo systemctl status nginx
```

再检查云厂商安全组是否开放 TCP 80。

---

## 2. 首页可以打开，但是刷新 `/profile` 404

基本就是缺少：

```nginx
try_files $uri $uri/ /index.html;
```

修改后：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

---

## 3. Nginx 显示 403 Forbidden

检查目录：

```bash
ls -la /var/www/frank-blog
```

确保 Nginx 至少能读取：

```bash
sudo chmod -R a+rX /var/www/frank-blog
```

Rocky / CentOS 如果启用 SELinux，还可以：

```bash
sudo restorecon -Rv /var/www/frank-blog
```

---

## 4. `npm install` 很慢或失败

先检查服务器是否能访问公网：

```bash
curl -I https://registry.npmjs.org/
```

如果服务器本身没有公网下载能力，可以改用：

```text
在自己电脑 npm run build
↓
只上传 dist
↓
服务器只运行 Nginx
```

---

## 5. 不要这样上线

不要长期运行：

```bash
npm run dev
```

也不要把：

```bash
npm run preview
```

当正式 Web 服务器。

生产环境使用：

```text
npm run build
+
Nginx
```

---

# 第十三部分：现在这版部署后的数据限制

这个问题非常重要。

目前还没有数据库和后端，所以：

```text
你电脑浏览器 localStorage 中的文章
≠
游客电脑浏览器 localStorage 中的文章
```

因此：

- 你在服务器网页上登录并写一篇文章，这篇文章只保存在“你当前使用的这个浏览器”；
- 另一个游客打开同一个网址，并不会得到你刚写的文章；
- 私有权限现在也只是 Vue 前端演示；
- 真正的作者账号、文章共享、草稿同步、图片上传都需要后端。

等后端阶段，建议结构变成：

```text
浏览器 Vue
    ↓ HTTP / JSON
Nginx /api/
    ↓
Java / SSM 后端
    ↓
MySQL
```

图片可以后面再选择：

```text
服务器本地文件
MinIO
OSS / COS
```

到那个阶段，再设计数据库和接口即可。
