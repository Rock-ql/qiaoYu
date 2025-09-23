<template>
  <view class="container">
    <input v-model="title" placeholder="标题" />
    <input v-model="venue" placeholder="场地" />
    <button @click="submit">创建(示例)</button>
    <text>{{ msg }}</text>
  </view>
</template>

<script>
import api from '../../utils/wechat'
export default {
  data() { return { title: '友谊赛', venue: '羽毛球馆', msg: '' } },
  methods: {
    submit() {
      this.msg = ''
      api.post('/activity/create', {
        organizerId: 'mock-user', title: this.title, venue: this.venue,
        startTime: new Date().toISOString(), endTime: new Date(Date.now()+7200000).toISOString(), maxPlayers: 4,
      }).then(res => { this.msg = '创建成功' }).catch(e => { this.msg = e.message })
    }
  }
}
</script>

<style>
.container { padding: 24rpx; display:flex; flex-direction: column; gap: 12rpx; }
</style>

