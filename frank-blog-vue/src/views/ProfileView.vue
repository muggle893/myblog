<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import AvatarView from '../components/AvatarView.vue'
import Icon from '../components/Icon.vue'
import { appState, defaultProfile, logoutOwner, saveProfile, toast } from '../services/state'
import { visiblePosts } from '../services/posts'

const router = useRouter()
const recent = computed(() => {
  void appState.postsRevision
  return visiblePosts().slice(0, 3)
})
const aboutParagraphs = computed(() =>
  (appState.profile.aboutBody || '').split(/\n\s*\n/).filter(Boolean)
)
const dialog = ref(null)
const avatarFileInput = ref(null)
const coverFileInput = ref(null)
const saving = ref(false)
const coverSrc = ref('')
const form = reactive({
  nickname: '',
  blogTitle: '',
  heroTitle: '',
  heroSubtitle: '',
  signature: '',
  aboutTitle: '',
  aboutBody: '',
  aboutTagsText: '',
  coverPosition: 48,
})
const previews = reactive({ avatar: '', cover: '' })
const pendingFiles = { avatar: null, cover: null }
const removed = { avatar: false, cover: false }
const objectUrls = { avatar: '', cover: '' }
const previewTokens = { avatar: 0, cover: 0 }

let coverToken = 0
let disposed = false

const defaultCover = '/assets/illustrations/profile-lake-anime.png'
const coverStyle = computed(() => imageStyle(coverSrc.value, appState.profile.coverPosition))
const previewCoverStyle = computed(() =>
  imageStyle(previews.cover || defaultCover, form.coverPosition)
)

function imageStyle(src, position) {
  return {
    backgroundImage: src ? `url("${src}")` : undefined,
    backgroundPosition: `center ${Math.min(100, Math.max(0, Number(position) || 0))}%`,
  }
}

function fallbackLetter() {
  return Array.from(form.nickname || appState.profile.nickname || 'F')[0] || 'F'
}

function clearObjectUrl(kind) {
  if (objectUrls[kind]) URL.revokeObjectURL(objectUrls[kind])
  objectUrls[kind] = ''
}

function clearDraftImages() {
  for (const kind of ['avatar', 'cover']) {
    previewTokens[kind]++
    clearObjectUrl(kind)
    pendingFiles[kind] = null
    removed[kind] = false
    previews[kind] = ''
  }
}

async function refreshCover() {
  const token = ++coverToken
  coverSrc.value = ''

  if (!appState.profile.coverImageId) return

  try {
    const url = await window.blogImages.getUrl(appState.profile.coverImageId)
    if (!disposed && token === coverToken) coverSrc.value = url
  } catch {
    // fallback to default cover if local image is unavailable
  }
}

watch(() => appState.profile.coverImageId, refreshCover, { immediate: true })

async function loadPreview(kind) {
  const token = ++previewTokens[kind]
  const id = appState.profile[`${kind}ImageId`]
  if (!id) return

  try {
    const url = await window.blogImages.getUrl(id)
    if (!disposed && dialog.value?.open && token === previewTokens[kind]) {
      previews[kind] = url
    }
  } catch {
    // ignore preview load failure
  }
}

function openDialog() {
  if (!appState.owner || saving.value) return
  clearDraftImages()
  Object.assign(form, appState.profile, {
    aboutTagsText: (appState.profile.aboutTags || []).join('，'),
  })
  dialog.value?.showModal()
  loadPreview('avatar')
  loadPreview('cover')
}

function closeDialog() {
  if (saving.value) return
  dialog.value?.close()
  clearDraftImages()
}

function onCancel(event) {
  event.preventDefault()
  closeDialog()
}

