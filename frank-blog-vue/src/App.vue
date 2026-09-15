<script setup>
import { watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
import ToastMessage from './components/ToastMessage.vue'
import { appState } from './services/state'

const route = useRoute()

function applyPage() {
  document.body.dataset.page = route.meta.page || ''
  document.title =
    (route.meta.title || '拾光手记') +
    (route.meta.title ? ' · ' + appState.profile.blogTitle : '')
}

watch(() => route.fullPath, applyPage, { immediate: true })
watch(() => appState.profile.blogTitle, applyPage)
watch(
  () => appState.theme,
  (value) => document.body.classList.toggle('theme-dark', value === 'dark'),
  { immediate: true }
)
</script>

<template>
  <template v-if="route.name === 'login'">
    <RouterView />
  </template>
  <template v-else>
    <SiteHeader />
    <RouterView />
    <SiteFooter />
  </template>
  <ToastMessage />
</template>

