<script setup lang="ts">
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore()
</script>

<template>
  <div>
    <h3>个人中心</h3>
    <div v-if="auth.user">
      <p>昵称：{{ auth.user.nickname }}</p>
      <p>手机号：{{ auth.user.phone }}</p>
      <img v-if="auth.user.avatar" :src="auth.user.avatar" alt="avatar" width="80" />
    </div>
  </div>
</template>

