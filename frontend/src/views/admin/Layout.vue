<template>
  <el-container>
    <el-header>
      <div class="header-left">
        <h1>单位内部饭堂订餐系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand" @visible-change="handleDropdownVisibleChange">
          <span class="username-dropdown">
            <el-icon class="user-icon"><User /></el-icon>
            <span class="username">{{ user.name }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="changePassword">
                <el-icon><Lock /></el-icon>
                <span>修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/system-config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/meal-type">
            <el-icon><Food /></el-icon>
            <span>餐食类型管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/employee">
            <el-icon><User /></el-icon>
            <span>员工管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/department">
            <el-icon><OfficeBuilding /></el-icon>
            <span>部门设置</span>
          </el-menu-item>
          <el-menu-item index="/admin/restaurant">
            <el-icon><Van /></el-icon>
            <span>餐厅管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/role">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/order-manage">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/operation-log">
            <el-icon><Document /></el-icon>
            <span>日志查询</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>

    <el-dialog
      v-model="changePasswordDialogVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form :model="changePasswordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input
            v-model="changePasswordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            @keyup.enter="handleSubmitChangePassword"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
            @keyup.enter="handleSubmitChangePassword"
          />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            @keyup.enter="handleSubmitChangePassword"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancelChangePassword">取消</el-button>
          <el-button type="primary" @click="handleSubmitChangePassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Setting, Food, User, UserFilled, OfficeBuilding, Document, ArrowDown, Lock, SwitchButton, List, Van } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
let userData = {}
try {
  userData = JSON.parse(localStorage.getItem('user') || '{}')
} catch (error) {
  userData = {}
}
const user = ref(userData)

const activeMenu = computed(() => {
  return route.path
})

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleCommand = (command) => {
  if (command === 'changePassword') {
    showChangePasswordDialog()
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleDropdownVisibleChange = (visible) => {
  if (visible) {
    document.addEventListener('click', handleDocumentClick)
  } else {
    document.removeEventListener('click', handleDocumentClick)
  }
}

const handleDocumentClick = (event) => {
  const dropdown = document.querySelector('.el-dropdown')
  if (dropdown && !dropdown.contains(event.target)) {
    dropdown.classList.remove('is-active')
  }
}

const changePasswordDialogVisible = ref(false)
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

const handleCancelChangePassword = () => {
  changePasswordDialogVisible.value = false
  changePasswordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const handleSubmitChangePassword = async () => {
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

  try {
    const response = await axios.post('/api/auth/change-password', {
      currentPassword: changePasswordForm.value.currentPassword,
      newPassword: changePasswordForm.value.newPassword
    })

    if (response.data.success) {
      ElMessage.success('密码修改成功，请重新登录')
      changePasswordDialogVisible.value = false
      changePasswordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      handleLogout()
    } else {
      ElMessage.error(response.data.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '密码修改失败，请稍后重试')
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.post('/api/auth/logout')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
    ElMessage.success('登出成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('登出失败')
    }
  }
}

onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
  }
})
</script>

<style scoped>
.header-left {
  float: left;
}

.header-right {
  float: right;
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h1 {
  font-size: 20px;
  margin: 0;
  color: #fff;
}

.username-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
  color: #fff;
}

.username-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-icon {
  font-size: 18px;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.el-menu-vertical-demo {
  height: 100%;
  background-color: #5470c6;
}

.el-menu-item {
  color: #fff;
}

.el-menu-item.is-active {
  background-color: #667eea;
  color: #fff;
}

.el-menu-item:hover {
  background-color: #667eea;
  color: #fff;
}

.el-icon {
  color: #fff;
}

.el-dropdown-menu {
  margin-top: 8px;
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-dropdown-menu__item .el-icon {
  color: #606266;
}

/* 鸿蒙系统适配 */
@media screen and (-webkit-min-device-pixel-ratio: 2) and (min-resolution: 192dpi) {
  .el-dropdown {
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  }
  
  .el-button {
    border-radius: 8px;
  }
}

/* 鸿蒙系统安全区域适配 */
@supports (padding: max(0px)) {
  .el-aside {
    padding-left: env(safe-area-inset-left);
  }
  
  .el-header {
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
  }
}
</style>
