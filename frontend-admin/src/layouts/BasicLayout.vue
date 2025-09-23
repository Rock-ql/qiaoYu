<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter, RouterView, RouterLink } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const logout = () => { auth.logout(); router.push('/login') }
</script>

<template>
  <header class="header">
    <div class="brand">Badminton Admin</div>
    <div class="nav">
      <RouterLink to="/">首页</RouterLink>
      <RouterLink to="/about">关于</RouterLink>
      <RouterLink to="/users">用户</RouterLink>
      <RouterLink to="/activities">活动</RouterLink>
      <RouterLink to="/expense">费用</RouterLink>
      <RouterLink to="/settings">设置</RouterLink>
      <a href="javascript:;" @click="logout">退出</a>
    </div>
  </header>
  <main class="content">
    <RouterView />
  </main>
</template>

<style scoped>
.header { display:flex; justify-content:space-between; align-items:center; padding:12px 16px; background:#f5f5f5; }
.brand { font-weight: bold; }
.nav a { margin-left: 12px; }
.content { padding: 16px; }
</style>
