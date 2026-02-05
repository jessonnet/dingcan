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
          <el-table-column prop="departmentName" label="部门" width="150">
            <template #default="scope">
              {{ scope.row.departmentName }}
            </template>
          </el-table-column>
          <el-table-column prop="restaurantName" label="食堂" width="150">
            <template #default="scope">
              {{ scope.row.restaurantName }}
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
          <el-table-column prop="balance" label="余额" width="100">
            <template #default="scope">
              {{ scope.row.balance || 0.00 }}
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
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="addEmployeeForm.departmentId" placeholder="选择部门" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="食堂" prop="restaurantId">
          <el-select v-model="addEmployeeForm.restaurantId" placeholder="选择食堂" clearable>
            <el-option
              v-for="restaurant in restaurants"
              :key="restaurant.id"
              :label="restaurant.name"
              :value="restaurant.id"
            />
          </el-select>
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
      <el-form :model="editEmployeeForm" :rules="editRules" ref="editEmployeeFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editEmployeeForm.username" placeholder="输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="editEmployeeForm.password" type="password" placeholder="输入密码（留空则不修改）" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editEmployeeForm.name" placeholder="输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="editEmployeeForm.departmentId" placeholder="选择部门" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="食堂" prop="restaurantId">
          <el-select v-model="editEmployeeForm.restaurantId" placeholder="选择食堂" clearable>
            <el-option
              v-for="restaurant in restaurants"
              :key="restaurant.id"
              :label="restaurant.name"
              :value="restaurant.id"
            />
          </el-select>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const employees = ref([])
const roles = ref([])
const departments = ref([])
const restaurants = ref([])

const addEmployeeDialogVisible = ref(false)
const addEmployeeFormRef = ref(null)
const addEmployeeForm = ref({
  username: '',
  password: '',
  name: '',
  departmentId: '',
  restaurantId: '',
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
  departmentId: '',
  restaurantId: '',
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
  departmentId: [
    { required: false, message: '请选择部门', trigger: 'change' }
  ],
  restaurantId: [
    { required: true, message: '请选择食堂', trigger: 'change' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'change' }
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'change',
      required: false
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' }
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (value && value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'change' }
  ],
  departmentId: [
    { required: false, message: '请选择部门', trigger: 'change' }
  ],
  restaurantId: [
    { required: true, message: '请选择食堂', trigger: 'change' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'change' }
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'change',
      required: false
    }
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

const loadDepartments = async () => {
  try {
    const response = await axios.get('/api/admin/department/enabled')
    if (response.success) {
      departments.value = response.data
    } else {
      ElMessage.error('加载部门列表失败')
    }
  } catch (error) {
    ElMessage.error('加载部门列表失败，请检查网络连接')
  }
}

const loadRestaurants = async () => {
  try {
    const response = await axios.get('/api/admin/restaurant/list')
    if (response.success) {
      restaurants.value = response.data
    } else {
      ElMessage.error('加载餐厅列表失败')
    }
  } catch (error) {
    ElMessage.error('加载餐厅列表失败，请检查网络连接')
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
            departmentId: '',
            restaurantId: '',
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
    departmentId: employee.departmentId,
    restaurantId: employee.restaurantId,
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
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/admin/user/delete/${id}`)
    if (response.success) {
      ElMessage.success(response.message)
      loadEmployees()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除员工失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '删除失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadEmployees()
  loadRoles()
  loadDepartments()
  loadRestaurants()
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
