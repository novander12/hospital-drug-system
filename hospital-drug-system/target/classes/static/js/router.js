import { createRouter, createWebHistory } from 'vue-router'

// 导入组件
import Login from './Login.vue'
import Register from './Register.vue'
import Dashboard from './Dashboard.vue'
import DrugManagement from './DrugManagement.vue'
import OperationLog from './OperationLog.vue'
import UserManagement from './UserManagement.vue'
import DrugInventory from './DrugInventory.vue'
import Prescription from './Prescription.vue'
import UserProfile from './UserProfile.vue'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/drugs',
    name: 'DrugManagement',
    component: DrugManagement,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/logs',
    name: 'OperationLog',
    component: OperationLog,
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: UserManagement,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/inventory',
    name: 'DrugInventory',
    component: DrugInventory,
    meta: { requiresAuth: true }
  },
  {
    path: '/prescriptions',
    name: 'Prescription',
    component: Prescription,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  
  // 获取用户信息和令牌
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let user = null
  
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      console.error('解析用户数据失败', e)
    }
  }
  
  // 判断是否需要认证
  if (requiresAuth && !token) {
    // 需要认证但没有令牌，重定向到登录页
    next({ name: 'Login' })
  } else if (requiresAdmin && (!user || user.role.toUpperCase() !== 'ADMIN')) {
    // 需要管理员权限但用户不是管理员，重定向到控制台
    next({ name: 'Dashboard' })
  } else {
    // 不需要认证或已经有令牌，继续导航
    next()
  }
})

export default router 