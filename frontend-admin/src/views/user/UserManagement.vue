<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { userApi } from '../../api/user'

const loading = ref(false)
const list = ref<any[]>([])

onMounted(async () => {
  loading.value = true
  try {
    list.value = await userApi.list()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h2>用户管理</h2>
    <div v-if="loading">加载中...</div>
    <table v-else>
      <thead><tr><th>ID</th><th>昵称</th><th>手机号</th><th>状态</th></tr></thead>
      <tbody>
        <tr v-for="u in list" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.nickname }}</td>
          <td>{{ u.phone }}</td>
          <td>{{ u.status === 1 ? '正常' : '禁用' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
  
</template>

<style scoped>
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #eee; padding: 6px 8px; }
</style>
