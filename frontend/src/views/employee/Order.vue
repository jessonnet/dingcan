<template>
  <div class="order-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>
      <div class="order-content">
        <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="100px">
          <el-form-item label="食堂" prop="restaurantId">
            <el-select v-model="orderForm.restaurantId" placeholder="选择食堂" style="width: 100%" :disabled="loadingRestaurants">
              <el-option
                v-for="restaurant in availableRestaurants"
                :key="restaurant.id"
                :label="restaurant.name + (restaurant.isDefault ? ' (默认)' : '')"
                :value="restaurant.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="餐食类型" prop="mealTypeId">
            <el-select v-model="orderForm.mealTypeId" placeholder="选择餐食类型" style="width: 100%">
              <el-option
                v-for="mealType in mealTypes"
                :key="mealType.id"
                :label="mealType.name + ' - ¥' + mealType.price"
                :value="mealType.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="订餐日期" prop="orderDates">
            <el-date-picker
              v-model="orderForm.orderDates"
              type="dates"
              placeholder="选择多个订餐日期"
              :disabled-date="disabledDate"
              style="width: 100%"
              multiple
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交订单</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <div v-if="selectedDatesInfo.length > 0" class="selected-dates-info">
          <el-divider content-position="left">已选择的日期</el-divider>
          <el-table :data="selectedDatesInfo" style="width: 100%" size="small">
            <el-table-column prop="date" label="日期" width="180" />
            <el-table-column prop="mealType" label="餐食类型" />
            <el-table-column prop="price" label="价格" width="100" />
          </el-table>
          <div class="total-price">
            <el-tag type="success" size="large">总计：¥{{ totalPrice }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const orderFormRef = ref(null)
const submitting = ref(false)
const orderForm = ref({
  orderDates: [],
  mealTypeId: '',
  restaurantId: ''
})

const rules = {
  restaurantId: [
    { required: true, message: '请选择食堂', trigger: 'change' }
  ],
  mealTypeId: [
    { required: true, message: '请选择餐食类型', trigger: 'change' }
  ],
  orderDates: [
    { required: true, message: '请选择至少一个订餐日期', trigger: 'change' }
  ]
}

const mealTypes = ref([])
const restaurants = ref([])
const loadingRestaurants = ref(false)
const userDepartmentId = ref(null)
const userRestaurantId = ref(null)

const availableRestaurants = computed(() => {
  const result = []
  const restaurantIds = new Set()

  console.log('availableRestaurants computed - userRestaurantId:', userRestaurantId.value)
  console.log('availableRestaurants computed - restaurants:', restaurants.value)

  // 首先添加用户关联的餐厅（不管是否可跨部门）
  if (userRestaurantId.value) {
    const userRestaurant = restaurants.value.find(r => r.id === userRestaurantId.value)
    if (userRestaurant) {
      result.push({
        ...userRestaurant,
        isDefault: true
      })
      restaurantIds.add(userRestaurant.id)
    }
  }

  // 然后添加所有可跨部门的餐厅
  restaurants.value.forEach(restaurant => {
    if (restaurant.crossDepartmentBooking === 1 && !restaurantIds.has(restaurant.id)) {
      result.push({
        ...restaurant,
        isDefault: false
      })
      restaurantIds.add(restaurant.id)
    }
  })

  console.log('availableRestaurants computed - result:', result)
  return result
})

const selectedDatesInfo = computed(() => {
  if (!orderForm.value.orderDates || orderForm.value.orderDates.length === 0) {
    return []
  }

  const selectedMealType = mealTypes.value.find(mt => mt.id === orderForm.value.mealTypeId)
  if (!selectedMealType) {
    return []
  }

  return orderForm.value.orderDates.map(date => {
    const dateStr = dayjs(date).format('YYYY-MM-DD')
    return {
      date: dateStr,
      mealType: selectedMealType.name,
      price: selectedMealType.price
    }
  })
})

const totalPrice = computed(() => {
  if (selectedDatesInfo.value.length === 0) {
    return 0
  }
  return selectedDatesInfo.value.reduce((sum, item) => sum + item.price, 0)
})

const disabledDate = (time) => {
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  return dayjs(time).isBefore(tomorrow)
}

const handleReset = () => {
  orderForm.value = {
    orderDates: [],
    mealTypeId: '',
    restaurantId: ''
  }
  if (orderFormRef.value) {
    orderFormRef.value.resetFields()
  }
}

const loadRestaurants = async () => {
  try {
    loadingRestaurants.value = true
    console.log('loadRestaurants - start')
    const response = await axios.get('/api/admin/restaurant/list')
    console.log('loadRestaurants - response:', response)
    if (response.success) {
      restaurants.value = response.data
      console.log('loadRestaurants - loaded restaurants:', restaurants.value)
    } else {
      ElMessage.error('加载餐厅列表失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载餐厅列表错误:', error)
    ElMessage.error('加载餐厅列表失败: ' + (error.message || '网络错误'))
  } finally {
    loadingRestaurants.value = false
  }
}

const loadUserInfo = () => {
  try {
    const userStr = localStorage.getItem('user')
    console.log('loadUserInfo - userStr:', userStr)
    if (userStr) {
      const user = JSON.parse(userStr)
      console.log('loadUserInfo - parsed user:', user)
      if (user && user.departmentId) {
        userDepartmentId.value = user.departmentId
      }
      if (user && user.restaurantId) {
        userRestaurantId.value = user.restaurantId
        orderForm.value.restaurantId = user.restaurantId
        console.log('loadUserInfo - set restaurantId:', user.restaurantId)
      }
    }
  } catch (error) {
    console.error('加载用户信息错误:', error)
  }
}

const handleSubmit = async () => {
  if (!orderFormRef.value) return

  try {
    await orderFormRef.value.validate()
  } catch (error) {
    return
  }

  if (!orderForm.value.orderDates || orderForm.value.orderDates.length === 0) {
    ElMessage.error('请选择至少一个订餐日期')
    return
  }

  if (!orderForm.value.mealTypeId) {
    ElMessage.error('请选择餐食类型')
    return
  }

  submitting.value = true

  try {
    const orderDateStrs = orderForm.value.orderDates.map(date => dayjs(date).format('YYYY-MM-DD'))

    const response = await axios.post('/api/order/batch-create', {
      restaurantId: orderForm.value.restaurantId,
      mealTypeId: orderForm.value.mealTypeId,
      orderDates: orderDateStrs
    })

    if (response.success) {
      const successCount = response.successCount || 0
      const failCount = response.failCount || 0

      if (failCount === 0) {
        ElMessage.success(response.message || '订餐成功')
      } else if (successCount === 0) {
        ElMessage.error(response.message || '订餐失败')
      } else {
        ElMessage.warning(response.message || '部分订餐成功')
      }

      orderForm.value = {
        orderDates: [],
        mealTypeId: '',
        restaurantId: ''
      }
      if (orderFormRef.value) {
        orderFormRef.value.resetFields()
      }
    } else {
      ElMessage.error(response.message || '订餐失败')
    }
  } catch (error) {
    console.error('创建订单请求错误:', error)
    ElMessage.error(error.response?.data?.message || '创建订单失败，请检查网络连接')
  } finally {
    submitting.value = false
  }
}

const loadMealTypes = async () => {
  try {
    const response = await axios.get('/api/order/meal-types')
    if (response.success) {
      mealTypes.value = response.data
    } else {
      ElMessage.error('加载餐食类型失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载餐食类型错误:', error)
    ElMessage.error('加载餐食类型失败: ' + (error.message || '网络错误'))
  }
}

onMounted(() => {
  loadRestaurants()
  loadUserInfo()
  loadMealTypes()
})
</script>

<style scoped>
.order-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-content {
  padding-top: 20px;
}

.selected-dates-info {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.total-price {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .order-container {
    padding: 10px;
  }

  .order-content {
    padding-top: 10px;
  }

  .selected-dates-info {
    padding: 15px;
    margin-top: 20px;
  }

  .el-form-item {
    margin-bottom: 18px;
  }

  .el-button {
    width: 100%;
    margin-bottom: 10px;
  }

  .el-button + .el-button {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .order-container {
    padding: 5px;
  }

  .el-table {
    font-size: 12px;
  }

  .el-table-column {
    padding: 8px 0;
  }
}

/* 鸿蒙系统适配 */
@media screen and (-webkit-min-device-pixel-ratio: 2) and (min-resolution: 192dpi) {
  .el-card {
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  }
  
  .el-button {
    border-radius: 8px;
  }
}

/* 鸿蒙系统安全区域适配 */
@supports (padding: max(0px)) {
  .order-container {
    padding-left: env(safe-area-inset-left);
    padding-right: env(safe-area-inset-right);
  }
}
</style>
