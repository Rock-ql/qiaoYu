<script setup lang="ts">
import { ref } from 'vue'
import { expenseApi } from '../../api/expense'

const activityId = ref('')
const list = ref<any[]>([])
const msg = ref('')

const load = async () => {
  msg.value = ''
  try {
    list.value = await expenseApi.activityExpenses(activityId.value)
  } catch (e: any) {
    msg.value = e.message || '加载失败'
  }
}
</script>

<template>
  <div>
    <h3>活动费用记录</h3>
    <input v-model="activityId" placeholder="活动ID" />
    <button @click="load">加载</button>
    <p>{{ msg }}</p>
    <ul>
      <li v-for="e in list" :key="e.id">{{ e.description }} - {{ e.totalAmount }}</li>
    </ul>
  </div>
</template>

