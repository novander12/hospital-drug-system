import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'
import App from './App.vue'

// 引入全局样式
import '../css/global.css'

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
      // 重定向到登录页（通过刷新）
      window.location.reload()
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

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载应用
app.mount('#app') 