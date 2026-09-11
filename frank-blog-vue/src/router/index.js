import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import LoginView from '../views/LoginView.vue'
import ArticleView from '../views/ArticleView.vue'
import EditorView from '../views/EditorView.vue'
import DraftsView from '../views/DraftsView.vue'
import { appState } from '../services/state'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path:'/', name:'home', component:HomeView, meta:{page:'home', title:'首页'} },
    { path:'/profile', name:'profile', component:ProfileView, meta:{page:'profile', title:'关于我'} },
    { path:'/login', name:'login', component:LoginView, meta:{page:'login', title:'登录'} },
    { path:'/article/:id', name:'article', component:ArticleView, meta:{page:'article', title:'文章详情'} },
    { path:'/editor', name:'editor', component:EditorView, meta:{page:'editor', title:'写文章', ownerOnly:true} },
    { path:'/drafts', name:'drafts', component:DraftsView, meta:{page:'drafts', title:'草稿箱', ownerOnly:true} },
    { path:'/:pathMatch(.*)*', redirect:'/' },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top:0 }
  },
})

router.beforeEach((to) => {
  if (to.meta.ownerOnly && !appState.owner) return { name:'login', query:{ next:to.fullPath } }
})

export default router
