import { apiFetch } from './api'

// 资源上传接口的前端适配层。
// 开发环境中的 /api 会由 Vite 代理到 Spring Boot，生产环境则由 Nginx 转发。
export async function uploadAsset(file) {
  if (!(file instanceof File)) throw new Error('请选择要上传的文件')

  const formData = new FormData()
  // 后端接口约定的 MultipartFile 参数名就是 file，不能改成 files 或 uploadFile。
  formData.append('file', file)

  const response = await apiFetch('/api/asset/upload', {
    method: 'POST',
    body: formData,
  })

  if (!response.ok) throw new Error(`文件上传失败（HTTP ${response.status}）`)

  const result = await response.json()
  if (result.code !== 200) throw new Error(result.msg || '文件上传失败')

  const asset = normalizeAsset(result.data)
  if (!asset.assetId || !asset.publicId || !asset.assetKind) {
    throw new Error('文件上传接口返回的数据不完整')
  }

  return asset
}

// 只暴露页面需要的字段，同时兼容后端偶尔返回下划线命名的情况。
function normalizeAsset(asset = {}) {
  const rawUrl = String(asset.url ?? '').trim()

  return {
    assetId: asset.assetId ?? asset.asset_id ?? null,
    publicId: String(asset.publicId ?? asset.public_id ?? ''),
    originalName: String(asset.originalName ?? asset.original_name ?? ''),
    assetKind: String(asset.assetKind ?? asset.asset_kind ?? '').toUpperCase(),
    // 后端返回 /asset/...，前端统一通过 /api 代理访问 Spring Boot。
    url: rawUrl.startsWith('/asset/') ? '/api' + rawUrl : rawUrl,
  }
}
