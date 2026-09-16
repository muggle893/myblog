<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import Icon from '../components/Icon.vue'
import { appState, loginOwner } from '../services/state'

// ref 用于声明基本类型的响应式数据；模板中可以直接使用 username，
// 在 JavaScript 中修改它时需要使用 username.value。
const username = ref('')
const password = ref('')
const error = ref('')
const showPassword = ref(false)
const loading = ref(false)

const router = useRouter()

// 必须和后端 CodeEnums.SUCCESS.getCode() 的值一致
const SUCCESS_CODE = 200

async function submit() {
  // submit 由表单触发；loading 为 true 时直接返回，防止重复发送登录请求。
  if (loading.value) return

  error.value = ''

  if (!username.value.trim() || !password.value) {
    error.value = '请填写账号和密码'
    return
  }

  // 进入异步请求前设置 loading，模板会据此禁用按钮并显示“登录中”。
  loading.value = true

  try {
    // 后端接收 String username、String password，
    // 所以这里发送表单参数
    const formData = new URLSearchParams()
    formData.set('username', username.value.trim())
    formData.set('password', password.value)

    // fetch 返回 Promise<Response>；await 会等待服务器响应。
    const response = await fetch('/api/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      // 登录接口成功后通常会通过 Set-Cookie 写入会话 Cookie。
      credentials: 'include',
      body: formData
    })

    // 检查 HTTP 状态，例如 404、500
    if (!response.ok) {
      error.value = `登录请求失败，HTTP 状态码：${response.status}`
      return
    }

    // response.json() 把响应体解析成 JavaScript 对象：{ code, msg, data }。
    const result = await response.json()

    // HTTP 请求成功，不代表账号密码验证成功
    if (result.code !== SUCCESS_CODE) {
      error.value = result.msg || '登录失败，请检查账号和密码'
      return
    }

    // 后端确认登录成功后，保存用户名并更新前端导航栏等显示状态。
    // loginOwner 同时修改 sessionStorage 和 reactive 的 appState。
    if (!loginOwner(username.value.trim())) {
      // 浏览器不允许使用 sessionStorage 时，
      // 仍然更新当前页面的响应式状态
      appState.owner = true
    }

    password.value = ''

    // 跳转到博客首页
    await router.replace({ name: 'home' })
  } catch {
    // fetch 遇到网络不可达、代理未启动等异常时会进入 catch。
    error.value = '登录请求未完成，请检查后端是否启动及代理配置'
  } finally {
    // finally 无论成功、失败还是提前 return 后的异常流程都会执行。
    loading.value = false
  }
}
</script>

<template>
  <main class="login-shell">
    <section class="login-visual">
      <RouterLink class="brand" to="/">
        <span class="brand-symbol">
          <Icon name="book" />
        </span>
        {{ appState.profile.blogTitle }}
      </RouterLink>

      <div class="login-quote">
        <div class="eyebrow">A LITTLE SPACE FOR YOUR THOUGHTS</div>
        <h1>
          让每一个想法，<br />
          都有地方落笔。
        </h1>
        <p>
          记录今天学会的事情，<br />
          也留住那些值得回看的瞬间。
        </p>
      </div>

      <small>LEARN · BUILD · SHARE</small>
    </section>

    <section class="login-panel">
      <RouterLink class="back-link" to="/">
        <Icon name="back" />
        返回博客
      </RouterLink>

      <form class="login-form" @submit.prevent="submit">
        <div class="eyebrow">WELCOME BACK</div>
        <h2>欢迎回来</h2>
        <p class="muted">登录你的账号，继续记录与分享。</p>

        <div class="field">
          <label for="username">账号</label>
          <div class="input-wrap">
            <Icon name="user" />
            <input
              id="username"
              v-model="username"
              autocomplete="username"
              placeholder="请输入你的账号"
              required
            >
          </div>
        </div>

        <div class="field">
          <label for="password">密码</label>
          <div class="input-wrap">
            <Icon name="lock" />
            <input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
              required
            >
            <button
              type="button"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <Icon name="eye" />
            </button>
          </div>
        </div>

        <div class="form-error" role="alert">{{ error }}</div>

        <button class="btn" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
          <Icon name="arrow" />
        </button>

        <p class="form-note">
        </p>
      </form>

      <div class="login-foot">
        © 2026 {{ appState.profile.blogTitle }} · 留住每一点进步
      </div>
    </section>
  </main>
</template>

