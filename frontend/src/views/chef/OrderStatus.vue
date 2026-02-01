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
        <div class="total-count">
          <el-tag size="large" type="info">总计：{{ totalCount }} 份</el-tag>
        </div>
        <div class="meal-type-orders">
          <el-collapse v-model="activeNames">
            <el-collapse-item
              v-for="(mealTypeOrder, key) in mealTypeOrders"
              :key="key"
              :title="mealTypeOrder.mealType.name + ' - ¥' + mealTypeOrder.mealType.price + '（' + mealTypeOrder.count + ' 份）'"
              :name="key"
            >
              <el-table :data="mealTypeOrder.orders" style="width: 100%">
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
                <el-table-column prop="username" label="用户名">
                  <template #default="scope">
                    {{ scope.row.username }}
                  </template>
                </el-table-column>
              </el-table>
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

.total-count {
  margin-bottom: 20px;
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
}

.el-collapse-item__content {
  padding: 10px;
  background-color: #fff;
}
</style>