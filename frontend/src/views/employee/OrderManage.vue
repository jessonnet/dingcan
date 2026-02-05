<template>
  <div class="order-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">在线订餐</h2>
      <el-radio-group v-model="viewMode" size="small" class="view-toggle">
        <el-radio-button label="calendar">
          <el-icon><Calendar /></el-icon>
          <span class="btn-text">日历视图</span>
        </el-radio-button>
        <el-radio-button label="list">
          <el-icon><List /></el-icon>
          <span class="btn-text">列表视图</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 步骤1: 选择食堂 -->
    <el-card class="meal-type-card restaurant-card" shadow="never" v-loading="restaurantsLoading">
      <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="80px">
        <el-form-item label="食堂" prop="restaurantId">
          <el-select v-model="selectedRestaurantId" placeholder="选择食堂" style="width: 100%" :disabled="loadingRestaurants">
            <el-option
              v-for="restaurant in availableRestaurants"
              :key="restaurant.id"
              :label="restaurant.name"
              :value="restaurant.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 步骤2: 选择餐食类型 -->
    <el-card class="meal-type-card" shadow="never" v-loading="mealTypesLoading">
      <div class="meal-type-list">
        <div
          v-for="mealType in mealTypes"
          :key="mealType.id"
          :class="['meal-type-item', { 
            active: selectedMealTypeId === mealType.id, 
            disabled: !mealType.status 
          }]"
          @click="selectMealType(mealType)"
        >
          <div class="meal-type-icon" :style="{ backgroundColor: getMealTypeColor(mealType.name) }">
            <el-icon><Food /></el-icon>
          </div>
          <div class="meal-type-info">
            <div class="meal-type-name">{{ mealType.name }}</div>
            <div class="meal-type-price">¥{{ mealType.price }}</div>
          </div>
          <div class="meal-type-status">
            <el-icon v-if="selectedMealTypeId === mealType.id" class="check-icon"><CircleCheckFilled /></el-icon>
            <el-tag v-else-if="!mealType.status" type="info" size="small" effect="plain">暂停</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 日历视图 -->
    <el-card v-show="viewMode === 'calendar'" class="calendar-card" shadow="never" v-loading="calendarLoading">
      <template #header>
        <div class="calendar-header">
          <div class="month-navigation">
            <el-button circle size="small" @click="changeMonth(-1)">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="current-month">{{ currentYearMonth }}</span>
            <el-button circle size="small" @click="changeMonth(1)">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button size="small" @click="goToToday" class="today-btn" type="primary" plain>
              今天
            </el-button>
          </div>
          <div class="legend">
            <span class="legend-item">
              <span class="legend-dot ordered"></span>
              <span class="legend-text">已订餐</span>
            </span>
            <span class="legend-item">
              <span class="legend-dot can-order"></span>
              <span class="legend-text">可订餐</span>
            </span>
            <span class="legend-item">
              <span class="legend-dot locked"></span>
              <span class="legend-text">已过期</span>
            </span>
          </div>
        </div>
      </template>

      <!-- 自定义日历 -->
      <div class="custom-calendar">
        <!-- 星期标题 -->
        <div class="calendar-weekdays">
          <div v-for="day in weekDays" :key="day" class="weekday">{{ day }}</div>
        </div>
        
        <!-- 日期网格 -->
        <div class="calendar-days">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            :class="[
              'calendar-day',
              {
                'other-month': !day.isCurrentMonth,
                'today': day.isToday,
                'ordered': day.hasOrder,
                'can-order': day.canOrder && selectedMealTypeId && !day.hasOrder,
                'locked': day.isLocked,
                'selected': selectedDate === day.dateStr
              }
            ]"
            @click="handleDateClick(day)"
          >
            <div class="day-number">{{ day.dayOfMonth }}</div>
            
            <!-- 已订餐状态显示 -->
            <div v-if="day.hasOrder" class="day-orders">
              <div
                v-for="order in day.orders.slice(0, 2)"
                :key="order.id"
                class="order-badge"
                :class="{ 'current-type': order.mealTypeId === selectedMealTypeId }"
                :style="{ 
                  backgroundColor: order.mealTypeId === selectedMealTypeId ? getMealTypeColor(order.mealTypeName) : '#909399'
                }"
              >
                {{ order.mealTypeName }}
              </div>
              <span v-if="day.orders.length > 2" class="more-orders">+{{ day.orders.length - 2 }}</span>
            </div>
            
            <!-- 可订餐状态显示 -->
            <div v-else-if="day.canOrder && selectedMealTypeId" class="day-status can-order-status">
              <el-icon class="add-icon"><Plus /></el-icon>
              <span class="order-text">点击订餐</span>
            </div>
            
            <!-- 未选择餐食类型时的提示 -->
            <div v-else-if="day.canOrder && !selectedMealTypeId" class="day-status">
              <el-icon class="select-icon"><Select /></el-icon>
            </div>
            
            <!-- 已过期状态 -->
            <div v-else-if="day.isLocked" class="day-status locked-status">
              <el-icon class="lock-icon"><Lock /></el-icon>
              <span class="locked-text">已过期</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作提示 -->
      <div class="calendar-tip">
        <el-alert
          v-if="!selectedMealTypeId"
          title="请先选择上方的餐食类型，然后点击日期进行订餐"
          type="info"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else
          :title="`当前选择: ${selectedMealType?.name || '未知'} - 点击未订餐日期可创建订单，点击已订餐日期可取消订单`"
          type="success"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>

    <!-- 列表视图 -->
    <el-card v-show="viewMode === 'list'" class="list-card" shadow="never">
      <template #header>
        <div class="list-header">
          <span>订单列表</span>
          <el-date-picker
            v-model="listDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            size="small"
            @change="loadOrderList"
          />
        </div>
      </template>
      
      <el-table :data="orderList" v-loading="listLoading" stripe border>
        <el-table-column prop="orderDate" label="日期" width="100">
          <template #default="scope">
            <div class="date-cell">
              <div class="date-day">{{ formatDay(scope.row.orderDate) }}</div>
              <div class="date-week">{{ getWeekday(scope.row.orderDate) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="mealTypeName" label="餐食类型" width="100">
          <template #default="scope">
            <div 
              class="meal-badge" 
              :style="{ backgroundColor: getMealTypeColor(scope.row.mealTypeName) }"
            >
              {{ scope.row.mealTypeName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="restaurantName" label="食堂" width="120">
          <template #default="scope">
            <span>{{ scope.row.restaurantName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="80">
          <template #default="scope">
            <span class="price">¥{{ scope.row.price?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
              {{ scope.row.status === 1 ? '有效' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="140" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 1 && canCancelOrder(scope.row.orderDate)"
              type="danger"
              size="small"
              @click="cancelOrder(scope.row)"
            >
              取消
            </el-button>
            <span v-else-if="scope.row.status !== 1" class="cancelled-text">已取消</span>
            <span v-else class="locked-text">不可取消</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="listPage"
          v-model:page-size="listPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="listTotal"
          @size-change="loadOrderList"
          @current-change="loadOrderList"
        />
      </div>
    </el-card>

    <!-- 创建订单确认对话框 - 仅显示订餐按钮 -->
    <el-dialog
      v-model="createOrderVisible"
      title="确认订餐"
      width="340px"
      :close-on-click-modal="false"
      destroy-on-close
      class="order-dialog"
    >
      <div class="confirm-order-content">
        <div class="confirm-icon success">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <div class="confirm-info">
          <div class="confirm-date">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDateFull(selectedDate) }}</span>
          </div>
          <div class="confirm-meal">
            <el-icon><Food /></el-icon>
            <span>{{ selectedMealType?.name }}</span>
            <span class="meal-price">¥{{ selectedMealType?.price }}</span>
          </div>
        </div>
        <el-divider />
        <div class="confirm-total">
          <span>订单金额:</span>
          <span class="total-price">¥{{ selectedMealType?.price }}</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmCreateOrder" :loading="submitting" size="default" class="full-width-btn">
          <el-icon><Check /></el-icon>
          确认订餐
        </el-button>
      </template>
    </el-dialog>

    <!-- 取消订单确认对话框 - 仅显示取消订餐按钮 -->
    <el-dialog
      v-model="cancelOrderVisible"
      title="确认取消订单"
      width="340px"
      :close-on-click-modal="false"
      destroy-on-close
      class="order-dialog"
    >
      <div class="cancel-order-content">
        <div class="confirm-icon warning">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <p class="cancel-text">确定要取消以下订单吗？</p>
        <div class="cancel-detail">
          <div class="cancel-item">
            <span class="label">
              <el-icon><Calendar /></el-icon>
              日期
            </span>
            <span class="value">{{ formatDateFull(orderToCancel?.orderDate) }}</span>
          </div>
          <div class="cancel-item">
            <span class="label">
              <el-icon><Food /></el-icon>
              餐食
            </span>
            <span class="value">{{ orderToCancel?.mealTypeName }}</span>
          </div>
          <div class="cancel-item">
            <span class="label">
              <el-icon><Money /></el-icon>
              金额
            </span>
            <span class="value price">¥{{ orderToCancel?.price?.toFixed(2) }}</span>
          </div>
        </div>
        <el-alert
          title="取消后无法恢复，请谨慎操作"
          type="warning"
          :closable="false"
          show-icon
          class="cancel-warning"
        />
      </div>
      <template #footer>
        <el-button type="danger" @click="confirmCancelOrder" :loading="submitting" size="default" class="full-width-btn">
          <el-icon><Delete /></el-icon>
          取消订餐
        </el-button>
      </template>
    </el-dialog>

    <!-- 统计信息卡片 -->
    <el-row :gutter="15" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon bg-blue">
              <el-icon><Food /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ currentMonthOrders.length }}</div>
              <div class="stat-label">本月订餐</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon bg-green">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ validOrdersCount }}</div>
              <div class="stat-label">有效订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon bg-orange">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ currentMonthTotal }}</div>
              <div class="stat-label">本月消费</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover" v-loading="statsLoading">
          <div class="stat-content">
            <div class="stat-icon bg-purple">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ upcomingOrdersCount }}</div>
              <div class="stat-label">待用餐</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { 
  Calendar, List, Food, Check, Money, Timer, 
  ArrowLeft, ArrowRight, Plus, Lock, Select, 
  WarningFilled, CircleCheckFilled, InfoFilled, Delete
} from '@element-plus/icons-vue'
import OptimisticUpdateManager from '@/utils/optimisticUpdate'

dayjs.locale('zh-cn')

// ============ 乐观更新管理器 ============
const updateManager = new OptimisticUpdateManager()

// 获取当前用户信息
const user = computed(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      console.error('解析用户信息失败:', e)
      return null
    }
  }
  return null
})

// ============ 本地存储键名 ============
const STORAGE_KEYS = {
  SELECTED_MEAL_TYPE: 'canteen_selected_meal_type',
  TEMP_ORDER_DATA: 'canteen_temp_order_data',
  VIEW_MODE: 'canteen_view_mode'
}

// ============ 状态管理 ============
const viewMode = ref('calendar')
const currentDate = ref(dayjs())
const selectedDate = ref('')
const selectedRestaurantId = ref(null)
const selectedMealTypeId = ref(null)
const submitting = ref(false)
const statsLoading = ref(false)
const calendarLoading = ref(false)
const listLoading = ref(false)
const mealTypesLoading = ref(false)
const restaurantsLoading = ref(false)

// 对话框状态
const createOrderVisible = ref(false)
const cancelOrderVisible = ref(false)
const orderToCancel = ref(null)

// 数据
const restaurants = ref([])
const mealTypes = ref([])
const allOrders = ref([])
const orderList = ref([])
const listTotal = ref(0)
const listPage = ref(1)
const listPageSize = ref(10)

// 初始化日期范围为当月
const now = dayjs()
const listDateRange = ref([now.startOf('month').toDate(), now.endOf('month').toDate()])

// 表单数据
const orderForm = ref({
  restaurantId: ''
})

// 表单验证规则
const rules = {
  restaurantId: [
    { required: true, message: '请选择食堂', trigger: 'change' }
  ]
}

// 用户信息
const userDepartmentId = ref(null)
const userRestaurantId = ref(null)

// ============ 计算属性 ============
const selectedRestaurant = computed(() => {
  return restaurants.value.find(r => r.id === selectedRestaurantId.value)
})

const selectedMealType = computed(() => {
  return mealTypes.value.find(mt => mt.id === selectedMealTypeId.value)
})

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

const currentYearMonth = computed(() => {
  return currentDate.value.format('YYYY年MM月')
})

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 日历天数计算 - 优化状态显示
const calendarDays = computed(() => {
  const year = currentDate.value.year()
  const month = currentDate.value.month()
  
  const firstDay = dayjs().year(year).month(month).date(1)
  const lastDay = firstDay.endOf('month')
  const startOfWeek = firstDay.startOf('week')
  const endOfWeek = lastDay.endOf('week')
  
  const days = []
  let current = startOfWeek
  
  while (current.isBefore(endOfWeek) || current.isSame(endOfWeek, 'day')) {
    const dateStr = current.format('YYYY-MM-DD')
    const dayOrders = getOrdersByDate(dateStr)
    
    // 检查是否有当前选中餐食类型的订单
    const hasCurrentMealTypeOrder = selectedMealTypeId.value && 
      dayOrders.some(order => order.mealTypeId === selectedMealTypeId.value)
    
    days.push({
      date: current,
      dateStr,
      dayOfMonth: current.date(),
      isCurrentMonth: current.month() === month,
      isToday: current.isSame(dayjs(), 'day'),
      hasOrder: dayOrders.length > 0,
      hasCurrentMealTypeOrder,
      orders: dayOrders,
      canOrder: canOrderOnDate(dateStr),
      isLocked: isDateLocked(dateStr)
    })
    
    current = current.add(1, 'day')
  }
  
  return days
})

const currentMonthOrders = computed(() => {
  const startOfMonth = currentDate.value.startOf('month')
  const endOfMonth = currentDate.value.endOf('month')
  
  return allOrders.value.filter(order => {
    const orderDate = dayjs(order.orderDate)
    return orderDate.isAfter(startOfMonth) && orderDate.isBefore(endOfMonth)
  })
})

const validOrdersCount = computed(() => {
  return currentMonthOrders.value.filter(order => order.status === 1).length
})

const currentMonthTotal = computed(() => {
  return currentMonthOrders.value
    .filter(order => order.status === 1)
    .reduce((sum, order) => sum + (order.price || 0), 0)
    .toFixed(2)
})

const upcomingOrdersCount = computed(() => {
  const today = dayjs().startOf('day')
  return allOrders.value.filter(order => {
    const orderDate = dayjs(order.orderDate)
    return order.status === 1 && (orderDate.isAfter(today) || orderDate.isSame(today, 'day'))
  }).length
})

// ============ 方法 ============
const getOrdersByDate = (dateStr) => {
  return allOrders.value.filter(order => order.orderDate === dateStr && order.status === 1)
}

const canOrderOnDate = (dateStr) => {
  const date = dayjs(dateStr)
  const today = dayjs().startOf('day')
  const tomorrow = today.add(1, 'day')
  
  // 只能预订明天及以后的日期
  if (date.isBefore(tomorrow)) {
    return false
  }
  
  // 检查是否已锁定（假设16:00后不能预订明天）
  if (date.isSame(tomorrow, 'day')) {
    const now = dayjs()
    const lockTime = dayjs().hour(16).minute(0).second(0)
    if (now.isAfter(lockTime)) {
      return false
    }
  }
  
  return true
}

const isDateLocked = (dateStr) => {
  const date = dayjs(dateStr)
  const today = dayjs().startOf('day')
  return date.isBefore(today)
}

const canCancelOrder = (orderDate) => {
  const date = dayjs(orderDate)
  const today = dayjs().startOf('day')
  return date.isAfter(today)
}

// 加载餐厅列表
const loadRestaurants = async () => {
  try {
    restaurantsLoading.value = true
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
    restaurantsLoading.value = false
  }
}

// 加载用户信息
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
        selectedRestaurantId.value = user.restaurantId
        orderForm.value.restaurantId = user.restaurantId
        console.log('loadUserInfo - set restaurantId:', user.restaurantId)
      }
    }
  } catch (error) {
    console.error('加载用户信息错误:', error)
  }
}

