<template>
  <div class="dispensing-records">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="处方号">
          <el-input v-model="searchForm.prescriptionId" placeholder="处方编号" clearable />
        </el-form-item>
        
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="患者姓名" clearable />
        </el-form-item>
        
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 发药记录表格 -->
    <el-table
      v-loading="loading"
      :data="paginatedRecords"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="发药记录ID" width="100" />
      <el-table-column prop="prescriptionId" label="处方号" width="120" />
      <el-table-column prop="patientName" label="患者姓名" min-width="120" />
      <el-table-column prop="dispensedBy" label="发药人员" min-width="120" />
      <el-table-column prop="dispensedTime" label="发药时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.dispensedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag type="success">已发药</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="viewDetails(scope.row.id)"
          >
            详情
          </el-button>
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
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="发药详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="recordDetails" class="record-details">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="发药记录ID">{{ recordDetails.id }}</el-descriptions-item>
          <el-descriptions-item label="处方号">{{ recordDetails.prescriptionId }}</el-descriptions-item>
          <el-descriptions-item label="患者姓名">{{ recordDetails.patientName }}</el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ recordDetails.patientId }}</el-descriptions-item>
          <el-descriptions-item label="发药人员">{{ recordDetails.dispensedBy }}</el-descriptions-item>
          <el-descriptions-item label="发药时间">{{ formatDateTime(recordDetails.dispensedTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">药品信息</el-divider>
        <el-table :data="recordDetails.drugs" border>
          <el-table-column prop="drugName" label="药品名称" min-width="180" />
          <el-table-column prop="specification" label="规格" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="usage" label="用法用量" min-width="200" />
        </el-table>
        
        <template v-if="recordDetails.remarks">
          <el-divider content-position="left">备注信息</el-divider>
          <p class="remarks">{{ recordDetails.remarks }}</p>
        </template>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="printRecord(recordDetails.id)"
          >
            打印记录
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 搜索表单
const searchForm = reactive({
  prescriptionId: '',
  patientName: '',
  dateRange: []
})

// 数据状态
const records = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情状态
const detailsVisible = ref(false)
const recordDetails = ref(null)

// 初始化
onMounted(() => {
  fetchRecords()
})

// 获取发药记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      records.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    // 构建查询参数
    const params = {}
    if (searchForm.prescriptionId) params.prescriptionId = searchForm.prescriptionId
    if (searchForm.patientName) params.patientName = searchForm.patientName
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await axios.get('/api/dispensing-records', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params
    })
    
    if (Array.isArray(response.data)) {
      records.value = response.data
      total.value = response.data.length
      
      if (records.value.length === 0) {
        ElMessage.info('没有查询到符合条件的发药记录')
      }
    } else {
      console.warn('服务器返回的发药记录数据不是数组格式:', response.data)
      records.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取发药记录失败:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '获取发药记录失败')
    }
    
    records.value = []
    total.value = 0
    loading.value = false
  }
}

// 查看详情
const viewDetails = async (id) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get(`/api/dispensing-records/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    recordDetails.value = response.data
    detailsVisible.value = true
  } catch (error) {
    console.error('获取发药记录详情失败:', error)
    ElMessage.error('获取发药记录详情失败')
  }
}

// 打印记录
const printRecord = (id) => {
  ElMessage.success('发药记录打印请求已发送')
  // 实际实现可能需要调用打印API或生成PDF
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRecords()
}

// 重置搜索
const resetSearch = () => {
  searchForm.prescriptionId = ''
  searchForm.patientName = ''
  searchForm.dateRange = []
  currentPage.value = 1
  fetchRecords()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 计算分页后的数据
const paginatedRecords = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return records.value.slice(startIndex, startIndex + pageSize.value)
})

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
</script>

<style scoped>
.dispensing-records {
  margin: 20px 0;
}

.search-area {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.record-details {
  padding: 0 20px;
}

.remarks {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #666;
}
</style> 