<script setup lang="ts">
/* eslint-disable vue/multi-word-component-names, @typescript-eslint/no-explicit-any */
// 登录页面 - 采用 Element Plus 重构交互与样式
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { Iphone, Lock } from '@element-plus/icons-vue'

// 表单状态
const phone = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()
const auth = useAuthStore()

// 提交登录
const onLogin = async () => {
  if (!phone.value || !password.value) {
    error.value = '请输入手机号与密码'
    return
  }

  error.value = ''

  try {
    // 使用store的login方法
    const success = await auth.login({
      phone: phone.value,
      password: password.value
    })

    if (!success) {
      error.value = '登录失败，请检查账号密码'
    }
  } catch (e: any) {
    error.value = e.message || '登录失败'
  }
}

// 跳转注册
const goRegister = () => router.push('/register')
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <img class="logo" alt="logo" src="/favicon.ico" />
        <div class="title-wrap">
          <h1 class="title">系统后台</h1>
          <p class="subtitle">欢迎回来，请登录您的账户</p>
        </div>
      </div>

      <el-alert v-if="error" type="error" :closable="false" show-icon class="mb16" :title="error" />

      <el-form label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="phone" placeholder="请输入手机号" clearable :prefix-icon="Iphone" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" size="large" @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" :loading="auth.isLoggingIn" class="submit-btn" @click="onLogin" size="large">
          {{ auth.isLoggingIn ? '登录中...' : '登录' }}
        </el-button>
        <div class="extra">
          <span>还没有账号？</span>
          <el-button type="primary" link @click="goRegister">去注册</el-button>
        </div>
      </el-form>
    </div>
    <div class="footer">
      <router-link to="/">返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
/* 全屏背景与居中布局 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% 10%, #f0f7ff 0%, #ffffff 30%),
              linear-gradient(135deg, #f3f9ff 0%, #fefefe 100%);
}

/* 卡片容器 */
.login-card {
  width: 100%;
  max-width: 760px; /* 更宽的表单区域，参考 Apple 风格 */
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  padding: 44px 48px 36px;
}

/* 品牌区域 */
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.logo { width: 36px; height: 36px; border-radius: 8px; }
.title-wrap { display: flex; flex-direction: column; }
.title { margin: 0; font-size: 28px; color: #111827; font-weight: 600; }
.subtitle { margin: 8px 0 0; color: #6b7280; font-size: 14px; }

.mb16 { margin-bottom: 16px; }
.submit-btn { width: 100%; margin-top: 8px; height: 48px; }
.extra { margin-top: 8px; text-align: right; color: #6b7280; }

/* 调整表单项间距 */
:deep(.el-form-item) { margin-bottom: 18px; }

.footer { margin-top: 16px; color: #6b7280; }
</style>
