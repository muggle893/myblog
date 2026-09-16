import { storage } from './storage'
import { appState, refreshPosts } from './state'
import { samplePosts } from '../data/samplePosts'

// 保存最近一次接口返回的文章列表。
// null 表示还没有成功请求过，此时 allPosts() 会使用本地演示数据。
// 请求成功后，这个变量变成数组，首页就会使用后端数据。
let remotePosts = null

// 后端的 publishedAt 是 LocalDateTime，例如："2026-09-16T14:30:00"。
// 首页卡片只需要日期，所以截取前 10 个字符得到 "2026-09-16"。
function formatPublishedDate(value) {
  const text = String(value || '')
  return text ? text.slice(0, 10) : ''
}

// ArticleListVO.tags 是 List<TagVO>，但首页模板只需要显示标签文字。
// 这里把 TagVO 对象转换成字符串数组，模板就可以统一使用 post.tags。
// name、tagName、label 是对不同 TagVO 命名习惯的兼容处理。
function normalizeTags(tags) {
  if (Array.isArray(tags)) {
    return tags
      .map((tag) => typeof tag === 'object' ? (tag.name || tag.tagName || tag.label || '') : tag)
      .map((tag) => String(tag).trim())
      .filter(Boolean)
  }
  return String(tags || '').split(',').map((tag) => tag.trim()).filter(Boolean)
}

// 将后端 ArticleListVO 转换成页面已有的文章结构。
//
// 这里使用对象展开运算符 ...post，先保留后端原始字段，
// 再用下面的同名或新名字覆盖页面需要的字段。
// 这样以后详情页需要 categoryId、publishedAt 等字段时仍然可以使用。
export const normalizePost = (post = {}) => ({
  ...post,
  id: String(post.id ?? post.articleId ?? post.article_id ?? ''),
  title: post.title || post.articleTitle || '',
  category: post.categoryName || post.category || post.type || '',
  tags: normalizeTags(post.tags),
  date: formatPublishedDate(post.publishedAt || post.date || post.createTime || post.createdAt),
  read: post.readingMinutes ? `${post.readingMinutes} 分钟` : (post.read || post.readCount || post.viewCount || '0'),
  excerpt: post.excerpt || post.summary || post.description || '',
  body: post.body || post.content || '',
  // 后端使用 PUBLIC / PRIVATE，前端页面使用小写 private / public。
  // 统一成小写后，模板中的判断就不需要重复处理大小写。
  visibility: String(post.visibility || '').toUpperCase() === 'PRIVATE' || post.isPrivate === true ? 'private' : 'public',
})

// 下面两个函数是权限相关的纯函数：
// 输入同一个文章对象，就会得到同一个结果，不会修改原对象。
export const isPublicPost = (post) => normalizePost(post).visibility === 'public'
export const canReadPost = (post) => appState.owner || isPublicPost(post)

export function allPosts() {
  // 读取 reactive 对象的属性，让 Vue 能知道这个函数依赖 postsRevision。
  // revision 变化时，调用 allPosts() 的 computed 会重新计算。
  void appState.postsRevision
  const source = remotePosts || [...storage.get('frank-posts', []), ...samplePosts]
  const merged = source.map(normalizePost)
  const seen = new Set()
  return merged.filter((post) => post?.id && !seen.has(post.id) && (seen.add(post.id), true))
}

export function visiblePosts() { return allPosts().filter(canReadPost) }
export function findPost(id) { return allPosts().find((post) => post.id === id) || null }

// 请求首页文章列表。
// 这是 async 函数，所以调用它会得到 Promise，调用方需要 await 或 catch。
// 首页只读取 ArticleListVO；列表对象不包含 Markdown 正文。
export async function loadPosts(author) {
  const name = String(author || '').trim()
  if (!name) throw new Error('缺少作者用户名')

  // URLSearchParams 会自动进行 URL 编码，避免用户名中的特殊字符破坏 URL。
  const query = new URLSearchParams({ author: name })
  // credentials: include 让浏览器在跨请求时也携带登录 Cookie。
  // 后端正是通过 Cookie 判断当前用户能否看到私有文章。
  const response = await fetch(`/api/article/list?${query}`, { credentials: 'include' })

  // response.ok 只检查 HTTP 层面，例如 200、404、500。
  if (!response.ok) throw new Error(`文章请求失败（HTTP ${response.status}）`)

  // response.json() 也会返回 Promise，所以需要 await 等待 JSON 解析完成。
  // HTTP 200 只代表请求到达后端，还需要检查业务层 Result.code。
  const result = await response.json()
  if (result.code !== 200) throw new Error(result.msg || '文章加载失败')
  if (!Array.isArray(result.data)) throw new Error('文章接口返回数据格式不正确')

  // map 不修改后端返回的原数组，而是创建一个统一格式的新数组。
  remotePosts = result.data.map(normalizePost)
  // 修改 revision，让依赖 appState.postsRevision 的 computed 重新计算。
  refreshPosts()
  return remotePosts
}

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
