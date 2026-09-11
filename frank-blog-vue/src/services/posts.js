import { storage } from './storage'
import { appState, refreshPosts } from './state'
import { samplePosts } from '../data/samplePosts'

export const normalizePost = (post) => ({ ...post, visibility: post?.visibility === 'private' ? 'private' : 'public' })
export const isPublicPost = (post) => normalizePost(post).visibility === 'public'
export const canReadPost = (post) => appState.owner || isPublicPost(post)

export function allPosts() {
  void appState.postsRevision
  const merged = [...storage.get('frank-posts', []), ...samplePosts].map(normalizePost)
  const seen = new Set()
  return merged.filter((post) => post?.id && !seen.has(post.id) && (seen.add(post.id), true))
}

export function visiblePosts() { return allPosts().filter(canReadPost) }
export function findPost(id) { return allPosts().find((post) => post.id === id) || null }

export function saveNewPost(data) {
  const post = { ...data, id: 'local-' + Date.now(), date: new Date().toLocaleDateString('sv-SE') }
  const ok = storage.set('frank-posts', [post, ...storage.get('frank-posts', [])])
  if (ok) refreshPosts()
  return ok ? post : null
}

export function updatePost(existing, data) {
  const updated = { ...existing, ...data, id: existing.id, date: existing.date || new Date().toLocaleDateString('sv-SE'), updatedAt: new Date().toISOString() }
  const local = storage.get('frank-posts', [])
  const index = local.findIndex((post) => post.id === existing.id)
  if (index >= 0) local[index] = updated
  else local.unshift(updated)
  const ok = storage.set('frank-posts', local)
  if (ok) refreshPosts()
  return ok ? updated : null
}
