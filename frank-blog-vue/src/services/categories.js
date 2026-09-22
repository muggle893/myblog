function normalizeCategory(category = {}) {
  return {
    id: category.id ?? category.categoryId ?? null,
    name: String(category.name ?? '').trim(),
  }
}

export async function loadCategories() {
  const response = await fetch('/api/category/list', { credentials: 'include' })
  if (!response.ok) throw new Error(`分类加载失败（HTTP ${response.status}）`)

  const result = await response.json()
  if (result.code !== 200) throw new Error(result.msg || '分类加载失败')
  if (!Array.isArray(result.data)) throw new Error('分类接口返回数据格式不正确')

  return result.data.map(normalizeCategory).filter((category) => category.name)
}
