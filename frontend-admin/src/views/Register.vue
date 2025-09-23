<script setup lang="ts">
/* eslint-disable vue/multi-word-component-names, @typescript-eslint/no-explicit-any */
// 注册页面 - 使用 Element Plus 表单
import { ref } from 'vue'
import { authApi } from '../api/auth'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Iphone, Lock } from '@element-plus/icons-vue'

const phone = ref('')
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

const router = useRouter()
const auth = useAuthStore()

const onRegister = async () => {
  if (!phone.value || !nickname.value || !password.value) {
    error.value = '请完整填写信息'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const resp = await authApi.register({ phone: phone.value, nickname: nickname.value, password: password.value })
    auth.setAuth(resp.token, resp.user)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/login')
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="brand">
        <img class="logo" alt="logo" src="/favicon.ico" />
        <div class="title-wrap">
          <h1 class="title">创建账户</h1>
          <p class="subtitle">欢迎加入，请填写注册信息</p>
        </div>
      </div>

      <el-alert v-if="error" type="error" :closable="false" show-icon class="mb16" :title="error" />

      <el-form label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="phone" placeholder="请输入手机号" clearable :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="nickname" placeholder="请输入昵称" clearable :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" size="large" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入密码" show-password :prefix-icon="Lock" size="large" @keyup.enter="onRegister" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="submit-btn" @click="onRegister" size="large">
          {{ loading ? '注册中...' : '注册' }}
        </el-button>
        <div class="extra">
          <span>已有账号？</span>
          <el-button type="primary" link @click="goLogin">去登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% 10%, #f0f7ff 0%, #ffffff 30%),
              linear-gradient(135deg, #f3f9ff 0%, #fefefe 100%);
}

.register-card {
  width: 100%;
  max-width: 760px; /* 更宽的表单区域，参考 Apple 风格 */
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  padding: 44px 48px 36px;
}

.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.logo { width: 36px; height: 36px; border-radius: 8px; }
.title-wrap { display: flex; flex-direction: column; }
.title { margin: 0; font-size: 28px; color: #111827; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #6b7280; font-size: 14px; }

.mb16 { margin-bottom: 16px; }
.submit-btn { width: 100%; margin-top: 8px; height: 48px; }
.mb16 { margin-bottom: 16px; }
.extra { margin-top: 8px; text-align: right; color: #6b7280; }

/* 调整表单项间距 */
:deep(.el-form-item) { margin-bottom: 18px; }
.extra { margin-top: 8px; text-align: right; color: #6b7280; }
</style>
