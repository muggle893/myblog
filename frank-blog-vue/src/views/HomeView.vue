<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AvatarView from '../components/AvatarView.vue'
import Icon from '../components/Icon.vue'
import { appState } from '../services/state'
import { loadPosts, visiblePosts } from '../services/posts'
import { draftCount } from '../services/drafts'

const route = useRoute()
// ref 创建一个响应式引用。修改 loading.value 后，模板中的 v-if 会自动更新。
const loading = ref(false)
const loadError = ref('')
// computed 会根据响应式依赖自动计算结果，并在依赖变化时重新计算。
// 这里的优先级是：URL 中指定的作者 > 登录用户名 > 本地资料昵称。
const author = computed(() => String(route.query.author || appState.username || appState.profile.nickname || '').trim())
const available = computed(() => {
  // 虽然这里只是读取函数返回值，但 allPosts() 内部依赖这个版本号。
  // 明确读取一次可以让 Vue 建立“文章版本变化 -> available 更新”的依赖关系。
  void appState.postsRevision
  return visiblePosts()
})

async function refreshPostsFromServer() {
  // author 为空时不发送请求，避免调用后端时违反 author 非空约束。
  if (!author.value) return
  loading.value = true
  loadError.value = ''
  try {
    // 后端会根据登录状态决定是否返回作者的私有文章。
    await loadPosts(author.value)
  } catch (error) {
    // 网络错误、HTTP 错误和 Result 业务错误都会进入这里。
    loadError.value = error instanceof Error ? error.message : '文章加载失败'
  } finally {
    // 无论成功还是失败，都要结束加载状态，否则页面会一直显示加载中。
    loading.value = false
  }
}

// onMounted 只在组件首次挂载后执行一次，适合发起首次数据请求。
onMounted(refreshPostsFromServer)
// watch 监听 author 的变化，例如登录后用户名变化或 URL 切换作者。
// 变化时重新请求列表，保证页面展示的是当前作者的数据。
watch(author, refreshPostsFromServer)
const filter = computed(() => String(route.query.category || ''))
// 分类筛选只影响显示，不会重新请求后端。
const list = computed(() =>
  available.value.filter((p) => !filter.value || p.category === filter.value)
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
        <span>共 {{ list.length }} 篇文章</span>
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
      <div class="feed-end">每一次记录，都是一点进步。</div>
    </section>
  </main>
</template>

