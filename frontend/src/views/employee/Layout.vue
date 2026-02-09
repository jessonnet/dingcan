<template>
  <el-container class="employee-layout">
    <el-header class="mobile-header">
      <div class="header-left">
        <h1 class="system-title">单位内部饭堂订餐系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="username-dropdown">
            <el-icon class="user-icon"><User /></el-icon>
            <span class="username hidden-xs">{{ user.name }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>{{ user.name }}</span>
              </el-dropdown-item>
              <el-dropdown-item command="changePassword" divided>
                <el-icon><Lock /></el-icon>
                <span>修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主内容区域 - 移除左侧菜单栏 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordDialogVisible"
      title="修改密码"
      width="360px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="changePasswordForm" label-width="90px" class="password-form">
        <el-form-item label="当前密码">
          <el-input
            v-model="changePasswordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            placeholder="至少6位字符"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitChangePassword" :loading="submitting">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, ArrowDown, Lock, SwitchButton } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 用户信息
const user = ref({ name: '' })

// 加载用户信息
const loadUserInfo = () => {
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    user.value = userData
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 下拉菜单处理
const handleCommand = (command) => {
  switch (command) {
    case 'changePassword':
      showChangePasswordDialog()
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
  ElMessage.success('已退出登录')
}

// 修改密码
const changePasswordDialogVisible = ref(false)
const submitting = ref(false)
const changePasswordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showChangePasswordDialog = () => {
  changePasswordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  changePasswordDialogVisible.value = true
}

const handleSubmitChangePassword = async () => {
  // 表单验证
  if (!changePasswordForm.value.currentPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!changePasswordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (changePasswordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (changePasswordForm.value.newPassword !== changePasswordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  submitting.value = true
  try {
    const response = await axios.post('/api/auth/change-password', {
      currentPassword: changePasswordForm.value.currentPassword,
      newPassword: changePasswordForm.value.newPassword
    })

    if (response.data?.success || response.success) {
      ElMessage.success('密码修改成功，请重新登录')
      changePasswordDialogVisible.value = false
      // 退出登录
      setTimeout(() => {
        handleLogout()
      }, 1500)
    } else {
      ElMessage.error(response.data?.message || response.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败：' + (error.response?.data?.message || error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.employee-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 移动端优化的头部 */
.mobile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  height: 56px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.system-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.header-right {
  display: flex;
  align-items: center;
}

.username-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.2);
  transition: background-color 0.3s;
}

.username-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.user-icon {
  font-size: 18px;
}

.username {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
}

/* 主内容区域 */
.main-content {
  padding: 0;
  background-color: #f5f7fa;
  min-height: calc(100vh - 56px);
}

/* 密码表单 */
.password-form {
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mobile-header {
    padding: 0 10px;
    height: 50px;
  }
  
  .system-title {
    font-size: 16px;
  }
  
  .username-dropdown {
    padding: 5px 10px;
  }
  
  .hidden-xs {
    display: none;
  }
  
  .main-content {
    min-height: calc(100vh - 50px);
  }
}

@media (max-width: 480px) {
  .mobile-header {
    padding: 0 8px;
  }
  
  .system-title {
    font-size: 15px;
  }
}
</style>
