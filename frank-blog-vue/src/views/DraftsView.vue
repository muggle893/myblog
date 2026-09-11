<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import Icon from '../components/Icon.vue'
import { appState, toast } from '../services/state'
import { loadDraftRecords, removeDraftRecord } from '../services/drafts'
const filter=ref('all')
const list=computed(()=>{void appState.draftsRevision;return loadDraftRecords()})
const items=computed(()=>list.value.filter((d)=>filter.value==='all'||d.kind===filter.value))
const formatTime=(value)=>{try{return new Date(value).toLocaleString('zh-CN',{month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'})}catch{return '刚刚'}}
const wordCount=(d)=>String(d.body||'').replace(/\s/g,'').length
const draftTo=(d)=>d.kind==='edit'?{name:'editor',query:{edit:d.postId||'',draft:d.id}}:{name:'editor',query:{draft:d.id}}
function remove(d){if(!confirm(`确定删除草稿“${d.title?.trim()||'无标题草稿'}”吗？删除后无法恢复。`))return;if(removeDraftRecord(d.id))toast('草稿已删除')}
</script>
<template>
<main class="wrap drafts-page"><div class="page-heading"><RouterLink to="/">首页</RouterLink><Icon name="chevron"/><span>草稿箱</span></div>
  <section class="drafts-heading"><div><span class="drafts-eyebrow">MY DRAFTS</span><h1>草稿箱</h1><p>未发布的想法和正在修改的文章，都可以从这里继续写。</p></div><RouterLink class="btn secondary" to="/editor"><Icon name="pen"/>新建文章</RouterLink></section>
  <section class="card drafts-toolbar"><div class="draft-filters"><button v-for="item in [{k:'all',t:'全部草稿'},{k:'new',t:'未发布'},{k:'edit',t:'文章修改'}]" :key="item.k" type="button" class="draft-filter" :class="{active:filter===item.k}" @click="filter=item.k">{{ item.t }}</button></div><span>共 {{ list.length }} 篇草稿</span></section>
  <section class="draft-list" aria-live="polite">
    <article v-for="draft in items" :key="draft.id" class="card draft-card"><div class="draft-card-main"><div class="draft-meta"><span class="draft-kind" :class="draft.kind==='edit'?'edit':'new'">{{ draft.kind==='edit'?'文章修改':'未发布' }}</span><span><Icon :name="draft.visibility==='private'?'lock':'eye'"/> {{ draft.visibility==='private'?'私有':'公开' }}</span><span>{{ draft.category||'未分类' }}</span></div><h2><RouterLink :to="draftTo(draft)">{{ draft.title?.trim()||'无标题草稿' }}</RouterLink></h2><p>{{ wordCount(draft) }} 字 · 最后保存 {{ formatTime(draft.savedAt) }}</p><div class="tags"><span v-for="tag in (draft.tags||[]).slice(0,5)" :key="tag" class="tag"># {{ tag }}</span><span v-if="!(draft.tags||[]).length" class="tag">还没有标签</span></div></div><div class="draft-actions"><RouterLink class="btn secondary" :to="draftTo(draft)"><Icon name="pen"/>继续编辑</RouterLink><button class="btn outline draft-delete" type="button" @click="remove(draft)"><Icon name="trash"/>删除</button></div></article>
    <div v-if="!items.length" class="card drafts-empty"><div class="draft-empty-icon"><Icon name="drafts"/></div><h2>{{ list.length?'这个分组里还没有草稿':'草稿箱还是空的' }}</h2><p>{{ list.length?'可以切换到“全部草稿”查看其他内容。':'写文章时点击“保存草稿”，或开始输入后等待自动保存，草稿就会出现在这里。' }}</p><RouterLink class="btn secondary" to="/editor"><Icon name="pen"/>开始写文章</RouterLink></div>
  </section><p class="form-note">Vue 前端阶段：草稿目前仍保存在这个浏览器中。后续接入后端时，再改成账号级草稿同步。</p>
</main>
</template>
