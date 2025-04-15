<template>
  <div class="operation-log">
    <h1 class="page-title">操作日志</h1>
    
    <!-- 操作区域 -->
    <div class="operation-area">
      <el-radio-group v-model="logType" @change="handleLogTypeChange">
        <el-radio-button label="all">所有日志</el-radio-button>
        <el-radio-button label="my">我的日志</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 日志表格 -->
    <el-table
      v-loading="loading"
      :data="logs"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 数据列表
const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logType = ref('all') // 'all' 或 'my'

// 初始化获取数据
onMounted(() => {
  fetchLogs()
})

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const endpoint = logType.value === 'my' ? '/api/logs/my' : '/api/logs'
    
    const response = await axios.get(endpoint, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      logs.value = response.data
      total.value = response.data.length
    } else {
      logs.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取操作日志失败:', error)
    ElMessage.error('获取操作日志失败')
    logs.value = []
    total.value = 0
    loading.value = false
  }
}

// 处理日志类型变化
const handleLogTypeChange = () => {
  currentPage.value = 1
  fetchLogs()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
  // 因为已经获取了所有日志，这里只做前端分页
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