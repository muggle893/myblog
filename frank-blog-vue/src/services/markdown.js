export function renderMarkdown(text = '') {
  if (!window.marked || !window.DOMPurify) return ''
  const html = window.marked.parse(text)
    .replace(/src="blog-image:([a-zA-Z0-9-]+)"/g, 'data-local-image="$1"')
    .replace(/href="blog-file:([a-zA-Z0-9-]+)"/g, 'data-local-file="$1"')
  return window.DOMPurify.sanitize(html, { FORBID_TAGS:['style','iframe','form'], FORBID_ATTR:['style'] })
}

export async function enhanceContent(container) {
  if (!container) return
  try { await window.blogImages?.hydrate(container) } catch {}
  if (window.renderMathInElement) {
    window.renderMathInElement(container, {
      delimiters:[{left:'$$',right:'$$',display:true},{left:'$',right:'$',display:false}],
      throwOnError:false,
      trust:false,
    })
  }
  if (window.hljs) container.querySelectorAll('pre code:not([data-highlighted])').forEach((el)=>window.hljs.highlightElement(el))
}
