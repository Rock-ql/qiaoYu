// API请求工具类
// =======================================

import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

/**
 * API响应接口
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页响应接口
 */
export interface PaginatedResponse<T> {
  list: T[]
  total: number
  current: number
  pageSize: number
  pages: number
}

/**
 * 请求配置扩展
 */
export interface RequestConfig extends AxiosRequestConfig {
  // 是否显示loading
  showLoading?: boolean
  // loading文本
  loadingText?: string
  // 是否显示错误消息
  showError?: boolean
  // 是否显示成功消息
  showSuccess?: boolean
  // 成功消息文本
  successText?: string
  // 是否重试
  retry?: boolean
  // 重试次数
  retryCount?: number
  // 重试延迟
  retryDelay?: number
}

/**
 * 响应状态码枚举
 */
export enum ResponseCode {
  SUCCESS = 0,           // 成功
  PARAM_ERROR = 400,     // 参数错误
  UNAUTHORIZED = 401,    // 未认证
  FORBIDDEN = 403,       // 无权限
  NOT_FOUND = 404,       // 未找到
  METHOD_NOT_ALLOWED = 405, // 方法不允许
  SERVER_ERROR = 500,    // 服务器错误
  BAD_GATEWAY = 502,     // 网关错误
  SERVICE_UNAVAILABLE = 503, // 服务不可用
  GATEWAY_TIMEOUT = 504  // 网关超时
}

/**
 * 错误类型枚举
 */
export enum ErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  BUSINESS_ERROR = 'BUSINESS_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  AUTH_ERROR = 'AUTH_ERROR'
}

/**
 * 自定义错误类
 */
export class ApiError extends Error {
  public code: number
  public type: ErrorType
  public data?: any

  constructor(message: string, code: number, type: ErrorType, data?: any) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.type = type
    this.data = data
  }
}

/**
 * 请求拦截器配置
 */
interface RequestInterceptorConfig {
  onRequest?: (config: InternalAxiosRequestConfig) => InternalAxiosRequestConfig
  onRequestError?: (error: any) => Promise<any>
}

/**
 * 响应拦截器配置
 */
interface ResponseInterceptorConfig {
  onResponse?: (response: AxiosResponse) => AxiosResponse
  onResponseError?: (error: AxiosError) => Promise<any>
}

/**
 * HTTP请求客户端类
 */
class HttpClient {
  private instance: AxiosInstance
  private loadingInstance: any = null
  private pendingRequests = new Map<string, AbortController>()

  constructor(config?: AxiosRequestConfig) {
    this.instance = axios.create({
      // 统一与 vite.config.ts 代理配置：优先 VITE_API_BASE，否则使用 /api
      baseURL: (import.meta as any).env.VITE_API_BASE || '/api',
      timeout: 15000,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
      ...config
    })

    this.setupInterceptors()
  }

