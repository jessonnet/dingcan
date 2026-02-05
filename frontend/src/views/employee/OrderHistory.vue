<template>
  <div class="order-history-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单历史</span>
        </div>
      </template>
      <div class="order-history-content">
        <div v-if="aggregatedOrders.length === 0" class="empty-state">
          <el-empty description="暂无订单记录" />
        </div>
        <div v-else class="aggregated-orders">
          <div v-for="aggregatedOrder in aggregatedOrders" :key="aggregatedOrder.orderDate" class="aggregated-order-item">
            <div class="order-date-header">
              <div class="date-info">
                <span class="date-text">{{ formatDate(aggregatedOrder.orderDate) }}</span>
                <span class="date-weekday">{{ getWeekday(aggregatedOrder.orderDate) }}</span>
              </div>
            </div>
            
            <el-table 
              :data="aggregatedOrder.orders" 
              style="width: 100%" 
              size="small"
              :row-class-name="tableRowClassName"
              :cell-style="{padding: '6px 0'}"
              :header-cell-style="{padding: '8px 0', backgroundColor: '#f5f7fa'}"
            >
              <el-table-column prop="mealTypeName" label="餐食类型" width="140">
                <template #default="scope">
                  <span class="meal-type-name">{{ scope.row.mealTypeName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100">
                <template #default="scope">
                  <span class="price-text">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="statusText" label="状态" width="80">
                <template #default="scope">
                  <el-tag v-if="scope.row.status === 1" type="success" size="small">有效</el-tag>
                  <el-tag v-else type="danger" size="small">无效</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="160">
                <template #default="scope">
                  <span class="created-time">{{ scope.row.createdAt }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleCancel(scope.row.id)" 
                    :disabled="!canEdit(aggregatedOrder.orderDate)"
                  >
                    取消订餐
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        
        <div class="pagination" v-if="aggregatedOrders.length > 0">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const aggregatedOrders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)



const canEdit = (orderDate) => {
  const orderDateObj = dayjs(orderDate)
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  
  // 不能修改今天或以前的订单
  if (orderDateObj.isBefore(today) || orderDateObj.isSame(today)) {
    return false
  }
  
  // 可以修改明天及以后的订单
  return true
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

const getWeekday = (date) => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[dayjs(date).day()]
}

const tableRowClassName = ({ row, rowIndex }) => {
  const mealTypeColors = {
    '早餐': 'meal-type-breakfast',
    '午餐': 'meal-type-lunch',
    '晚餐': 'meal-type-dinner',
    '夜宵': 'meal-type-snack'
  }
  return mealTypeColors[row.mealTypeName] || 'meal-type-default'
}

const loadAggregatedOrderHistory = async () => {
  try {
    const response = await axios.get('/api/order/aggregated-history', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    if (response.success) {
      aggregatedOrders.value = response.data
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
  loadAggregatedOrderHistory()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadAggregatedOrderHistory()
}

const handleCancel = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/api/order/delete/${orderId}`)
    
    if (response.success) {
      ElMessage.success('取消订餐成功')
      loadAggregatedOrderHistory()
    } else {
      ElMessage.error(response.message || '取消订餐失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订餐失败:', error)
      const errorMessage = error.message || error.response?.data?.message || '取消订餐失败，请检查网络连接'
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadAggregatedOrderHistory()
})
</script>

<style scoped>
.order-history-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.order-history-content {
  padding-top: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.aggregated-orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.aggregated-order-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s;
}

.aggregated-order-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.order-date-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  min-height: 40px;
}

.date-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.date-text {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.date-weekday {
  font-size: 13px;
  opacity: 0.9;
  font-weight: 400;
}

:deep(.el-table) {
  font-size: 13px;
  border-top: none;
}

:deep(.el-table .cell) {
  padding: 0 8px;
  line-height: 1.3;
}

:deep(.el-table th) {
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  background-color: #f5f7fa;
  padding: 8px 0;
}

:deep(.el-table td) {
  padding: 5px 0;
}

:deep(.el-table tr) {
  transition: background-color 0.2s;
}

:deep(.meal-type-breakfast) {
  background-color: #fff7e6;
}

:deep(.meal-type-breakfast:hover) {
  background-color: #ffe7ba;
}

:deep(.meal-type-lunch) {
  background-color: #e6f7ff;
}

:deep(.meal-type-lunch:hover) {
  background-color: #bae7ff;
}

:deep(.meal-type-dinner) {
  background-color: #f6ffed;
}

:deep(.meal-type-dinner:hover) {
  background-color: #d9f7be;
}

:deep(.meal-type-snack) {
  background-color: #fff0f6;
}

:deep(.meal-type-snack:hover) {
  background-color: #ffadd2;
}

:deep(.meal-type-default) {
  background-color: #fafafa;
}

:deep(.meal-type-default:hover) {
  background-color: #f0f0f0;
}

.meal-type-name {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.price-text {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.created-time {
  color: #909399;
  font-size: 12px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .order-history-container {
    padding: 10px;
  }

  .order-date-header {
    padding: 8px 12px;
    min-height: auto;
  }

  .date-text {
    font-size: 14px;
  }

  .date-weekday {
    font-size: 12px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table .cell) {
    padding: 0 6px;
  }
}

@media (max-width: 480px) {
  .order-date-header {
    padding: 6px 10px;
  }

  .date-info {
    gap: 6px;
  }

  .date-text {
    font-size: 13px;
  }

  .date-weekday {
    font-size: 11px;
  }

  :deep(.el-table) {
    font-size: 11px;
  }

  :deep(.el-table .cell) {
    padding: 0 4px;
  }

  :deep(.el-button--small) {
    padding: 4px 8px;
    font-size: 11px;
  }
}
</style>
