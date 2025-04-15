<template>
  <div class="app-container">
    <!-- 未登录时显示登录页面 -->
    <login-page
      v-if="!isLoggedIn"
      @login-success="handleLoginSuccess"
    />
    
    <!-- 登录后显示主页面 -->
    <el-container v-else class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-title">医院药品管理系统</div>
        <div class="user-info">
          <span>{{ userInfo.username }}</span>
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
            <el-menu-item index="drug-management" @click="activeMenu = 'drug-management'">
              <el-icon><Box /></el-icon>
              <span>药品管理</span>
            </el-menu-item>
            <el-menu-item index="drug-statistics" @click="activeMenu = 'drug-statistics'">
              <el-icon><DataAnalysis /></el-icon>
              <span>药品统计</span>
            </el-menu-item>
            <el-menu-item index="drug-chart" @click="activeMenu = 'drug-chart'">
              <el-icon><TrendCharts /></el-icon>
              <span>库存走势</span>
            </el-menu-item>
            <el-menu-item index="operation-log" @click="activeMenu = 'operation-log'">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="user-management" @click="activeMenu = 'user-management'" v-if="userInfo.role === 'ADMIN'">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="system-settings" @click="activeMenu = 'system-settings'" v-if="userInfo.role === 'ADMIN'">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区 -->
        <el-main class="app-main">
          <!-- 不同页面组件 -->
          <keep-alive>
            <drug-management v-if="activeMenu === 'drug-management'" />
            <user-management v-else-if="activeMenu === 'user-management'" />
            <drug-statistics v-else-if="activeMenu === 'drug-statistics'" />
            <drug-chart v-else-if="activeMenu === 'drug-chart'" />
            <operation-log v-else-if="activeMenu === 'operation-log'" />
            <system-settings v-else-if="activeMenu === 'system-settings'" />
          </keep-alive>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled, User, Box, DataAnalysis, TrendCharts, Document, Setting } from '@element-plus/icons-vue'
import LoginPage from './LoginPage.vue'
import DrugManagement from './DrugManagement.vue'
import UserManagement from './UserManagement.vue'
import DrugStatistics from './DrugStatistics.vue'
import DrugChart from './DrugChart.vue'
import OperationLog from './OperationLog.vue'
import SystemSettings from './SystemSettings.vue'
import axios from 'axios'

// 登录状态
const isLoggedIn = ref(false)
const userInfo = reactive({
  id: null,
  username: '',
  role: ''
})

// 当前激活的菜单
const activeMenu = ref('drug-management')

// 页面加载时检查用户是否已登录
onMounted(() => {
  checkLoginStatus()
})

// 检查登录状态
const checkLoginStatus = async () => {
  const userStr = localStorage.getItem('user')
  const token = localStorage.getItem('token')
  
  // 只有当用户信息和令牌都存在时才认为登录有效
  if (userStr && token) {
    try {
      const user = JSON.parse(userStr)
      userInfo.id = user.id
      userInfo.username = user.username
      userInfo.role = user.role
      
      // 尝试验证令牌有效性
      try {
        await axios.get('/auth/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        isLoggedIn.value = true
      } catch (error) {
        console.error('令牌验证失败:', error)
        // 清除无效的存储数据
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        ElMessage.error('登录已过期，请重新登录')
      }
    } catch (e) {
      console.error('解析用户数据失败', e)
      // 清除无效的存储数据
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    }
  } else {
    // 如果用户信息和令牌不同时存在，清除所有存储数据
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }
}

// 登录成功处理
const handleLoginSuccess = (user) => {
  userInfo.id = user.id
  userInfo.username = user.username
  userInfo.role = user.role
  isLoggedIn.value = true
}

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
      // 重置状态
      isLoggedIn.value = false
      userInfo.id = null
      userInfo.username = ''
      userInfo.role = ''
      ElMessage.success('已退出登录')
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
.app-container {
  height: 100vh;
  width: 100%;
}

.main-container {
  height: 100%;
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