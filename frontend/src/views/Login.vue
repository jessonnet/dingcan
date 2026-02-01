<template>
  <div class="login-container">
    <div class="login-form">
      <h2>单位内部饭堂订餐系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-button">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
    console.log('开始登录流程')
    if (!loginFormRef.value) return
    
    try {
      console.log('开始表单验证')
      // 使用回调函数形式的validate方法
      loginFormRef.value.validate(async (valid, fields) => {
        if (valid) {
          console.log('表单验证成功')
          
          // 发送登录请求
          console.log('开始发送登录请求')
          console.log('登录表单数据:', loginForm)
          
          try {
            // 直接使用fetch API，绕过axios拦截器
            console.log('准备使用fetch发送登录请求')
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
            console.log('fetch响应头:', response.headers)
            
            if (!response.ok) {
              console.error('HTTP错误:', response.status, response.statusText)
              throw new Error(`HTTP error! status: ${response.status}`)
            }
            
            const data = await response.json()
            console.log('登录响应数据:', data)
            
            // 检查响应格式
            if (data && data.success) {
              console.log('响应成功，success为true')
              
              // 提取user和token
              const user = data.user
              const token = data.token
              
              console.log('响应user:', user)
              console.log('响应user.role:', user.role)
              
              // 将用户角色转换为小写
              if (user.role) {
                user.role = user.role.toLowerCase()
              }
              
              localStorage.setItem('token', token)
              localStorage.setItem('user', JSON.stringify(user))
              console.log('存储的token:', localStorage.getItem('token'))
              console.log('存储的user:', localStorage.getItem('user'))
              console.log('存储的user.role:', JSON.parse(localStorage.getItem('user')).role)
              
              // 根据角色跳转到对应页面
              console.log('开始路由跳转，角色:', user.role)
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
          }
        } else {
          console.error('表单验证失败:', fields)
          ElMessage.error('表单验证失败，请检查输入内容')
        }
      })
    } catch (error) {
      console.error('登录流程错误:', error)
      ElMessage.error('登录失败，请刷新页面重试')
    }
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

.login-button {
  width: 100%;
}
</style>