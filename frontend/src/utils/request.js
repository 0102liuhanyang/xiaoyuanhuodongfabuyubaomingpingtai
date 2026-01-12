import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from './auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data && typeof data.code !== 'undefined' && data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data.data ?? data
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service
