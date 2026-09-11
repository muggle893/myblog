import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

async function waitForLegacyAssets(timeout = 6000) {
  const start = Date.now()
  while (Date.now() - start < timeout) {
    if (window.marked && window.DOMPurify && window.blogImages) return
    await new Promise((resolve) => setTimeout(resolve, 30))
  }
  console.warn('部分 Markdown/本地图片资源加载较慢，页面会继续启动。')
}

await waitForLegacyAssets()
createApp(App).use(router).mount('#app')
