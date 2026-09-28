<script setup>
import { ref, watch, watchEffect } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import Icon from '../components/Icon.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'
import { appState } from '../services/state'
import { loadArticleDetail } from '../services/posts'

const route = useRoute()
const post = ref(null)
const loading = ref(false)
const loadError = ref('')

async function refreshArticle() {
  const articleId = String(route.params.id || '')
  post.value = null
  loadError.value = ''
  if (!articleId) {
    loadError.value = '缺少文章 ID'
    return
  }

  loading.value = true
  try {
    post.value = await loadArticleDetail(articleId)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '文章详情加载失败'
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, refreshArticle, { immediate: true })

watchEffect(() => {
  if (post.value) {
    document.title = post.value.title + ' · ' + appState.profile.blogTitle
  }
})
</script>

<template>
  <main class="wrap">
    <div class="page-heading">
      <RouterLink to="/">首页</RouterLink>
      <Icon name="chevron" />
      <span>文章</span>
    </div>

    <article class="card article-shell">
      <div v-if="loading" class="empty">正在加载文章...</div>

      <div v-else-if="loadError" class="empty" role="alert">
        <h1>文章加载失败</h1>
        <p>{{ loadError }}</p>
        <button class="btn secondary" type="button" @click="refreshArticle">重新加载</button>
      </div>

      <div v-else-if="!post" class="empty">
        <h1>没有找到这篇文章</h1>
        <p>这篇文章可能已经被删除，或当前账号没有访问权限。</p>
        <RouterLink class="btn secondary" to="/">返回首页</RouterLink>
      </div>

      <template v-else>
        <header class="article-heading">
          <div class="post-kicker">
            <span class="pill">{{ post.category }}</span>
            <span v-if="post.visibility === 'private'" class="visibility-badge private">
              <Icon name="lock" /> 私有 · 仅自己可见
            </span>
            <span v-else class="visibility-badge public">
              <Icon name="eye" /> 公开
            </span>
            <span>{{ post.authorName || '作者' }}</span>
          </div>

          <div class="article-title-row">
            <h1>{{ post.title }}</h1>
            <RouterLink
              v-if="post.canEdit"
              class="btn secondary article-edit-button"
              :to="{ name: 'editor', query: { edit: post.id } }"
            >
              <Icon name="pen" />
              编辑文章
            </RouterLink>
          </div>

          <div class="post-kicker">
            <span>{{ post.authorName || '作者' }}</span>
            <span>·</span>
            <span>{{ post.date || '未发布' }}</span>
            <span>·</span>
            <span>{{ post.read || '0' }}阅读</span>
          </div>
        </header>

        <MarkdownRenderer :source="post.body" />
      </template>
    </article>
  </main>
</template>