function selectImage(kind, event) {
  const file = event.target.files?.[0]
  event.target.value = ''

  if (!file || saving.value) return

  if (!['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type)) {
    toast('请选择 PNG、JPG、WebP 或 GIF 图片')
    return
  }

  if (file.size > 20 * 1024 * 1024) {
    toast('图片请控制在 20 MB 以内')
    return
  }

  previewTokens[kind]++
  pendingFiles[kind] = file
  removed[kind] = false
  clearObjectUrl(kind)
  objectUrls[kind] = URL.createObjectURL(file)
  previews[kind] = objectUrls[kind]
}

function resetImage(kind) {
  if (saving.value) return

  previewTokens[kind]++
  pendingFiles[kind] = null
  removed[kind] = true
  clearObjectUrl(kind)
  previews[kind] = ''

  if (kind === 'cover') form.coverPosition = defaultProfile.coverPosition
}

async function discardImages(ids) {
  await Promise.all(ids.map((id) => window.blogImages.remove(id).catch(() => {})))
}

async function submitProfile() {
  if (saving.value) return
  if (!appState.owner) {
    toast('请先登录自己的账号')
    return
  }

  if (
    !form.nickname.trim() ||
    !form.blogTitle.trim() ||
    !form.heroTitle.trim() ||
    !form.aboutTitle.trim()
  ) {
    toast('昵称、博客名称、首页标题和介绍标题不能为空')
    return
  }

  const tags = [
    ...new Set(
      form.aboutTagsText
        .split(/[,，\n]/)
        .map((tag) => tag.trim())
        .filter(Boolean)
    ),
  ]

  if (tags.length > 12 || tags.some((tag) => Array.from(tag).length > 20)) {
    toast('最多填写 12 个标签，每个标签不超过 20 个字')
    return
  }

  const current = { ...appState.profile }
  const next = { ...current, aboutTags: tags, coverPosition: Number(form.coverPosition) }

  for (const key of [
    'nickname',
    'blogTitle',
    'heroTitle',
    'heroSubtitle',
    'signature',
    'aboutTitle',
    'aboutBody',
  ]) {
    next[key] = form[key].trim()
  }

  const newIds = []
  saving.value = true

  try {
    for (const kind of ['avatar', 'cover']) {
      const key = `${kind}ImageId`
      if (removed[kind]) next[key] = ''
      else if (pendingFiles[kind]) {
        const id = `profile-${kind}-${Date.now().toString(36)}-${crypto.getRandomValues(new Uint32Array(1))[0].toString(36)}`
        newIds.push(id)
        await window.blogImages.put(pendingFiles[kind], id)
        next[key] = id
      }
    }

    if (disposed || !appState.owner) {
      throw new Error('编辑已结束或登录已失效，请重新登录后保存')
    }

    if (!saveProfile(next)) {
      throw new Error('浏览器无法保存个人资料，请检查存储空间后重试')
    }

    discardImages(
      ['avatarImageId', 'coverImageId']
        .filter((key) => current[key] && current[key] !== next[key])
        .map((key) => current[key])
    )

    saving.value = false
    closeDialog()
    toast('个人资料已保存，封面与自我介绍已更新')
  } catch (error) {
    await discardImages(newIds)
    toast(error.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function accountAction() {
  if (appState.owner) {
    logoutOwner()
    router.replace('/')
    return
  }

  router.push({ name: 'login', query: { next: '/profile' } })
}

onBeforeUnmount(() => {
  disposed = true
  coverToken++
  clearDraftImages()
})
</script>

<template>
  <main class="wrap">
    <div class="page-heading">
      <RouterLink to="/">首页</RouterLink>
      <Icon name="chevron" />
      <span>关于我</span>
    </div>

    <section class="card">
      <div class="profile-cover" :style="coverStyle"></div>
      <div class="profile-main">
        <div class="profile-intro">
          <div class="profile-name">
            <AvatarView size="xl" />
            <h1>{{ appState.profile.nickname }}</h1>
            <p>@frank · 学习中的开发者</p>
          </div>
          <RouterLink class="btn" :to="appState.owner ? '/editor' : '/'">
            <Icon :name="appState.owner ? 'pen' : 'book'" />
            {{ appState.owner ? '写一篇文章' : '查看公开文章' }}
          </RouterLink>
        </div>

        <div class="profile-grid">
          <div class="profile-about">
            <h2 class="section-title">{{ appState.profile.aboutTitle }}</h2>
            <p
              v-for="(paragraph, index) in aboutParagraphs"
              :key="index"
              class="profile-about-paragraph"
            >
              {{ paragraph }}
            </p>
            <div v-if="appState.profile.aboutTags.length" class="tags">
              <span v-for="tag in appState.profile.aboutTags" :key="tag" class="tag">
                {{ tag }}
              </span>
            </div>
          </div>

          <div>
            <h2 class="section-title">个人信息</h2>
            <div class="info-row"><span>昵称</span><span>{{ appState.profile.nickname }}</span></div>
            <div class="info-row"><span>账号</span><span>frank</span></div>
            <div class="info-row"><span>博客主题</span><span>技术学习 / 生活记录</span></div>
            <div class="info-row"><span>个人签名</span><span>{{ appState.profile.signature }}</span></div>
            <button
              v-if="appState.owner"
              class="btn secondary"
              style="margin-top: 20px; margin-right: 8px"
              @click="openDialog"
            >
              编辑资料
            </button>
            <button class="btn outline" style="margin-top: 20px" @click="accountAction">
              <Icon :name="appState.owner ? 'logout' : 'user'" />
              {{ appState.owner ? '退出演示登录' : '登录我的账号' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="card profile-posts">
      <h2 class="section-title">最近的记录</h2>
      <RouterLink
        v-for="post in recent"
        :key="post.id"
        class="mini-post"
        :to="{ name: 'article', params: { id: post.id } }"
      >
        <span>
          {{ post.title }}
          <em v-if="post.visibility === 'private' && appState.owner" class="mini-private">
            私有
          </em>
        </span>
        <small>{{ post.date }}</small>
      </RouterLink>
      <div v-if="!recent.length" class="empty compact">暂时还没有公开文章</div>
    </section>

    <dialog
      ref="dialog"
      class="profile-dialog"
      aria-labelledby="profile-dialog-title"
      @cancel="onCancel"
      @close="clearDraftImages"
    >
      <form @submit.prevent="submitProfile">
        <fieldset class="profile-form-fields" :disabled="saving">
          <div class="dialog-heading">
            <h2 id="profile-dialog-title">编辑个人资料</h2>
            <button type="button" class="icon-button" aria-label="关闭" @click="closeDialog">
              ×
            </button>
          </div>

          <p class="muted">设置你的封面、头像与自我介绍，点击“保存修改”后生效。</p>

          <h3 class="profile-editor-heading">个人主页封面</h3>
          <div class="profile-cover-preview" :style="previewCoverStyle" role="img" aria-label="个人主页封面预览"></div>
          <input
            ref="coverFileInput"
            type="file"
            accept="image/png,image/jpeg,image/webp,image/gif"
            hidden
            @change="selectImage('cover', $event)"
          >
          <div class="profile-image-actions">
            <button type="button" class="btn secondary" @click="coverFileInput?.click()">
              更换封面
            </button>
            <button type="button" class="btn outline" @click="resetImage('cover')">
              恢复默认封面
            </button>
          </div>

          <label class="cover-position-label">
            画面上下位置
            <input
              v-model.number="form.coverPosition"
              type="range"
              min="0"
              max="100"
              aria-label="封面画面上下位置"
            >
          </label>
          <small class="profile-edit-hint">建议选择横向图片。支持 PNG、JPG、WebP、GIF，单张不超过 20 MB。</small>

          <h3 class="profile-editor-heading">头像</h3>
          <div class="avatar-editor">
            <div class="avatar xl avatar-preview" :class="{ 'has-photo': previews.avatar }">
              <img v-if="previews.avatar" :src="previews.avatar" alt="头像预览" />
              <template v-else>{{ fallbackLetter() }}</template>
            </div>
            <div class="avatar-editor-actions">
              <input
                ref="avatarFileInput"
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                hidden
                @change="selectImage('avatar', $event)"
              >
              <div>
                <button type="button" class="btn secondary" @click="avatarFileInput?.click()">
                  选择本地图片
                </button>
                <button type="button" class="btn outline" @click="resetImage('avatar')">
                  移除头像
                </button>
              </div>
              <small>头像、昵称会同步到首页、顶部导航和个人主页。</small>
            </div>
          </div>

          <h3 class="profile-editor-heading">自我介绍</h3>
          <label>
            介绍标题
            <input v-model="form.aboutTitle" maxlength="60" required />
          </label>
          <label>
            介绍正文
            <textarea
              v-model="form.aboutBody"
              maxlength="3000"
              rows="6"
              placeholder="介绍一下自己、正在学习的知识，或想记录的生活……"
            ></textarea>
          </label>
          <small class="profile-edit-hint">支持换行与空行分段，最多 3000 字。</small>

          <label>
            个人标签
            <input v-model="form.aboutTagsText" maxlength="300" placeholder="Java，SSM，Vue，持续学习" />
          </label>
          <small class="profile-edit-hint">用中文或英文逗号分隔；最多 12 个，每个不超过 20 个字。留空可隐藏标签。</small>

          <h3 class="profile-editor-heading">基本资料与首页</h3>
          <label>
            昵称
            <input v-model="form.nickname" maxlength="20" required />
          </label>
          <label>
            博客名称
            <input v-model="form.blogTitle" maxlength="24" required />
          </label>
          <label>
            首页大标题
            <input v-model="form.heroTitle" maxlength="50" required />
          </label>
          <label>
            首页介绍
            <textarea v-model="form.heroSubtitle" maxlength="100" rows="2"></textarea>
          </label>
          <label>
            个人签名
            <input v-model="form.signature" maxlength="50" />
          </label>

          <p class="form-note">前端阶段资料与图片仅保存在此浏览器；账号 frank 不随昵称改变。</p>

          <div class="dialog-actions">
            <button type="button" class="btn outline" @click="closeDialog">取消</button>
            <button type="submit" class="btn">{{ saving ? '正在保存…' : '保存修改' }}</button>
          </div>
        </fieldset>
      </form>
    </dialog>
  </main>
</template>

