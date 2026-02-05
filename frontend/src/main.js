import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'

// 配置axios - 使用相对路径，由Nginx代理到后端
// 开发环境通过vite.config.js的proxy配置代理
// 生产环境通过Nginx的location /api配置代理
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
      return Promise.reject(error)
    }
    
    if (error.response && error.response.data) {
      const errorData = error.response.data
      
      let errorMessage = '操作失败'
      
      if (typeof errorData === 'string') {
        try {
          const parsedData = JSON.parse(errorData)
          errorMessage = parsedData.message || parsedData.msg || errorMessage
        } catch (e) {
          errorMessage = errorData
        }
      } else if (typeof errorData === 'object') {
        errorMessage = errorData.message || errorData.msg || errorData.error || errorMessage
      }
      
      console.error('错误消息:', errorMessage)
      
      let friendlyMessage = errorMessage
      
      if (typeof errorMessage === 'string') {
        const lowerMessage = errorMessage.toLowerCase()
        
        if (lowerMessage.includes('foreign key constraint fails') || 
            lowerMessage.includes('外键约束') || 
            lowerMessage.includes('sqlintegrityconstraintviolationexception')) {
          
          if (lowerMessage.includes('meal_type') || lowerMessage.includes('餐食类型')) {
            friendlyMessage = '删除失败：该餐食类型已被订单使用，无法删除'
          } else if (lowerMessage.includes('restaurant') || lowerMessage.includes('餐厅')) {
            friendlyMessage = '删除失败：该餐厅已被订单使用，无法删除'
          } else if (lowerMessage.includes('department') || lowerMessage.includes('部门')) {
            friendlyMessage = '删除失败：该部门下还有员工，无法删除'
          } else if (lowerMessage.includes('user') || lowerMessage.includes('员工') || lowerMessage.includes('用户')) {
            friendlyMessage = '删除失败：该用户存在订单记录，无法删除'
          } else if (lowerMessage.includes('role') || lowerMessage.includes('角色')) {
            friendlyMessage = '删除失败：该角色已被分配给用户，无法删除'
          } else {
            friendlyMessage = '删除失败：该数据存在关联记录，无法删除'
          }
        } else if (lowerMessage.includes('duplicate entry') || lowerMessage.includes('重复')) {
          friendlyMessage = '操作失败：数据已存在，请勿重复添加'
        } else if (lowerMessage.includes('data too long') || lowerMessage.includes('数据过长')) {
          friendlyMessage = '操作失败：输入内容过长，请缩短后重试'
        } else if (lowerMessage.includes('column') && lowerMessage.includes('cannot be null')) {
          friendlyMessage = '操作失败：必填字段不能为空'
        } else if (lowerMessage.includes('connection') || lowerMessage.includes('连接')) {
          friendlyMessage = '操作失败：网络连接异常，请检查网络后重试'
        } else if (lowerMessage.includes('timeout') || lowerMessage.includes('超时')) {
          friendlyMessage = '操作失败：请求超时，请稍后重试'
        }
      }
      
      console.error('友好提示:', friendlyMessage)
      
      const enhancedError = new Error(friendlyMessage)
      enhancedError.response = error.response
      enhancedError.config = error.config
      
      return Promise.reject(enhancedError)
    }
    
    return Promise.reject(error)
  }
)

const app = createApp(App)
app.use(router)
app.use(store)
app.use(ElementPlus)
app.mount('#app')
