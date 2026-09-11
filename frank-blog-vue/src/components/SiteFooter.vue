<script setup>
import { appState } from '../services/state'
</script>
<template>
<footer class="wrap footer">
  <div>© 2026 {{ appState.profile.blogTitle }}<span class="dot">·</span>记录学习，也记录生活</div>
  <div>日系插画视觉 · Vue 3 前端版本</div>
</footer>
</template>
