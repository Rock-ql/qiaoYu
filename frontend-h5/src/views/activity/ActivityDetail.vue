<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { activityApi } from '../../api/activity'

const route = useRoute()
const activity = ref<any>(null)
const loading = ref(false)

onMounted(async () => {
  const id = route.query.id as string
  if (!id) return
  loading.value = true
  try {
    activity.value = await activityApi.detail(id)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <div v-if="loading">加载中...</div>
    <div v-else-if="activity">
      <h3>{{ activity.title }}</h3>
      <p>场地：{{ activity.venue }}</p>
      <p>时间：{{ activity.startTime }} ~ {{ activity.endTime }}</p>
      <p>人数：{{ activity.currentPlayers }}/{{ activity.maxPlayers }}</p>
      <p>描述：{{ activity.description }}</p>
    </div>
    <div v-else>未找到活动</div>
  </div>
</template>

