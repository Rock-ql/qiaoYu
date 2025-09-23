<template>
  <view class="container">
    <text class="title">微信登录</text>
    <button type="primary" @click="onLogin">一键登录(示例)</button>
    <text class="tips">示例占位：应通过 wx.login 获取 code 调后端</text>
  </view>
</template>

<script>
export default {
  name: 'LoginPage',
  methods: {
    onLogin() {
      uni.login({
        success: (res) => {
          const code = res.code
          // 实际应将 code 发至后端 /api/auth/wechat
          uni.showToast({ title: '登录成功(示例)', icon: 'success' })
        },
      })
    },
  },
}
</script>

<style>
.container { padding: 24rpx; }
.title { font-size: 32rpx; }
.tips { color: #888; }
</style>

