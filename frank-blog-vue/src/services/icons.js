const icons = {
  book:'<path d="M4 4h6a3 3 0 0 1 3 3v14a4 4 0 0 0-4-2H4z"/><path d="M13 7a3 3 0 0 1 3-3h4v15h-3a4 4 0 0 0-4 2"/>',
  pen:'<path d="m15 4 5 5M4 20l5-1L20 8a2 2 0 0 0-5-5L4 14z"/>',
  moon:'<path d="M20 14A8 8 0 0 1 10 4a8 8 0 1 0 10 10z"/>',
  arrow:'<path d="M4 12h15m-5-5 5 5-5 5"/>',
  back:'<path d="M20 12H5m5-5-5 5 5 5"/>',
  user:'<circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/>',
  lock:'<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>',
  eye:'<path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12"/><circle cx="12" cy="12" r="3"/>',
  folder:'<path d="M3 7V5h6l2 3h10v12H3z"/>',
  tag:'<path d="M3 3h8l10 10-8 8L3 11z"/><circle cx="7" cy="7" r="1"/>',
  chevron:'<path d="m9 5 7 7-7 7"/>',
  logout:'<path d="M10 4H4v16h6m4-4 4-4-4-4m-4 4h11"/>',
  save:'<path d="M4 3h13l4 4v14H3V3zM7 3v6h9V3M7 21v-8h10v8"/>',
  drafts:'<path d="M6 3h9l4 4v14H6z"/><path d="M14 3v5h5M9 12h7M9 16h7"/>',
  trash:'<path d="M4 7h16M9 7V4h6v3m-8 0 1 14h8l1-14M10 11v6M14 11v6"/>',
}
export function icon(name) { return icons[name] || icons.book }
