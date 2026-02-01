<template>
  <div class="order-status-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订餐情况</span>
          <el-date-picker
            v-model="orderDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
            @change="loadOrderStatus"
          />
        </div>
      </template>
      <div class="order-status-content">
        <div class="meal-type-summary">
          <div v-for="(mealTypeOrder, key) in mealTypeOrders" :key="key" class="meal-type-summary-item">
            <el-tag size="large" :type="getMealTypeTagType(mealTypeOrder.mealType.name)">
              {{ mealTypeOrder.mealType.name }}：{{ mealTypeOrder.count }} 份
            </el-tag>
          </div>
        </div>
        <div class="meal-type-orders">
          <el-collapse v-model="activeNames">
            <el-collapse-item
              v-for="(mealTypeOrder, key) in mealTypeOrders"
              :key="key"
              :title="mealTypeOrder.mealType.name + '（' + mealTypeOrder.count + ' 份）'"
              :name="key"
            >
              <div class="order-names-container">
                <div v-for="order in mealTypeOrder.orders" :key="order.id" class="order-name-item">
                  {{ order.name }}
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const orderDate = ref(dayjs().format('YYYY-MM-DD'))
const mealTypeOrders = ref({})
const totalCount = ref(0)
const activeNames = ref([])

const getMealTypeTagType = (mealTypeName) => {
  const tagTypes = {
    '早餐': 'warning',
    '午餐': 'success',
    '晚餐': 'primary',
    '夜宵': 'danger'
  }
  return tagTypes[mealTypeName] || 'info'
}

const loadOrderStatus = async () => {
  try {
    const response = await axios.get('/api/chef/order-status', {
      params: {
        orderDate: orderDate.value
      }
    })
    
    console.log('订餐情况响应:', response)
    
    if (response.success) {
      mealTypeOrders.value = response.data || {}
      totalCount.value = response.totalCount || 0
      // 展开第一个餐食类型
      const keys = Object.keys(mealTypeOrders.value)
      if (keys.length > 0) {
        activeNames.value = [keys[0]]
      }
    } else {
      ElMessage.error(response.message || '加载订餐情况失败')
    }
  } catch (error) {
    console.error('加载订餐情况错误:', error)
    ElMessage.error('加载订餐情况失败，请检查网络连接')
  }
}

onMounted(() => {
  loadOrderStatus()
})
</script>

<style scoped>
.order-status-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-status-content {
  padding-top: 20px;
}

.meal-type-summary {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.meal-type-summary-item {
  display: flex;
  align-items: center;
}

.meal-type-orders {
  margin-top: 20px;
}

.el-collapse {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.el-collapse-item {
  border-bottom: 1px solid #ebeef5;
}

.el-collapse-item__header {
  background-color: #f5f7fa;
  font-weight: 500;
  font-size: 15px;
}

.el-collapse-item__content {
  padding: 16px;
  background-color: #fff;
}

.order-names-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.order-name-item {
  padding: 6px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  cursor: default;
}

.order-name-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .meal-type-summary {
    flex-direction: column;
    gap: 8px;
  }

  .meal-type-summary-item {
    width: 100%;
  }

  .order-names-container {
    gap: 8px;
  }

  .order-name-item {
    padding: 5px 12px;
    font-size: 13px;
  }
}
</style>