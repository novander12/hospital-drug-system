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
import OperationLog from './OperationLog.vue'
import SystemSettings from './SystemSettings.vue'
import LoginPage from './LoginPage.vue'
import MainLayout from './components/MainLayout.vue'
import PrescriptionList from './PrescriptionList.vue'
import UserSettings from './UserSettings.vue'
import InventoryReport from './InventoryReport.vue'
import DrugConsumptionReport from './DrugConsumptionReport.vue'

// 配置路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/drug-management',
    meta: { requiresAuth: true },
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
        component: DrugStatistics
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
      {
        path: 'prescriptions',
        name: 'PrescriptionList',
        component: PrescriptionList
      },
      {
        path: 'user-settings',
        name: 'UserSettings',
        component: UserSettings
      },
      {
        path: 'inventory-report',
        name: 'InventoryReport',
        component: InventoryReport
      },
      {
        path: 'drug-consumption-report',
        name: 'DrugConsumptionReport',
        component: DrugConsumptionReport
      },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: to => {
      const token = localStorage.getItem('token');
      return token ? '/' : '/login';
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，处理未登录用户的访问控制
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  console.log(`Navigating to: ${to.path}, Requires Auth: ${requiresAuth}`);

  if (requiresAuth && !token) {
    console.log('Redirecting to login (requires auth, no token)');
    next('/login');
  } else if (to.path === '/login' && token) {
    console.log('Redirecting to home (already logged in)');
    next('/');
  } else {
    console.log('Proceeding with navigation');
    next();
  }
})

// 配置axios
axios.defaults.baseURL = 'http://localhost:8082'
axios.defaults.headers.common['Content-Type'] = 'application/json'

// 添加请求拦截器，设置用户信息到请求头
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 添加响应拦截器处理401错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login').catch(err => {
         if (err.name !== 'NavigationDuplicated') {
            console.error('Router push error:', err);
         }
      });
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