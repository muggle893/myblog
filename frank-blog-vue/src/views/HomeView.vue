<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AvatarView from '../components/AvatarView.vue'
import Icon from '../components/Icon.vue'
import { appState } from '../services/state'
import { loadPostPage } from '../services/posts'
import { draftCount } from '../services/drafts'

const route = useRoute()
// ref 创建一个响应式引用。修改 loading.value 后，模板中的 v-if 会自动更新。
const loading = ref(false)
const loadError = ref('')
const page = ref(1)
const pageSize = 5
const pagePosts = ref([])
const totalPosts = ref(null)
const hasNextPage = ref(false)
const authorId = computed(() => String(route.query.authorId || (appState.owner && appState.userId) || 1).trim())

async function refreshPostsFromServer() {
  loading.value = true
  loadError.value = ''
  try {
    const result = await loadPostPage(authorId.value, page.value, pageSize)
    pagePosts.value = result.posts
    totalPosts.value = result.total
    hasNextPage.value = result.hasNext
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '文章加载失败'
  } finally {
    loading.value = false
  }
}

const filter = computed(() => String(route.query.category || ''))
watch([authorId, page], (values, oldValues) => {
  if (oldValues && values[0] !== oldValues[0] && page.value !== 1) {
    page.value = 1
    return
  }
  refreshPostsFromServer()
}, { immediate: true })
watch(filter, () => { page.value = 1 })
const available = computed(() => pagePosts.value)
const list = computed(() =>
  available.value
    .filter((p) => !filter.value || p.category === filter.value)
    .sort((a, b) => Date.parse(b.publishedAt || b.date || '') - Date.parse(a.publishedAt || a.date || ''))
)
const categories = computed(() =>
  [...new Set(available.value.map((p) => p.category).filter(Boolean))]
)
const tags = computed(() =>
  [...new Set(
    available.value
      .flatMap((p) => (Array.isArray(p.tags) ? p.tags : []))
      .map((t) => String(t).trim())
      .filter(Boolean)
  )]
)
const drafts = computed(() => {
  void appState.draftsRevision
  return draftCount()
})
const categoryCount = (cat) => available.value.filter((p) => p.category === cat).length
const pageCount = computed(() => totalPosts.value === null ? null : Math.max(1, Math.ceil(totalPosts.value / pageSize)))
const pageNumbers = computed(() => {
  if (!pageCount.value) {
    const start = Math.max(1, page.value - 3)
    const end = page.value + (hasNextPage.value ? 1 : 0)
    return Array.from({ length: end - start + 1 }, (_, index) => start + index)
  }
  const start = Math.min(page.value, Math.max(1, pageCount.value - 4))
  const end = Math.min(pageCount.value, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})
</script>

<template>
  <section class="hero">
    <div class="wrap">
      <div class="eyebrow">
        {{
          /^[a-z\s]+$/i.test(appState.profile.nickname)
            ? appState.profile.nickname.toUpperCase() + '’S PERSONAL SPACE'
            : appState.profile.nickname + '的学习空间'
        }}
      </div>
      <h1>{{ appState.profile.heroTitle }}</h1>
      <p>{{ appState.profile.heroSubtitle }}</p>
      <div class="hero-note">STAY CURIOUS. KEEP GROWING.</div>
    </div>
  </section>

  <main class="wrap layout">
    <aside class="sidebar">
      <section class="card author">
        <AvatarView size="lg" to="/profile" />
        <h2>{{ appState.profile.nickname }}</h2>
        <p>
          一个热爱学习的开发者<br />
          在这里，慢慢积累。
        </p>
        <p class="tiny-label">LEARN · BUILD · SHARE</p>

        <div v-if="appState.owner" class="owner-quick-buttons">
          <RouterLink to="/drafts" class="owner-quick-link">
            <Icon name="drafts" />
            <span>草稿箱</span>
            <b>{{ drafts }}</b>
          </RouterLink>
          <RouterLink to="/editor" class="owner-quick-link">
            <Icon name="pen" />
            <span>写文章</span>
          </RouterLink>
        </div>

        <div class="stats">
          <div><strong>{{ available.length }}</strong><span>文章</span></div>
          <div><strong>{{ categories.length }}</strong><span>分类</span></div>
          <div><strong>{{ tags.length }}</strong><span>标签</span></div>
        </div>
      </section>

      <section class="card side-section">
        <h3 class="side-title"><Icon name="folder" />文章分类</h3>
        <RouterLink class="category" :class="{ selected: !filter }" to="/">
          <span>全部文章</span>
          <span>{{ available.length }}</span>
        </RouterLink>
        <RouterLink
          v-for="cat in categories"
          :key="cat"
          class="category"
          :class="{ selected: filter === cat }"
          :to="{ name: 'home', query: { category: cat } }"
        >
          <span>{{ cat }}</span>
          <span>{{ categoryCount(cat) }}</span>
        </RouterLink>
      </section>

      <section class="card side-section">
        <h3 class="side-title"><Icon name="tag" />最近在关注</h3>
        <div class="tags">
          <span v-for="tag in tags.slice(0, 10)" :key="tag" class="tag"># {{ tag }}</span>
          <span v-if="!tags.length" class="tag">还没有标签</span>
        </div>
      </section>
    </aside>

    <section>
      <div class="card feed-top">
        <h2>{{ filter || '最新文章' }}</h2>
        <span v-if="totalPosts !== null">共 {{ totalPosts }} 篇文章</span>
        <span v-else>本页 {{ list.length }} 篇文章</span>
      </div>

      <div v-if="loading" class="card empty">正在加载文章...</div>
      <div v-else-if="loadError" class="card empty" role="alert">
        <p>{{ loadError }}</p>
        <button class="btn secondary" type="button" @click="refreshPostsFromServer">重新加载</button>
      </div>
      <template v-else-if="list.length">
        <article v-for="post in list" :key="post.id" class="card post-card">
          <div class="post-kicker">
            <span class="pill">{{ post.category }}</span>
            <span
              v-if="post.visibility === 'private' && appState.owner"
              class="visibility-badge private"
            >
              <Icon name="lock" /> 私有
            </span>
            <span>{{ post.date.replaceAll('-', ' / ') }}</span>
            <span>· {{ post.read }}</span>
          </div>

          <h2>
            <RouterLink :to="{ name: 'article', params: { id: post.id } }">
              {{ post.title }}
            </RouterLink>
          </h2>
          <p>{{ post.excerpt }}</p>

          <div class="post-bottom">
            <div class="tags">
              <span v-for="tag in post.tags" :key="tag" class="tag"># {{ tag }}</span>
            </div>
            <RouterLink class="read" :to="{ name: 'article', params: { id: post.id } }">
              阅读全文
              <Icon name="arrow" />
            </RouterLink>
          </div>
        </article>
      </template>

      <div v-else class="card empty">这个分类还没有可见文章</div>
      <nav v-if="!loading && !loadError" class="pagination" aria-label="文章分页">
        <button class="btn secondary" type="button" :disabled="page <= 1" @click="page = 1">首页</button>
        <button class="btn secondary" type="button" :disabled="page <= 1" @click="page--">上一页</button>
        <button
          v-for="pageNumber in pageNumbers"
          :key="pageNumber"
          class="btn secondary page-number"
          :class="{ 'page-current': page === pageNumber }"
          type="button"
          :aria-current="page === pageNumber ? 'page' : undefined"
          @click="page = pageNumber"
        >
          {{ pageNumber }}
        </button>
        <button class="btn secondary" type="button" :disabled="!hasNextPage" @click="page++">下一页</button>
        <button class="btn secondary" type="button" :disabled="!pageCount || page >= pageCount" @click="page = pageCount">尾页</button>
      </nav>
      <div class="feed-end">每一次记录，都是一点进步。</div>
    </section>
  </main>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.pagination .btn {
  min-width: 72px;
  padding-right: 12px;
  padding-left: 12px;
}

.pagination .page-number {
  min-width: 40px;
}

.pagination .page-current {
  background: var(--blue);
  border-color: var(--blue);
  color: #fff;
}

@media (max-width: 700px) {
  .pagination {
    gap: 6px;
  }

  .pagination .btn {
    min-width: 0;
    padding-right: 10px;
    padding-left: 10px;
  }

  .pagination .page-number {
    min-width: 34px;
  }
}
</style>

