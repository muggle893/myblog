import { logoutOwner } from './state'

export async function apiFetch(input, options = {}) {
  const headers = new Headers(options.headers || {})
  const token = getUserToken()
  if (token) headers.set('user-token', token)

  const response = await fetch(input, {
    ...options,
    headers,
    credentials: options.credentials || 'include',
  })

  let authMessage = ''
  let result
  try {
    result = await response.clone().json()
  } catch {}
  if (response.status === 401 || Number(result?.code) === 401 || Number(result?.code) === -1) {
    authMessage = result?.msg || (response.status === 401 ? '登录已过期，请重新登录' : '请先登录后再访问')
  }

  if (authMessage) {
    logoutOwner()
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('user-auth-expired', { detail: authMessage }))
    }
  }

  return response
}

function getUserToken() {
  try {
    return localStorage.getItem('user-token') || ''
  } catch {
    return ''
  }
}