<template>
  <div class="system-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      <div class="system-config-content">
        <el-form :model="systemConfigForm" :rules="rules" ref="systemConfigFormRef" label-width="150px">
          <el-form-item label="锁单时间" prop="lockTime">
            <el-time-picker
              v-model="systemConfigForm.lockTime"
              format="HH:mm"
              placeholder="选择锁单时间"
              style="width: 150px"
            />
            <el-button type="primary" @click="updateSystemConfig('lock_time', systemConfigForm.lockTime)" style="margin-left: 10px">保存</el-button>
          </el-form-item>
          <el-form-item label="系统名称" prop="systemName">
            <el-input v-model="systemConfigForm.systemName" placeholder="输入系统名称" style="width: 300px" />
            <el-button type="primary" @click="updateSystemConfig('system_name', systemConfigForm.systemName)" style="margin-left: 10px">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const systemConfigFormRef = ref(null)
const systemConfigForm = ref({
  lockTime: '',
  systemName: ''
})

// 存储系统配置的 id 映射
const systemConfigIds = ref({
  lock_time: '',
  system_name: ''
})

const rules = {
  lockTime: [
    { required: true, message: '请选择锁单时间', trigger: 'change' }
  ],
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'change' }
  ]
}

const loadSystemConfig = async () => {
  try {
    console.log('开始加载系统配置')
    const response = await axios.get('/api/admin/system-config/list')
    console.log('加载系统配置响应:', response)
    if (response.success) {
      const systemConfigs = response.data
      console.log('系统配置数据:', systemConfigs)
      for (const config of systemConfigs) {
        if (config.configKey === 'lock_time') {
          // 保存 id
          systemConfigIds.value.lock_time = config.id
          // Convert string time to Date object for el-time-picker
          const timeParts = config.configValue.split(':')
          const date = new Date()
          date.setHours(parseInt(timeParts[0]), parseInt(timeParts[1]), 0, 0)
          systemConfigForm.value.lockTime = date
        } else if (config.configKey === 'system_name') {
          // 保存 id
          systemConfigIds.value.system_name = config.id
          systemConfigForm.value.systemName = config.configValue
        }
      }
    } else {
      ElMessage.error('加载系统配置失败')
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
    ElMessage.error('加载系统配置失败，请检查网络连接')
  }
}

const updateSystemConfig = async (configKey, configValue) => {
  try {
    console.log('开始更新系统配置:', configKey, configValue)
    let finalValue = configValue
    // Convert Date object to string time for lock_time
    if (configKey === 'lock_time' && configValue instanceof Date) {
      const hours = configValue.getHours().toString().padStart(2, '0')
      const minutes = configValue.getMinutes().toString().padStart(2, '0')
      finalValue = `${hours}:${minutes}`
    }
    // 获取对应的 id
    const id = systemConfigIds.value[configKey]
    console.log('配置项 ID:', id)
    if (!id) {
      ElMessage.error('未找到配置项的 ID，请重新加载页面')
      return
    }
    console.log('准备发送更新请求:', { id, configKey, configValue: finalValue })
    const response = await axios.put('/api/admin/system-config/update', {
      id: id,
      configKey: configKey,
      configValue: finalValue
    })
    console.log('更新系统配置响应:', response)
    if (response.success) {
      ElMessage.success(response.message)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('更新系统配置失败:', error)
    ElMessage.error('更新系统配置失败，请检查网络连接')
  }
}

onMounted(() => {
  loadSystemConfig()
})
</script>

<style scoped>
.system-config-container {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-config-content {
  padding-top: 20px;
}
</style>