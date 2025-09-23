<template>
  <view class="container">
    <text class="title">羽毛球约球 - 首页</text>
    <navigator url="/pages/activity/list">前往活动列表</navigator>
    <navigator url="/pages/auth/login">登录</navigator>
  </view>
</template>

<script>
export default {
  name: 'IndexPage',
  onShareAppMessage() {
    return { title: '羽毛球约球 - 快来一起打球！', path: '/pages/index/index' }
  }
}
</script>

<style>
.container { padding: 24rpx; }
.title { font-size: 36rpx; color: #00C853; }
</style>
