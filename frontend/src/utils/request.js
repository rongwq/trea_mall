import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求队列管理
const pendingRequests = new Map()

const generateRequestKey = (config) => {
  return `${config.method}-${config.url}-${JSON.stringify(config.params || {})}-${JSON.stringify(config.data || {})}`
}

const removePendingRequest = (config) => {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const cancelToken = pendingRequests.get(key)
    cancelToken('请求取消：重复请求')
    pendingRequests.delete(key)
  }
}

// 统一处理401未授权
const handleUnauthorized = () => {
  ElMessage.error('登录已过期，请重新登录')
  const userStore = useUserStore()
  userStore.logout()
  router.push('/login')
}

// 处理业务错误码
const handleBusinessError = (code, message) => {
  switch (code) {
    case 401:
      handleUnauthorized()
      break
    case 403:
      ElMessage.error('没有权限执行此操作')
      break
    case 404:
      ElMessage.error('请求的资源不存在')
      break
    case 500:
      ElMessage.error('服务器错误，请稍后重试')
      break
    default:
      ElMessage.error(message || '请求失败')
  }
}

// 处理HTTP错误状态码
const handleHttpError = (status) => {
  switch (status) {
    case 400:
      ElMessage.error('请求参数错误')
      break
    case 401:
      handleUnauthorized()
      break
    case 403:
      ElMessage.error('没有权限执行此操作')
      break
    case 404:
      ElMessage.error('请求的资源不存在')
      break
    case 405:
      ElMessage.error('请求方法不允许')
      break
    case 500:
      ElMessage.error('服务器错误，请稍后重试')
      break
    case 502:
      ElMessage.error('网关错误')
      break
    case 503:
      ElMessage.error('服务暂不可用')
      break
    case 504:
      ElMessage.error('网关超时')
      break
    default:
      ElMessage.error(`请求失败: HTTP ${status}`)
  }
}

// 处理网络错误
const handleNetworkError = (error) => {
  if (error.message.includes('timeout')) {
    ElMessage.error('请求超时，请检查网络连接')
  } else if (error.message.includes('Network')) {
    ElMessage.error('网络错误，请检查网络连接')
  } else {
    ElMessage.error('网络连接失败')
  }
}

request.interceptors.request.use(
  config => {
    // 取消重复请求
    removePendingRequest(config)
    
    // 创建取消令牌
    const source = axios.CancelToken.source()
    config.cancelToken = source.token
    const key = generateRequestKey(config)
    pendingRequests.set(key, source.cancel)
    
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 添加请求时间戳
    config.metadata = { startTime: new Date() }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    // 移除请求队列中的记录
    removePendingRequest(response.config)
    
    // 计算请求耗时
    const endTime = new Date()
    const duration = endTime - response.config.metadata.startTime
    console.log(`[API] ${response.config.url} - ${duration}ms`)
    
    const res = response.data
    if (res.code !== 200) {
      handleBusinessError(res.code, res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    // 移除请求队列中的记录
    if (error.config) {
      removePendingRequest(error.config)
    }
    
    // 处理取消请求的情况
    if (axios.isCancel(error)) {
      console.log('请求已取消:', error.message)
      return Promise.reject(error)
    }
    
    // 处理网络错误
    if (!error.response) {
      handleNetworkError(error)
    } else {
      // 处理HTTP错误状态码
      handleHttpError(error.response.status)
    }
    
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request
