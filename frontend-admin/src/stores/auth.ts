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
  phone: string
  password: string
  captcha?: string
  rememberMe?: boolean
}

/**
 * 登录响应接口（匹配后端返回格式）
 */
export interface LoginResponse {
  token: string
  user: UserInfo
  refreshToken?: string
  expiresIn?: number
}

/**
 * 注册请求接口（匹配后端格式）
 */
export interface RegisterRequest {
  phone: string
  nickname: string
  password: string
  // 前端额外字段，不发送给后端
  confirmPassword?: string
  captcha?: string
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
const TOKEN_EXPIRE_TIME_KEY = 'badminton_admin_token_expire_time'

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

      // 兼容返回包裹 {code,message,data} 或直接数据两种格式
      const payload: any = (response as any)?.data?.data ?? (response as any)?.data ?? response
      const { token: accessToken, user, refreshToken: newRefreshToken, expiresIn = 3600 } = payload

      // 保存认证信息
      setToken(accessToken, expiresIn)
      if (newRefreshToken) {
        setRefreshToken(newRefreshToken)
      }
      setUserInfo(user)

      authStatus.value = AuthStatus.LOGGED_IN

      // 设置token自动刷新
      setupTokenRefresh(expiresIn)

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
      // 只发送后端需要的字段
      const backendRequest = {
        phone: registerData.phone,
        nickname: registerData.nickname,
        password: registerData.password
      }

      await request.post('/auth/register', backendRequest, {
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

      const payload: any = (response as any)?.data?.data ?? (response as any)?.data ?? response
      const { token: newToken, refreshToken: newRefreshToken, expiresIn = 3600 } = payload

      setToken(newToken, expiresIn)
      if (newRefreshToken) {
        setRefreshToken(newRefreshToken)
      }

      authStatus.value = AuthStatus.LOGGED_IN

      // 重新设置token刷新定时器
      setupTokenRefresh(expiresIn)

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

      const payload: any = (response as any)?.data?.data ?? (response as any)?.data ?? response
      setUserInfo(payload)
      return payload
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

      const payload: any = (response as any)?.data?.data ?? (response as any)?.data ?? response
      setUserInfo(payload)
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
  function setToken(newToken: string, expiresIn?: number) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)

    // 保存token过期时间 (默认60分钟)
    const expireSeconds = expiresIn || (60 * 60) // 默认60分钟
    const expireTime = Date.now() + (expireSeconds * 1000)
    localStorage.setItem(TOKEN_EXPIRE_TIME_KEY, expireTime.toString())
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
    localStorage.removeItem(TOKEN_EXPIRE_TIME_KEY)

    // 清理定时器
    if (refreshTokenTimer.value) {
      clearTimeout(refreshTokenTimer.value)
      refreshTokenTimer.value = null
    }
  }

  // 检查token是否过期
  function isTokenExpired(): boolean {
    const expireTimeStr = localStorage.getItem(TOKEN_EXPIRE_TIME_KEY)
    if (!expireTimeStr) return true

    const expireTime = parseInt(expireTimeStr)
    return Date.now() >= expireTime
  }

  // 检查token是否即将过期（5分钟内）
  function isTokenExpiringSoon(): boolean {
    const expireTimeStr = localStorage.getItem(TOKEN_EXPIRE_TIME_KEY)
    if (!expireTimeStr) return true

    const expireTime = parseInt(expireTimeStr)
    return (expireTime - Date.now()) <= (5 * 60 * 1000) // 5分钟
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

      if (savedToken && savedUserInfo) {
        // 检查token是否过期
        if (isTokenExpired()) {
          // token已过期
          if (savedRefreshToken) {
            // 尝试刷新token
            refreshAccessToken()
          } else {
            // 没有refresh token，清理过期数据
            clearAuthData()
            ElMessage.warning('登录已过期，请重新登录')
          }
          return
        }

        // token未过期，恢复认证状态
        token.value = savedToken
        refreshToken.value = savedRefreshToken || ''
        userInfo.value = JSON.parse(savedUserInfo)
        authStatus.value = AuthStatus.LOGGED_IN

        // 检查是否即将过期，如果是则尝试刷新
        if (isTokenExpiringSoon() && savedRefreshToken) {
          refreshAccessToken()
        }

        // 尝试获取最新用户信息
        getUserInfo()
      } else {
        // 没有完整的认证信息
        clearAuthData()
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
