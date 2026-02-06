<template>
  <div class="system-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      <div class="system-config-content">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="基础配置" name="basic">
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
          </el-tab-pane>
          
          <el-tab-pane label="登录配置" name="login">
            <el-form :model="loginConfigForm" ref="loginConfigFormRef" label-width="150px">
              <el-form-item label="微信登录功能">
                <el-switch
                  v-model="loginConfigForm.wechatLoginEnabled"
                  active-text="启用"
                  inactive-text="禁用"
                  @change="handleWeChatLoginEnabledChange"
                />
                <el-button type="primary" @click="updateLoginConfig('wechat_login_enabled', loginConfigForm.wechatLoginEnabled ? '1' : '0', '微信登录功能开关')" style="margin-left: 10px">保存</el-button>
              </el-form-item>
              <el-alert
                title="功能说明"
                type="info"
                :closable="false"
                style="margin-bottom: 20px"
              >
                <template #default>
                  <p>启用微信登录功能后，用户可以通过微信快速登录系统。禁用后，用户只能使用账号密码登录。</p>
                  <p>建议在微信环境（如企业微信、微信内置浏览器）中启用此功能。</p>
                </template>
              </el-alert>
              
              <el-form-item label="微信登录模式">
                <el-select v-model="loginConfigForm.wechatLoginMode" placeholder="选择登录模式" style="width: 200px">
                  <el-option label="自动检测" value="auto" />
                  <el-option label="强制微信" value="force" />
                  <el-option label="手动选择" value="manual" />
                </el-select>
                <el-button type="primary" @click="updateLoginConfig('wechat_login_mode', loginConfigForm.wechatLoginMode, '微信登录模式')" style="margin-left: 10px">保存</el-button>
              </el-form-item>
              <el-alert
                title="模式说明"
                type="info"
                :closable="false"
              >
                <template #default>
                  <ul>
                    <li><strong>自动检测</strong>：系统自动检测浏览器环境，微信环境优先使用微信登录</li>
                    <li><strong>强制微信</strong>：所有用户必须使用微信登录（适用于纯微信环境）</li>
                    <li><strong>手动选择</strong>：用户可以自由选择登录方式（适用于混合环境）</li>
                  </ul>
                </template>
              </el-alert>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const systemConfigFormRef = ref(null)
const loginConfigFormRef = ref(null)
const activeTab = ref('basic')
const systemConfigForm = ref({
  lockTime: '',
  systemName: ''
})

const loginConfigForm = ref({
  wechatLoginEnabled: true,
  wechatLoginMode: 'auto'
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
    const response = await axios.get('/api/system/config/list')
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
        } else if (config.configKey === 'wechat_login_enabled') {
          loginConfigForm.value.wechatLoginEnabled = config.configValue === '1'
        } else if (config.configKey === 'wechat_login_mode') {
          loginConfigForm.value.wechatLoginMode = config.configValue
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

const updateLoginConfig = async (configKey, configValue, description) => {
  try {
    console.log('开始更新登录配置:', configKey, configValue)
    
    const response = await axios.post('/api/system/config/update', {
      configKey: configKey,
      configValue: configValue,
      description: description
    })
    
    console.log('更新登录配置响应:', response)
    if (response.success) {
      ElMessage.success(response.message)
    } else {
      ElMessage.error(response.message)
      // 更新失败，恢复原值
      await loadSystemConfig()
    }
  } catch (error) {
    console.error('更新登录配置失败:', error)
    ElMessage.error('更新登录配置失败，请检查网络连接')
    // 更新失败，恢复原值
    await loadSystemConfig()
  }
}

const handleWeChatLoginEnabledChange = async (newValue) => {
  if (!newValue) {
    try {
      await ElMessageBox.confirm(
        '禁用微信登录功能后，用户只能使用账号密码登录。确定要禁用吗？',
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      // 用户取消操作，恢复原值
      loginConfigForm.value.wechatLoginEnabled = true
      return
    }
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