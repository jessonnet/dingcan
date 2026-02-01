<template>
  <div class="order-history-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单历史</span>
        </div>
      </template>
      <div class="order-history-content">
        <el-table :data="orderHistory" style="width: 100%">
          <el-table-column prop="orderDate" label="订餐日期" width="180">
            <template #default="scope">
              {{ scope.row.orderDate }}
            </template>
          </el-table-column>
          <el-table-column prop="mealType" label="餐食类型" width="180">
            <template #default="scope">
              {{ scope.row.mealType }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">有效</el-tag>
              <el-tag v-else type="danger">无效</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="200">
            <template #default="scope">
              {{ scope.row.createdAt }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleUpdate(scope.row)" :disabled="!canEdit(scope.row.orderDate)">修改</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" :disabled="!canEdit(scope.row.orderDate)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 修改订单对话框 -->
    <el-dialog
      v-model="updateDialogVisible"
      title="修改订单"
      width="500px"
    >
      <el-form :model="updateForm" :rules="rules" ref="updateFormRef" label-width="100px">
        <el-form-item label="订餐日期" prop="orderDate">
          <el-date-picker
            v-model="updateForm.orderDate"
            type="date"
            placeholder="选择订餐日期"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="餐食类型" prop="mealTypeId">
          <el-select v-model="updateForm.mealTypeId" placeholder="选择餐食类型" style="width: 100%">
            <el-option
              v-for="mealType in mealTypes"
              :key="mealType.id"
              :label="mealType.name + ' - ¥' + mealType.price"
              :value="mealType.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="updateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const orderHistory = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const mealTypes = ref([])

const updateDialogVisible = ref(false)
const updateFormRef = ref(null)
const updateForm = ref({
  id: '',
  orderDate: '',
  mealTypeId: ''
})

const rules = {
  orderDate: [
    { required: true, message: '请选择订餐日期', trigger: 'change' }
  ],
  mealTypeId: [
    { required: true, message: '请选择餐食类型', trigger: 'change' }
  ]
}

const disabledDate = (time) => {
  // 只能选择明天及以后的日期
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  return dayjs(time).isBefore(tomorrow)
}

const canEdit = (orderDate) => {
  // 检查是否可以修改或删除订单
  const orderDateObj = dayjs(orderDate)
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  
  // 只能修改或删除明天及以后的订单
  return orderDateObj.isAfter(today)
}

const loadOrderHistory = async () => {
  try {
    const response = await axios.get('/api/order/history', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    if (response.success) {
      // 这里需要实现获取餐食类型的API，暂时使用模拟数据
      const mealTypeMap = {
        1: { name: '早餐', price: 5.00 },
        2: { name: '午餐', price: 15.00 },
        3: { name: '晚餐', price: 10.00 }
      }
      
      orderHistory.value = response.data.map(order => {
        const mealType = mealTypeMap[order.mealTypeId] || { name: '未知', price: 0 }
        return {
          ...order,
          mealType: mealType.name,
          price: mealType.price
        }
      })
      total.value = 100 // 这里需要实现获取总条数的API
    } else {
      ElMessage.error('加载订单历史失败')
    }
  } catch (error) {
    ElMessage.error('加载订单历史失败，请检查网络连接')
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadOrderHistory()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadOrderHistory()
}

const handleUpdate = (order) => {
  updateForm.value = {
    id: order.id,
    orderDate: order.orderDate,
    mealTypeId: order.mealTypeId
  }
  updateDialogVisible.value = true
}

const handleUpdateSubmit = async () => {
  if (!updateFormRef.value) return
  
  await updateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 检查是否可以修改
        const canOrderResponse = await axios.get('/api/order/can-order', {
          params: {
            orderDate: updateForm.value.orderDate
          }
        })
        
        if (!canOrderResponse.canOrder) {
          ElMessage.error('超过修改时间，无法修改')
          return
        }
        
        // 修改订单
        const response = await axios.put('/api/order/update', {
          id: updateForm.value.id,
          mealTypeId: updateForm.value.mealTypeId,
          orderDate: updateForm.value.orderDate
        })
        
        if (response.success) {
          ElMessage.success(response.message)
          updateDialogVisible.value = false
          loadOrderHistory()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('修改失败，请检查网络连接')
      }
    }
  })
}

const handleDelete = async (orderId) => {
  try {
    const response = await axios.delete(`/api/order/delete/${orderId}`)
    
    if (response.success) {
      ElMessage.success(response.message)
      loadOrderHistory()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('删除失败，请检查网络连接')
  }
}

const loadMealTypes = async () => {
  try {
    // 这里需要实现获取餐食类型的API
    // 暂时使用模拟数据
    mealTypes.value = [
      { id: 1, name: '早餐', price: 5.00 },
      { id: 2, name: '午餐', price: 15.00 },
      { id: 3, name: '晚餐', price: 10.00 }
    ]
  } catch (error) {
    ElMessage.error('加载餐食类型失败')
  }
}

onMounted(() => {
  loadOrderHistory()
  loadMealTypes()
})
</script>

<style scoped>
.order-history-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-history-content {
  padding-top: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
