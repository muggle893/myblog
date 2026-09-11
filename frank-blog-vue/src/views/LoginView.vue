<script setup>
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import Icon from '../components/Icon.vue'
import { appState, loginOwner } from '../services/state'
const username=ref(''), password=ref(''), error=ref(''), showPassword=ref(false)
const route=useRoute(), router=useRouter()
function safeNext(value){return typeof value==='string' && value.startsWith('/') && !value.startsWith('//') ? value : '/editor'}
function submit(){
  error.value=''
  if(!username.value.trim()||!password.value){error.value='请填写账号和密码';return}
  if(!loginOwner()){error.value='当前浏览器无法建立登录会话';return}
  router.replace(safeNext(route.query.next))
}
</script>
<template>
<main class="login-shell">
  <section class="login-visual">
    <RouterLink class="brand" to="/"><span class="brand-symbol"><Icon name="book"/></span>{{ appState.profile.blogTitle }}</RouterLink>
    <div class="login-quote"><div class="eyebrow">A LITTLE SPACE FOR YOUR THOUGHTS</div><h1>让每一个想法，<br>都有地方落笔。</h1><p>记录今天学会的事情，<br>也留住那些值得回看的瞬间。</p></div><small>LEARN · BUILD · SHARE</small>
  </section>
  <section class="login-panel">
    <RouterLink class="back-link" to="/"><Icon name="back"/>返回博客</RouterLink>
    <form class="login-form" @submit.prevent="submit"><div class="eyebrow">WELCOME BACK</div><h2>欢迎回来</h2><p class="muted">登录你的账号，继续记录与分享。</p>
      <div class="field"><label for="username">账号</label><div class="input-wrap"><Icon name="user"/><input id="username" v-model="username" autocomplete="username" placeholder="请输入你的账号" required></div></div>
      <div class="field"><label for="password">密码</label><div class="input-wrap"><Icon name="lock"/><input id="password" v-model="password" :type="showPassword?'text':'password'" autocomplete="current-password" placeholder="请输入密码" required><button type="button" :aria-label="showPassword?'隐藏密码':'显示密码'" @click="showPassword=!showPassword"><Icon name="eye"/></button></div></div>
      <div class="form-error" role="alert">{{ error }}</div><button class="btn" type="submit">登录<Icon name="arrow"/></button>
      <p class="form-note">页面演示：填写任意非空账号和密码即可体验。<br>登录仅在当前浏览器会话有效，关闭浏览器后会自动退出。<br>尚未连接后端，请勿输入真实密码。</p>
    </form><div class="login-foot">© 2026 {{ appState.profile.blogTitle }} · 留住每一点进步</div>
  </section>
</main>
</template>
