import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 — 自动附加 JWT Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一业务错误处理
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data

    if (code === 200) {
      return data
    }

    // 401 — Token 失效，跳转登录
    if (code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(message))
    }

    // 其他业务错误提示
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response?.status
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '未授权访问',
      403: '权限不足',
      404: '资源不存在',
      500: '服务器内部错误',
    }
    ElMessage.error(messages[status] || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
