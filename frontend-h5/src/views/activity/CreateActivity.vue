<script setup lang="ts">
import { ref } from 'vue'
import { activityApi } from '../../api/activity'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const title = ref('友谊赛')
const venue = ref('羽毛球馆A')
const startTime = ref(new Date().toISOString())
const endTime = ref(new Date(Date.now()+2*60*60*1000).toISOString())
const maxPlayers = ref(4)
const description = ref('快乐运动')
const address = ref('')
const msg = ref('')

const submit = async () => {
  msg.value = ''
  try {
    const res = await activityApi.create({
      organizerId: auth.user?.id!, title: title.value, venue: venue.value,
      startTime: startTime.value, endTime: endTime.value, maxPlayers: maxPlayers.value,
      description: description.value, address: address.value,
    })
    msg.value = '创建成功：' + res.id
  } catch (e: any) {
    msg.value = e.message || '创建失败'
  }
}
</script>

<template>
  <div>
    <h3>发起约球</h3>
    <div class="form">
      <input v-model="title" placeholder="标题" />
      <input v-model="venue" placeholder="场地" />
      <input v-model="startTime" placeholder="开始时间(ISO)" />
      <input v-model="endTime" placeholder="结束时间(ISO)" />
      <input v-model.number="maxPlayers" type="number" placeholder="人数" />
      <input v-model="address" placeholder="地址(可选)" />
      <textarea v-model="description" placeholder="描述(可选)"></textarea>
      <button @click="submit">创建</button>
    </div>
    <p>{{ msg }}</p>
  </div>
</template>

<style scoped>
.form { display:flex; flex-direction:column; gap:8px; max-width: 480px; }
input, textarea { padding:8px; }
</style>

