<template>
  <div class="not-found-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>404 - 页面未找到</span>
        </div>
      </template>
      <div class="not-found-content">
        <el-empty description="您访问的页面不存在"></el-empty>
        <el-button type="primary" @click="goBack">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  max-width: 800px;
  margin: 100px auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.not-found-content {
  text-align: center;
  padding: 50px 0;
}
</style>