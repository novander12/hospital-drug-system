import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import { ElMessage } from 'element-plus'

// 引入全局样式
import '../css/global.css'

// 引入页面组件
import DrugManagement from './DrugManagement.vue'
import UserManagement from './UserManagement.vue'
import DrugStatistics from './DrugStatistics.vue'
import DrugChart from './DrugChart.vue'
import OperationLog from './OperationLog.vue'
import SystemSettings from './SystemSettings.vue'
import LoginPage from './LoginPage.vue'
import MainLayout from './components/MainLayout.vue'

// 配置路由
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: LoginPage
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: 'drug-management',
          name: 'DrugManagement',
          component: DrugManagement
        },
        {
          path: 'user-management',
          name: 'UserManagement',
          component: UserManagement
        },
        {
          path: 'drug-statistics',
          name: 'DrugStatistics',
          component: DrugStatistics,
          alias: 'statistics' // 别名，兼容之前的路径
        },
        {
          path: 'drug-chart',
          name: 'DrugChart',
          component: DrugChart
        },
        {
          path: 'operation-log',
          name: 'OperationLog',
          component: OperationLog
        },
        {
          path: 'system-settings',
          name: 'SystemSettings',
          component: SystemSettings
        },
        // 默认路由，重定向到药品管理页面
        {
          path: '',
          redirect: '/drug-management'
        }
      ]
    }
  ]
})

// 将以下代码替换main.js中的路由守卫部分

// 路由守卫，处理未登录用户的访问控制
router.beforeEach((to, from, next) => {
  console.log('路由守卫触发:', from.path, '->', to.path, '查询参数:', to.query)
  const token = localStorage.getItem('token')
  
  // 检查是否需要强制刷新用户信息
  const needsRefresh = to.query.refresh === 'true'
  if (needsRefresh && to.query._t) {
    console.log('检测到refresh参数，将在导航后刷新用户信息')
    // 移除refresh参数，防止刷新循环
    const query = {...to.query}
    delete query.refresh
    delete query._t
    
    // 如果没有其他参数，则完全移除query
    if (Object.keys(query).length === 0) {
      next({...to, query: {}})
      return
    } else {
      next({...to, query})
      return
    }
  }
  
  // 未登录时，只能访问登录页
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  
  // 已登录但访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    next('/')
    return
  }
  
  // 检查用户角色权限
  if (token && (to.path === '/user-management' || to.path === '/system-settings')) {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (userInfo.role !== 'ADMIN') {
      // 非管理员用户尝试访问管理员页面，重定向到首页
      console.warn('无权限访问:', to.path)
      ElMessage.warning('您没有权限访问该页面')
      next('/drug-management')
      return
    }
  }
  
  next()
})

// 配置axios
axios.defaults.baseURL = window.location.origin
axios.defaults.headers.common['Content-Type'] = 'application/json'

// 添加请求拦截器，设置用户信息到请求头
axios.interceptors.request.use(config => {
  // 从localStorage获取JWT令牌
  const token = localStorage.getItem('token')
  if (token) {
    // 将JWT令牌添加到Authorization请求头
    config.headers['Authorization'] = 'Bearer ' + token
  }
  
  return config
})

// 添加响应拦截器处理401错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // 清除存储的token
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 重定向到登录页
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

// 创建应用实例
const app = createApp(App)

// 注册Element Plus
app.use(ElementPlus, {
  locale: zhCn
})

// 注册Vue Router
app.use(router)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载应用
app.mount('#app') 