// 用户认证状态管理
// =======================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { request } from '@/utils/request'

/**
 * 用户信息接口
 */
export interface UserInfo {
  id: number
  username: string
  email: string
  phone?: string
  nickname?: string
  avatar?: string
  roles: string[]
  permissions: string[]
  organizationId?: number
  organizationName?: string
  lastLoginTime?: string
  status: 'active' | 'inactive' | 'locked'
}

/**
 * 登录请求接口
 */
export interface LoginRequest {
  username: string
  password: string
  captcha?: string
  rememberMe?: boolean
}

/**
 * 登录响应接口
 */
export interface LoginResponse {
  token: string
  refreshToken: string
  expiresIn: number
  userInfo: UserInfo
}

/**
 * 注册请求接口
 */
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  email: string
  phone?: string
  nickname?: string
  captcha: string
  inviteCode?: string
}

/**
 * 修改密码请求接口
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 认证状态枚举
 */
export enum AuthStatus {
  LOGGED_OUT = 'logged_out',
  LOGGING_IN = 'logging_in', 
  LOGGED_IN = 'logged_in',
  REFRESHING = 'refreshing',
  LOGIN_EXPIRED = 'login_expired'
}

const TOKEN_KEY = 'badminton_admin_token'
const REFRESH_TOKEN_KEY = 'badminton_admin_refresh_token'
const USER_INFO_KEY = 'badminton_admin_user_info'

