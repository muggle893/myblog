<script setup>
import { computed } from 'vue'
import { icon } from '../services/icons'
const props = defineProps({ name: { type:String, default:'book' } })
const path = computed(()=>icon(props.name))
</script>
<template><svg viewBox="0 0 24 24" aria-hidden="true" v-html="path"></svg></template>
