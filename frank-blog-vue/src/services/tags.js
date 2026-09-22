function normalizeTag(tag = {}) {
  return {
    id: tag.id ?? tag.tagId ?? null,
    name: String(tag.name ?? tag.tagName ?? '').trim(),
  }
}

export async function loadTags() {
  const response = await fetch('/api/tag/list', { credentials: 'include' })
  if (!response.ok) throw new Error(`标签加载失败（HTTP ${response.status}）`)

  const result = await response.json()
  if (result.code !== 200) throw new Error(result.msg || '标签加载失败')
  if (!Array.isArray(result.data)) throw new Error('标签接口返回数据格式不正确')

  return result.data.map(normalizeTag).filter((tag) => tag.name)
}

export async function createTag(tagName) {
  const formData = new URLSearchParams()
  formData.set('tagName', tagName)

  const response = await fetch('/api/tag/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData,
    credentials: 'include',
  })
  if (!response.ok) throw new Error(`标签创建失败（HTTP ${response.status}）`)

  const result = await response.json()
  if (result.code !== 200) throw new Error(result.msg || '标签创建失败')

  const tag = normalizeTag(result.data)
  if (!tag.name) throw new Error('标签接口返回数据不完整')
  return tag
}
