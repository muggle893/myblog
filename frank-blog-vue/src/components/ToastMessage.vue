<script setup>
import { appState } from '../services/state'
</script>
<template><div class="toast" :class="{show:appState.toastVisible}" role="status" aria-live="polite">{{ appState.toastMessage }}</div></template>
