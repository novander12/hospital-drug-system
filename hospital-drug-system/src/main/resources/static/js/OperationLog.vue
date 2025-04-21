<template>
  <div class="operation-log">
    <h1 class="page-title">操作日志</h1>
    
    <!-- 操作区域 -->
    <div class="operation-area">
      <el-radio-group v-model="logType" @change="handleLogTypeChange">
        <el-radio-button label="all" v-if="isAdmin">所有日志</el-radio-button>
        <el-radio-button label="my">我的日志</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 日志表格 -->
    <el-table
      v-loading="loading"
      :data="paginatedLogs"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作用户" min-width="120" />
      <el-table-column label="操作类型" min-width="120">
        <template #default="scope">
          <el-tag
            :type="getActionTagType(scope.row.action)"
          >
            {{ getActionText(scope.row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="drugName" label="药品名称" min-width="150" />
      <el-table-column prop="details" label="操作详情" min-width="200" />
      <el-table-column label="操作时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.timestamp) }}
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 数据列表
const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logType = ref('my') // 默认显示"我的日志"
const lastStoredUser = ref('') // 存储上一次解析的用户数据，用于检测变化
const checkUserInfoInterval = ref(null) // 使用ref存储定时器ID

// 获取当前用户信息和角色
const userInfo = reactive({
  username: '',
  role: ''
})

// 是否为管理员
const isAdmin = computed(() => {
  // 大小写不敏感的管理员角色判断
  return userInfo.role && userInfo.role.toUpperCase() === 'ADMIN'
})

// 监听用户角色变化，自动调整日志类型
watch(() => userInfo.role, (newRole) => {
  console.log('用户角色发生变化，新角色:', newRole)
  if (newRole && newRole.toUpperCase() === 'ADMIN') {
    logType.value = 'all'
    console.log('角色变为管理员，设置为查看所有日志')
  } else {
    logType.value = 'my'
    console.log('角色变为非管理员，只能查看个人日志')
  }
}, { immediate: true })

// 监听日志类型变化，确保权限一致性
watch(logType, (newLogType) => {
  console.log('日志类型变更为:', newLogType)
  // 确保非管理员不能查看所有日志
  if (newLogType === 'all' && !isAdmin.value) {
    console.log('检测到非管理员尝试查看所有日志，自动切换为个人日志')
    setTimeout(() => {
      logType.value = 'my'
      ElMessage.warning('权限不足，只能查看自己的操作日志')
    }, 0)
  }
})

// 初始化获取用户信息和数据
onMounted(async () => {
  console.log('操作日志组件已挂载')
  
  // 先加载用户信息
  await loadUserInfo()
  
  // 再获取日志数据
  fetchLogs()
  
  // 添加localStorage变化监听
  window.addEventListener('storage', checkUserChange)
  
  // 设置定时检查用户信息变化
  checkUserInfoInterval.value = setInterval(checkUserInfoChange, 2000)
})

// 在组件卸载前移除事件监听器和清除定时器
onBeforeUnmount(() => {
  console.log('操作日志组件即将卸载，清理资源')
  window.removeEventListener('storage', checkUserChange)
  if (checkUserInfoInterval.value) {
    clearInterval(checkUserInfoInterval.value)
    checkUserInfoInterval.value = null
  }
})

// 检查用户信息是否变化
const checkUserChange = async (e) => {
  if (e.key === 'user' || e.key === 'token') {
    console.log('检测到存储变化:', e.key, '重新加载用户信息')
    
    // 先重置状态
    currentPage.value = 1
    
    // 重新加载用户信息并等待完成
    await loadUserInfo()
    
    // 确保根据用户角色设置正确的日志类型
    if (isAdmin.value) {
      if (logType.value !== 'all') {
        logType.value = 'all'
        console.log('storage事件: 是管理员但日志类型不是all，已更正')
      }
    } else {
      if (logType.value !== 'my') {
        logType.value = 'my'
        console.log('storage事件: 不是管理员但日志类型不是my，已更正')
      }
    }
    
    // 额外执行权限守卫
    guardPermissions()
    
    // 获取日志数据
    fetchLogs()
  }
}

// 定时检查用户信息变化
const checkUserInfoChange = async () => {
  const currentUser = localStorage.getItem('user')
  if (currentUser !== lastStoredUser.value) {
    console.log('用户信息已变更，重新加载数据')
    
    // 先重置界面状态
    currentPage.value = 1
    
    // 重新加载用户信息并等待完成
    await loadUserInfo()
    
    // 确保根据新的用户角色设置正确的日志类型
    console.log('用户切换后，再次确认角色:', userInfo.role, '管理员状态:', isAdmin.value)
    
    // 强制再次检查权限和日志类型
    if (isAdmin.value) {
      if (logType.value !== 'all') {
        logType.value = 'all'
        console.log('是管理员但日志类型不是all，已更正')
      }
    } else {
      if (logType.value !== 'my') {
        logType.value = 'my'
        console.log('不是管理员但日志类型不是my，已更正')
      }
    }
    
    // 额外执行权限守卫
    guardPermissions()
    
    // 最后重新获取日志数据
    fetchLogs()
  }
}

// 获取日志列表
const fetchLogs = async (retryCount = 0) => {
  // 在获取日志前先执行权限守卫
  guardPermissions()
  
  loading.value = true
  try {
    // 再次检查用户权限，确保非管理员无法查看所有日志
    if (!isAdmin.value && logType.value === 'all') {
      console.log('fetchLogs检测到非管理员尝试查看所有日志，强制切换为个人日志')
      logType.value = 'my'
      ElMessage.warning('权限不足，只能查看自己的操作日志')
    }
    
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      logs.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    const endpoint = logType.value === 'my' ? '/api/logs/my' : '/api/logs'
    
    console.log('获取日志类型:', logType.value, '请求端点:', endpoint, '当前用户角色:', userInfo.role)
    
    const response = await axios.get(endpoint, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      timeout: 10000 // 设置超时时间为10秒
    })
    
    if (Array.isArray(response.data)) {
      logs.value = response.data
      total.value = response.data.length
      
      // 如果数据为空，显示提示信息
      if (logs.value.length === 0) {
        ElMessage.info('没有查询到操作日志数据')
      }
    } else {
      console.warn('服务器返回的日志数据不是数组格式:', response.data)
      logs.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取操作日志失败:', error)
    
    // 检查是否是认证错误
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
      // 清空日志并重置状态
      logs.value = []
      total.value = 0
      logType.value = 'my'  // 强制重置为"我的日志"
    } 
    // 检查是否是网络错误，并进行重试
    else if ((error.code === 'ECONNABORTED' || !error.response) && retryCount < 2) {
      ElMessage.warning('网络请求超时，正在重试...')
      setTimeout(() => {
        fetchLogs(retryCount + 1)
      }, 1000 * (retryCount + 1)) // 逐次增加重试延迟
      return
    } 
    else {
      ElMessage.error(error.response?.data?.message || '获取操作日志失败')
    }
    
    logs.value = []
    total.value = 0
    loading.value = false
  }
}

// 监听路由或其他导航事件
// 这里假设你可能有一个全局事件总线或路由变化可以监听
// 如果你的项目中有Vue Router，可以通过router.afterEach等钩子监听
// 这里使用一个模拟的监听方法
const setupRouteListener = () => {
  // 这里可以监听路由变化或全局登录状态变化事件
  // 例如: router.afterEach(() => { loadUserInfo(); fetchLogs(); })
  // 或者使用全局事件总线: eventBus.on('login-changed', () => {...})
}

// 处理日志类型变化
const handleLogTypeChange = () => {
  // 确保非管理员不能查看所有日志
  if (!isAdmin.value && logType.value === 'all') {
    logType.value = 'my'
    ElMessage.warning('权限不足，只能查看自己的操作日志')
    return
  }
  
  currentPage.value = 1
  fetchLogs()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
  console.log(`切换到第${page}页，显示数据范围: ${(page-1)*pageSize.value+1}-${Math.min(page*pageSize.value, total.value)}`)
  // 使用paginatedLogs计算属性自动处理分页，无需额外操作
}

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return ''
  
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取操作类型对应的标签类型
const getActionTagType = (action) => {
  switch (action) {
    case 'ADD':
      return 'success'
    case 'UPDATE':
      return 'warning'
    case 'DELETE':
      return 'danger'
    case 'BATCH_DELETE':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取操作类型对应的文本
const getActionText = (action) => {
  switch (action) {
    case 'ADD':
      return '添加'
    case 'UPDATE':
      return '更新'
    case 'DELETE':
      return '删除'
    case 'BATCH_DELETE':
      return '批量删除'
    default:
      return action
  }
}

// 添加paginatedLogs计算属性
const paginatedLogs = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return logs.value.slice(startIndex, startIndex + pageSize.value)
})
</script>

<style scoped>
.operation-log {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.operation-area {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style> 