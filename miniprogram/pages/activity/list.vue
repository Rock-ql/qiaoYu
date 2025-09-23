<template>
  <view class="container">
    <text class="title">活动列表</text>
    <view v-for="a in list" :key="a.id" class="item" @click="openDetail(a.id)">
      <text>{{ a.title }} - {{ a.venue }}</text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/wechat'
export default {
  data() { return { list: [] } },
  onLoad() {
    api.post('/activity/available', {}).then(res => { this.list = res || [] })
  },
  methods: {
    openDetail(id) { uni.navigateTo({ url: '/pages/activity/detail?id=' + id }) }
  }
}
</script>

<style>
.container { padding: 24rpx; }
.item { padding: 16rpx 0; border-bottom: 1px solid #eee; }
</style>