  /**
   * 设置拦截器
   */
  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        // 添加认证token
        const authStore = useAuthStore()
        const token = authStore.token
        
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }

        // 添加请求唯一标识
        const requestId = this.generateRequestId(config)
        config.metadata = { requestId }

        // 处理重复请求
        this.handleDuplicateRequest(config, requestId)

        // 显示loading
        const customConfig = config as RequestConfig
        if (customConfig.showLoading !== false) {
          this.showLoading(customConfig.loadingText)
        }

        // 添加请求时间戳
        config.headers['X-Request-Time'] = Date.now().toString()

        console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
          params: config.params,
          data: config.data,
          headers: config.headers
        })

        return config
      },
      (error) => {
        this.hideLoading()
        console.error('[API Request Error]', error)
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        const config = response.config as RequestConfig
        const requestId = (config as any).metadata?.requestId

        // 移除pending请求
        if (requestId) {
          this.pendingRequests.delete(requestId)
        }

        this.hideLoading()

        console.log(`[API Response] ${config.method?.toUpperCase()} ${config.url}`, {
          status: response.status,
          data: response.data
        })

        // 检查业务状态码
        const apiResponse = response.data as ApiResponse
        
        if (apiResponse && typeof apiResponse.code === 'number') {
          if (apiResponse.code === ResponseCode.SUCCESS) {
            // 显示成功消息
            if (config.showSuccess && config.successText) {
              ElMessage.success(config.successText)
            }
            return response
          } else {
            // 业务错误处理
            const error = new ApiError(
              apiResponse.message || '请求失败',
              apiResponse.code,
              ErrorType.BUSINESS_ERROR,
              apiResponse.data
            )
            return this.handleBusinessError(error, config)
          }
        }

        return response
      },
      (error: AxiosError) => {
        const config = error.config as RequestConfig
        const requestId = (config as any)?.metadata?.requestId

        // 移除pending请求
        if (requestId) {
          this.pendingRequests.delete(requestId)
        }

        this.hideLoading()

        console.error(`[API Error] ${config?.method?.toUpperCase()} ${config?.url}`, error)

        return this.handleHttpError(error)
      }
    )
  }

  /**
   * 生成请求ID
   */
  private generateRequestId(config: InternalAxiosRequestConfig): string {
    const url = config.url || ''
    const method = config.method || 'get'
    const params = JSON.stringify(config.params || {})
    const data = JSON.stringify(config.data || {})
    
    return `${method.toUpperCase()}_${url}_${btoa(params + data).replace(/[/+=]/g, '')}`
  }

  /**
   * 处理重复请求
   */
  private handleDuplicateRequest(config: InternalAxiosRequestConfig, requestId: string) {
    // 取消之前的相同请求
    if (this.pendingRequests.has(requestId)) {
      const controller = this.pendingRequests.get(requestId)
      controller?.abort('重复请求被取消')
    }

    // 创建新的AbortController
    const controller = new AbortController()
    config.signal = controller.signal
    this.pendingRequests.set(requestId, controller)
  }

  /**
   * 处理HTTP错误
   */
  private async handleHttpError(error: AxiosError): Promise<never> {
    const config = error.config as RequestConfig
    
    let apiError: ApiError

    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      // 超时错误
      apiError = new ApiError('请求超时，请稍后重试', 408, ErrorType.TIMEOUT_ERROR)
    } else if (!error.response) {
      // 网络错误
      apiError = new ApiError('网络连接失败，请检查网络设置', 0, ErrorType.NETWORK_ERROR)
    } else {
      // HTTP状态码错误
      const { status, data } = error.response
      let message = '请求失败'
      let type = ErrorType.BUSINESS_ERROR

      switch (status) {
        case ResponseCode.UNAUTHORIZED:
          message = '登录已过期，请重新登录'
          type = ErrorType.AUTH_ERROR
          // 清除认证信息并跳转到登录页
          const authStore = useAuthStore()
          authStore.logout()
          router.push('/login')
          break
        case ResponseCode.FORBIDDEN:
          message = '没有权限访问该资源'
          type = ErrorType.AUTH_ERROR
          break
        case ResponseCode.NOT_FOUND:
          message = '请求的资源不存在'
          break
        case ResponseCode.METHOD_NOT_ALLOWED:
          message = '请求方法不允许'
          break
        case ResponseCode.SERVER_ERROR:
          message = '服务器内部错误'
          break
        case ResponseCode.BAD_GATEWAY:
          message = '网关错误'
          break
        case ResponseCode.SERVICE_UNAVAILABLE:
          message = '服务暂时不可用'
          break
        case ResponseCode.GATEWAY_TIMEOUT:
          message = '网关超时'
          break
        default:
          if (data && data.message) {
            message = data.message
          }
      }

      apiError = new ApiError(message, status, type, data)
    }

    // 重试逻辑
    if (config.retry && (config.retryCount || 0) < 3) {
      return this.retryRequest(config, apiError)
    }

    // 显示错误消息
    if (config.showError !== false) {
      this.showError(apiError)
    }

    return Promise.reject(apiError)
  }

  /**
   * 处理业务错误
   */
  private handleBusinessError(error: ApiError, config: RequestConfig): Promise<never> {
    // 特殊业务错误处理
    switch (error.code) {
      case ResponseCode.UNAUTHORIZED:
        // 认证失败
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
        break
      case ResponseCode.FORBIDDEN:
        // 权限不足
        router.push('/403')
        break
    }

    // 显示错误消息
    if (config.showError !== false) {
      this.showError(error)
    }

    return Promise.reject(error)
  }

  /**
   * 重试请求
   */
  private async retryRequest(config: RequestConfig, lastError: ApiError): Promise<never> {
    const retryCount = (config.retryCount || 0) + 1
    const retryDelay = config.retryDelay || 1000

    console.log(`[API Retry] 第${retryCount}次重试 ${config.method?.toUpperCase()} ${config.url}`)

    // 等待指定时间后重试
    await new Promise(resolve => setTimeout(resolve, retryDelay * retryCount))

    // 更新重试次数
    const newConfig: RequestConfig = {
      ...config,
      retryCount,
      showLoading: false // 重试时不显示loading
    }

    try {
      return await this.instance.request(newConfig)
    } catch (error) {
      // 重试仍然失败，返回原始错误
      return Promise.reject(lastError)
    }
  }

  /**
   * 显示loading
   */
  private showLoading(text?: string) {
    if (this.loadingInstance) {
      return
    }

    this.loadingInstance = ElLoading.service({
      lock: true,
      text: text || '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }

  /**
   * 隐藏loading
   */
  private hideLoading() {
    if (this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
    }
  }

  /**
   * 显示错误消息
   */
  private showError(error: ApiError) {
    switch (error.type) {
      case ErrorType.NETWORK_ERROR:
        ElMessage.error({
          message: error.message,
          duration: 5000,
          showClose: true
        })
        break
      case ErrorType.TIMEOUT_ERROR:
        ElMessage.warning({
          message: error.message,
          duration: 3000
        })
        break
      case ErrorType.AUTH_ERROR:
        ElMessageBox.alert(error.message, '认证失败', {
          confirmButtonText: '确定',
          type: 'warning'
        })
        break
      default:
        ElMessage.error({
          message: error.message,
          duration: 3000
        })
    }
  }

  /**
   * GET请求
   */
  public get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.get(url, config)
  }

  /**
   * POST请求
   */
  public post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config)
  }

  /**
   * PUT请求
   */
  public put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config)
  }

  /**
   * DELETE请求
   */
  public delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config)
  }

  /**
   * PATCH请求
   */
  public patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.patch(url, data, config)
  }

  /**
   * 上传文件
   */
  public upload<T = any>(
    url: string, 
    file: File | FormData, 
    config?: RequestConfig & {
      onProgress?: (progress: number) => void
    }
  ): Promise<ApiResponse<T>> {
    const formData = file instanceof File ? new FormData() : file
    
    if (file instanceof File) {
      formData.append('file', file)
    }

    return this.instance.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
        ...config?.headers
      },
      onUploadProgress: (progressEvent) => {
        if (config?.onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded / progressEvent.total) * 100)
          config.onProgress(progress)
        }
      }
    })
  }

  /**
   * 下载文件
   */
  public download(url: string, filename?: string, config?: RequestConfig): Promise<void> {
    return this.instance.get(url, {
      ...config,
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      
      link.href = downloadUrl
      link.download = filename || this.getFilenameFromResponse(response) || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }

  /**
   * 从响应头获取文件名
   */
  private getFilenameFromResponse(response: AxiosResponse): string | null {
    const contentDisposition = response.headers['content-disposition']
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        return filenameMatch[1].replace(/['"]/g, '')
      }
    }
    return null
  }

  /**
   * 取消所有pending请求
   */
  public cancelAllRequests(reason?: string) {
    this.pendingRequests.forEach((controller, requestId) => {
      controller.abort(reason || '请求被取消')
      console.log(`[API Cancel] 取消请求: ${requestId}`)
    })
    this.pendingRequests.clear()
  }

  /**
   * 获取axios实例
   */
  public getInstance(): AxiosInstance {
    return this.instance
  }
}

// 创建默认实例
const httpClient = new HttpClient()

// 导出请求方法
export const request = httpClient
export default httpClient

// 导出类型
export type { RequestConfig, PaginatedResponse }
