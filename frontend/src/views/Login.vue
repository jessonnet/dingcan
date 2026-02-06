<template>
  <div class="login-container">
    <div class="login-form">
      <h2>单位内部饭堂订餐系统</h2>
      
      <div v-if="!wechatLoginEnabled" class="login-notice">
        <el-alert
          title="系统提示"
          type="info"
          :closable="false"
          show-icon
        >
          微信登录功能已禁用，请使用账号密码登录
        </el-alert>
      </div>
      
      <div class="login-tabs" v-if="wechatLoginEnabled && wechatLoginMode === 'manual'">
        <div 
          class="tab-item" 
          :class="{ active: loginMode === 'password' }"
          @click="loginMode = 'password'"
        >
          账号密码登录
        </div>
        <div 
          class="tab-item" 
          :class="{ active: loginMode === 'wechat' }"
          @click="loginMode = 'wechat'"
        >
          微信登录
        </div>
      </div>
      
      <div v-if="wechatLoginEnabled && wechatLoginMode === 'force'" class="login-notice">
        <el-alert
          title="系统提示"
          type="success"
          :closable="false"
          show-icon
        >
          当前使用微信登录模式
        </el-alert>
      </div>

      <div v-if="loginMode === 'password'" class="login-content">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="login-button" :loading="loading">
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="loginMode === 'wechat' && !needBind" class="login-content wechat-login">
        <div class="wechat-login-info">
          <p>使用微信快速登录</p>
          <p class="tip">首次登录需要绑定员工账号</p>
        </div>
        <el-button type="success" @click="handleWeChatLogin" class="wechat-button" :loading="loading">
          <svg class="wechat-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path d="M664.5 335.8c-6.9 0-13.8 0.6-20.6 1.1 2.3-11.8 3.5-24 3.5-36.4 0-101.8-86.7-184.3-193.7-184.3C251.4 116.2 168 192.9 168 288.4c0 53.6 23.6 102.3 62.1 139.1-6.5 19.7-18.7 53.9-21.3 62.1-3.5 11.2 4.1 11.2 8.5 8.1 3.5-2.4 28.1-19.1 39.6-27.1 14.3-10.1 33.2-20.6 46.9-26.5 24.2 8.7 50.8 13.5 78.5 13.5 6.4 0 12.7-0.3 18.9-0.8-2.1-10.3-3.2-21-3.2-31.9 0-97.3 87.3-176.2 195-176.2 107.7 0 195 78.9 195 176.2 0 97.3-87.3 176.2-195 176.2-24.4 0-47.7-4.3-69.2-12.1-12.4 6.7-29.7 16.5-42.8 26.1-10.4 7.6-33.2 23.2-36.4 25.4-4 2.8-11.1 2.8-7.9-7.6 2.4-7.5 13.5-39.3 19.5-57.6 35.2-34.5 56.5-82.3 56.5-135.1 0-7.4-0.5-14.6-1.4-21.8 5.7-0.5 11.5-0.8 17.3-0.8z" fill="#07C160"></path>
            <path d="M835.6 485.3c0-80.4-72.3-145.6-161.5-145.6-89.2 0-161.5 65.2-161.5 145.6 0 80.4 72.3 145.6 161.5 145.6 20.6 0 40.4-3.3 58.8-9.3 11.4 5.8 26.6 14.4 39.3 23.5 10.2 7.4 31.4 21.8 34.4 23.8 3.6 2.5 10 2.5 7.1-6.7-2.1-6.6-12.1-34.8-17.5-51.1 29.1-29.8 46.4-70.8 46.4-115.8z" fill="#07C160"></path>
          </svg>
          {{ loading ? '登录中...' : '微信一键登录' }}
        </el-button>
      </div>

      <div v-if="needBind" class="login-content bind-form">
        <div class="wechat-info">
          <img :src="wechatInfo.avatar" alt="微信头像" class="wechat-avatar">
          <p class="wechat-nickname">{{ wechatInfo.nickname }}</p>
          <p class="bind-tip">请绑定员工账号以完成登录</p>
        </div>
        
        <el-form :model="bindForm" :rules="bindRules" ref="bindFormRef" label-width="80px">
          <el-form-item label="员工账号" prop="username">
            <el-input v-model="bindForm.username" placeholder="请输入员工账号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="bindForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleBindUser" class="bind-button" :loading="loading">
              {{ loading ? '绑定中...' : '绑定并登录' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="text" @click="cancelBind" class="cancel-button">
              取消绑定
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const bindFormRef = ref(null)
const loading = ref(false)
const loginMode = ref('password')
const isWeChat = ref(false)
const isHarmonyOS = ref(false)
const needBind = ref(false)
const wechatLoginEnabled = ref(true)
const wechatLoginMode = ref('auto')
const configLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const bindForm = reactive({
  username: '',
  password: ''
})

const wechatInfo = reactive({
  openid: '',
  nickname: '',
  avatar: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const bindRules = {
  username: [
    { required: true, message: '请输入员工账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadWeChatConfig()
  await checkBrowser()
  handleWeChatCallback()
})

const loadWeChatConfig = async () => {
  try {
    configLoading.value = true
    const response = await axios.get('/api/system/config/wechat')
    if (response.success) {
      wechatLoginEnabled.value = response.data.enabled
      wechatLoginMode.value = response.data.mode || 'auto'
      console.log('微信登录配置:', wechatLoginEnabled.value, wechatLoginMode.value)
    }
  } catch (error) {
    console.error('加载微信登录配置失败:', error)
  } finally {
    configLoading.value = false
  }
}

const checkBrowser = async () => {
  try {
    const response = await fetch('/api/wechat/check-browser', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    const data = await response.json()
    if (data.success) {
      isWeChat.value = data.isWeChat
      isHarmonyOS.value = data.isHarmonyOS
      
      console.log('浏览器环境:', isWeChat.value, isHarmonyOS.value)
      console.log('微信登录配置:', wechatLoginEnabled.value, wechatLoginMode.value)
      
      if (!wechatLoginEnabled.value) {
        loginMode.value = 'password'
      } else if (wechatLoginMode.value === 'force') {
        loginMode.value = 'wechat'
      } else if (wechatLoginMode.value === 'auto' && isWeChat.value) {
        loginMode.value = 'wechat'
      } else {
        loginMode.value = 'password'
      }
    }
  } catch (error) {
    console.error('检测浏览器环境失败:', error)
  }
}

const handleLogin = async () => {
  console.log('开始登录流程')
  if (!loginFormRef.value) return
  
  try {
    console.log('开始表单验证')
    loginFormRef.value.validate(async (valid, fields) => {
      if (valid) {
        console.log('表单验证成功')
        
        loading.value = true
        
        try {
          console.log('开始发送登录请求')
          console.log('登录表单数据:', loginForm)
          
          const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json'
            },
            body: JSON.stringify(loginForm)
          })
          
          console.log('fetch响应状态:', response.status)
          
          if (!response.ok) {
            console.error('HTTP错误:', response.status, response.statusText)
            throw new Error(`HTTP error! status: ${response.status}`)
          }
          
          const data = await response.json()
          console.log('登录响应数据:', data)
          
          if (data && data.success) {
            console.log('响应成功，success为true')
            
            const user = data.user
            const token = data.token
            
            console.log('响应user:', user)
            console.log('响应user.role:', user.role)
            
            if (user.role) {
              user.role = user.role.toLowerCase()
            }
            
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            console.log('存储的token:', localStorage.getItem('token'))
            console.log('存储的user:', localStorage.getItem('user'))
            
            let redirectPath = '/login'
            const role = user.role ? user.role : ''
            switch (role) {
              case 'employee':
                redirectPath = '/employee/order'
                break
              case 'chef':
                redirectPath = '/chef/order-status'
                break
              case 'admin':
                redirectPath = '/admin/system-config'
                break
            }
            console.log('准备跳转到:', redirectPath)
            
            try {
              router.push(redirectPath)
              console.log('路由跳转命令已执行')
            } catch (error) {
              console.error('路由跳转错误:', error)
              ElMessage.error('页面跳转失败，请刷新页面重试')
            }
            
            ElMessage.success('登录成功')
          } else {
            console.error('登录失败，success为false或响应格式错误')
            console.error('响应数据:', data)
            ElMessage.error(data?.message || '登录失败')
          }
        } catch (error) {
          console.error('登录错误:', error)
          console.error('错误消息:', error.message)
          ElMessage.error('登录失败，请检查用户名和密码')
        } finally {
          loading.value = false
        }
      } else {
        console.error('表单验证失败:', fields)
        ElMessage.error('表单验证失败，请检查输入内容')
      }
    })
  } catch (error) {
    console.error('登录流程错误:', error)
    ElMessage.error('登录失败，请刷新页面重试')
    loading.value = false
  }
}

const handleWeChatLogin = async () => {
  try {
    loading.value = true
    
    const response = await fetch('/api/wechat/auth/url', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    
    if (data.success && data.authUrl) {
      window.location.href = data.authUrl
    } else {
      ElMessage.error('获取微信授权链接失败')
    }
  } catch (error) {
    console.error('微信登录失败:', error)
    ElMessage.error('微信登录失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleWeChatCallback = async () => {
  const code = route.query.code
  const state = route.query.state
  
  if (code) {
    loading.value = true
    
    try {
      const response = await fetch('/api/wechat/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ code, state })
      })
      
      const data = await response.json()
      
      if (data.success) {
        if (data.needBind) {
          needBind.value = true
          wechatInfo.openid = data.openid
          wechatInfo.nickname = data.nickname
          wechatInfo.avatar = data.avatar
          ElMessage.info('首次登录，请绑定员工账号')
        } else {
          const user = data.user
          const token = data.token
          
          if (user.role) {
            user.role = user.role.toLowerCase()
          }
          
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          let redirectPath = '/login'
          const role = user.role ? user.role : ''
          switch (role) {
            case 'employee':
              redirectPath = '/employee/order'
              break
            case 'chef':
              redirectPath = '/chef/order-status'
              break
            case 'admin':
              redirectPath = '/admin/system-config'
              break
          }
          
          router.push(redirectPath)
          ElMessage.success('登录成功')
        }
      } else {
        ElMessage.error(data?.message || '微信登录失败')
      }
    } catch (error) {
      console.error('微信登录回调处理失败:', error)
      ElMessage.error('微信登录失败，请重试')
    } finally {
      loading.value = false
    }
  }
}

const handleBindUser = async () => {
  if (!bindFormRef.value) return
  
  try {
    bindFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        
        try {
          const response = await fetch('/api/wechat/bind', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              openid: wechatInfo.openid,
              username: bindForm.username,
              password: bindForm.password
            })
          })
          
          const data = await response.json()
          
          if (data.success) {
            const user = data.user
            const token = data.token
            
            if (user.role) {
              user.role = user.role.toLowerCase()
            }
            
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(user))
            
            let redirectPath = '/login'
            const role = user.role ? user.role : ''
            switch (role) {
              case 'employee':
                redirectPath = '/employee/order'
                break
              case 'chef':
                redirectPath = '/chef/order-status'
                break
              case 'admin':
                redirectPath = '/admin/system-config'
                break
            }
            
            router.push(redirectPath)
            ElMessage.success('绑定成功，已登录')
          } else {
            ElMessage.error(data?.message || '绑定失败')
          }
        } catch (error) {
          console.error('绑定失败:', error)
          ElMessage.error('绑定失败，请检查员工账号和密码')
        } finally {
          loading.value = false
        }
      } else {
        ElMessage.error('请填写完整的员工账号和密码')
      }
    })
  } catch (error) {
    console.error('绑定流程错误:', error)
    ElMessage.error('绑定失败，请刷新页面重试')
    loading.value = false
  }
}

