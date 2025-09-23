<template>
  <view class="container">
    <view v-if="!activity">加载中...</view>
    <view v-else>
      <text class="title">{{ activity.title }}</text>
      <text>场地：{{ activity.venue }}</text>
      <text>人数：{{ activity.currentPlayers }}/{{ activity.maxPlayers }}</text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/wechat'
export default {
  data() { return { activity: null } },
  onLoad(query) {
    const id = query.id
    api.post('/activity/detail', { activityId: id }).then(res => { this.activity = res })
  }
}
</script>

<style>
.container { padding: 24rpx; display:flex; flex-direction: column; gap: 12rpx; }
.title { font-size: 32rpx; }
</style>

