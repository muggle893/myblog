<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import Icon from '../components/Icon.vue'
import { appState, toast } from '../services/state'
import { allPosts, findPost, saveNewPost, updatePost } from '../services/posts'
import { findEditDraft, getDraftRecord, removeDraftRecord, upsertDraftRecord } from '../services/drafts'
import { enhanceContent, renderMarkdown } from '../services/markdown'

const route = useRoute()
const router = useRouter()
const requestedDraftId = String(route.query.draft || '')

let draftRecord = requestedDraftId ? getDraftRecord(requestedDraftId) : null
const editId = String(route.query.edit || draftRecord?.postId || '')
const editingPost = editId ? findPost(editId) : null

if (!draftRecord && editingPost) {
  draftRecord = findEditDraft(editingPost.id)
}

const invalid = ref(Boolean((requestedDraftId && !draftRecord) || (editId && !editingPost)))
const source = draftRecord || editingPost || null
const title = ref(source?.title || '')
const category = ref(source?.category || 'Java 学习')
const visibility = ref(source?.visibility === 'private' ? 'private' : 'public')
const selectedTags = ref(
  [
    ...new Set(
      (Array.isArray(source?.tags) ? source.tags : [])
        .map((v) => String(v).trim().replace(/^#+/, ''))
        .filter(Boolean)
    ),
  ].slice(0, 10)
)
const tagInput = ref('')
const wordCount = ref(String(source?.body || '').replace(/\s/g, '').length)
const saveState = ref(
  draftRecord
    ? editingPost
      ? '已从草稿箱恢复文章修改'
      : '已从草稿箱恢复草稿'
    : editingPost
      ? '正在编辑原文章'
      : '开始写作后自动保存到草稿箱'
)
const currentDraftId = ref(draftRecord?.id || '')
const imageFiles = ref(null)
const attachmentFiles = ref(null)
const importMarkdownInput = ref(null)

let editor = null
let cm = null
let timer = null
let publishing = false
let dirty = false
const pending = ref(0)

const categories = computed(() =>
  [...new Set(['Java 学习', '学习笔记', '生活日常', category.value].filter(Boolean))]
)
const suggestions = computed(() => {
  void appState.postsRevision
  const base = ['Java', 'Spring', 'SpringBoot', 'MyBatis', 'Redis', 'Vue', 'SSM', '后端开发', '学习笔记']
  const fromPosts = allPosts().flatMap((p) => (Array.isArray(p.tags) ? p.tags : []))

  return [...new Set([...fromPosts, ...base].map((v) => String(v).trim()).filter(Boolean))]
    .filter((t) => !selectedTags.value.some((s) => s.toLowerCase() === t.toLowerCase()))
    .slice(0, 10)
})
const isBusy = computed(() => pending.value > 0)

function addTags(raw, notify = true) {
  const incoming = String(raw || '')
    .split(/[,，]/)
    .map((v) => v.trim().replace(/^#+/, ''))
    .filter(Boolean)

  if (!incoming.length) return false

  let added = 0

  for (const tag of incoming) {
    if (tag.length > 20) {
      if (notify) toast('单个标签最多 20 个字符：' + tag.slice(0, 12) + '…')
      continue
    }

    if (selectedTags.value.some((t) => t.toLowerCase() === tag.toLowerCase())) continue

    if (selectedTags.value.length >= 10) {
      if (notify) toast('一篇文章最多添加 10 个标签')
      break
    }

    selectedTags.value.push(tag)
    added++
  }

  if (added) changed()
  return added > 0
}

function commitTagInput(notify = true) {
  if (!tagInput.value.trim()) return
  addTags(tagInput.value, notify)
  tagInput.value = ''
}

function tagKeydown(e) {
  if (e.isComposing) return
  if (e.key === 'Enter' || e.key === ',' || e.key === '，') {
    e.preventDefault()
    commitTagInput()
  }
}

function removeTag(index) {
  selectedTags.value.splice(index, 1)
  changed()
}

function currentBody() {
  return editor?.value() ?? source?.body ?? ''
}

function getDraft() {
  return {
    title: title.value.trim(),
    body: currentBody(),
    category: category.value,
    tags: [...selectedTags.value],
    visibility: visibility.value,
  }
}

function saveDraft(notify = false) {
  clearTimeout(timer)

  if (pending.value || publishing || invalid.value) return
  if (!dirty && !notify) return

  if (!currentDraftId.value) {
    currentDraftId.value =
      'draft-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8)
  }

  const saved = upsertDraftRecord({
    ...getDraft(),
    id: currentDraftId.value,
    kind: editingPost ? 'edit' : 'new',
    postId: editingPost?.id || null,
    createdAt: draftRecord?.createdAt,
  })

  if (saved) {
    draftRecord = saved
    dirty = false
    const query = editingPost
      ? { edit: editingPost.id, draft: currentDraftId.value }
      : { draft: currentDraftId.value }

    if (String(route.query.draft || '') !== currentDraftId.value) {
      router.replace({ name: 'editor', query })
    }

    saveState.value =
      (editingPost ? '编辑草稿' : '草稿') +
      '已保存到草稿箱 · ' +
      new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

    if (notify) {
      toast(editingPost ? '文章修改已保存到草稿箱' : '草稿已保存到草稿箱')
    }
  } else {
    toast('浏览器无法保存草稿')
  }
}

function changed() {
  if (!editor) return
  dirty = true
  wordCount.value = editor.value().replace(/\s/g, '').length
  saveState.value = pending.value ? '正在添加文件…' : '有未保存的修改'
  clearTimeout(timer)

  if (!pending.value && !publishing) {
    timer = setTimeout(() => saveDraft(), 1200)
  }
}

function formChanged() {
  if (editor) changed()
}

function waitForEasyMDE(timeout = 5000) {
  return new Promise((resolve, reject) => {
    const start = Date.now()

    const tick = () => {
      if (window.EasyMDE) return resolve(window.EasyMDE)
      if (Date.now() - start > timeout) {
        return reject(new Error('Markdown 编辑器资源加载失败'))
      }
      setTimeout(tick, 50)
    }

    tick()
  })
}

function custom(name, title, cls, action) {
  return { name, title, className: 'fa ' + cls, action }
}

function pick(refObj) {
  return () => refObj.value?.click()
}

async function initEditor() {
  const EasyMDE = await waitForEasyMDE()
  const textarea = document.getElementById('markdown-input')

  editor = new EasyMDE({
    element: textarea,
    initialValue:
      source?.body ??
      '## 今天，学到了一点新东西\n\n在这里记录你的学习过程。\n\n### 值得记住的地方\n\n- 理解概念\n- 动手验证\n- 记录总结\n\n> 慢慢积累，每一篇都算数。\n\n```java\nSystem.out.println("Hello, my blog!");\n```',
    autoDownloadFontAwesome: false,
    spellChecker: false,
    nativeSpellcheck: false,
    forceSync: true,
    lineNumbers: true,
    tabSize: 4,
    minHeight: '480px',
    maxHeight: '65vh',
    status: false,
    sideBySideFullscreen: false,
    uploadImage: false,
    autofocus: false,
    previewClass: ['editor-preview', 'markdown'],
    previewRender: (text, preview) => {
      setTimeout(() => enhanceContent(preview), 0)
      return renderMarkdown(text)
    },
    toolbar: [
      'undo',
      'redo',
      '|',
      'heading',
      'bold',
      'italic',
      'strikethrough',
      '|',
      'quote',
      'unordered-list',
      'ordered-list',
      custom('task', '任务列表', 'fa-check-square-o', (e) =>
        e.codemirror.replaceSelection('\n- [ ] 待办事项\n')
      ),
      '|',
      'link',
      'code',
      'table',
      'horizontal-rule',
      custom('formula', '数学公式', 'fa-superscript', (e) =>
        e.codemirror.replaceSelection('\n$$\nE = mc^2\n$$\n')
      ),
      '|',
      custom('local-image', '添加图片', 'fa-picture-o', pick(imageFiles)),
      custom('attachment', '添加文件附件', 'fa-paperclip', pick(attachmentFiles)),
      custom('import-md', '导入 Markdown（追加到光标处）', 'fa-upload', pick(importMarkdownInput)),
      custom('export-md', '导出 Markdown', 'fa-download', exportMarkdown),
      '|',
      'preview',
      'side-by-side',
      'fullscreen',
    ],
  })

  cm = editor.codemirror

  const labels = {
    undo: '撤销',
    redo: '重做',
    heading: '标题',
    bold: '加粗',
    italic: '斜体',
    strikethrough: '删除线',
    quote: '引用',
    'unordered-list': '无序列表',
    'ordered-list': '有序列表',
    link: '插入链接',
    code: '代码块',
    table: '表格',
    'horizontal-rule': '分隔线',
    preview: '预览',
    'side-by-side': '分屏预览',
    fullscreen: '全屏',
  }

  for (const [key, label] of Object.entries(labels)) {
    const el = editor.toolbarElements[key]
    if (el) {
      el.title = label
      el.setAttribute('aria-label', label)
    }
  }

  if (window.innerWidth > 760) editor.toggleSideBySide()

  cm.on('change', changed)
  cm.on('paste', (instance, event) => {
    const files = Array.from(event.clipboardData?.items || [])
      .filter((item) => item.kind === 'file')
      .map((item) => item.getAsFile())
      .filter(Boolean)

    if (files.length) {
      event.preventDefault()
      addFiles(files)
    }
  })
  cm.on('dragover', (instance, event) => {
    if (Array.from(event.dataTransfer?.types || []).includes('Files')) {
      event.preventDefault()
    }
  })
  cm.on('drop', (instance, event) => {
    if (event.dataTransfer?.files.length) {
      event.preventDefault()
      const pos = cm.coordsChar({ left: event.clientX, top: event.clientY }, 'window')
      cm.setCursor(pos)
      addFiles(event.dataTransfer.files)
    }
  })

  wordCount.value = editor.value().replace(/\s/g, '').length
}

async function addFiles(files, attachmentOnly = false) {
  files = Array.from(files)
  if (!files.length || !cm) return

  const jobs = files.map((file) => ({
    file,
    image:
      !attachmentOnly && ['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type),
    id:
      'file-' +
      Date.now().toString(36) +
      '-' +
      crypto.getRandomValues(new Uint32Array(2)).join('-'),
  }))

  const placeholder = (job) => `[正在添加文件](pending-file:${job.id})`

  pending.value += jobs.length
  saveState.value = '正在添加文件…'
  cm.replaceSelection('\n\n' + jobs.map(placeholder).join('\n\n') + '\n\n', 'end')

  let ok = 0

  for (const job of jobs) {
    let replacement = ''

    try {
      await (job.image
        ? window.blogImages.put(job.file, job.id)
        : window.blogImages.putAttachment(job.file, job.id))

      const name = (job.file.name || '截图.png')
        .replace(/[\[\]\\\r\n<>]/g, ' ')
        .slice(0, 120)

      replacement = job.image
        ? `![${name}](blog-image:${job.id})`
        : `[附件：${name}](blog-file:${job.id})`
      ok++
    } catch (error) {
      toast(error.message || '文件添加失败')
    }

    const token = placeholder(job)
    const at = editor.value().indexOf(token)
    if (at >= 0) {
      cm.replaceRange(
        replacement,
        cm.posFromIndex(at),
        cm.posFromIndex(at + token.length),
        '+input'
      )
    }
    pending.value--
  }

  changed()
  if (ok) toast(`已添加 ${ok} 个文件`)
}

function imageChanged(e) {
  addFiles(e.target.files)
  e.target.value = ''
}

function attachmentChanged(e) {
  addFiles(e.target.files, true)
  e.target.value = ''
}

async function importMarkdown(e) {
  const file = e.target.files?.[0]
  e.target.value = ''

  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    toast('Markdown 文件请控制在 2 MB 以内')
    return
  }

  try {
    cm.replaceSelection('\n' + (await file.text()) + '\n', 'end')
    toast('已将 Markdown 插入光标位置')
  } catch {
    toast('文件读取失败')
  }
}

function exportMarkdown() {
  if (pending.value) {
    toast('请等待文件添加完成')
    return
  }

  const blob = new Blob([currentBody()], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')

  a.href = url
  a.download =
    (title.value.trim() || '未命名文章').replace(/[\\/:*?"<>|]/g, '_') + '.md'
  a.click()

  setTimeout(() => URL.revokeObjectURL(url), 1000)

  if (/blog-(image|file):/.test(currentBody())) {
    toast('已导出正文；本地图片和附件仍保存在此浏览器')
  }
}

function publish() {
  if (pending.value || !editor) return
  commitTagInput(false)

  const data = getDraft()
  if (!data.title) {
    toast('先给文章起一个标题吧')
    document.getElementById('article-title')?.focus()
    return
  }

  if (!data.body.trim()) {
    toast('请填写文章正文')
    cm.focus()
    return
  }

  data.read = Math.max(1, Math.ceil(data.body.length / 350)) + ' 分钟'
  data.excerpt = data.body
    .replace(/!?\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/[#*>`\n]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 110)

  const saved = editingPost ? updatePost(editingPost, data) : saveNewPost(data)

  if (saved) {
    publishing = true
    clearTimeout(timer)
    if (currentDraftId.value) removeDraftRecord(currentDraftId.value)
    router.push({ name: 'article', params: { id: saved.id } })
  } else {
    toast('文章保存失败，请检查浏览器本地存储')
  }
}

function keyHandler(e) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault()
    commitTagInput(false)
    saveDraft(true)
  }
}

function unloadHandler() {
  if (dirty) saveDraft()
}

onMounted(async () => {
  if (invalid.value) {
    toast(requestedDraftId && !draftRecord ? '没有找到这篇草稿' : '没有找到要编辑的文章')
    setTimeout(() => router.replace(requestedDraftId ? '/drafts' : '/'), 700)
    return
  }

  try {
    await initEditor()
  } catch (error) {
    toast(error.message || '编辑器加载失败')
  }

  window.addEventListener('keydown', keyHandler)
  window.addEventListener('beforeunload', unloadHandler)
})

onBeforeUnmount(() => {
  clearTimeout(timer)
  window.removeEventListener('keydown', keyHandler)
  window.removeEventListener('beforeunload', unloadHandler)

  if (dirty && !publishing) saveDraft()

  try {
    editor?.toTextArea()
  } catch {
    // ignore editor teardown issues
  }

  editor = null
  cm = null
})
</script>

<template>
  <main class="wrap">
    <div class="editor-header">
      <div>
        <h1>{{ editingPost ? '编辑文章' : '写下今天的收获' }}</h1>
        <p v-if="editingPost">
          正在修改已发布文章
          <span class="editor-editing-note">保存后覆盖原文章，不会重复发布</span>
        </p>
        <p v-else>把零散的想法，慢慢整理成文章。</p>
      </div>

      <div class="editor-buttons">
        <RouterLink class="btn draftbox-editor-btn" to="/drafts">
          <Icon name="drafts" />
          草稿箱
        </RouterLink>
        <button
          class="btn outline"
          type="button"
          :disabled="isBusy"
          @click="commitTagInput(false); saveDraft(true)"
        >
          <Icon name="save" />
          保存草稿
        </button>
        <button class="btn" type="button" :disabled="isBusy" @click="publish">
          {{ editingPost ? '保存修改' : '预览发布' }}
          <Icon :name="editingPost ? 'save' : 'arrow'" />
        </button>
      </div>
    </div>

    <section class="card editor-card">
      <input
        id="article-title"
        v-model="title"
        class="title-input"
        placeholder="给这篇文章起一个标题…"
        aria-label="文章标题"
        maxlength="100"
        @input="formChanged"
      >

      <div class="post-options">
        <label>
          分类
          <select v-model="category" @change="formChanged">
            <option v-for="item in categories" :key="item">{{ item }}</option>
          </select>
        </label>

        <div class="tags-builder">
          <span class="option-label">标签</span>
          <div class="tags-builder-main">
            <div class="selected-tags" aria-live="polite">
              <span v-if="!selectedTags.length" class="tag-empty">
                还没有标签，下面可以直接输入你自己的标签
              </span>
              <button
                v-for="(tag, index) in selectedTags"
                :key="tag"
                type="button"
                class="editable-tag"
                :aria-label="'删除标签 ' + tag"
                @click="removeTag(index)"
              >
                <span># {{ tag }}</span>
                <b aria-hidden="true">×</b>
              </button>
            </div>

            <div class="tag-entry">
              <input
                v-model="tagInput"
                placeholder="输入任意标签，如 SpringBoot"
                maxlength="20"
                autocomplete="off"
                @keydown="tagKeydown"
                @blur="commitTagInput(false)"
              >
              <button type="button" class="tag-add-button" @click="commitTagInput()">
                添加
              </button>
            </div>

            <div class="tag-help">可以自定义任意标签 · 按 Enter 或逗号添加 · 点击 × 删除 · 最多 10 个</div>

            <div class="tag-suggestions">
              <span v-if="suggestions.length" class="suggestion-label">快捷添加：</span>
              <button
                v-for="tag in suggestions"
                :key="tag"
                type="button"
                class="suggestion-tag"
                @click="addTags(tag)"
              >
                # {{ tag }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <fieldset class="visibility-panel" aria-label="文章权限">
        <legend>文章权限</legend>
        <label class="visibility-choice">
          <input v-model="visibility" type="radio" value="public" @change="formChanged" />
          <span class="visibility-choice-icon public"><Icon name="eye" /></span>
          <span>
            <strong>公开文章</strong>
            <small>游客和作者都可以查看</small>
          </span>
        </label>
        <label class="visibility-choice">
          <input v-model="visibility" type="radio" value="private" @change="formChanged" />
          <span class="visibility-choice-icon private"><Icon name="lock" /></span>
          <span>
            <strong>私有文章</strong>
            <small>只有作者登录后可以查看</small>
          </span>
        </label>
      </fieldset>

      <div class="editor-mode-label">
        <span>Markdown 编辑器</span>
        <span>支持粘贴图片、拖放文件 · 快捷键 Ctrl / ⌘ + S 保存</span>
      </div>

      <div class="advanced-editor">
        <textarea id="markdown-input" aria-label="Markdown 文章正文"></textarea>
      </div>

      <input
        ref="imageFiles"
        type="file"
        accept="image/png,image/jpeg,image/webp,image/gif"
        multiple
        hidden
        @change="imageChanged"
      >
      <input ref="attachmentFiles" type="file" multiple hidden @change="attachmentChanged" />
      <input
        ref="importMarkdownInput"
        type="file"
        accept=".md,.markdown,.txt"
        hidden
        @change="importMarkdown"
      >

      <div class="editor-status">
        <span>{{ wordCount }} 字</span>
        <span>{{ saveState }}</span>
      </div>
    </section>

    <p class="form-note">
      Vue 前端阶段：文章、图片与附件仍仅保存在当前浏览器。私有文章权限也是前端演示；正式上线接后端后必须由后端鉴权。
    </p>
  </main>
</template>

