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
  return Boolean(readUserToken())
}

function readUserToken() {
  try { return localStorage.getItem('user-token') || '' } catch { return '' }
}

function readTokenClaims(token = readUserToken()) {
  try {
    const payload = String(token).replace(/^Bearer\s+/i, '').split('.')[1]
    if (!payload) return {}
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const binary = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='))
    const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0))
    const claims = JSON.parse(new TextDecoder().decode(bytes))
    return claims && typeof claims === 'object' ? claims : {}
  } catch {
    return {}
  }
}

// 首页请求文章列表需要知道当前登录用户的用户名。
// 用户名和登录标记一样，只在当前浏览器标签页会话中保存。
function readUsername() {
  try {
    return sessionStorage.getItem('frank-owner-username') || localStorage.getItem('frank-owner-username') || readTokenClaims().username || ''
  } catch {
    return readTokenClaims().username || ''
  }
}

function readUserId() {
  const id = readTokenClaims().id
  return id === null || id === undefined ? '' : String(id)
}

// reactive 会把普通对象变成响应式对象：属性变化时，使用这些属性的模板和 computed 会更新。
// owner 控制前端权限界面，username 用于向后端传递 author 参数。
export const appState = reactive({
  owner: readOwner(),
  username: readUsername(),
  userId: readUserId(),
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

// 登录成功后保存 token 和用户名；token 是后续接口鉴权的唯一依据。
export function loginOwner(username = '', token = '') {
  if (!token) return false
  const claims = readTokenClaims(token)
  const resolvedUsername = String(claims.username || username)
  const userId = claims.id === null || claims.id === undefined ? '' : String(claims.id)
  try {
    localStorage.setItem('user-token', token)
    localStorage.setItem('frank-owner-username', resolvedUsername)
  } catch {
    try { localStorage.removeItem('user-token') } catch {}
    return false
  }
  try {
    sessionStorage.setItem('frank-owner-session', '1')
    sessionStorage.setItem('frank-owner-username', resolvedUsername)
  } catch {}
  appState.owner = true
  appState.username = resolvedUsername
  appState.userId = userId
  return true
}

// 退出时清理前端权限状态和文章接口所需的用户名。
// 清理 appState 很重要，否则当前页面仍可能显示作者专属按钮。
export function logoutOwner() {
  try {
    sessionStorage.removeItem('frank-owner-session')
    sessionStorage.removeItem('frank-owner-username')
  } catch {}
  try { localStorage.removeItem('user-token') } catch {}
  try { localStorage.removeItem('frank-owner-username') } catch {}
  appState.owner = false
  appState.username = ''
  appState.userId = ''
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
