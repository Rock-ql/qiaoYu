<script setup lang="ts">
import { ref } from 'vue'

const redisHost = ref('localhost')
const redisPort = ref(6379)
const tips = '此页仅演示占位，实际应在后端/配置中心管理。'
</script>

<template>
  <div>
    <h2>系统设置</h2>
    <p class="tip">{{ tips }}</p>
    <div class="form">
      <label>Redis Host <input v-model="redisHost" /></label>
      <label>Redis Port <input type="number" v-model.number="redisPort" /></label>
      <button disabled>保存（占位）</button>
    </div>
  </div>
</template>

<style scoped>
.tip { color: #999; }
.form { display: flex; gap: 12px; align-items: center; }
label { display: flex; gap: 6px; align-items: center; }
</style>