/**
 * 用户认证状态管理
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  const authStatus = ref<AuthStatus>(AuthStatus.LOGGED_OUT)
  const loginLoading = ref(false)
  const refreshTokenTimer = ref<NodeJS.Timeout | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => 
    authStatus.value === AuthStatus.LOGGED_IN && !!token.value
  )

  const isLoggingIn = computed(() => 
    authStatus.value === AuthStatus.LOGGING_IN || loginLoading.value
  )

  const isRefreshing = computed(() => 
    authStatus.value === AuthStatus.REFRESHING
  )

  const currentUser = computed(() => userInfo.value)

  const userRoles = computed(() => userInfo.value?.roles || [])

  const userPermissions = computed(() => userInfo.value?.permissions || [])

  const hasRole = computed(() => (role: string | string[]) => {
    if (!userInfo.value?.roles) return false
    
    if (Array.isArray(role)) {
      return role.some(r => userInfo.value!.roles.includes(r))
    }
    
    return userInfo.value.roles.includes(role)
  })

  const hasPermission = computed(() => (permission: string | string[]) => {
    if (!userInfo.value?.permissions) return false
    
    if (Array.isArray(permission)) {
      return permission.some(p => userInfo.value!.permissions.includes(p))
    }
    
    return userInfo.value.permissions.includes(permission)
  })

  const isAdmin = computed(() => 
    hasRole.value(['admin', 'super_admin'])
  )

  // 认证方法
  async function login(loginData: LoginRequest): Promise<boolean> {
    try {
      loginLoading.value = true
      authStatus.value = AuthStatus.LOGGING_IN

      const response = await request.post<LoginResponse>('/auth/login', loginData, {
        showLoading: true,
        loadingText: '正在登录...',
        showError: true
      })

      const { token: accessToken, refreshToken: newRefreshToken, userInfo: user } = response.data

      // 保存认证信息
      setToken(accessToken)
      setRefreshToken(newRefreshToken)
      setUserInfo(user)
      
      authStatus.value = AuthStatus.LOGGED_IN

      // 设置token自动刷新
      setupTokenRefresh(response.data.expiresIn)

      ElMessage.success('登录成功')
      
      // 跳转到首页或之前访问的页面
      const redirect = router.currentRoute.value.query.redirect as string
      await router.push(redirect || '/dashboard')

      return true
    } catch (error) {
      console.error('登录失败:', error)
      authStatus.value = AuthStatus.LOGGED_OUT
      return false
    } finally {
      loginLoading.value = false
    }
  }

  async function logout(showMessage = true): Promise<void> {
    try {
      // 调用后端登出接口
      if (token.value) {
        await request.post('/auth/logout', {}, {
          showError: false
        }).catch(() => {
          // 登出接口失败不影响本地清理
        })
      }
    } finally {
      // 清理本地状态
      clearAuthData()
      
      if (showMessage) {
        ElMessage.success('已退出登录')
      }
      
      // 跳转到登录页
      await router.push('/login')
    }
  }

  async function register(registerData: RegisterRequest): Promise<boolean> {
    try {
      await request.post('/auth/register', registerData, {
        showLoading: true,
        loadingText: '正在注册...',
        showSuccess: true,
        successText: '注册成功，请登录'
      })

      return true
    } catch (error) {
      console.error('注册失败:', error)
      return false
    }
  }

  async function refreshAccessToken(): Promise<boolean> {
    if (!refreshToken.value || authStatus.value === AuthStatus.REFRESHING) {
      return false
    }

    try {
      authStatus.value = AuthStatus.REFRESHING

      const response = await request.post<LoginResponse>('/auth/refresh', {
        refreshToken: refreshToken.value
      }, {
        showError: false
      })

      const { token: newToken, refreshToken: newRefreshToken } = response.data

      setToken(newToken)
      setRefreshToken(newRefreshToken)
      
      authStatus.value = AuthStatus.LOGGED_IN

      // 重新设置token刷新定时器
      setupTokenRefresh(response.data.expiresIn)

      return true
    } catch (error) {
      console.error('刷新token失败:', error)
      
      // 刷新失败，清理状态并跳转到登录页
      clearAuthData()
      authStatus.value = AuthStatus.LOGIN_EXPIRED
      
      ElMessage.warning('登录已过期，请重新登录')
      await router.push('/login')
      
      return false
    }
  }

  async function getUserInfo(): Promise<UserInfo | null> {
    if (!token.value) return null

    try {
      const response = await request.get<UserInfo>('/auth/userinfo', {
        showError: false
      })

      setUserInfo(response.data)
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  async function updateUserInfo(updateData: Partial<UserInfo>): Promise<boolean> {
    try {
      const response = await request.put<UserInfo>('/auth/profile', updateData, {
        showLoading: true,
        loadingText: '正在更新...',
        showSuccess: true,
        successText: '更新成功'
      })

      setUserInfo(response.data)
      return true
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return false
    }
  }

  async function changePassword(passwordData: ChangePasswordRequest): Promise<boolean> {
    try {
      await request.post('/auth/change-password', passwordData, {
        showLoading: true,
        loadingText: '正在修改密码...',
        showSuccess: true,
        successText: '密码修改成功'
      })

      return true
    } catch (error) {
      console.error('修改密码失败:', error)
      return false
    }
  }

  // 工具方法
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  function setRefreshToken(newRefreshToken: string) {
    refreshToken.value = newRefreshToken
    localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken)
  }

  function setUserInfo(user: UserInfo) {
    userInfo.value = user
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
  }

  function clearAuthData() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    authStatus.value = AuthStatus.LOGGED_OUT

    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)

    // 清理定时器
    if (refreshTokenTimer.value) {
      clearTimeout(refreshTokenTimer.value)
      refreshTokenTimer.value = null
    }
  }

  function setupTokenRefresh(expiresIn: number) {
    // 清理之前的定时器
    if (refreshTokenTimer.value) {
      clearTimeout(refreshTokenTimer.value)
    }

    // 在token过期前5分钟刷新
    const refreshTime = Math.max(0, expiresIn - 5 * 60) * 1000
    
    refreshTokenTimer.value = setTimeout(() => {
      refreshAccessToken()
    }, refreshTime)
  }

  function loadAuthData() {
    try {
      const savedToken = localStorage.getItem(TOKEN_KEY)
      const savedRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
      const savedUserInfo = localStorage.getItem(USER_INFO_KEY)

      if (savedToken) {
        token.value = savedToken
      }

      if (savedRefreshToken) {
        refreshToken.value = savedRefreshToken
      }

      if (savedUserInfo) {
        userInfo.value = JSON.parse(savedUserInfo)
      }

      // 如果有token，设置为已登录状态
      if (savedToken && savedUserInfo) {
        authStatus.value = AuthStatus.LOGGED_IN
        
        // 尝试刷新用户信息
        getUserInfo()
      }
    } catch (error) {
      console.warn('加载认证数据失败:', error)
      clearAuthData()
    }
  }

  // 初始化方法
  function initAuth() {
    loadAuthData()
  }

  // 权限检查方法
  function checkRoute(route: any): boolean {
    // 公开路由无需认证
    if (route.meta?.public) {
      return true
    }

    // 未登录用户重定向到登录页
    if (!isLoggedIn.value) {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
      return false
    }

    // 检查角色权限
    if (route.meta?.roles && !hasRole.value(route.meta.roles)) {
      router.push('/403')
      return false
    }

    // 检查具体权限
    if (route.meta?.permissions && !hasPermission.value(route.meta.permissions)) {
      router.push('/403')
      return false
    }

    return true
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    authStatus,
    loginLoading,

    // 计算属性
    isLoggedIn,
    isLoggingIn,
    isRefreshing,
    currentUser,
    userRoles,
    userPermissions,
    hasRole,
    hasPermission,
    isAdmin,

    // 认证方法
    login,
    logout,
    register,
    refreshAccessToken,
    getUserInfo,
    updateUserInfo,
    changePassword,

    // 工具方法
    setToken,
    setRefreshToken,
    setUserInfo,
    clearAuthData,
    initAuth,
    checkRoute
  }
})

