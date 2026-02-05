<template>
  <div class="order-manage">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>订单筛选</span>
        </div>
      </template>
      <el-form :model="filterForm" inline>
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="有效" :value="1" />
            <el-option label="无效" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户姓名">
          <el-input v-model="filterForm.userName" placeholder="输入用户姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <span class="total-info">共 {{ pagination.total }} 条记录</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="orderList"
        stripe
        border
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="订单编号" width="100" sortable="custom" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="departmentName" label="部门" width="150" />
        <el-table-column prop="restaurantName" label="食堂" width="150" />
        <el-table-column prop="mealTypeName" label="餐食类型" width="120" />
        <el-table-column prop="mealPrice" label="价格" width="100" sortable="custom">
          <template #default="scope">
            <span>¥{{ scope.row.mealPrice?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="120" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.orderDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
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
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 编辑订单对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑订单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="订单编号">
          <el-input v-model="editForm.id" disabled />
        </el-form-item>
        <el-form-item label="用户信息">
          <el-input :value="editForm.realName + ' (' + editForm.username + ')'" disabled />
        </el-form-item>
        <el-form-item label="餐食类型">
          <el-select v-model="editForm.mealTypeId" placeholder="选择餐食类型" style="width: 100%">
            <el-option
              v-for="meal in mealTypeList"
              :key="meal.id"
              :label="meal.name + ' (¥' + meal.price + ')'"
              :value="meal.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单日期">
          <el-date-picker
            v-model="editForm.orderDate"
            type="date"
            placeholder="选择订单日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="editForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="有效" :value="1" />
            <el-option label="无效" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 加载状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  status: null,
  userName: ''
})

// 日期范围
const dateRange = ref([])

// 排序参数
const sortParams = reactive({
  sortField: 'createdAt',
  sortOrder: 'desc'
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 订单列表
const orderList = ref([])

// 餐食类型列表
const mealTypeList = ref([])

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  userId: null,
  username: '',
  realName: '',
  mealTypeId: null,
  orderDate: '',
  status: 1
})

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: filterForm.status,
      userName: filterForm.userName,
      sortField: sortParams.sortField,
      sortOrder: sortParams.sortOrder
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const data = await axios.get('/api/admin/order/list', { params })
    if (data && data.success) {
      orderList.value = data.data
      pagination.total = data.total
    } else {
      ElMessage.error(data?.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取餐食类型列表
const fetchMealTypeList = async () => {
  try {
    const data = await axios.get('/api/admin/meal-type/list')
    if (data && data.success) {
      mealTypeList.value = data.data
    }
  } catch (error) {
    console.error('获取餐食类型列表失败:', error)
  }
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  fetchOrderList()
}

// 重置
const handleReset = () => {
  filterForm.status = null
  filterForm.userName = ''
  dateRange.value = []
  sortParams.sortField = 'createdAt'
  sortParams.sortOrder = 'desc'
  pagination.page = 1
  fetchOrderList()
}

// 排序变化
const handleSortChange = ({ prop, order }) => {
  if (prop) {
    sortParams.sortField = prop
    sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  } else {
    sortParams.sortField = 'createdAt'
    sortParams.sortOrder = 'desc'
  }
  fetchOrderList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchOrderList()
}

// 页码变化
const handlePageChange = (page) => {
  pagination.page = page
  fetchOrderList()
}

// 导出Excel
const handleExport = async () => {
  try {
    const params = {
      status: filterForm.status,
      userName: filterForm.userName
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await axios.get('/api/admin/order/export', {
      params,
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `订单数据_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 编辑订单
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.userId = row.userId
  editForm.username = row.username
  editForm.realName = row.realName
  editForm.mealTypeId = row.mealTypeId
  editForm.orderDate = row.orderDate
  editForm.status = row.status
  editDialogVisible.value = true
}

// 提交编辑
const handleSubmitEdit = async () => {
  try {
    const data = await axios.put('/api/admin/order/update', editForm)
    if (data && data.success) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      fetchOrderList()
    } else {
      ElMessage.error(data?.message || '修改失败')
    }
  } catch (error) {
    console.error('修改订单失败:', error)
    ElMessage.error('修改失败')
  }
}

// 删除订单
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除订单 ${row.id} 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const data = await axios.delete(`/api/admin/order/delete/${row.id}`)
      if (data && data.success) {
        ElMessage.success('删除成功')
        fetchOrderList()
      } else {
        ElMessage.error(data?.message || '删除失败')
      }
    } catch (error) {
      console.error('删除订单失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '删除失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr
}

// 页面加载时获取数据
onMounted(() => {
  fetchOrderList()
  fetchMealTypeList()
})
</script>

<style scoped>
.order-manage {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-info {
  color: #666;
  font-size: 14px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  flex-shrink: 0;
}
</style>
