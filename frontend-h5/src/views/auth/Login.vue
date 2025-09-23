<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'

const phone = ref('13800138001')
const password = ref('123456')
const loading = ref(false)
const error = ref('')
const router = useRouter()
const auth = useAuthStore()

const onLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const resp = await authApi.login({ phone: phone.value, password: password.value })
    auth.setAuth(resp.token, resp.user)
    router.push('/')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <h2>登录</h2>
    <div class="form">
      <input v-model="phone" placeholder="手机号" />
      <input v-model="password" type="password" placeholder="密码" />
      <button :disabled="loading" @click="onLogin">{{ loading ? '登录中...' : '登录' }}</button>
      <p class="error" v-if="error">{{ error }}</p>
    </div>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.login { max-width: 400px; margin: 40px auto; padding: 24px; }
.form { display: flex; flex-direction: column; gap: 12px; }
.error { color: #d33; }
</style>

