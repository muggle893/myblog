<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import AvatarView from '../components/AvatarView.vue'
import Icon from '../components/Icon.vue'
import { appState, logoutOwner, saveProfile, toast } from '../services/state'
import { visiblePosts } from '../services/posts'

const router=useRouter()
const recent=computed(()=>{void appState.postsRevision;return visiblePosts().slice(0,3)})
const dialog=ref(null), avatarFileInput=ref(null), previewSrc=ref(''), removeAvatar=ref(false)
let pendingAvatarFile=null, previewObjectUrl=''
const form=reactive({nickname:'',blogTitle:'',heroTitle:'',heroSubtitle:'',signature:''})
function fallbackLetter(){return Array.from(form.nickname||appState.profile.nickname||'F')[0]||'F'}
function clearPreviewObject(){if(previewObjectUrl){URL.revokeObjectURL(previewObjectUrl);previewObjectUrl=''}}
async function openDialog(){Object.assign(form,appState.profile);pendingAvatarFile=null;removeAvatar.value=false;clearPreviewObject();previewSrc.value='';dialog.value?.showModal();await nextTick();if(appState.profile.avatarImageId){try{previewSrc.value=await window.blogImages.getUrl(appState.profile.avatarImageId)}catch{}}}
function closeDialog(){clearPreviewObject();previewSrc.value='';dialog.value?.close()}
function chooseAvatar(){avatarFileInput.value?.click()}
function selectAvatar(event){const file=event.target.files?.[0];event.target.value='';if(!file)return;if(!['image/png','image/jpeg','image/webp','image/gif'].includes(file.type)){toast('请选择 PNG、JPG、WebP 或 GIF 图片');return}if(file.size>20*1024*1024){toast('头像图片请控制在 20 MB 以内');return}pendingAvatarFile=file;removeAvatar.value=false;clearPreviewObject();previewObjectUrl=URL.createObjectURL(file);previewSrc.value=previewObjectUrl;toast('头像已选择，点击“保存修改”后生效')}
function removeAvatarClick(){pendingAvatarFile=null;removeAvatar.value=true;clearPreviewObject();previewSrc.value='';toast('保存后将恢复默认字母头像')}
async function submitProfile(){
  if(!form.nickname.trim()||!form.blogTitle.trim()||!form.heroTitle.trim()){toast('昵称、博客名称和首页标题不能为空');return}
  const current={...appState.profile}, next={nickname:form.nickname.trim(),blogTitle:form.blogTitle.trim(),heroTitle:form.heroTitle.trim(),heroSubtitle:form.heroSubtitle.trim(),signature:form.signature.trim(),avatarImageId:current.avatarImageId||''}
  let newAvatarId=''
  try{
    if(removeAvatar.value)next.avatarImageId=''
    else if(pendingAvatarFile){newAvatarId='profile-avatar-'+Date.now().toString(36)+'-'+crypto.getRandomValues(new Uint32Array(1))[0].toString(36);await window.blogImages.put(pendingAvatarFile,newAvatarId);next.avatarImageId=newAvatarId}
    if(!saveProfile(next)){if(newAvatarId)await window.blogImages.remove(newAvatarId).catch(()=>{});toast('浏览器无法保存个人资料');return}
    if(current.avatarImageId&&current.avatarImageId!==next.avatarImageId)window.blogImages.remove(current.avatarImageId).catch(()=>{})
    pendingAvatarFile=null;removeAvatar.value=false;closeDialog();toast('个人资料和头像已保存，所有页面会同步更新')
  }catch(error){if(newAvatarId)await window.blogImages.remove(newAvatarId).catch(()=>{});toast(error.message||'头像保存失败，请重试')}
}
function accountAction(){if(appState.owner){logoutOwner();router.replace('/')}else router.push({name:'login',query:{next:'/profile'}})}
</script>
<template>
<main class="wrap">
  <div class="page-heading"><RouterLink to="/">首页</RouterLink><Icon name="chevron"/><span>关于我</span></div>
  <section class="card"><div class="profile-cover"></div><div class="profile-main"><div class="profile-intro"><div class="profile-name"><AvatarView size="xl"/><h1>{{ appState.profile.nickname }}</h1><p>@frank · 学习中的开发者</p></div><RouterLink class="btn" :to="appState.owner?'/editor':'/'"><Icon :name="appState.owner?'pen':'book'"/>{{ appState.owner?'写一篇文章':'查看公开文章' }}</RouterLink></div>
    <div class="profile-grid"><div><h2 class="section-title">你好，很高兴认识你</h2><p>这里是我的学习笔记，也是一个慢慢生长的个人空间。<br>喜欢把复杂的问题拆开，把学过的知识重新讲清楚。</p><p>正在学习 Java 后端开发，也尝试用 Vue 做一些自己的小东西。希望每一次动手，都能让理解更深一点。</p><div class="tags"><span class="tag">Java</span><span class="tag">SSM</span><span class="tag">Vue</span><span class="tag">持续学习</span></div></div>
      <div><h2 class="section-title">个人信息</h2><div class="info-row"><span>昵称</span><span>{{ appState.profile.nickname }}</span></div><div class="info-row"><span>账号</span><span>frank</span></div><div class="info-row"><span>博客主题</span><span>技术学习 / 生活记录</span></div><div class="info-row"><span>个人签名</span><span>{{ appState.profile.signature }}</span></div><button v-if="appState.owner" class="btn secondary" style="margin-top:20px;margin-right:8px" @click="openDialog">编辑资料</button><button class="btn outline" style="margin-top:20px" @click="accountAction"><Icon :name="appState.owner?'logout':'user'"/>{{ appState.owner?'退出演示登录':'登录我的账号' }}</button></div>
    </div></div>
  </section>
  <section class="card profile-posts"><h2 class="section-title">最近的记录</h2><RouterLink v-for="post in recent" :key="post.id" class="mini-post" :to="{name:'article',params:{id:post.id}}"><span>{{ post.title }} <em v-if="post.visibility==='private'&&appState.owner" class="mini-private">私有</em></span><small>{{ post.date }}</small></RouterLink><div v-if="!recent.length" class="empty compact">暂时还没有公开文章</div></section>

  <dialog ref="dialog" class="profile-dialog" aria-labelledby="profile-dialog-title">
    <form @submit.prevent="submitProfile"><div class="dialog-heading"><h2 id="profile-dialog-title">编辑个人资料</h2><button type="button" class="icon-button" aria-label="关闭" @click="closeDialog">×</button></div><p class="muted">头像、昵称会同步到首页、顶部导航和个人主页；首页介绍可以单独设置。</p>
      <div class="avatar-editor"><div class="avatar xl avatar-preview" :class="{'has-photo':previewSrc}"><img v-if="previewSrc" :src="previewSrc" alt="头像预览"><template v-else>{{ fallbackLetter() }}</template></div><div class="avatar-editor-actions"><input ref="avatarFileInput" type="file" accept="image/png,image/jpeg,image/webp,image/gif" hidden @change="selectAvatar"><div><button type="button" class="btn secondary" @click="chooseAvatar">选择本地图片</button><button type="button" class="btn outline" @click="removeAvatarClick">移除头像</button></div><small>支持 PNG、JPG、WebP、GIF，单张不超过 20 MB。Vue 前端阶段图片仅保存在此浏览器。</small></div></div>
      <label>昵称<input v-model="form.nickname" maxlength="20" required></label><label>博客名称<input v-model="form.blogTitle" maxlength="24" required></label><label>首页大标题<input v-model="form.heroTitle" maxlength="50" required></label><label>首页介绍<textarea v-model="form.heroSubtitle" maxlength="100" rows="2"></textarea></label><label>个人签名<input v-model="form.signature" maxlength="50"></label><p class="form-note">前端阶段修改仅保存在此浏览器；账号 frank 不随昵称改变。</p><div class="dialog-actions"><button type="button" class="btn outline" @click="closeDialog">取消</button><button type="submit" class="btn">保存修改</button></div>
    </form>
  </dialog>
</main>
</template>
