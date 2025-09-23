<script setup lang="ts">
import { ref } from 'vue'
import { expenseApi } from '../../api/expense'

const activityId = ref('')
const expenses = ref<any[]>([])
const msg = ref('')

const load = async () => {
  msg.value = ''
  try {
    expenses.value = await expenseApi.activityExpenses(activityId.value)
  } catch (e: any) {
    msg.value = e.message || '加载失败'
  }
}
</script>

<template>
  <div>
    <h2>费用统计</h2>
    <div>
      <input v-model="activityId" placeholder="活动ID" />
      <button @click="load">查询</button>
      <span>{{ msg }}</span>
    </div>
    <table>
      <thead><tr><th>描述</th><th>金额</th><th>付款人</th></tr></thead>
      <tbody>
        <tr v-for="e in expenses" :key="e.id">
          <td>{{ e.description }}</td>
          <td>{{ e.totalAmount }}</td>
          <td>{{ e.payerId }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #eee; padding: 6px 8px; }
</style>
