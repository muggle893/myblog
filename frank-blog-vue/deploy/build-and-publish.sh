#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="${1:-$PWD}"
WEB_ROOT="${2:-/var/www/frank-blog}"

cd "$PROJECT_DIR"

echo "[1/4] 安装前端依赖..."
npm install

echo "[2/4] 构建 Vue 生产版本..."
npm run build

echo "[3/4] 发布 dist 到 $WEB_ROOT ..."
sudo mkdir -p "$WEB_ROOT"
sudo rm -rf "$WEB_ROOT"/*
sudo cp -a dist/. "$WEB_ROOT"/
sudo chmod -R a+rX "$WEB_ROOT"

echo "[4/4] 检查并重载 Nginx..."
sudo nginx -t
sudo systemctl reload nginx

echo "发布完成。"
