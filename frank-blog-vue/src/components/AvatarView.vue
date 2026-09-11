<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { appState } from '../services/state'

const props = defineProps({
  size: { type:String, default:'' },
  to: { type:[String,Object], default:null },
})
const src = ref('')
let token = 0
const cls = computed(()=>['avatar', props.size, src.value ? 'has-photo' : ''])
const fallback = computed(()=>Array.from(appState.profile.nickname || 'F')[0] || 'F')

async function refresh() {
  const current = ++token
  src.value = ''
  const id = appState.profile.avatarImageId
  if (!id || !window.blogImages?.getUrl) return
  try {
    const url = await window.blogImages.getUrl(id)
    if (current === token) src.value = url
  } catch {}
}
watch(()=>appState.profile.avatarImageId, refresh, { immediate:true })
onBeforeUnmount(()=>{ token++ })
</script>
<template>
  <component :is="to ? RouterLink : 'div'" :to="to || undefined" :class="cls" :aria-label="appState.profile.nickname + '的个人资料'">
    <img v-if="src" :src="src" :alt="appState.profile.nickname + '的头像'" draggable="false" />
    <template v-else>{{ fallback }}</template>
  </component>
</template>