// 选择餐食类型 - 优化联动效果，确保立即刷新日历状态
const selectMealType = (mealType) => {
  if (!mealType.status) {
    ElMessage.warning('该餐食类型当前暂停供应')
    return
  }
  
  selectedMealTypeId.value = mealType.id
  
  // 保存到本地存储
  localStorage.setItem(STORAGE_KEYS.SELECTED_MEAL_TYPE, JSON.stringify({
    id: mealType.id,
    timestamp: Date.now()
  }))
  
  // 保存临时数据
  saveTempData()
  
  ElMessage.success({
    message: `已选择: ${mealType.name}，日历状态已更新`,
    duration: 2000,
    showClose: true
  })
}

// 处理日期点击 - 优化交互逻辑，确保即时响应
const handleDateClick = (day) => {
  selectedDate.value = day.dateStr
  
  // 立即保存临时数据
  saveTempData()
  
  if (day.hasCurrentMealTypeOrder) {
    // 已订当前选中的餐食类型，显示取消对话框
    const orders = getOrdersByDate(day.dateStr)
    if (orders.length > 0) {
      const targetOrder = orders.find(o => o.mealTypeId === selectedMealTypeId.value) || orders[0]
      
      if (canCancelOrder(day.dateStr)) {
        orderToCancel.value = targetOrder
        cancelOrderVisible.value = true
      } else {
        ElMessage.info({
          message: '该订单已过期，无法取消',
          duration: 2000
        })
      }
    }
  } else if (day.canOrder) {
    // 可以订餐，检查餐食类型选择状态
    if (!selectedMealTypeId.value) {
      ElMessage.warning({
        message: '请先选择餐食类型',
        duration: 2000,
        showClose: true
      })
      document.querySelector('.meal-type-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
      return
    }
    
    createOrderVisible.value = true
  }
}

// 确认创建订单 - 实现乐观更新
const confirmCreateOrder = async () => {
  if (!selectedRestaurantId.value) {
    ElMessage.warning({
      message: '请先选择食堂',
      duration: 2000
    })
    return
  }
  
  if (!selectedMealTypeId.value || !selectedDate.value) {
    ElMessage.warning({
      message: '请选择餐食类型和日期',
      duration: 2000
    })
    return
  }
  
  submitting.value = true
  
  try {
    // 保存当前状态用于回滚
    updateManager.saveState(allOrders.value)
    
    // 乐观更新：立即在前端添加订单
    const newOrder = {
      id: `temp_${Date.now()}`,
      userId: user.value?.id,
      restaurantId: selectedRestaurantId.value,
      restaurantName: selectedRestaurant.value?.name,
      mealTypeId: selectedMealTypeId.value,
      mealTypeName: selectedMealType.value?.name,
      price: selectedMealType.value?.price,
      orderDate: selectedDate.value,
      status: 1,
      createdAt: new Date().toISOString()
    }
    
    // 立即更新数据
    allOrders.value.push(newOrder)
    createOrderVisible.value = false
    ElMessage.success({
      message: '订餐成功！',
      duration: 2000,
      showClose: true
    })
    
    // 异步调用后端API
    const response = await axios.post('/api/order/create', {
      restaurantId: selectedRestaurantId.value,
      mealTypeId: selectedMealTypeId.value,
      orderDate: selectedDate.value
    })
    
    if (response.success) {
      // 后端成功，更新订单ID
      const index = allOrders.value.findIndex(o => o.id === newOrder.id)
      if (index !== -1) {
        allOrders.value[index].id = response.orderId || newOrder.id
      }
      updateManager.clearHistory()
      // 清除临时数据
      clearTempData()
    } else {
      // 后端失败，回滚状态
      const previousState = updateManager.restoreState()
      if (previousState) {
        allOrders.value = previousState
      }
      ElMessage.error({
        message: response.message || '订餐失败',
        duration: 3000,
        showClose: true
      })
    }
  } catch (error) {
    console.error('订餐失败:', error)
    // 网络错误，回滚状态
    const previousState = updateManager.restoreState()
    if (previousState) {
      allOrders.value = previousState
    }
    ElMessage.error({
      message: '订餐失败：' + (error.response?.data?.message || error.message || '网络错误'),
      duration: 3000,
      showClose: true
    })
  } finally {
    submitting.value = false
  }
}

// 取消订单
const cancelOrder = (order) => {
  orderToCancel.value = order
  cancelOrderVisible.value = true
}

// 确认取消订单 - 实现乐观更新
const confirmCancelOrder = async () => {
  if (!orderToCancel.value) return
  
  submitting.value = true
  
  try {
    // 保存当前状态用于回滚
    updateManager.saveState(allOrders.value)
    const orderId = orderToCancel.value.id
    
    // 乐观更新：立即在前端删除订单
    allOrders.value = allOrders.value.filter(o => o.id !== orderId)
    cancelOrderVisible.value = false
    ElMessage.success({
      message: '订单已取消',
      duration: 2000,
      showClose: true
    })
    
    // 异步调用后端API
    const response = await axios.delete(`/api/order/delete/${orderId}`)
    
    if (response.success) {
      // 后端成功
      orderToCancel.value = null
      updateManager.clearHistory()
    } else {
      // 后端失败，回滚状态
      const previousState = updateManager.restoreState()
      if (previousState) {
        allOrders.value = previousState
      }
      ElMessage.error({
        message: response.message || '取消失败',
        duration: 3000,
        showClose: true
      })
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    // 网络错误，回滚状态
    const previousState = updateManager.restoreState()
    if (previousState) {
      allOrders.value = previousState
    }
    ElMessage.error({
      message: '取消失败：' + (error.response?.data?.message || error.message || '网络错误'),
      duration: 3000,
      showClose: true
    })
  } finally {
    submitting.value = false
  }
}

const changeMonth = (delta) => {
  currentDate.value = currentDate.value.add(delta, 'month')
  saveTempData()
}

const goToToday = () => {
  currentDate.value = dayjs()
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  saveTempData()
}

// ============ 本地存储 ============
const saveTempData = () => {
  const tempData = {
    currentDate: currentDate.value.format('YYYY-MM-DD'),
    selectedDate: selectedDate.value,
    viewMode: viewMode.value,
    timestamp: Date.now()
  }
  localStorage.setItem(STORAGE_KEYS.TEMP_ORDER_DATA, JSON.stringify(tempData))
}

const loadTempData = () => {
  try {
    // 加载视图模式
    const savedViewMode = localStorage.getItem(STORAGE_KEYS.VIEW_MODE)
    if (savedViewMode) {
      viewMode.value = savedViewMode
    }
    
    // 加载选择的餐食类型
    const savedMealType = localStorage.getItem(STORAGE_KEYS.SELECTED_MEAL_TYPE)
    if (savedMealType) {
      const mealTypeData = JSON.parse(savedMealType)
      // 检查是否超过24小时，如果超过则清除
      if (Date.now() - mealTypeData.timestamp < 24 * 60 * 60 * 1000) {
        selectedMealTypeId.value = mealTypeData.id
      } else {
        localStorage.removeItem(STORAGE_KEYS.SELECTED_MEAL_TYPE)
      }
    }
    
    // 加载临时订单数据
    const tempData = localStorage.getItem(STORAGE_KEYS.TEMP_ORDER_DATA)
    if (tempData) {
      const data = JSON.parse(tempData)
      // 检查是否超过1小时
      if (Date.now() - data.timestamp < 60 * 60 * 1000) {
        if (data.currentDate) {
          currentDate.value = dayjs(data.currentDate)
        }
        if (data.selectedDate) {
          selectedDate.value = data.selectedDate
        }
        if (data.viewMode) {
          viewMode.value = data.viewMode
        }
      } else {
        localStorage.removeItem(STORAGE_KEYS.TEMP_ORDER_DATA)
      }
    }
  } catch (error) {
    console.error('加载临时数据失败:', error)
  }
}

const clearTempData = () => {
  localStorage.removeItem(STORAGE_KEYS.TEMP_ORDER_DATA)
}

// ============ 工具方法 ============
const getMealTypeColor = (mealTypeName) => {
  const colors = {
    '早餐': '#e6a23c',
    '午餐': '#67c23a',
    '晚餐': '#409eff',
    '夜宵': '#f56c6c'
  }
  return colors[mealTypeName] || '#909399'
}

const formatDateFull = (dateStr) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY年MM月DD日 dddd')
}

const formatDay = (dateStr) => {
  return dayjs(dateStr).format('MM-DD')
}

const getWeekday = (dateStr) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[dayjs(dateStr).day()]
}

