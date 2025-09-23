import './assets/main.css'
// 引入全局样式系统
import './styles/variables.scss'
import './styles/utils.scss'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
// 引入 Element Plus 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
