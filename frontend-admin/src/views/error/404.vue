<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
const goHome = () => router.push('/')
</script>

<template>
  <div class="error-page">
    <div class="content">
      <h1 class="code">404</h1>
      <p class="title">页面不存在</p>
      <p class="desc">您访问的页面已移动或不存在，请检查链接是否正确。</p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page { min-height: 70vh; display:flex; align-items:center; justify-content:center; }
.content { text-align: center; padding: 24px; }
.code { font-size: 96px; line-height:1; margin: 0; color: #4b5563; }
.title { font-size: 24px; margin: 16px 0 8px; color: #374151; }
.desc { color: #6b7280; margin-bottom: 16px; }
.actions :deep(.el-button) { margin: 0 6px; }
</style>

