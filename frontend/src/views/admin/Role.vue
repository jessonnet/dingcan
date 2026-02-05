<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="addRoleDialogVisible = true">添加角色</el-button>
        </div>
      </template>
      <div class="role-content">
        <el-table :data="roles" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="scope">
              {{ scope.row.id }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="角色名称" width="180">
            <template #default="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="角色描述">
            <template #default="scope">
              {{ scope.row.description }}
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

    <!-- 添加角色对话框 -->
    <el-dialog
      v-model="addRoleDialogVisible"
      title="添加角色"
      width="500px"
    >
      <el-form :model="addRoleForm" :rules="rules" ref="addRoleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="addRoleForm.name" placeholder="输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="addRoleForm.description" placeholder="输入角色描述" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addRoleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑角色对话框 -->
    <el-dialog
      v-model="editRoleDialogVisible"
      title="编辑角色"
      width="500px"
    >
      <el-form :model="editRoleForm" :rules="rules" ref="editRoleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="editRoleForm.name" placeholder="输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="editRoleForm.description" placeholder="输入角色描述" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editRoleDialogVisible = false">取消</el-button>
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

const roles = ref([])

const addRoleDialogVisible = ref(false)
const addRoleFormRef = ref(null)
const addRoleForm = ref({
  name: '',
  description: ''
})

const editRoleDialogVisible = ref(false)
const editRoleFormRef = ref(null)
const editRoleForm = ref({
  id: '',
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'change' }
  ]
}

const loadRoles = async () => {
  try {
    const response = await axios.get('/api/admin/role/list')
    if (response.success) {
      roles.value = response.data
    } else {
      ElMessage.error('加载角色失败')
    }
  } catch (error) {
    ElMessage.error('加载角色失败，请检查网络连接')
  }
}

const handleAddSubmit = async () => {
  if (!addRoleFormRef.value) return
  
  await addRoleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.post('/api/admin/role/add', addRoleForm.value)
        if (response.success) {
          ElMessage.success(response.message)
          addRoleDialogVisible.value = false
          addRoleForm.value = {
            name: '',
            description: ''
          }
          addRoleFormRef.value.resetFields()
          loadRoles()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('添加失败，请检查网络连接')
      }
    }
  })
}

const handleEdit = (role) => {
  editRoleForm.value = {
    id: role.id,
    name: role.name,
    description: role.description
  }
  editRoleDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editRoleFormRef.value) return
  
  await editRoleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.put('/api/admin/role/update', editRoleForm.value)
        if (response.success) {
          ElMessage.success(response.message)
          editRoleDialogVisible.value = false
          loadRoles()
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
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/admin/role/delete/${id}`)
    if (response.success) {
      ElMessage.success(response.message)
      loadRoles()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '删除失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-content {
  padding-top: 20px;
}

.dialog-footer {
  width: 100%;
  text-align: right;
}
</style>