const cancelBind = () => {
  needBind.value = false
  bindForm.username = ''
  bindForm.password = ''
  wechatInfo.openid = ''
  wechatInfo.nickname = ''
  wechatInfo.avatar = ''
  ElMessage.info('已取消绑定')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #409eff;
}

.login-notice {
  margin-bottom: 20px;
}

.login-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 2px solid #e4e7ed;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  margin-bottom: -2px;
  font-weight: bold;
}

.login-content {
  min-height: 200px;
}

.wechat-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.wechat-login-info {
  text-align: center;
  margin-bottom: 30px;
}

.wechat-login-info p {
  margin: 5px 0;
  color: #606266;
}

.wechat-login-info .tip {
  font-size: 12px;
  color: #909399;
}

.wechat-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.wechat-icon {
  width: 24px;
  height: 24px;
}

.login-button {
  width: 100%;
}

.bind-form {
  padding: 10px 0;
}

.wechat-info {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
}

.wechat-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 15px;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.wechat-nickname {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 10px 0;
}

.bind-tip {
  font-size: 14px;
  color: #606266;
  margin: 5px 0;
}

.bind-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.cancel-button {
  width: 100%;
  color: #909399;
}

.cancel-button:hover {
  color: #409eff;
}

@media (max-width: 768px) {
  .login-form {
    width: 90%;
    padding: 20px;
  }
  
  .login-form h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }
  
  .tab-item {
    font-size: 13px;
    padding: 8px 0;
  }
}

/* 鸿蒙系统适配 */
@media screen and (-webkit-min-device-pixel-ratio: 2) and (min-resolution: 192dpi) {
  .login-form {
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  }
  
  .login-button,
  .wechat-button {
    border-radius: 8px;
  }
}

/* 鸿蒙系统安全区域适配 */
@supports (padding: max(0px)) {
  .login-container {
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
  }
}
</style>
