<template>
  <div class="restaurant-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>餐厅管理</span>
          <el-button type="primary" @click="addRestaurantDialogVisible = true">添加餐厅</el-button>
        </div>
      </template>
      <div class="search-bar">
        <el-input
          v-model="searchName"
          placeholder="搜索餐厅名称"
          clearable
          style="width: 200px; margin-right: 10px"
          @clear="loadRestaurants"
          @keyup.enter="loadRestaurants"
        />
        <el-input
          v-model="searchLocation"
          placeholder="搜索地点"
          clearable
          style="width: 200px; margin-right: 10px"
          @clear="loadRestaurants"
          @keyup.enter="loadRestaurants"
        />
        <el-button type="primary" @click="loadRestaurants">搜索</el-button>
      </div>
      <div class="restaurant-content">
        <el-table :data="restaurants" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="scope">
              {{ scope.row.id }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="餐厅名称" width="150">
            <template #default="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" width="200">
            <template #default="scope">
              {{ scope.row.location }}
            </template>
          </el-table-column>
          <el-table-column prop="crossDepartmentBooking" label="跨部门预订" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.crossDepartmentBooking === 1" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </div>
    </el-card>

    <!-- 添加餐厅对话框 -->
    <el-dialog
      v-model="addRestaurantDialogVisible"
      title="添加餐厅"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="addRestaurantForm" :rules="rules" ref="addRestaurantFormRef" label-width="100px">
        <el-form-item label="餐厅名称" prop="name">
          <el-input v-model="addRestaurantForm.name" placeholder="输入餐厅名称（最多50字符）" maxlength="50" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="addRestaurantForm.location" type="textarea" :rows="2" placeholder="输入地点（最多100字符）" maxlength="100" />
        </el-form-item>
        <el-form-item label="跨部门预订" prop="crossDepartmentBooking">
          <el-switch v-model="addRestaurantForm.crossDepartmentBooking" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addRestaurantDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑餐厅对话框 -->
    <el-dialog
      v-model="editRestaurantDialogVisible"
      title="编辑餐厅"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editRestaurantForm" :rules="rules" ref="editRestaurantFormRef" label-width="100px">
        <el-form-item label="餐厅名称" prop="name">
          <el-input v-model="editRestaurantForm.name" placeholder="输入餐厅名称（最多50字符）" maxlength="50" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="editRestaurantForm.location" type="textarea" :rows="2" placeholder="输入地点（最多100字符）" maxlength="100" />
        </el-form-item>
        <el-form-item label="跨部门预订" prop="crossDepartmentBooking">
          <el-switch v-model="editRestaurantForm.crossDepartmentBooking" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editRestaurantDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const restaurants = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchName = ref('')
const searchLocation = ref('')

const addRestaurantDialogVisible = ref(false)
const addRestaurantFormRef = ref(null)
const addRestaurantForm = ref({
  name: '',
  location: '',
  crossDepartmentBooking: false
})

const editRestaurantDialogVisible = ref(false)
const editRestaurantFormRef = ref(null)
const editRestaurantForm = ref({
  id: '',
  name: '',
  location: '',
  crossDepartmentBooking: false
})

const rules = {
  name: [
    { required: true, message: '请输入餐厅名称', trigger: 'change' },
    { max: 50, message: '餐厅名称最多50字符', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入地点', trigger: 'change' },
    { max: 100, message: '地点最多100字符', trigger: 'change' }
  ],
  crossDepartmentBooking: [
    { required: true, message: '请选择是否可跨部门预订', trigger: 'change' }
  ]
}

const loadRestaurants = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchName.value) {
      params.name = searchName.value
    }
    if (searchLocation.value) {
      params.location = searchLocation.value
    }
    const response = await axios.get('/api/admin/restaurant/list', { params })
    if (response.success) {
      restaurants.value = response.data
      total.value = response.total
    } else {
      ElMessage.error('加载餐厅列表失败')
    }
  } catch (error) {
    ElMessage.error('加载餐厅列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadRestaurants()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadRestaurants()
}

const handleAddSubmit = async () => {
  if (!addRestaurantFormRef.value) return
  
  try {
    await addRestaurantFormRef.value.validate()
    const formData = {
      name: addRestaurantForm.value.name,
      location: addRestaurantForm.value.location,
      crossDepartmentBooking: addRestaurantForm.value.crossDepartmentBooking ? 1 : 0
    }
    const response = await axios.post('/api/admin/restaurant/add', formData)
    if (response.success) {
      ElMessage.success('添加餐厅成功')
      addRestaurantDialogVisible.value = false
      addRestaurantForm.value = {
        name: '',
        location: '',
        crossDepartmentBooking: false
      }
      loadRestaurants()
    } else {
      ElMessage.error(response.message || '添加餐厅失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('添加餐厅失败，请检查网络连接')
    }
  }
}

const handleEdit = (restaurant) => {
  editRestaurantForm.value = {
    id: restaurant.id,
    name: restaurant.name,
    location: restaurant.location,
    crossDepartmentBooking: restaurant.crossDepartmentBooking === 1
  }
  editRestaurantDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editRestaurantFormRef.value) return
  
  try {
    await editRestaurantFormRef.value.validate()
    const formData = {
      id: editRestaurantForm.value.id,
      name: editRestaurantForm.value.name,
      location: editRestaurantForm.value.location,
      crossDepartmentBooking: editRestaurantForm.value.crossDepartmentBooking ? 1 : 0
    }
    const response = await axios.put('/api/admin/restaurant/update', formData)
    if (response.success) {
      ElMessage.success('修改餐厅成功')
      editRestaurantDialogVisible.value = false
      loadRestaurants()
    } else {
      ElMessage.error(response.message || '修改餐厅失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('修改餐厅失败，请检查网络连接')
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该餐厅吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/admin/restaurant/delete/${id}`)
    if (response.success) {
      ElMessage.success('删除餐厅成功')
      loadRestaurants()
    } else {
      ElMessage.error(response.message || '删除餐厅失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除餐厅失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '删除餐厅失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadRestaurants()
})
</script>

<style scoped>
.restaurant-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.restaurant-content {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
