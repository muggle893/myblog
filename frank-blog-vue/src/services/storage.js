export const storage = {
  get(key, fallback) {
    try {
      const value = localStorage.getItem(key)
      return value == null ? fallback : (JSON.parse(value) ?? fallback)
    } catch {
      return fallback
    }
  },
  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
      return true
    } catch {
      return false
    }
  },
  remove(key) {
    try { localStorage.removeItem(key) } catch {}
  },
}
