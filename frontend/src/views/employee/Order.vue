<template>
  <div class="order-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线订餐</span>
        </div>
      </template>
      <div class="order-content">
        <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="100px">
          <el-form-item label="订餐日期" prop="orderDate">
            <el-date-picker
              v-model="orderForm.orderDate"
              type="date"
              placeholder="选择订餐日期"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
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
          <el-form-item>
            <el-button type="primary" @click="handleSubmit">提交订单</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

console.log('Order.vue 组件加载成功')
console.log('axios:', axios)
console.log('axios.defaults:', axios.defaults)
console.log('axios.defaults.baseURL:', axios.defaults.baseURL)
console.log('ElMessage:', ElMessage)
console.log('dayjs:', dayjs)

const orderFormRef = ref(null)
const orderForm = ref({
  orderDate: '',
  mealTypeId: ''
})

// 监听订单日期变化
watch(
  () => orderForm.value.orderDate,
  (newValue, oldValue) => {
    console.log('订单日期变化:', oldValue, '->', newValue)
    console.log('订单日期类型:', typeof newValue)
    console.log('订单日期值:', newValue)
  }
)

// 监听餐食类型变化
watch(
  () => orderForm.value.mealTypeId,
  (newValue, oldValue) => {
    console.log('餐食类型变化:', oldValue, '->', newValue)
    console.log('餐食类型类型:', typeof newValue)
    console.log('餐食类型值:', newValue)
  }
)

const rules = {
  orderDate: [
    { required: true, message: '请选择订餐日期', trigger: 'change' }
  ],
  mealTypeId: [
    { required: true, message: '请选择餐食类型', trigger: 'change' }
  ]
}

const mealTypes = ref([])
const canSubmit = computed(() => {
  // 当订单日期和餐食类型都选择后，才能提交
  const result = !!orderForm.value.orderDate && !!orderForm.value.mealTypeId
  console.log('canSubmit 计算结果:', result)
  console.log('订单日期:', orderForm.value.orderDate)
  console.log('订单日期类型:', typeof orderForm.value.orderDate)
  console.log('餐食类型:', orderForm.value.mealTypeId)
  console.log('餐食类型类型:', typeof orderForm.value.mealTypeId)
  return result
})

const disabledDate = (time) => {
  // 只能选择明天及以后的日期
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  return dayjs(time).isBefore(tomorrow)
}

const handleSubmit = () => {
  console.log('handleSubmit 函数被调用')
  
  // 检查订单日期和餐食类型是否选择
  if (!orderForm.value.orderDate) {
    console.error('订单日期未选择')
    ElMessage.error('请选择订餐日期')
    return
  }
  
  if (!orderForm.value.mealTypeId) {
    console.error('餐食类型未选择')
    ElMessage.error('请选择餐食类型')
    return
  }
  
  // 将日期对象转换为字符串格式
  let orderDateStr = ''
  try {
    if (typeof orderForm.value.orderDate === 'string') {
      orderDateStr = orderForm.value.orderDate
    } else {
      orderDateStr = dayjs(orderForm.value.orderDate).format('YYYY-MM-DD')
    }
    console.log('订单日期字符串:', orderDateStr)
  } catch (error) {
    console.error('格式化日期错误:', error)
    ElMessage.error('日期格式错误，请重新选择')
    return
  }
  
  // 检查是否可以预订
  console.log('开始发送 can-order 请求...')
  const fullCanOrderUrl = '/api/order/can-order?orderDate=' + encodeURIComponent(orderDateStr)
  console.log('完整的 can-order 请求 URL:', fullCanOrderUrl)
  
  axios.get(fullCanOrderUrl)
    .then(response => {
      console.log('can-order 请求响应:', response)
      if (response.success && response.canOrder) {
        console.log('可以预订，开始创建订单...')
        
        axios.post('/api/order/create', {
          mealTypeId: orderForm.value.mealTypeId,
          orderDate: orderDateStr
        })
        .then(response => {
          console.log('create 请求响应:', response)
          if (response.success) {
            console.log('订餐成功')
            ElMessage.success(response.message)
            orderForm.value = {
              orderDate: '',
              mealTypeId: ''
            }
            if (orderFormRef.value) {
              orderFormRef.value.resetFields()
            }
          } else {
            console.log('订餐失败')
            ElMessage.error(response.message)
          }
        })
        .catch(error => {
          console.error('创建订单请求错误:', error)
          console.error('创建订单请求错误详情:', error.response)
          ElMessage.error('创建订单失败，请检查网络连接')
        })
      } else {
        console.log('不能预订')
        ElMessage.error('超过预订时间，无法预订')
      }
    })
    .catch(error => {
      console.error('网络请求失败:', error)
      console.error('网络请求失败详情:', error.response)
      ElMessage.error('网络请求失败，请检查网络连接')
    })
}

const loadMealTypes = async () => {
  try {
    // 从后端 API 获取餐食类型列表
    console.log('开始加载餐食类型...')
    const response = await axios.get('/api/order/meal-types')
    console.log('餐食类型加载响应:', response)
    if (response.success) {
      console.log('餐食类型数据:', response.data)
      mealTypes.value = response.data
    } else {
      console.error('加载餐食类型失败，响应:', response)
      ElMessage.error('加载餐食类型失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载餐食类型错误:', error)
    console.error('错误详情:', error.response)
    ElMessage.error('加载餐食类型失败: ' + (error.message || '网络错误'))
  }
}

onMounted(() => {
  loadMealTypes()
})
</script>

<style scoped>
.order-container {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-content {
  padding-top: 20px;
}
</style>
