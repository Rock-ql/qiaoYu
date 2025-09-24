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
    <h2 style="margin:0 0 12px 0;">用户管理</h2>
    <el-table v-loading="loading" :data="list" stripe border size="large" header-cell-class-name="table-header" :header-row-style="{ height: '46px' }" :row-style="{ height: '46px' }">
      <el-table-column prop="id" label="ID" min-width="280" align="left" header-align="left" />
      <el-table-column prop="nickname" label="昵称" min-width="120" align="left" header-align="left" />
      <el-table-column prop="phone" label="手机号" min-width="160" align="left" header-align="left" />
      <el-table-column label="状态" min-width="100" align="left" header-align="left">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
  
</template>

<style scoped>
.table-header { font-weight: 600; color: #374151; }
</style>
