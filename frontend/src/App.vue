<template>
  <div id="app">
    <router-view v-if="!$route.meta.requiresAuth || isLoggedIn" />
    <div v-else>
      <el-alert type="error" title="请先登录" :closable="false" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  
  // 如果需要登录但未登录，重定向到登录页面
  if (route.meta.requiresAuth && !isLoggedIn.value) {
    router.push('/login')
  }
}

onMounted(() => {
  checkLoginStatus()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f7fa;
}

#app {
  min-height: 100vh;
}

.el-container {
  min-height: 100vh;
}

.el-header {
  background-color: #409eff;
  color: #fff;
  line-height: 60px;
  padding: 0 20px;
}

.el-aside {
  width: 200px !important;
  background-color: #5470c6;
  color: #fff;
}

.el-main {
  padding: 20px;
}

.el-menu {
  border-right: none;
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

.el-submenu__title {
  color: #fff;
}

.el-submenu__title:hover {
  background-color: #667eea;
  color: #fff;
}

.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
}

.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.el-form-item__label {
  font-weight: 500;
}

.el-card {
  margin-bottom: 20px;
}

.el-table {
  margin-top: 20px;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
