import { storage } from './storage'
import { appState, refreshDrafts } from './state'

const KEY = 'frank-drafts-v2'
const normalizeDraft = (draft) => draft && typeof draft === 'object'
  ? { ...draft, tags: Array.isArray(draft.tags) ? draft.tags : [], visibility: draft.visibility === 'private' ? 'private' : 'public' }
  : null

export function loadDraftRecords() {
  void appState.draftsRevision
  const records = (storage.get(KEY, []) || []).map(normalizeDraft).filter(Boolean)
  let changed = false
  const legacyNew = storage.get('frank-draft', null)
  if (legacyNew && typeof legacyNew === 'object' && !records.some((d) => d.legacyKey === 'frank-draft')) {
    records.unshift({ ...normalizeDraft(legacyNew), id:'draft-legacy-'+Date.now(), kind:'new', postId:null, legacyKey:'frank-draft', createdAt:new Date().toISOString(), savedAt:new Date().toISOString() })
    changed = true
  }
  try {
    for (let i=0;i<localStorage.length;i++) {
      const key = localStorage.key(i)
      if (!key?.startsWith('frank-edit-draft-')) continue
      const postId = key.slice('frank-edit-draft-'.length)
      const data = storage.get(key, null)
      if (data && typeof data === 'object' && !records.some((d) => d.legacyKey === key || (d.postId === postId && d.kind === 'edit'))) {
        records.unshift({ ...normalizeDraft(data), id:'draft-edit-legacy-'+postId, kind:'edit', postId, legacyKey:key, createdAt:new Date().toISOString(), savedAt:new Date().toISOString() })
        changed = true
      }
    }
  } catch {}
  if (changed) {
    storage.set(KEY, records)
    storage.remove('frank-draft')
    records.filter((d)=>d.legacyKey?.startsWith('frank-edit-draft-')).forEach((d)=>storage.remove(d.legacyKey))
    refreshDrafts()
  }
  return records.sort((a,b)=>String(b.savedAt||'').localeCompare(String(a.savedAt||'')))
}

export function getDraftRecord(id) { return loadDraftRecords().find((d)=>d.id===id) || null }
export function findEditDraft(postId) { return loadDraftRecords().find((d)=>d.kind==='edit' && d.postId===postId) || null }
export function draftCount() { return loadDraftRecords().length }

export function upsertDraftRecord(record) {
  const list = loadDraftRecords()
  const index = list.findIndex((d)=>d.id===record.id)
  const now = new Date().toISOString()
  const next = { ...normalizeDraft(record), savedAt: now, createdAt: record.createdAt || list[index]?.createdAt || now }
  if (index >= 0) list[index] = next
  else list.unshift(next)
  if (!storage.set(KEY, list)) return null
  refreshDrafts()
  return next
}

export function removeDraftRecord(id) {
  const list = loadDraftRecords()
  const target = list.find((d)=>d.id===id)
  const ok = storage.set(KEY, list.filter((d)=>d.id!==id))
  if (ok) {
    if (target?.legacyKey) storage.remove(target.legacyKey)
    refreshDrafts()
  }
  return ok
}
