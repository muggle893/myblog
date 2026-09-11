<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import Icon from './Icon.vue'
import AvatarView from './AvatarView.vue'
import { appState, logoutOwner, setTheme } from '../services/state'
import { draftCount } from '../services/drafts'

const route = useRoute()
const router = useRouter()
const drafts = computed(()=>{ void appState.draftsRevision; return appState.owner ? draftCount() : 0 })
function logout(){ logoutOwner(); router.push('/') }
function toggleTheme(){ setTheme(appState.theme === 'dark' ? 'light' : 'dark') }
</script>
<template>
<header class="header">
  <div class="wrap nav">
    <RouterLink class="brand" to="/"><span class="brand-symbol"><Icon name="book" /></span>{{ appState.profile.blogTitle }}</RouterLink>
    <nav class="navlinks" aria-label="主导航">
      <RouterLink :class="{active:route.name==='home'}" to="/">首页</RouterLink>
      <RouterLink :class="{active:route.name==='profile'}" to="/profile">关于我</RouterLink>
    </nav>
    <div class="nav-actions">
      <button class="icon-button" type="button" title="切换明暗主题" aria-label="切换明暗主题" @click="toggleTheme"><Icon name="moon" /></button>
      <template v-if="appState.owner">
        <span class="owner-state">作者已登录</span>
        <RouterLink class="btn draftbox-btn" :class="{active:route.name==='drafts'}" to="/drafts" title="打开草稿箱"><Icon name="drafts"/><span>草稿箱</span><b class="draft-count-badge">{{ drafts }}</b></RouterLink>
        <RouterLink class="btn secondary write-btn" to="/editor"><Icon name="pen"/><span>写文章</span></RouterLink>
        <button class="header-logout" type="button" @click="logout">退出</button>
      </template>
      <RouterLink v-else class="btn secondary" :to="{name:'login',query:{next:route.fullPath}}"><Icon name="user"/><span>作者登录</span></RouterLink>
      <AvatarView to="/profile" />
    </div>
  </div>
</header>
</template>
