import { createRouter, createWebHistory } from 'vue-router'
import { adminRoutes } from './admin'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: adminRoutes,
})

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 羽毛球预约系统管理后台`
  }
  
  // 检查是否需要认证
  const requireAuth = to.meta.requireAuth !== false // 默认需要认证，除非明确设置为false
  
  if (requireAuth && !auth.isAuthed()) {
    // 未认证，跳转到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath } // 保存要跳转的页面
    })
  } else if (to.path === '/login' && auth.isAuthed()) {
    // 已认证用户访问登录页，重定向到首页
    next({ path: '/' })
  } else {
    // 权限验证通过
    next()
  }
})

export default router
