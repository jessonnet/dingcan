<template>
  <div class="operation-log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleExport">导出日志</el-button>
            <el-button type="danger" @click="clearDialogVisible = true">清理日志</el-button>
          </div>
        </div>
      </template>

      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="用户ID">
            <el-input v-model="filterForm.userId" placeholder="输入用户ID" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="filterForm.operationType" placeholder="选择操作类型" clearable style="width: 150px">
              <el-option label="登录" value="LOGIN" />
              <el-option label="登出" value="LOGOUT" />
              <el-option label="创建" value="CREATE" />
              <el-option label="更新" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="查询" value="QUERY" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="logs" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeText(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120" show-overflow-tooltip />
        <el-table-column prop="description" label="操作内容" width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="createdAt" label="操作时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationTypeText(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="clearDialogVisible"
      title="清理日志"
      width="500px"
    >
      <el-form :model="clearForm" label-width="100px">
        <el-form-item label="清理时间">
          <el-date-picker
            v-model="clearForm.beforeDate"
            type="datetime"
            placeholder="选择清理时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-alert
          title="警告"
          type="warning"
          description="此操作将永久删除指定时间之前的所有日志记录，请谨慎操作！"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="clearDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleClear">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

const logs = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const clearDialogVisible = ref(false)
const currentLog = ref({})
const dateRange = ref([])

const filterForm = reactive({
  userId: '',
  operationType: ''
})

const clearForm = reactive({
  beforeDate: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getOperationTypeText = (type) => {
  const typeMap = {
    'LOGIN': '登录',
    'LOGOUT': '登出',
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'QUERY': '查询'
  }
  return typeMap[type] || type
}

const getOperationTypeTag = (type) => {
  const tagMap = {
    'LOGIN': 'success',
    'LOGOUT': 'info',
    'CREATE': 'primary',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': ''
  }
  return tagMap[type] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filterForm
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const data = await axios.get('/api/admin/operation-log/list', { params })
    console.log('操作日志响应:', data)
    
    if (data && data.success) {
      logs.value = data.data
      pagination.total = data.total
    } else {
      console.error('获取日志失败:', data)
    }
  } catch (error) {
    console.error('获取日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchLogs()
}

const handleReset = () => {
  filterForm.userId = ''
  filterForm.operationType = ''
  dateRange.value = []
  pagination.page = 1
  fetchLogs()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchLogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchLogs()
}

const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

const handleExport = async () => {
  try {
    const params = {
      ...filterForm
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const response = await axios.get('/api/admin/operation-log/export', {
      params,
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `操作日志_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error('导出失败:', error)
  }
}

const handleClear = async () => {
  if (!clearForm.beforeDate) {
    alert('请选择清理时间')
    return
  }
  
  try {
    const response = await axios.delete('/api/admin/operation-log/clear', {
      params: { beforeDate: clearForm.beforeDate }
    })
    if (response.data && response.data.success) {
      alert('清理成功')
      clearDialogVisible.value = false
      fetchLogs()
    }
  } catch (error) {
    console.error('清理失败:', error)
    alert('清理失败')
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.operation-log-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
