<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AvatarView from '../components/AvatarView.vue'
import Icon from '../components/Icon.vue'
import { appState } from '../services/state'
import { visiblePosts } from '../services/posts'
import { draftCount } from '../services/drafts'

const route = useRoute()
const available = computed(()=>{ void appState.postsRevision; return visiblePosts() })
const filter = computed(()=>String(route.query.category || ''))
const list = computed(()=>available.value.filter((p)=>!filter.value || p.category===filter.value))
const categories = computed(()=>[...new Set(available.value.map((p)=>p.category).filter(Boolean))])
const tags = computed(()=>[...new Set(available.value.flatMap((p)=>Array.isArray(p.tags)?p.tags:[]).map((t)=>String(t).trim()).filter(Boolean))])
const drafts = computed(()=>{ void appState.draftsRevision; return draftCount() })
const categoryCount = (cat)=>available.value.filter((p)=>p.category===cat).length
</script>
<template>
<section class="hero"><div class="wrap"><div class="eyebrow">{{ /^[a-z\s]+$/i.test(appState.profile.nickname) ? appState.profile.nickname.toUpperCase()+"’S PERSONAL SPACE" : appState.profile.nickname+'的学习空间' }}</div><h1>{{ appState.profile.heroTitle }}</h1><p>{{ appState.profile.heroSubtitle }}</p><div class="hero-note">STAY CURIOUS. KEEP GROWING.</div></div></section>
<main class="wrap layout">
  <aside class="sidebar">
    <section class="card author">
      <AvatarView size="lg" to="/profile" />
      <h2>{{ appState.profile.nickname }}</h2>
      <p>一个热爱学习的开发者<br>在这里，慢慢积累。</p>
      <p class="tiny-label">LEARN · BUILD · SHARE</p>
      <div v-if="appState.owner" class="owner-quick-buttons">
        <RouterLink to="/drafts" class="owner-quick-link"><Icon name="drafts"/><span>草稿箱</span><b>{{ drafts }}</b></RouterLink>
        <RouterLink to="/editor" class="owner-quick-link"><Icon name="pen"/><span>写文章</span></RouterLink>
      </div>
      <div class="stats"><div><strong>{{ available.length }}</strong><span>文章</span></div><div><strong>{{ categories.length }}</strong><span>分类</span></div><div><strong>{{ tags.length }}</strong><span>标签</span></div></div>
    </section>
    <section class="card side-section">
      <h3 class="side-title"><Icon name="folder"/>文章分类</h3>
      <RouterLink class="category" :class="{selected:!filter}" to="/"><span>全部文章</span><span>{{ available.length }}</span></RouterLink>
      <RouterLink v-for="cat in categories" :key="cat" class="category" :class="{selected:filter===cat}" :to="{name:'home',query:{category:cat}}"><span>{{ cat }}</span><span>{{ categoryCount(cat) }}</span></RouterLink>
    </section>
    <section class="card side-section"><h3 class="side-title"><Icon name="tag"/>最近在关注</h3><div class="tags"><span v-for="tag in tags.slice(0,10)" :key="tag" class="tag"># {{ tag }}</span><span v-if="!tags.length" class="tag">还没有标签</span></div></section>
  </aside>
  <section>
    <div class="card feed-top"><h2>{{ filter || '最新文章' }}</h2><span>共 {{ list.length }} 篇文章</span></div>
    <template v-if="list.length">
      <article v-for="post in list" :key="post.id" class="card post-card">
        <div class="post-kicker"><span class="pill">{{ post.category }}</span><span v-if="post.visibility==='private' && appState.owner" class="visibility-badge private"><Icon name="lock"/> 私有</span><span>{{ post.date.replaceAll('-',' / ') }}</span><span>· {{ post.read }}</span></div>
        <h2><RouterLink :to="{name:'article',params:{id:post.id}}">{{ post.title }}</RouterLink></h2><p>{{ post.excerpt }}</p>
        <div class="post-bottom"><div class="tags"><span v-for="tag in post.tags" :key="tag" class="tag"># {{ tag }}</span></div><RouterLink class="read" :to="{name:'article',params:{id:post.id}}">阅读全文 <Icon name="arrow"/></RouterLink></div>
      </article>
    </template>
    <div v-else class="card empty">这个分类还没有可见文章</div>
    <div class="feed-end">每一次记录，都是一点进步。</div>
  </section>
</main>
</template>
