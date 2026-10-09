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
  container.querySelectorAll('pre').forEach((pre) => {
    const code = pre.querySelector('code')
    if (!code || pre.dataset.enhanced) return

    pre.dataset.enhanced = 'true'
    const lines = code.textContent.split('\n').length
    const collapsible = lines > 14
    const actions = document.createElement('div')
    actions.className = 'code-block-actions'

    if (collapsible) pre.classList.add('is-collapsed')

    const copyButton = document.createElement('button')
    copyButton.className = 'code-block-button'
    copyButton.type = 'button'
    copyButton.textContent = '复制代码'
    copyButton.setAttribute('aria-label', '复制代码')
    copyButton.addEventListener('click', async () => {
      let copied = false
      try {
        await navigator.clipboard.writeText(code.textContent)
        copied = true
      } catch {
        const textarea = document.createElement('textarea')
        textarea.value = code.textContent
        textarea.setAttribute('readonly', '')
        textarea.style.position = 'fixed'
        textarea.style.opacity = '0'
        document.body.append(textarea)
        textarea.select()
        copied = document.execCommand('copy')
        textarea.remove()
      }
      copyButton.textContent = copied ? '已复制' : '复制失败'
      setTimeout(() => { copyButton.textContent = '复制代码' }, 1500)
    })
    actions.append(copyButton)

    const colorPicker = document.createElement('div')
    colorPicker.className = 'code-block-color-picker'
    const colorButton = document.createElement('button')
    colorButton.className = 'code-block-button'
    colorButton.type = 'button'
    colorButton.textContent = '背景色'
    colorButton.setAttribute('aria-label', '选择代码块背景色')
    colorButton.setAttribute('aria-expanded', 'false')
    const colorOptions = document.createElement('div')
    colorOptions.className = 'code-block-color-options'
    colorOptions.hidden = true
    colorOptions.setAttribute('role', 'group')
    colorOptions.setAttribute('aria-label', '代码块背景颜色')

    const backgroundColors = [
      { name: '默认', className: '', swatchClass: 'default' },
      { name: '绿色', className: 'code-bg-green', swatchClass: 'green' },
      { name: '蓝色', className: 'code-bg-blue', swatchClass: 'blue' },
      { name: '紫色', className: 'code-bg-purple', swatchClass: 'purple' },
      { name: '暖黄', className: 'code-bg-amber', swatchClass: 'amber' },
      { name: '粉色', className: 'code-bg-rose', swatchClass: 'rose' },
    ]

    colorButton.addEventListener('click', () => {
      const isExpanded = colorButton.getAttribute('aria-expanded') === 'true'
      colorButton.setAttribute('aria-expanded', String(!isExpanded))
      colorOptions.hidden = isExpanded
    })

    backgroundColors.forEach(({ name, className, swatchClass }) => {
      const option = document.createElement('button')
      option.className = `code-block-color-swatch code-block-color-${swatchClass}`
      option.type = 'button'
      option.setAttribute('aria-label', `${name}背景`)
      option.setAttribute('aria-pressed', String(!className))
      option.title = name
      option.addEventListener('click', () => {
        backgroundColors.filter((color) => color.className).forEach((color) => {
          pre.classList.remove(color.className)
        })
        if (className) pre.classList.add(className)
        colorOptions.querySelectorAll('.code-block-color-swatch').forEach((swatch) => {
          swatch.setAttribute('aria-pressed', String(swatch === option))
        })
        colorOptions.hidden = true
        colorButton.setAttribute('aria-expanded', 'false')
      })
      colorOptions.append(option)
    })

    colorPicker.append(colorButton, colorOptions)
    actions.append(colorPicker)

    if (collapsible) {
      const toggleButton = document.createElement('button')
      toggleButton.className = 'code-block-button code-block-toggle'
      toggleButton.type = 'button'
      toggleButton.textContent = `展开代码（${lines} 行）`
      toggleButton.setAttribute('aria-expanded', 'false')
      toggleButton.addEventListener('click', () => {
        const expanded = pre.classList.toggle('is-expanded')
        pre.classList.toggle('is-collapsed', !expanded)
        toggleButton.textContent = expanded ? '折叠代码' : `展开代码（${lines} 行）`
        toggleButton.setAttribute('aria-expanded', String(expanded))
      })
      actions.append(toggleButton)
    }

    pre.before(actions)
  })
}
