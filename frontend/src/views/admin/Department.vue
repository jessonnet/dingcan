<template>
  <div class="department-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门设置</span>
          <el-button type="primary" @click="addDepartmentDialogVisible = true">添加部门</el-button>
        </div>
      </template>
      <div class="department-content">
        <el-table :data="departments" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="scope">
              {{ scope.row.id }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="部门名称" width="200">
            <template #default="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="部门描述" width="300">
            <template #default="scope">
              {{ scope.row.description }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
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
      </div>
    </el-card>

    <!-- 添加部门对话框 -->
    <el-dialog
      v-model="addDepartmentDialogVisible"
      title="添加部门"
      width="500px"
    >
      <el-form :model="addDepartmentForm" :rules="rules" ref="addDepartmentFormRef" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="addDepartmentForm.name" placeholder="输入部门名称" />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input v-model="addDepartmentForm.description" type="textarea" :rows="3" placeholder="输入部门描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="addDepartmentForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDepartmentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑部门对话框 -->
    <el-dialog
      v-model="editDepartmentDialogVisible"
      title="编辑部门"
      width="500px"
    >
      <el-form :model="editDepartmentForm" :rules="rules" ref="editDepartmentFormRef" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="editDepartmentForm.name" placeholder="输入部门名称" />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input v-model="editDepartmentForm.description" type="textarea" :rows="3" placeholder="输入部门描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="editDepartmentForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDepartmentDialogVisible = false">取消</el-button>
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

const departments = ref([])

const addDepartmentDialogVisible = ref(false)
const addDepartmentFormRef = ref(null)
const addDepartmentForm = ref({
  name: '',
  description: '',
  status: 1
})

const editDepartmentDialogVisible = ref(false)
const editDepartmentFormRef = ref(null)
const editDepartmentForm = ref({
  id: '',
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入部门描述', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const loadDepartments = async () => {
  try {
    const response = await axios.get('/api/admin/department/list')
    if (response.success) {
      departments.value = response.data
    } else {
      ElMessage.error('加载部门列表失败')
    }
  } catch (error) {
    ElMessage.error('加载部门列表失败，请检查网络连接')
  }
}

const handleAddSubmit = async () => {
  if (!addDepartmentFormRef.value) return
  
  try {
    await addDepartmentFormRef.value.validate()
    const response = await axios.post('/api/admin/department/add', addDepartmentForm.value)
    if (response.success) {
      ElMessage.success('添加部门成功')
      addDepartmentDialogVisible.value = false
      addDepartmentForm.value = {
        name: '',
        description: '',
        status: 1
      }
      loadDepartments()
    } else {
      ElMessage.error(response.message || '添加部门失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('添加部门失败，请检查网络连接')
    }
  }
}

const handleEdit = (department) => {
  editDepartmentForm.value = {
    id: department.id,
    name: department.name,
    description: department.description,
    status: department.status
  }
  editDepartmentDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editDepartmentFormRef.value) return
  
  try {
    await editDepartmentFormRef.value.validate()
    const response = await axios.put('/api/admin/department/update', editDepartmentForm.value)
    if (response.success) {
      ElMessage.success('修改部门成功')
      editDepartmentDialogVisible.value = false
      loadDepartments()
    } else {
      ElMessage.error(response.message || '修改部门失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('修改部门失败，请检查网络连接')
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该部门吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/admin/department/delete/${id}`)
    if (response.success) {
      ElMessage.success('删除部门成功')
      loadDepartments()
    } else {
      ElMessage.error(response.message || '删除部门失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除部门失败，请检查网络连接')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.department-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.department-content {
  margin-top: 20px;
}
</style>
