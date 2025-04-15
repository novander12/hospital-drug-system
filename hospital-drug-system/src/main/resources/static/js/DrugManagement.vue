<template>
  <div class="drug-management">
    <h1 class="page-title">药品管理系统</h1>
    
    <!-- 过期预警提示 -->
    <div v-if="expiringDrugs.length > 0" class="expiring-warning">
      <el-alert
        title="药品过期预警"
        type="error"
        :closable="false"
        show-icon
      >
        <div class="expiring-list">
          <span>以下药品即将在30天内过期，请及时处理：</span>
          <el-tag
            v-for="drug in expiringDrugs"
            :key="drug.id"
            type="danger"
            class="expiring-tag"
          >
            {{ drug.name }} ({{ formatDate(drug.expirationDate) }})
          </el-tag>
        </div>
      </el-alert>
    </div>
    
    <!-- 搜索和操作区域 -->
    <div class="operation-area">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入药品名称搜索"
        class="search-input"
        clearable
        @clear="fetchDrugs"
      >
        <template #append>
          <el-button @click="searchDrugs">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <div class="action-buttons">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新增药品
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedDrugs.length === 0">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="success" @click="exportToCsv">
          <el-icon><Download /></el-icon> 导出CSV
        </el-button>
      </div>
    </div>
    
    <!-- 药品表格 -->
    <el-table
      v-loading="loading"
      :data="drugs"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="药品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" min-width="120" />
      <el-table-column prop="category" label="药品类别" min-width="120" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="expirationDate" label="过期日期" min-width="120">
        <template #default="scope">
          {{ formatDate(scope.row.expirationDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="supplier" label="供应商" min-width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editDrug(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="deleteDrug(scope.row.id)">
            删除
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
    
    <!-- 新增/编辑药品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑药品' : '新增药品'"
      width="500px"
    >
      <el-form :model="drugForm" label-width="100px" :rules="rules" ref="drugFormRef">
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="drugForm.name" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="drugForm.spec" placeholder="请输入药品规格" />
        </el-form-item>
        <el-form-item label="药品类别" prop="category">
          <el-input v-model="drugForm.category" placeholder="请输入药品类别" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="drugForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="过期日期" prop="expirationDate">
          <el-date-picker
            v-model="drugForm.expirationDate"
            type="date"
            placeholder="选择过期日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="drugForm.supplier" placeholder="请输入供应商" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, Download } from '@element-plus/icons-vue'
import axios from 'axios'

// 数据列表
const drugs = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedDrugs = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const expiringDrugs = ref([]) // 即将过期的药品

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const drugFormRef = ref(null)
const drugForm = reactive({
  id: null,
  name: '',
  spec: '',
  category: '',
  stock: 0,
  expirationDate: '',
  supplier: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入药品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  spec: [
    { required: true, message: '请输入药品规格', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入药品类别', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ],
  expirationDate: [
    { required: true, message: '请选择过期日期', trigger: 'change' }
  ],
  supplier: [
    { required: true, message: '请输入供应商', trigger: 'blur' }
  ]
}

// 初始化获取数据
onMounted(() => {
  fetchDrugs()
  fetchExpiringDrugs()
})

// 获取药品列表
const fetchDrugs = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/drugs')
    drugs.value = response.data
    total.value = response.data.length
    loading.value = false
  } catch (error) {
    console.error('获取药品列表失败:', error)
    ElMessage.error('获取药品列表失败')
    loading.value = false
  }
}

// 搜索药品
const searchDrugs = async () => {
  if (searchKeyword.value.trim() === '') {
    fetchDrugs()
    return
  }
  
  loading.value = true
  try {
    const response = await axios.get(`/api/drugs/search?name=${encodeURIComponent(searchKeyword.value.trim())}`)
    if (response.data && response.data.drugs) {
      drugs.value = response.data.drugs
      total.value = response.data.drugs.length
    } else {
      drugs.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('搜索药品失败:', error)
    ElMessage.error('搜索药品失败')
    drugs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 多选变化
const handleSelectionChange = (selection) => {
  selectedDrugs.value = selection
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchDrugs()
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

// 打开新增药品对话框
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑药品
const editDrug = (row) => {
  isEdit.value = true
  Object.keys(drugForm).forEach(key => {
    drugForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 删除单个药品
const deleteDrug = (id) => {
  ElMessageBox.confirm('确定要删除该药品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(`/api/drugs/${id}`)
      if (response.data && response.data.status === 'success') {
        ElMessage.success('删除成功')
        fetchDrugs()
      } else {
        ElMessage.error(response.data?.message || '删除失败')
      }
    } catch (error) {
      console.error('删除药品失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

// 批量删除药品
const batchDelete = () => {
  if (selectedDrugs.value.length === 0) {
    ElMessage.warning('请选择要删除的药品')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedDrugs.value.length} 个药品吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedDrugs.value.map(item => item.id)
      const response = await axios.delete('/api/drugs/batch', {
        data: ids
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success('批量删除成功')
        fetchDrugs()
      } else {
        ElMessage.error(response.data?.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除药品失败:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (drugFormRef.value) {
    drugFormRef.value.resetFields()
  }
  
  // 手动重置表单属性
  Object.assign(drugForm, {
    id: null,
    name: '',
    spec: '',
    category: '',
    stock: 0,
    expirationDate: '',
    supplier: ''
  })
}

// 提交表单
const submitForm = () => {
  if (!drugFormRef.value) return
  
  drugFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      let response
      
      if (isEdit.value) {
        // 编辑药品
        response = await axios.put(`/api/drugs/${drugForm.id}`, drugForm)
      } else {
        // 新增药品
        response = await axios.post('/api/drugs', drugForm)
      }
      
      if (response.data) {
        ElMessage.success(isEdit.value ? '药品更新成功' : '药品添加成功')
        dialogVisible.value = false
        fetchDrugs()
      } else {
        ElMessage.error(response.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
      }
    } catch (error) {
      console.error(isEdit.value ? '更新药品失败:' : '添加药品失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  })
}

// 获取即将过期的药品
const fetchExpiringDrugs = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/drugs/expiring?days=30', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data && response.data.status === 'success') {
      expiringDrugs.value = response.data.drugs
    }
  } catch (error) {
    console.error('获取过期预警药品失败:', error)
  }
}

// 导出CSV
const exportToCsv = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/drugs/export', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      responseType: 'blob' // 以二进制数据形式接收响应
    })
    
    // 创建Blob对象
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
    
    // 创建下载链接
    const downloadLink = document.createElement('a')
    downloadLink.href = URL.createObjectURL(blob)
    downloadLink.download = 'drug_export.csv'
    
    // 添加到文档并触发点击
    document.body.appendChild(downloadLink)
    downloadLink.click()
    
    // 清理
    document.body.removeChild(downloadLink)
    URL.revokeObjectURL(downloadLink.href)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出药品数据失败:', error)
    ElMessage.error('导出药品数据失败')
  }
}
</script>

<style scoped>
.drug-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.operation-area {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.expiring-warning {
  margin-bottom: 20px;
}

.expiring-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.expiring-tag {
  margin-right: 5px;
}
</style> 