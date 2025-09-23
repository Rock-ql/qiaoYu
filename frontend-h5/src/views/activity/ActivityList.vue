<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { activityApi } from '../../api/activity'
import { useRouter } from 'vue-router'

const loading = ref(false)
const list = ref<any[]>([])
const router = useRouter()

onMounted(async () => {
  loading.value = true
  try {
    list.value = await activityApi.available()
  } finally {
    loading.value = false
  }
})

const open = (id: string) => router.push({ path: '/activity/detail', query: { id } })
</script>

<template>
  <div>
    <h3>可参加的活动</h3>
    <div v-if="loading">加载中...</div>
    <ul v-else>
      <li v-for="a in list" :key="a.id">
        <a href="javascript:;" @click="open(a.id)">{{ a.title }}（{{ a.venue }}）</a>
      </li>
    </ul>
  </div>
</template>

