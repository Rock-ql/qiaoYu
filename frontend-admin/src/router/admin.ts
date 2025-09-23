// 羽毛球预约系统后台管理 - 路由配置
// ===========================================

import { RouteRecordRaw } from 'vue-router'

/**
 * 后台管理路由配置
 * 使用懒加载优化性能，支持代码分割
 */
export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'BasicLayout',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 仪表板
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/HomeView.vue'),
        meta: {
          title: '仪表板',
          icon: 'Dashboard',
          breadcrumb: [{ title: '首页', path: '/dashboard' }]
        }
      },
      
      // 用户管理
      {
        path: '/users',
        name: 'UserManagement',
        component: () => import('@/views/user/UserManagement.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          breadcrumb: [
            { title: '首页', path: '/dashboard' },
            { title: '用户管理', path: '/users' }
          ]
        }
      },
      
      // 活动管理
      {
        path: '/activities',
        name: 'ActivityManagement',
        component: () => import('@/views/activity/ActivityManagement.vue'),
        meta: {
          title: '活动管理',
          icon: 'Calendar',
          breadcrumb: [
            { title: '首页', path: '/dashboard' },
            { title: '活动管理', path: '/activities' }
          ]
        }
      },
      
      // 费用统计
      {
        path: '/expense',
        name: 'ExpenseStatistics',
        component: () => import('@/views/expense/ExpenseStatistics.vue'),
        meta: {
          title: '费用统计',
          icon: 'Money',
          breadcrumb: [
            { title: '首页', path: '/dashboard' },
            { title: '费用统计', path: '/expense' }
          ]
        }
      },
      
      // 系统设置
      {
        path: '/settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/Settings.vue'),
        meta: {
          title: '系统设置',
          icon: 'Setting',
          breadcrumb: [
            { title: '首页', path: '/dashboard' },
            { title: '系统设置', path: '/settings' }
          ]
        }
      },
      
      // 关于页面
      {
        path: '/about',
        name: 'About',
        component: () => import('@/views/AboutView.vue'),
        meta: {
          title: '关于系统',
          icon: 'InfoFilled',
          breadcrumb: [
            { title: '首页', path: '/dashboard' },
            { title: '关于系统', path: '/about' }
          ]
        }
      }
    ]
  },
  
  // 登录页面 (独立布局)
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '用户登录',
      hideInMenu: true,
      requireAuth: false
    }
  },
  
  // 注册页面 (独立布局)
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '用户注册',
      hideInMenu: true,
      requireAuth: false
    }
  },
  
  // 403 无权限页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限访问',
      hideInMenu: true,
      requireAuth: false
    }
  },
  
  // 404 页面
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      hideInMenu: true,
      requireAuth: false
    }
  },
  
  // 通配符路由，重定向到404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

/**
 * 侧边栏菜单配置
 * 基于路由自动生成，支持图标和权限控制
 */
export interface MenuConfig {
  path: string
  title: string
  icon?: string
  children?: MenuConfig[]
  hidden?: boolean
  permission?: string
}

export const menuConfig: MenuConfig[] = [
  {
    path: '/dashboard',
    title: '仪表板',
    icon: 'Dashboard'
  },
  {
    path: '/users',
    title: '用户管理',
    icon: 'User',
    permission: 'user:read'
  },
  {
    path: '/activities',
    title: '活动管理',
    icon: 'Calendar',
    permission: 'activity:read'
  },
  {
    path: '/expense',
    title: '费用统计',
    icon: 'Money',
    permission: 'expense:read'
  },
  {
    path: '/settings',
    title: '系统设置',
    icon: 'Setting',
    permission: 'system:read'
  },
  {
    path: '/about',
    title: '关于系统',
    icon: 'InfoFilled'
  }
]

/**
 * 路由元信息类型定义
 */
declare module 'vue-router' {
  interface RouteMeta {
    // 页面标题
    title?: string
    // 菜单图标
    icon?: string
    // 面包屑导航
    breadcrumb?: Array<{ title: string; path?: string }>
    // 是否在菜单中隐藏
    hideInMenu?: boolean
    // 是否需要认证
    requireAuth?: boolean
    // 权限要求
    permission?: string
    // 缓存设置
    keepAlive?: boolean
    // 页面角色
    roles?: string[]
  }
}