<template>
  <div class="employee-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工管理</span>
          <el-button type="primary" @click="addEmployeeDialogVisible = true">添加员工</el-button>
        </div>
      </template>
      <div class="employee-content">
        <el-table :data="employees" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="scope">
              {{ scope.row.id }}
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="150">
            <template #default="scope">
              {{ scope.row.username }}
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120">
            <template #default="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="department" label="部门" width="150">
            <template #default="scope">
              {{ scope.row.department }}
            </template>
          </el-table-column>
          <el-table-column prop="roleName" label="角色" width="120">
            <template #default="scope">
              {{ scope.row.roleName }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="150">
            <template #default="scope">
              {{ scope.row.phone }}
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" width="200">
            <template #default="scope">
              {{ scope.row.email }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
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

    <!-- 添加员工对话框 -->
    <el-dialog
      v-model="addEmployeeDialogVisible"
      title="添加员工"
      width="500px"
    >
      <el-form :model="addEmployeeForm" :rules="rules" ref="addEmployeeFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addEmployeeForm.username" placeholder="输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addEmployeeForm.password" type="password" placeholder="输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addEmployeeForm.name" placeholder="输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="addEmployeeForm.department" placeholder="输入部门" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="addEmployeeForm.roleId" placeholder="选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addEmployeeForm.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addEmployeeForm.email" placeholder="输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="addEmployeeForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addEmployeeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑员工对话框 -->
    <el-dialog
      v-model="editEmployeeDialogVisible"
      title="编辑员工"
      width="500px"
    >
      <el-form :model="editEmployeeForm" :rules="rules" ref="editEmployeeFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editEmployeeForm.username" placeholder="输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="editEmployeeForm.password" type="password" placeholder="输入密码（留空则不修改）" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editEmployeeForm.name" placeholder="输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="editEmployeeForm.department" placeholder="输入部门" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="editEmployeeForm.roleId" placeholder="选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editEmployeeForm.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editEmployeeForm.email" placeholder="输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="editEmployeeForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editEmployeeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const employees = ref([])
const roles = ref([])

const addEmployeeDialogVisible = ref(false)
const addEmployeeFormRef = ref(null)
const addEmployeeForm = ref({
  username: '',
  password: '',
  name: '',
  department: '',
  roleId: '',
  phone: '',
  email: '',
  status: 1
})

const editEmployeeDialogVisible = ref(false)
const editEmployeeFormRef = ref(null)
const editEmployeeForm = ref({
  id: '',
  username: '',
  password: '',
  name: '',
  department: '',
  roleId: '',
  phone: '',
  email: '',
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请输入部门', trigger: 'change' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const loadEmployees = async () => {
  try {
    const response = await axios.get('/api/admin/user/list')
    if (response.success) {
      employees.value = response.data
    } else {
      ElMessage.error('加载员工列表失败')
    }
  } catch (error) {
    ElMessage.error('加载员工列表失败，请检查网络连接')
  }
}

const loadRoles = async () => {
  try {
    const response = await axios.get('/api/admin/role/list')
    if (response.success) {
      roles.value = response.data
    } else {
      ElMessage.error('加载角色列表失败')
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败，请检查网络连接')
  }
}

const handleAddSubmit = async () => {
  if (!addEmployeeFormRef.value) return
  
  await addEmployeeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换 status 为整数类型
        const formData = {
          ...addEmployeeForm.value,
          status: addEmployeeForm.value.status ? 1 : 0
        }
        const response = await axios.post('/api/admin/user/add', formData)
        if (response.success) {
          ElMessage.success(response.message)
          addEmployeeDialogVisible.value = false
          addEmployeeForm.value = {
            username: '',
            password: '',
            name: '',
            department: '',
            roleId: '',
            phone: '',
            email: '',
            status: 1
          }
          addEmployeeFormRef.value.resetFields()
          loadEmployees()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('添加失败，请检查网络连接')
      }
    }
  })
}

const handleEdit = (employee) => {
  editEmployeeForm.value = {
    id: employee.id,
    username: employee.username,
    password: '',
    name: employee.name,
    department: employee.department,
    roleId: employee.roleId,
    phone: employee.phone,
    email: employee.email,
    status: employee.status
  }
  editEmployeeDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editEmployeeFormRef.value) return
  
  await editEmployeeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换 status 为整数类型
        const formData = {
          ...editEmployeeForm.value,
          status: editEmployeeForm.value.status ? 1 : 0
        }
        const response = await axios.put('/api/admin/user/update', formData)
        if (response.success) {
          ElMessage.success(response.message)
          editEmployeeDialogVisible.value = false
          loadEmployees()
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
    const response = await axios.delete(`/api/admin/user/delete/${id}`)
    if (response.success) {
      ElMessage.success(response.message)
      loadEmployees()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('删除失败，请检查网络连接')
  }
}

onMounted(() => {
  loadEmployees()
  loadRoles()
})
</script>

<style scoped>
.employee-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.employee-content {
  padding-top: 20px;
}

.dialog-footer {
  width: 100%;
  text-align: right;
}
</style>