// ============ 数据加载 ============
const loadMealTypes = async () => {
  mealTypesLoading.value = true
  try {
    const response = await axios.get('/api/order/meal-types')
    if (response.data?.success || response.success) {
      mealTypes.value = response.data?.data || response.data || []
    }
  } catch (error) {
    console.error('加载餐食类型失败:', error)
    ElMessage.error('加载餐食类型失败')
  } finally {
    mealTypesLoading.value = false
  }
}

const loadAllOrders = async () => {
  calendarLoading.value = true
  statsLoading.value = true
  try {
    // 计算日历显示的完整日期范围（包括前后月份）
    const year = currentDate.value.year()
    const month = currentDate.value.month()
    const firstDay = dayjs().year(year).month(month).date(1)
    const lastDay = firstDay.endOf('month')
    const startOfWeek = firstDay.startOf('week')
    const endOfWeek = lastDay.endOf('week')
    
    // 加载完整日历范围的订单数据
    const startDate = startOfWeek.format('YYYY-MM-DD')
    const endDate = endOfWeek.format('YYYY-MM-DD')
    
    const response = await axios.get('/api/order/list', {
      params: { startDate, endDate }
    })
    
    if (response.data?.success || response.success) {
      allOrders.value = response.data?.data || response.data || []
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单数据失败')
  } finally {
    calendarLoading.value = false
    statsLoading.value = false
  }
}

const loadOrderList = async () => {
  listLoading.value = true
  try {
    const params = {
      page: listPage.value,
      pageSize: listPageSize.value
    }
    
    if (listDateRange.value && listDateRange.value.length === 2) {
      params.startDate = dayjs(listDateRange.value[0]).format('YYYY-MM-DD')
      params.endDate = dayjs(listDateRange.value[1]).format('YYYY-MM-DD')
    }
    
    const response = await axios.get('/api/order/page', { params })
    
    if (response.data?.success || response.success) {
      orderList.value = response.data?.data || response.data || []
      listTotal.value = response.data?.total || response.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    listLoading.value = false
  }
}

// ============ 生命周期 ============
onMounted(() => {
  // 先加载临时数据
  loadTempData()
  
  // 加载用户信息和餐厅列表
  loadUserInfo()
  loadRestaurants()
  
  // 加载数据
  loadMealTypes()
  loadAllOrders()
  loadOrderList()
  
  // 如果没有选中日期，默认选中今天
  if (!selectedDate.value) {
    const today = dayjs()
    selectedDate.value = today.format('YYYY-MM-DD')
  }
})

// 组件卸载时清理资源
onUnmounted(() => {
  // 清理乐观更新历史记录
  updateManager.clearHistory()
})

// 监听月份变化，重新加载订单
watch(currentDate, () => {
  loadAllOrders()
})

// 监听餐食类型变化，确保日历实时更新
watch(selectedMealTypeId, () => {
  // 餐食类型变化时，日历会自动重新计算
  // 因为 calendarDays 计算属性依赖于 selectedMealTypeId
})

// 监听视图模式变化
watch(viewMode, (newMode) => {
  localStorage.setItem(STORAGE_KEYS.VIEW_MODE, newMode)
  saveTempData()
})
</script>

<style scoped>
.order-manage-container {
  padding: 15px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: nowrap;
  gap: 8px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 20px;
  color: #303133;
  flex-shrink: 0;
  min-width: fit-content;
  white-space: nowrap;
}

.view-toggle {
  flex-shrink: 0;
}

.view-toggle :deep(.el-radio-button) {
  padding: 5px 8px;
}

.view-toggle :deep(.el-radio-button__inner) {
  padding: 5px 10px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.btn-text {
  display: none;
}

@media (max-width: 768px) {
  .page-header {
    flex-wrap: nowrap;
    gap: 6px;
  }

  .page-title {
    font-size: 17px;
    gap: 4px;
  }

  .view-toggle :deep(.el-radio-button) {
    padding: 3px 6px;
  }

  .view-toggle :deep(.el-radio-button__inner) {
    padding: 3px 6px;
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .page-header {
    gap: 4px;
  }

  .page-title {
    font-size: 15px;
    gap: 3px;
  }

  .view-toggle :deep(.el-radio-button) {
    padding: 2px 5px;
  }

  .view-toggle :deep(.el-radio-button__inner) {
    padding: 2px 5px;
    font-size: 10px;
  }
}

@media (min-width: 768px) {
  .btn-text {
    display: inline;
  }
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 12px;
}

.stat-card {
  margin-bottom: 10px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.bg-blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.bg-green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.bg-orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.bg-purple { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 简单卡片头部 */
.card-header-simple {
  display: flex;
  align-items: center;
}

.header-title-simple {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 餐食类型选择 */
.meal-type-card {
  margin-bottom: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.restaurant-card {
  margin-bottom: 12px;
}

.restaurant-card :deep(.el-form-item) {
  margin-bottom: 12px;
}

.restaurant-card :deep(.el-form-item__label) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.restaurant-card :deep(.el-select) {
  height: 36px;
}

@media (max-width: 768px) {
  .restaurant-card :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  .restaurant-card :deep(.el-form-item__label) {
    padding-top: 6px;
    padding-bottom: 6px;
  }

  .restaurant-card :deep(.el-select) {
    height: 32px;
  }
}

.meal-type-card.has-selection {
  border-color: #67c23a;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.selected-tag {
  margin-left: 8px;
}

/* 餐食类型列表 */
.meal-type-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

@media (max-width: 768px) {
  .meal-type-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}

.meal-type-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: white;
  position: relative;
  overflow: hidden;
  min-height: 60px;
}

.meal-type-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background-color: transparent;
  transition: background-color 0.3s;
}

.meal-type-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.meal-type-item.active {
  border-color: #67c23a;
  background-color: #f0f9ff;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
}

.meal-type-item.active::before {
  background-color: #67c23a;
}

.meal-type-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.meal-type-item.disabled:hover {
  border-color: #e4e7ed;
  transform: none;
  box-shadow: none;
}

.meal-type-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  flex-shrink: 0;
}

.meal-type-info {
  flex: 1;
  min-width: 0;
}

.meal-type-name {
  font-weight: bold;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meal-type-price {
  font-size: 12px;
  color: #f56c6c;
  font-weight: bold;
}

.check-icon {
  font-size: 20px;
  color: #67c23a;
}

/* 日历样式 */
.calendar-card {
  margin-bottom: 12px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.month-navigation {
  display: flex;
  align-items: center;
  gap: 10px;
}

.current-month {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  min-width: 100px;
  text-align: center;
}

@media (max-width: 768px) {
  .current-month {
    font-size: 14px;
    min-width: 80px;
  }
}

.today-btn {
  margin-left: 5px;
}

.legend {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .legend {
    gap: 8px;
  }
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #606266;
}

@media (max-width: 768px) {
  .legend-item {
    font-size: 11px;
  }
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid transparent;
}

.legend-dot.ordered { 
  background-color: #67c23a; 
  border-color: #67c23a;
}

.legend-dot.can-order { 
  background-color: #409eff; 
  border-color: #409eff;
}

.legend-dot.locked { 
  background-color: #c0c4cc; 
  border-color: #c0c4cc;
}

.legend-text {
  font-weight: 500;
}

/* 自定义日历 */
.custom-calendar {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.weekday {
  padding: 10px 5px;
  text-align: center;
  font-weight: bold;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 768px) {
  .weekday {
    padding: 8px 3px;
    font-size: 12px;
  }
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #ebeef5;
}

.calendar-day {
  background-color: white;
  min-height: 75px;
  padding: 5px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .calendar-day {
    min-height: 55px;
    padding: 3px;
  }
}

.calendar-day:hover {
  background-color: #f5f7fa;
  transform: scale(1.02);
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .calendar-day:hover {
    transform: none;
    box-shadow: none;
  }
}

.calendar-day.other-month {
  background-color: #fafafa;
  color: #c0c4cc;
}

.calendar-day.today {
  background-color: #ecf5ff;
}

.calendar-day.today .day-number {
  color: #409eff;
  font-weight: bold;
  background-color: #409eff;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .calendar-day.today .day-number {
    width: 20px;
    height: 20px;
    font-size: 12px;
  }
}

/* 已订餐日期样式 - 强化视觉标识 */
.calendar-day.ordered {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-left: 4px solid #67c23a;
  border: 2px solid #67c23a;
}

/* 可订餐日期样式 - 更明显的视觉标识 */
.calendar-day.can-order {
  background-color: #fff;
  border: 2px dashed #409eff;
}

.calendar-day.can-order:hover {
  background-color: #ecf5ff;
  border-color: #409eff;
  border-style: solid;
}

@media (max-width: 768px) {
  .calendar-day.can-order:hover {
    border-style: dashed;
  }
}

.calendar-day.locked {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.calendar-day.locked:hover {
  transform: none;
  box-shadow: none;
}

.calendar-day.selected {
  background-color: #ecf5ff;
  box-shadow: inset 0 0 0 2px #409eff;
}

.day-number {
  font-size: 13px;
  margin-bottom: 4px;
  font-weight: 500;
  width: fit-content;
}

@media (max-width: 768px) {
  .day-number {
    font-size: 12px;
    margin-bottom: 2px;
  }
}

.day-orders {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
}

@media (max-width: 768px) {
  .day-orders {
    gap: 2px;
  }
}

.order-badge {
  font-size: 10px;
  padding: 3px 6px;
  border-radius: 4px;
  color: white;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
  transition: all 0.3s;
}

@media (max-width: 768px) {
  .order-badge {
    font-size: 9px;
    padding: 2px 4px;
  }
}

.order-badge.current-type {
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .order-badge.current-type {
    transform: none;
    box-shadow: none;
  }
}

.more-orders {
  font-size: 10px;
  color: #909399;
  text-align: center;
  font-weight: bold;
}

@media (max-width: 768px) {
  .more-orders {
    font-size: 9px;
  }
}

.day-status {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  gap: 4px;
}

.can-order-status {
  color: #409eff;
}

.locked-status {
  color: #c0c4cc;
}

.add-icon {
  font-size: 20px;
  color: #409eff;
}

@media (max-width: 768px) {
  .add-icon {
    font-size: 16px;
  }
}

.select-icon {
  font-size: 18px;
  color: #909399;
}

@media (max-width: 768px) {
  .select-icon {
    font-size: 14px;
  }
}

.lock-icon {
  font-size: 16px;
  color: #c0c4cc;
}

@media (max-width: 768px) {
  .lock-icon {
    font-size: 14px;
  }
}

.order-text {
  font-size: 10px;
  color: #409eff;
  font-weight: 500;
}

@media (max-width: 768px) {
  .order-text {
    font-size: 9px;
  }
}

.locked-text {
  font-size: 10px;
  color: #c0c4cc;
}

@media (max-width: 768px) {
  .locked-text {
    font-size: 9px;
  }
}

/* 日历提示 */
.calendar-tip {
  margin-top: 15px;
}

@media (max-width: 768px) {
  .calendar-tip {
    margin-top: 10px;
  }
}

/* 列表视图 */
.list-card {
  margin-bottom: 15px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

.date-cell {
  text-align: center;
}

.date-day {
  font-weight: bold;
  font-size: 13px;
}

@media (max-width: 768px) {
  .date-day {
    font-size: 12px;
  }
}

.date-week {
  font-size: 11px;
  color: #909399;
}

@media (max-width: 768px) {
  .date-week {
    font-size: 10px;
  }
}

.meal-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .meal-badge {
    padding: 3px 8px;
    font-size: 11px;
  }
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-wrapper {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .pagination-wrapper {
    justify-content: center;
  }
}

.cancelled-text {
  color: #f56c6c;
  font-size: 12px;
}

@media (max-width: 768px) {
  .cancelled-text {
    font-size: 11px;
  }
}

/* 对话框样式 */
.order-dialog :deep(.el-dialog__header) {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  padding: 15px 20px 10px;
}

.order-dialog :deep(.el-dialog__body) {
  padding: 15px 20px;
}

.order-dialog :deep(.el-dialog__footer) {
  padding: 10px 20px 15px;
}

.confirm-order-content,
.cancel-order-content {
  padding: 5px 0;
}

.confirm-icon {
  text-align: center;
  margin-bottom: 15px;
}

.confirm-icon.success {
  color: #67c23a;
  font-size: 48px;
}

.confirm-icon.warning {
  color: #e6a23c;
  font-size: 48px;
}

.confirm-info {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.confirm-date,
.confirm-meal {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.confirm-meal {
  margin-bottom: 0;
}

.meal-price {
  margin-left: auto;
  color: #f56c6c;
  font-weight: bold;
  font-size: 15px;
}

.confirm-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: bold;
  padding: 0 5px;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
}

.cancel-text {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
}

.cancel-detail {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.cancel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.cancel-item:last-child {
  margin-bottom: 0;
}

.cancel-item .label {
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
}

.cancel-item .value {
  color: #303133;
  font-weight: bold;
  font-size: 14px;
}

.cancel-item .value.price {
  color: #f56c6c;
  font-size: 15px;
}

.cancel-warning {
  margin-top: 8px;
}

.full-width-btn {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-manage-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: row;
    align-items: center;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .btn-text {
    display: none;
  }
  
  .order-dialog :deep(.el-dialog) {
    width: 90% !important;
    max-width: 340px;
  }
  
  .order-dialog :deep(.el-dialog__header) {
    padding: 12px 15px 8px;
    font-size: 16px;
  }
  
  .order-dialog :deep(.el-dialog__body) {
    padding: 12px 15px;
  }
  
  .order-dialog :deep(.el-dialog__footer) {
    padding: 8px 15px 12px;
  }
  
  .confirm-icon {
    margin-bottom: 12px;
  }
  
  .confirm-icon.success,
  .confirm-icon.warning {
    font-size: 40px;
  }
  
  .confirm-info {
    padding: 10px;
    margin-bottom: 10px;
  }
  
  .confirm-date,
  .confirm-meal {
    gap: 6px;
    margin-bottom: 6px;
    font-size: 13px;
  }
  
  .meal-price {
    font-size: 14px;
  }
  
  .confirm-total {
    font-size: 14px;
    padding: 0 3px;
  }
  
  .total-price {
    font-size: 18px;
  }
  
  .cancel-text {
    font-size: 13px;
    margin-bottom: 12px;
  }
  
  .cancel-detail {
    padding: 10px;
    margin-bottom: 10px;
  }
  
  .cancel-item {
    margin-bottom: 6px;
  }
  
  .cancel-item .label {
    gap: 4px;
    font-size: 12px;
  }
  
  .cancel-item .value {
    font-size: 13px;
  }
  
  .cancel-item .value.price {
    font-size: 14px;
  }
  
  .cancel-warning {
    margin-top: 6px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .meal-type-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  
  .meal-type-item {
    padding: 8px;
    gap: 6px;
    min-height: 55px;
  }
  
  .meal-type-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
    border-radius: 5px;
  }
  
  .meal-type-name {
    font-size: 12px;
  }
  
  .meal-type-price {
    font-size: 11px;
  }
  
  .check-icon {
    font-size: 18px;
  }
  
  .calendar-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .legend {
    width: 100%;
    justify-content: flex-start;
  }
  
  .calendar-day {
    min-height: 55px;
    padding: 3px;
  }
  
  .day-number {
    font-size: 11px;
  }
  
  .order-badge {
    font-size: 9px;
    padding: 2px 4px;
  }
  
  .weekday {
    padding: 8px 2px;
    font-size: 11px;
  }
  
  .stat-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .order-text,
  .locked-text {
    display: none;
  }
}

@media (max-width: 480px) {
  .order-dialog :deep(.el-dialog) {
    width: 95% !important;
    max-width: 320px;
  }
  
  .order-dialog :deep(.el-dialog__header) {
    padding: 10px 12px 6px;
    font-size: 15px;
  }
  
  .order-dialog :deep(.el-dialog__body) {
    padding: 10px 12px;
  }
  
  .order-dialog :deep(.el-dialog__footer) {
    padding: 6px 12px 10px;
  }
  
  .confirm-icon {
    margin-bottom: 10px;
  }
  
  .confirm-icon.success,
  .confirm-icon.warning {
    font-size: 36px;
  }
  
  .confirm-info {
    padding: 8px;
    margin-bottom: 8px;
  }
  
  .confirm-date,
  .confirm-meal {
    gap: 5px;
    margin-bottom: 5px;
    font-size: 12px;
  }
  
  .meal-price {
    font-size: 13px;
  }
  
  .confirm-total {
    font-size: 13px;
    padding: 0 2px;
  }
  
  .total-price {
    font-size: 16px;
  }
  
  .cancel-text {
    font-size: 12px;
    margin-bottom: 10px;
  }
  
  .cancel-detail {
    padding: 8px;
    margin-bottom: 8px;
  }
  
  .cancel-item {
    margin-bottom: 5px;
  }
  
  .cancel-item .label {
    gap: 3px;
    font-size: 11px;
  }
  
  .cancel-item .value {
    font-size: 12px;
  }
  
  .cancel-item .value.price {
    font-size: 13px;
  }
  
  .cancel-warning {
    margin-top: 5px;
  }
  
  .meal-type-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  
  .meal-type-item {
    padding: 6px;
    gap: 5px;
    min-height: 50px;
  }
  
  .meal-type-icon {
    width: 28px;
    height: 28px;
    font-size: 14px;
    border-radius: 4px;
  }
  
  .meal-type-name {
    font-size: 11px;
  }
  
  .meal-type-price {
    font-size: 10px;
  }
  
  .check-icon {
    font-size: 16px;
  }
  
  .calendar-day {
    min-height: 55px;
  }
  
  .day-status {
    height: auto;
  }
  
  .add-icon {
    font-size: 16px;
  }
  
  .legend-item {
    font-size: 11px;
  }
}

/* 动画效果 */
.calendar-day {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.meal-type-item {
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 脉冲动画 - 用于提示 */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
}

/* 全宽按钮样式 */
.full-width-btn {
  width: 100%;
}

.calendar-day.can-order:hover {
  animation: pulse 1s infinite;
}
</style>
