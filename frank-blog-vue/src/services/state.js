import { reactive } from 'vue'
import { storage } from './storage'

const defaultProfile = {
  nickname: 'Frank',
  blogTitle: '拾光手记',
  heroTitle: '把学到的，写成自己的。',
  heroSubtitle: '代码、阅读、日常，以及一些正在发生的思考。',
  signature: '慢慢积累，每一篇都算数。',
  avatarImageId: '',
}

function readOwner() {
  try { return sessionStorage.getItem('frank-owner-session') === '1' } catch { return false }
}

export const appState = reactive({
  owner: readOwner(),
  theme: storage.get('frank-theme', 'light'),
  profile: { ...defaultProfile, ...storage.get('frank-profile', {}) },
  postsRevision: 0,
  draftsRevision: 0,
  toastMessage: '',
  toastVisible: false,
})

let toastTimer
export function toast(message) {
  appState.toastMessage = message
  appState.toastVisible = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { appState.toastVisible = false }, 3000)
}

export function loginOwner() {
  try {
    sessionStorage.setItem('frank-owner-session', '1')
    appState.owner = true
    return true
  } catch {
    return false
  }
}

export function logoutOwner() {
  try { sessionStorage.removeItem('frank-owner-session') } catch {}
  appState.owner = false
}

export function setTheme(theme) {
  appState.theme = theme === 'dark' ? 'dark' : 'light'
  storage.set('frank-theme', appState.theme)
}

export function saveProfile(profile) {
  const next = { ...defaultProfile, ...profile }
  if (!storage.set('frank-profile', next)) return false
  Object.assign(appState.profile, next)
  return true
}

export function refreshPosts() { appState.postsRevision++ }
export function refreshDrafts() { appState.draftsRevision++ }
export { defaultProfile }
