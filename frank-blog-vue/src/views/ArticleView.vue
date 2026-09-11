<script setup>
import { computed, watchEffect } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import Icon from '../components/Icon.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'
import { appState } from '../services/state'
import { canReadPost, findPost } from '../services/posts'
const route=useRoute()
const post=computed(()=>{void appState.postsRevision;return findPost(String(route.params.id||''))})
const allowed=computed(()=>post.value && canReadPost(post.value))
watchEffect(()=>{ if(post.value&&allowed.value) document.title=post.value.title+' · '+appState.profile.blogTitle })
</script>
<template>
<main class="wrap"><div class="page-heading"><RouterLink to="/">首页</RouterLink><Icon name="chevron"/><span>文章</span></div>
  <article class="card article-shell">
    <div v-if="!post" class="empty"><h1>没有找到这篇文章</h1><p>本地演示文章只保存在发布时使用的浏览器中。</p><RouterLink class="btn secondary" to="/">返回首页</RouterLink></div>
    <div v-else-if="!allowed" class="empty private-denied"><div class="private-lock"><Icon name="lock"/></div><h1>这是一篇私有文章</h1><p>仅作者本人登录后可以访问，游客无法查看文章标题和正文内容。</p><div class="denied-actions"><RouterLink class="btn secondary" to="/">返回公开文章</RouterLink><RouterLink class="btn outline" :to="{name:'login',query:{next:route.fullPath}}">作者登录</RouterLink></div></div>
    <template v-else><header class="article-heading"><div class="post-kicker"><span class="pill">{{ post.category }}</span><span v-if="post.visibility==='private'" class="visibility-badge private"><Icon name="lock"/> 私有 · 仅自己可见</span><span v-else class="visibility-badge public"><Icon name="eye"/> 公开</span><span>{{ post.id.startsWith('local-')?'我的文章':'示例文章' }}</span></div><div class="article-title-row"><h1>{{ post.title }}</h1><RouterLink v-if="appState.owner" class="btn secondary article-edit-button" :to="{name:'editor',query:{edit:post.id}}"><Icon name="pen"/>编辑文章</RouterLink></div><div class="post-kicker"><span>{{ appState.profile.nickname }}</span><span>·</span><span>{{ post.date }}</span><span>·</span><span>{{ post.read }}阅读</span></div></header><MarkdownRenderer :source="post.body" /></template>
  </article>
</main>
</template>
