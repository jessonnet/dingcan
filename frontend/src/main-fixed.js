import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'

// 配置axios - 修改为服务器IP地址
axios.defaults.baseURL = 'http://111.230.115.247:8080'
axios.defaults.timeout = 10000

// 配置请求转换器
axios.defaults.transformRequest = [function (data, headers) {
  console.log('transformRequest - data:', data)
  console.log('transformRequest - headers:', headers)
  // 如果数据是对象，转换为JSON字符串
  if (data && typeof data === 'object') {
    const jsonStr = JSON.stringify(data)
    console.log('transformRequest - JSON字符串:', jsonStr)
    return jsonStr
  }
  return data
}]

// 请求拦截器
axios.interceptors.request.use(
  config => {
    console.log('===== 请求拦截器开始 =====')
    console.log('发送请求:', config.url)
    console.log('请求数据:', config.data)
    console.log('请求数据类型:', typeof config.data)
    console.log('请求方法:', config.method)
    console.log('请求头:', config.headers)
    console.log('请求配置:', config)
    console.log('===== 请求拦截器结束 =====')
    
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 确保 Content-Type 是 application/json，并且不覆盖已有的Content-Type
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.status)
    console.log('原始响应:', response)
    // 检查响应格式
    const data = response.data
    console.log('响应数据:', data)
    
    // 如果响应是对象
    if (data && typeof data === 'object') {
      // 如果响应包含data字段，并且data字段包含user和token字段（登录响应）
      if ('data' in data && data.data && typeof data.data === 'object' && 'user' in data.data && 'token' in data.data) {
        console.log('响应包含data字段，并且data字段包含user和token字段，返回处理后的响应')
        // 构建符合前端期望的响应格式
        return {
          success: true,
          message: data.message || '登录成功',
          user: data.data.user,
          token: data.data.token
        }
      } else if ('user' in data && 'token' in data) {
        console.log('响应包含user和token字段，返回处理后的响应')
        // 构建符合前端期望的响应格式
        return {
          success: true,
          message: data.message || '登录成功',
          user: data.user,
          token: data.token
        }
      } else {
        console.log('响应不包含user和token字段，直接返回原始数据')
        // 对于其他所有响应（包括包含success字段的），直接返回原始数据
        return data
      }
    } else {
      console.log('响应不是对象，直接返回')
      return data
    }
  },
  error => {
    console.error('响应错误:', error.config ? error.config.url : '未知URL', error.message)
    console.error('错误详情:', error)
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

const app = createApp(App)
app.use(router)
app.use(store)
app.use(ElementPlus)
app.mount('#app')
