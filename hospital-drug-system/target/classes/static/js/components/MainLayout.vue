<template>
  <el-container class="main-container">
    <!-- 顶部导航栏 -->
    <el-header class="app-header">
      <div class="header-title">医院药品管理系统</div>
      <div class="user-info">
        <span>{{ userInfo.username }}</span>
        <span class="user-role">({{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }})</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-avatar :size="32" :icon="UserFilled" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 内容区域 -->
    <el-container>
      <!-- 侧边菜单 -->
      <el-aside width="200px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="el-menu-vertical"
        >
          <el-menu-item index="/drug-management">
            <el-icon><Box /></el-icon>
            <span>药品管理</span>
          </el-menu-item>
          <el-menu-item index="/drug-statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>药品统计</span>
          </el-menu-item>
          <el-menu-item index="/drug-chart">
            <el-icon><TrendCharts /></el-icon>
            <span>库存走势</span>
          </el-menu-item>
          <el-menu-item index="/operation-log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/user-management" v-if="userInfo.role === 'ADMIN'">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system-settings" v-if="userInfo.role === 'ADMIN'">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="app-main">
        <!-- 路由视图 -->
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, onActivated, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled, User, Box, DataAnalysis, TrendCharts, Document, Setting } from '@element-plus/icons-vue'
import axios from 'axios'

// 获取router和route实例
const router = useRouter()
const route = useRoute()

// 用户信息
const userInfo = reactive({
  id: null,
  username: '',
  role: ''
})

// 当前激活的菜单，基于当前路由路径
const activeMenu = computed(() => route.path)

// 当前用户数据的版本，用于判断是否需要更新
const userInfoVersion = ref(0)

// 加载用户信息
const loadUserInfo = () => {
  console.log('执行loadUserInfo函数...')
  const userStr = localStorage.getItem('user')
  console.log('从localStorage获取的原始数据:', userStr)
  
  // 保存上一次的用户信息以便对比
  const oldUsername = userInfo.username
  const oldRole = userInfo.role
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      console.log('解析后的用户信息:', user)
      
      // 重置用户信息
      userInfo.id = null
      userInfo.username = ''
      userInfo.role = ''
      
      // 设置新数据 - 确保防止undefined或null值
      userInfo.id = user.id || null
      userInfo.username = user.username || '未知用户'
      userInfo.role = user.role || 'USER'
      
      // 检测是否发生变化
      if (oldUsername !== userInfo.username || oldRole !== userInfo.role) {
        console.log('用户信息已变化!', oldUsername, '->', userInfo.username)
        userInfoVersion.value++ // 增加版本号，触发依赖于它的computed属性重新计算
        
        // 如果是普通用户尝试访问管理员页面，重定向到药品管理页面
        if (userInfo.role !== 'ADMIN' && 
            (route.path === '/user-management' || route.path === '/system-settings')) {
          console.warn('无权限访问:', route.path)
          router.push('/drug-management')
        }
      }
      
      console.log('设置后的userInfo状态:', JSON.stringify(userInfo))
    } catch (e) {
      console.error('解析用户数据失败', e)
      router.push('/login')
    }
  } else {
    console.warn('未找到用户数据，重定向到登录页')
    router.push('/login')
  }
}

// 添加一个方法来强制重新加载用户信息
const forceReloadUserInfo = () => {
  console.log('强制重新加载用户信息')
  loadUserInfo()
}

// 监听路由变化，主动检查用户信息
watch(() => route.path, (newPath, oldPath) => {
  console.log('路由发生变化:', oldPath, '->', newPath)
  loadUserInfo()
})

// 页面加载时获取用户信息
onMounted(() => {
  console.log('MainLayout组件已挂载，正在加载用户信息...')
  loadUserInfo()
  
  // 增加事件监听，在localStorage变化时重新加载
  window.addEventListener('storage', (event) => {
    if (event.key === 'user') {
      console.log('检测到localStorage中user数据变化')
      loadUserInfo()
    }
  })
  
  // 每1秒检查一次用户信息是否有变化（比原来的5秒更频繁）
  const intervalId = setInterval(loadUserInfo, 1000)
  
  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(intervalId)
  })
})

// 组件激活时也检查用户信息
onActivated(() => {
  console.log('MainLayout组件激活，检查用户信息')
  loadUserInfo()
})

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 清除本地存储
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    ElMessageBox.alert(`用户ID: ${userInfo.id}<br>用户名: ${userInfo.username}<br>角色: ${userInfo.role}`, '用户信息', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    })
  }
}
</script>

<style scoped>
.main-container {
  height: 100vh;
}

.app-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
  position: relative;
  z-index: 10;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
}

.app-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style> 