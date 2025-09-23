<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { activityApi } from '../../api/activity'

const loading = ref(false)
const list = ref<any[]>([])

onMounted(async () => {
  loading.value = true
  try {
    list.value = await activityApi.available()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h2>活动管理</h2>
    <div v-if="loading">加载中...</div>
    <table v-else>
      <thead><tr><th>标题</th><th>场地</th><th>人数</th><th>状态</th></tr></thead>
      <tbody>
        <tr v-for="a in list" :key="a.id">
          <td>{{ a.title }}</td>
          <td>{{ a.venue }}</td>
          <td>{{ a.currentPlayers }}/{{ a.maxPlayers }}</td>
          <td>{{ a.status }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #eee; padding: 6px 8px; }
</style>
