<template>
  <div class="statistics-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>统计分析</span>
          <div class="date-range">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px"
            />
            <el-button type="primary" @click="loadStatistics" style="margin-left: 10px">查询</el-button>
            <el-button type="success" @click="exportExcel" style="margin-left: 10px">导出Excel</el-button>
          </div>
        </div>
      </template>
      <div class="statistics-content">
        <div class="total-count">
          <el-tag size="large" type="info">总计：{{ totalCount }} 份</el-tag>
        </div>
        <div class="meal-type-statistics">
          <el-card>
            <template #header>
              <span>按餐食类型统计</span>
            </template>
            <el-table :data="mealTypeStatistics" style="width: 100%">
              <el-table-column prop="name" label="餐食类型" width="120">
                <template #default="scope">
                  {{ scope.row.name }}
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="count" label="数量" width="100">
                <template #default="scope">
                  {{ scope.row.count }}
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
        <div class="daily-statistics">
          <el-card>
            <template #header>
              <span>按日期统计</span>
            </template>
            <el-table :data="dailyStatistics" style="width: 100%">
              <el-table-column prop="date" label="日期" width="120">
                <template #default="scope">
                  {{ scope.row.date }}
                </template>
              </el-table-column>
              <el-table-column prop="count" label="数量" width="100">
                <template #default="scope">
                  {{ scope.row.count }}
                </template>
              </el-table-column>
              <template v-for="mealType in mealTypes" :key="mealType.id">
                <el-table-column :prop="'mealType_' + mealType.id" :label="mealType.name" width="100">
                  <template #default="scope">
                    {{ scope.row['mealType_' + mealType.id] || 0 }}
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </el-card>
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

const dateRange = ref([dayjs().subtract(7, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const mealTypeStatistics = ref([])
const dailyStatistics = ref([])
const mealTypes = ref([])
const totalCount = ref(0)

const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/chef/statistics', {
      params: {
        startDate: dateRange.value[0],
        endDate: dateRange.value[1]
      }
    })
    
    console.log('统计响应:', response)
    
    if (response.success) {
      mealTypes.value = response.mealTypes
      totalCount.value = response.totalCount
      
      // 构建餐食类型统计
      mealTypeStatistics.value = mealTypes.value.map(mealType => {
        let count = 0
        for (const [date, mealTypeCounts] of Object.entries(response.data)) {
          count += mealTypeCounts[mealType.id] || 0
        }
        const percentage = totalCount.value > 0 ? Math.round((count / totalCount.value) * 100) : 0
        return {
          id: mealType.id,
          name: mealType.name,
          price: mealType.price,
          count,
          percentage
        }
      })
      
      // 构建日期统计
      dailyStatistics.value = Object.entries(response.data).map(([date, mealTypeCounts]) => {
        const dailyStat = {
          date,
          count: 0
        }
        for (const [mealTypeId, count] of Object.entries(mealTypeCounts)) {
          dailyStat['mealType_' + mealTypeId] = count
          dailyStat.count += count
        }
        return dailyStat
      })
    } else {
      ElMessage.error(response.message || '加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据错误:', error)
    ElMessage.error('加载统计数据失败，请检查网络连接')
  }
}

const exportExcel = async () => {
  try {
    // 构建导出URL
    const url = `/api/chef/export?startDate=${dateRange.value[0]}&endDate=${dateRange.value[1]}`
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = url
    link.download = `订餐统计_${dateRange.value[0]}_${dateRange.value[1]}.xlsx`
    link.click()
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败，请检查网络连接')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-range {
  display: flex;
  align-items: center;
}

.statistics-content {
  padding-top: 20px;
}

.total-count {
  margin-bottom: 20px;
}

.meal-type-statistics {
  margin-bottom: 20px;
}

.daily-statistics {
  margin-top: 20px;
}
</style>
