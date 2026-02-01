import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/employee',
    name: 'Employee',
    component: () => import('../views/employee/Layout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['employee']
    },
    children: [
      {
        path: 'order',
        name: 'EmployeeOrder',
        component: () => import('../views/employee/Order.vue'),
        meta: {
          requiresAuth: true,
          roles: ['employee']
        }
      },
      {
        path: 'order-history',
        name: 'EmployeeOrderHistory',
        component: () => import('../views/employee/OrderHistory.vue'),
        meta: {
          requiresAuth: true,
          roles: ['employee']
        }
      }
    ]
  },
  {
    path: '/chef',
    name: 'Chef',
    component: () => import('../views/chef/Layout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['chef']
    },
    children: [
      {
        path: 'order-status',
        name: 'ChefOrderStatus',
        component: () => import('../views/chef/OrderStatus.vue'),
        meta: {
          requiresAuth: true,
          roles: ['chef']
        }
      },
      {
        path: 'statistics',
        name: 'ChefStatistics',
        component: () => import('../views/chef/Statistics.vue'),
        meta: {
          requiresAuth: true,
          roles: ['chef']
        }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['admin']
    },
    children: [
      {
        path: 'system-config',
        name: 'AdminSystemConfig',
        component: () => import('../views/admin/SystemConfig.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'meal-type',
        name: 'AdminMealType',
        component: () => import('../views/admin/MealType.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'employee',
        name: 'AdminEmployee',
        component: () => import('../views/admin/Employee.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'department',
        name: 'AdminDepartment',
        component: () => import('../views/admin/Department.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'role',
        name: 'AdminRole',
        component: () => import('../views/admin/Role.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'operation-log',
        name: 'AdminOperationLog',
        component: () => import('../views/admin/OperationLog.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      },
      {
        path: 'order-manage',
        name: 'AdminOrderManage',
        component: () => import('../views/admin/OrderManage.vue'),
        meta: {
          requiresAuth: true,
          roles: ['admin']
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      requiresAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('开始路由守卫检查，目标路径:', to.path)
  const token = localStorage.getItem('token')
  console.log('获取的token:', token)
  let user = {}
  try {
    const userStr = localStorage.getItem('user')
    console.log('获取的user字符串:', userStr)
    // 检查userStr是否为字符串'undefined'
    if (userStr === 'undefined') {
      user = {}
    } else {
      user = JSON.parse(userStr || '{}')
    }
    console.log('获取的user:', user)
    console.log('用户角色:', user.role)
  } catch (error) {
    user = {}
    console.error('解析user失败:', error)
  }
  
  if (to.meta.requiresAuth) {
    console.log('目标路径需要认证')
    if (!token) {
      console.log('没有token，跳转到登录页面')
      next('/login')
    } else if (to.meta.roles && !to.meta.roles.includes(user.role)) {
      console.log('用户角色不匹配，目标路径需要角色:', to.meta.roles, '，用户角色:', user.role)
      next('/login')
    } else {
      console.log('认证通过，允许访问')
      next()
    }
  } else {
    console.log('目标路径不需要认证，允许访问')
    next()
  }
})

export default router
