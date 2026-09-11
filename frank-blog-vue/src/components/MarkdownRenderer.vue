<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { enhanceContent, renderMarkdown } from '../services/markdown'
const props = defineProps({ source:{type:String,default:''} })
const root = ref(null)
const html = computed(()=>renderMarkdown(props.source))
async function enhance(){ await nextTick(); await enhanceContent(root.value) }
onMounted(enhance)
watch(()=>props.source, enhance)
</script>
<template><div ref="root" class="markdown" v-html="html"></div></template>
