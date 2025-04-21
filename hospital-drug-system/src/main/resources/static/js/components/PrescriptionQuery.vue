<template>
  <div class="prescription-query">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="处方号">
          <el-input v-model="searchForm.prescriptionId" placeholder="处方编号" clearable />
        </el-form-item>
        
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="患者姓名" clearable />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="处方状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已审核" value="APPROVED" />
            <el-option label="已发药" value="DISPENSED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
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
    
    <!-- 处方表格 -->
    <el-table
      v-loading="loading"
      :data="paginatedPrescriptions"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="处方编号" width="120" />
      <el-table-column prop="patientName" label="患者姓名" min-width="120" />
      <el-table-column prop="department" label="开方科室" min-width="120" />
      <el-table-column prop="doctor" label="开方医生" min-width="120" />
      <el-table-column prop="createTime" label="创建时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="viewPrescription(scope.row.id)"
          >
            查看详情
          </el-button>
          
          <el-button
            size="small"
            type="danger"
            @click="cancelPrescription(scope.row.id)"
            v-if="canCancel(scope.row.status)"
          >
            取消处方
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
    
    <!-- 处方详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="处方详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="prescriptionDetails" class="prescription-details">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="处方编号">{{ prescriptionDetails.id }}</el-descriptions-item>
          <el-descriptions-item label="患者姓名">{{ prescriptionDetails.patientName }}</el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ prescriptionDetails.patientId }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ prescriptionDetails.age }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ prescriptionDetails.gender === 'male' ? '男' : prescriptionDetails.gender === 'female' ? '女' : '其他' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ prescriptionDetails.department }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ prescriptionDetails.doctor }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(prescriptionDetails.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">诊断信息</el-divider>
        <p class="diagnosis-info">{{ prescriptionDetails.diagnosis }}</p>
        
        <el-divider content-position="left">药品信息</el-divider>
        <el-table :data="prescriptionDetails.drugs" border>
          <el-table-column prop="drugName" label="药品名称" min-width="180" />
          <el-table-column prop="specification" label="规格" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="usage" label="用法用量" min-width="200" />
        </el-table>
        
        <el-divider content-position="left">处方状态</el-divider>
        <el-steps :active="getStepActive(prescriptionDetails.status)" finish-status="success">
          <el-step title="创建处方" description="处方已创建" />
          <el-step title="审核" :description="getStepDescription(prescriptionDetails.status, 'APPROVED')" />
          <el-step title="发药" :description="getStepDescription(prescriptionDetails.status, 'DISPENSED')" />
        </el-steps>
        
        <template v-if="prescriptionDetails.remarks">
          <el-divider content-position="left">备注信息</el-divider>
          <p class="remarks">{{ prescriptionDetails.remarks }}</p>
        </template>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="printPrescription(prescriptionDetails.id)"
          >
            打印处方
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 搜索表单
const searchForm = reactive({
  prescriptionId: '',
  patientName: '',
  status: '',
  dateRange: []
})

// 数据状态
const prescriptions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情状态
const detailsVisible = ref(false)
const prescriptionDetails = ref(null)

// 初始化
onMounted(() => {
  fetchPrescriptions()
})

// 获取处方列表
const fetchPrescriptions = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      prescriptions.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    // 构建查询参数
    const params = {}
    if (searchForm.prescriptionId) params.id = searchForm.prescriptionId
    if (searchForm.patientName) params.patientName = searchForm.patientName
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await axios.get('/api/prescriptions', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params
    })
    
    if (Array.isArray(response.data)) {
      prescriptions.value = response.data
      total.value = response.data.length
      
      if (prescriptions.value.length === 0) {
        ElMessage.info('没有查询到符合条件的处方')
      }
    } else {
      console.warn('服务器返回的处方数据不是数组格式:', response.data)
      prescriptions.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取处方列表失败:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '获取处方列表失败')
    }
    
    prescriptions.value = []
    total.value = 0
    loading.value = false
  }
}

// 获取处方详情
const viewPrescription = async (id) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get(`/api/prescriptions/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    prescriptionDetails.value = response.data
    detailsVisible.value = true
  } catch (error) {
    console.error('获取处方详情失败:', error)
    ElMessage.error('获取处方详情失败')
  }
}

// 取消处方
const cancelPrescription = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该处方吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    await axios.put(`/api/prescriptions/${id}/cancel`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    ElMessage.success('处方已取消')
    fetchPrescriptions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消处方失败:', error)
      ElMessage.error(error.response?.data?.message || '取消处方失败')
    }
  }
}

// 打印处方
const printPrescription = (id) => {
  ElMessage.success('处方打印请求已发送')
  // 实际实现可能需要调用打印API或生成PDF
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPrescriptions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.prescriptionId = ''
  searchForm.patientName = ''
  searchForm.status = ''
  searchForm.dateRange = []
  currentPage.value = 1
  fetchPrescriptions()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 计算分页后的数据
const paginatedPrescriptions = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return prescriptions.value.slice(startIndex, startIndex + pageSize.value)
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

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'info'
    case 'APPROVED':
      return 'success'
    case 'DISPENSED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待审核'
    case 'APPROVED':
      return '已审核'
    case 'DISPENSED':
      return '已发药'
    case 'CANCELLED':
      return '已取消'
    case 'REJECTED':
      return '已拒绝'
    default:
      return status
  }
}

// 判断是否可以取消处方
const canCancel = (status) => {
  return ['PENDING', 'APPROVED'].includes(status)
}

// 获取进度条的激活步骤
const getStepActive = (status) => {
  switch (status) {
    case 'PENDING':
      return 1
    case 'APPROVED':
      return 2
    case 'DISPENSED':
      return 3
    case 'CANCELLED':
    case 'REJECTED':
      return 1
    default:
      return 0
  }
}

// 获取步骤描述
const getStepDescription = (currentStatus, targetStatus) => {
  if (currentStatus === 'CANCELLED') {
    return '处方已取消'
  }
  
  if (currentStatus === 'REJECTED') {
    return '处方已拒绝'
  }
  
  if (targetStatus === 'APPROVED') {
    if (['APPROVED', 'DISPENSED'].includes(currentStatus)) {
      return '审核通过'
    }
    return '等待审核'
  }
  
  if (targetStatus === 'DISPENSED') {
    if (currentStatus === 'DISPENSED') {
      return '药品已发放'
    }
    return '等待发药'
  }
  
  return ''
}
</script>

<style scoped>
.prescription-query {
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

.prescription-details {
  padding: 0 20px;
}

.diagnosis-info {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 40px;
}

.remarks {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #666;
}
</style> 