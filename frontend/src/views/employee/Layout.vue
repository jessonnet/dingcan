<template>
  <el-container>
    <el-header>
      <div class="header-left">
        <h1>单位内部饭堂订餐系统</h1>
      </div>
      <div class="header-right">
        <span>{{ user.name }}</span>
        <el-button type="text" @click="handleLogout">登出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/employee/order">
            <el-icon><Calendar /></el-icon>
            <span>在线订餐</span>
          </el-menu-item>
          <el-menu-item index="/employee/order-history">
            <el-icon><Clock /></el-icon>
            <span>订单历史</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, Clock } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

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

const handleLogout = async () => {
  try {
    await axios.post('/api/auth/logout')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
    ElMessage.success('登出成功')
  } catch (error) {
    ElMessage.error('登出失败')
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
</style>
