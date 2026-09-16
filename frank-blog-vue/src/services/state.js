import { reactive } from 'vue'
import { storage } from './storage'

const defaultProfile = {
  nickname: 'Frank',
  blogTitle: '拾光手记',
  heroTitle: '把学到的，写成自己的。',
  heroSubtitle: '代码、阅读、日常，以及一些正在发生的思考。',
  signature: '慢慢积累，每一篇都算数。',
  avatarImageId: '',
  coverImageId: '',
  coverPosition: 48,
  aboutTitle: '你好，很高兴认识你',
  aboutBody: '这里是我的学习笔记，也是一个慢慢生长的个人空间。\n喜欢把复杂的问题拆开，把学过的知识重新讲清楚。\n\n正在学习 Java 后端开发，也尝试用 Vue 做一些自己的小东西。希望每一次动手，都能让理解更深一点。',
  aboutTags: ['Java', 'SSM', 'Vue', '持续学习'],
}

function readOwner() {
  // sessionStorage 只能保存字符串；页面刷新后需要把 "1" 转回布尔值。
  // 某些浏览器隐私设置可能禁止 sessionStorage，因此使用 try/catch。
  try { return sessionStorage.getItem('frank-owner-session') === '1' } catch { return false }
}

// 首页请求文章列表需要知道当前登录用户的用户名。
// 用户名和登录标记一样，只在当前浏览器标签页会话中保存。
function readUsername() {
  try { return sessionStorage.getItem('frank-owner-username') || '' } catch { return '' }
}

// reactive 会把普通对象变成响应式对象：属性变化时，使用这些属性的模板和 computed 会更新。
// owner 控制前端权限界面，username 用于向后端传递 author 参数。
export const appState = reactive({
  owner: readOwner(),
  username: readUsername(),
  theme: storage.get('frank-theme', 'light'),
  profile: { ...defaultProfile, ...storage.get('frank-profile', {}) },
  postsRevision: 0,
  draftsRevision: 0,
  toastMessage: '',
  toastVisible: false,
})

let toastTimer
export function toast(message) {
  // 直接修改 reactive 对象的属性会触发使用它的组件重新渲染。
  appState.toastMessage = message
  appState.toastVisible = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { appState.toastVisible = false }, 3000)
}

// 登录成功后同时保存会话标记和用户名，刷新页面时可以恢复首页请求参数。
// 返回 true 表示 sessionStorage 写入成功；失败时由调用方决定是否只更新内存状态。
export function loginOwner(username = '') {
  try {
    sessionStorage.setItem('frank-owner-session', '1')
    sessionStorage.setItem('frank-owner-username', username)
    appState.owner = true
    appState.username = username
    return true
  } catch {
    return false
  }
}

// 退出时清理前端权限状态和文章接口所需的用户名。
// 清理 appState 很重要，否则当前页面仍可能显示作者专属按钮。
export function logoutOwner() {
  try {
    sessionStorage.removeItem('frank-owner-session')
    sessionStorage.removeItem('frank-owner-username')
  } catch {}
  appState.owner = false
  appState.username = ''
}

export function setTheme(theme) {
  // 只接受 dark，其余值统一回退到 light，避免状态出现未知主题。
  appState.theme = theme === 'dark' ? 'dark' : 'light'
  storage.set('frank-theme', appState.theme)
}

export function saveProfile(profile) {
  // 先合并默认值，避免旧版本保存的数据缺少新字段。
  const next = { ...defaultProfile, ...profile }
  if (!storage.set('frank-profile', next)) return false
  Object.assign(appState.profile, next)
  return true
}

// revision 是一个简单的“版本号”。文章数据变化时递增，
// 依赖它的 computed 会因此重新执行，即使文章数组本身不是 reactive。
export function refreshPosts() { appState.postsRevision++ }
export function refreshDrafts() { appState.draftsRevision++ }
export { defaultProfile }
