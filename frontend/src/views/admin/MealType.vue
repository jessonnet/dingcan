<template>
  <div class="meal-type-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>餐食类型管理</span>
          <el-button type="primary" @click="addMealTypeDialogVisible = true">添加餐食类型</el-button>
        </div>
      </template>
      <div class="meal-type-content">
        <el-table :data="mealTypes" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="scope">
              {{ scope.row.id }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="餐食类型" width="180">
            <template #default="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="200">
            <template #default="scope">
              {{ scope.row.createdAt }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 添加餐食类型对话框 -->
    <el-dialog
      v-model="addMealTypeDialogVisible"
      title="添加餐食类型"
      width="500px"
    >
      <el-form :model="addMealTypeForm" :rules="rules" ref="addMealTypeFormRef" label-width="100px">
        <el-form-item label="餐食类型" prop="name">
          <el-input v-model="addMealTypeForm.name" placeholder="输入餐食类型" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="addMealTypeForm.price" :min="0" :step="0.01" :precision="2" placeholder="输入价格" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="addMealTypeForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addMealTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑餐食类型对话框 -->
    <el-dialog
      v-model="editMealTypeDialogVisible"
      title="编辑餐食类型"
      width="500px"
    >
      <el-form :model="editMealTypeForm" :rules="rules" ref="editMealTypeFormRef" label-width="100px">
        <el-form-item label="餐食类型" prop="name">
          <el-input v-model="editMealTypeForm.name" placeholder="输入餐食类型" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editMealTypeForm.price" :min="0" :step="0.01" :precision="2" placeholder="输入价格" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="editMealTypeForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editMealTypeDialogVisible = false">取消</el-button>
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

const mealTypes = ref([])

const addMealTypeDialogVisible = ref(false)
const addMealTypeFormRef = ref(null)
const addMealTypeForm = ref({
  name: '',
  price: 0,
  status: 1
})

const editMealTypeDialogVisible = ref(false)
const editMealTypeFormRef = ref(null)
const editMealTypeForm = ref({
  id: '',
  name: '',
  price: 0,
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入餐食类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const loadMealTypes = async () => {
  try {
    const response = await axios.get('/api/admin/meal-type/list')
    if (response.success) {
      mealTypes.value = response.data
    } else {
      ElMessage.error('加载餐食类型失败')
    }
  } catch (error) {
    ElMessage.error('加载餐食类型失败，请检查网络连接')
  }
}

const handleAddSubmit = async () => {
  if (!addMealTypeFormRef.value) return
  
  await addMealTypeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换 status 为整数类型
        const formData = {
          ...addMealTypeForm.value,
          status: addMealTypeForm.value.status ? 1 : 0
        }
        const response = await axios.post('/api/admin/meal-type/add', formData)
        if (response.success) {
          ElMessage.success(response.message)
          addMealTypeDialogVisible.value = false
          addMealTypeForm.value = {
            name: '',
            price: 0,
            status: 1
          }
          addMealTypeFormRef.value.resetFields()
          loadMealTypes()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('添加失败，请检查网络连接')
      }
    }
  })
}

const handleEdit = (mealType) => {
  editMealTypeForm.value = {
    id: mealType.id,
    name: mealType.name,
    price: mealType.price,
    status: mealType.status
  }
  editMealTypeDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editMealTypeFormRef.value) return
  
  await editMealTypeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换 status 为整数类型
        const formData = {
          ...editMealTypeForm.value,
          status: editMealTypeForm.value.status ? 1 : 0
        }
        const response = await axios.put('/api/admin/meal-type/update', formData)
        if (response.success) {
          ElMessage.success(response.message)
          editMealTypeDialogVisible.value = false
          loadMealTypes()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('编辑失败，请检查网络连接')
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该餐食类型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/admin/meal-type/delete/${id}`)
    if (response.success) {
      ElMessage.success(response.message)
      loadMealTypes()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除餐食类型失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '删除失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadMealTypes()
})
</script>

<style scoped>
.meal-type-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-type-content {
  padding-top: 20px;
}

.dialog-footer {
  width: 100%;
  text-align: right;
}
</style>
