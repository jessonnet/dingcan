<template>
  <div class="callback-container">
    <div class="callback-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="50">
          <Loading />
        </el-icon>
        <p class="loading-text">{{ loadingText }}</p>
      </div>
      
      <div v-else-if="error" class="error-state">
        <el-icon :size="50" color="#f56c6c">
          <CircleClose />
        </el-icon>
        <p class="error-text">{{ error }}</p>
        <el-button type="primary" @click="goToLogin">返回登录</el-button>
      </div>
      
      <div v-else class="success-state">
        <el-icon :size="50" color="#67c23a">
          <CircleCheck />
        </el-icon>
        <p class="success-text">登录成功</p>
        <p class="redirect-text">正在跳转...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleClose, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const error = ref('')
const loadingText = ref('正在处理微信授权...')

onMounted(async () => {
  await handleWeChatCallback()
})

const handleWeChatCallback = async () => {
  const code = route.query.code
  const state = route.query.state
  
  if (!code) {
    error.value = '授权失败，未获取到授权码'
    loading.value = false
    return
  }
  
  try {
    loadingText.value = '正在获取用户信息...'
    
    const response = await fetch('/api/wechat/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ code, state })
    })
    
    const data = await response.json()
    
    if (data.success) {
      loadingText.value = '正在登录...'
      
      const user = data.user
      const token = data.token
      
      if (user.role) {
        user.role = user.role.toLowerCase()
      }
      
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      
      loading.value = false
      
      if (data.isNewUser) {
        ElMessage.info('欢迎使用！您的账号已自动创建')
      }
      
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
      
      setTimeout(() => {
        router.push(redirectPath)
      }, 1000)
    } else {
      error.value = data?.message || '微信登录失败'
      loading.value = false
    }
  } catch (err) {
    console.error('微信登录回调处理失败:', err)
    error.value = '网络错误，请稍后重试'
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.callback-content {
  width: 400px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
}

.loading-state,
.error-state,
.success-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.loading-text,
.error-text,
.success-text {
  margin-top: 20px;
  font-size: 16px;
  color: #606266;
}

.error-text {
  color: #f56c6c;
}

.success-text {
  color: #67c23a;
}

.redirect-text {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
}

@media (max-width: 768px) {
  .callback-content {
    width: 90%;
    padding: 30px 20px;
  }
}
</style>
