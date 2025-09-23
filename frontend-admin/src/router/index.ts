import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import BasicLayout from '../layouts/BasicLayout.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
    {
      path: '/',
      component: BasicLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'home', component: HomeView },
        { path: 'about', name: 'about', component: () => import('../views/AboutView.vue') },
        { path: 'users', name: 'users', component: () => import('../views/user/UserManagement.vue') },
        { path: 'activities', name: 'activities', component: () => import('../views/activity/ActivityManagement.vue') },
        { path: 'expense', name: 'expense', component: () => import('../views/expense/ExpenseStatistics.vue') },
        { path: 'settings', name: 'settings', component: () => import('../views/system/Settings.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthed()) {
    return { path: '/login' }
  }
})

export default router
