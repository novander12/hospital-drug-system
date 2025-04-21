<template>
  <div id="app">
    <el-container v-if="isLoggedIn">
      <el-aside width="200px">
        <el-menu
          default-active="dashboard"
          class="el-menu-vertical"
          :router="true"
          :collapse="isCollapse"
        >
          <div class="logo-container">
            <img src="./assets/logo.png" class="logo" alt="Logo" />
            <span v-if="!isCollapse">医院药品系统</span>
          </div>
          
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          
          <!-- 管理员菜单项 -->
          <template v-if="isAdmin">
            <el-sub-menu index="drug-management">
              <template #title>
                <el-icon><Medicine /></el-icon>
                <span>药品管理</span>
              </template>
              <el-menu-item index="/drugs">药品列表</el-menu-item>
              <el-menu-item index="/categories">分类管理</el-menu-item>
              <el-menu-item index="/suppliers">供应商管理</el-menu-item>
            </el-sub-menu>
            
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </template>
          
          <!-- 药师/护士菜单项 -->
          <template v-else>
            <el-menu-item index="/inventory">
              <el-icon><List /></el-icon>
              <span>药品库存查询</span>
            </el-menu-item>
            
            <el-menu-item index="/prescriptions">
              <el-icon><Document /></el-icon>
              <span>处方管理</span>
            </el-menu-item>
          </template>
          
          <!-- 通用菜单项 -->
          <el-menu-item index="/logs">
            <el-icon><InfoFilled /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          
          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-left">
            <el-button
              :icon="isCollapse ? 'Expand' : 'Fold'"
              @click="toggleCollapse"
              circle
            />
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRoute }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
        
        <el-footer>
          © {{ new Date().getFullYear() }} 医院药品管理系统 | 版权所有
        </el-footer>
      </el-container>
    </el-container>
    
    <router-view v-else />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  User,
  Setting,
  List,
  Document,
  Medicine,
  InfoFilled,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
const username = ref('')
const role = ref('')

// 判断是否已登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 判断是否为管理员
const isAdmin = computed(() => {
  return role.value && role.value.toUpperCase() === 'ADMIN'
})

// 获取当前路由名称
const currentRoute = computed(() => {
  const routeMap = {
    'dashboard': '控制台',
    'drugs': '药品管理',
    'categories': '分类管理',
    'suppliers': '供应商管理',
    'users': '用户管理',
    'logs': '操作日志',
    'inventory': '药品库存查询',
    'prescriptions': '处方管理',
    'profile': '个人信息'
  }
  
  const currentPath = route.path.split('/')[1]
  return routeMap[currentPath] || '未知页面'
})

// 监听路由变化
watch(() => route.path, (newPath) => {
  console.log('路由变化:', newPath)
})

// 组件挂载
onMounted(() => {
  loadUserInfo()
  
  // 添加存储变化监听
  window.addEventListener('storage', handleStorageChange)
})

// 处理本地存储变化
const handleStorageChange = (e) => {
  if (e.key === 'user' || e.key === 'token') {
    loadUserInfo()
  }
}

// 加载用户信息
const loadUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      username.value = user.username || ''
      role.value = user.role || ''
    } catch (e) {
      console.error('解析用户数据失败', e)
      username.value = ''
      role.value = ''
    }
  } else {
    username.value = ''
    role.value = ''
  }
}

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除本地存储的令牌和用户信息
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    // 提示用户已登出
    ElMessage.success('已成功退出登录')
    
    // 重定向到登录页
    router.push('/login')
  }).catch(() => {
    // 用户取消操作
  })
}
</script>

<style scoped>
#app {
  font-family: 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  display: flex;
}

.el-container {
  height: 100%;
  width: 100%;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  transition: width 0.3s;
}

.el-menu {
  border-right: none;
  height: 100%;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.el-footer {
  background-color: #fff;
  color: #909399;
  text-align: center;
  line-height: 60px;
  font-size: 12px;
  border-top: 1px solid #e6e6e6;
}

.logo-container {
  height: 60px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.logo {
  height: 30px;
  margin-right: 10px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left .el-button {
  margin-right: 15px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.user-dropdown:hover {
  color: #409EFF;
}
</style>