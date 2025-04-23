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
        <el-button type="primary" @click="openAddDialog" v-if="isAdmin">
          <el-icon><Plus /></el-icon> 新增药品
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedDrugs.length === 0" v-if="isAdmin">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="success" @click="exportToCsv" v-if="isAdmin">
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
      <el-table-column type="selection" width="55" v-if="isAdmin"/>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="药品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" min-width="100" show-overflow-tooltip />
      <el-table-column prop="category" label="药品类别" min-width="100" show-overflow-tooltip />
      <el-table-column prop="totalStock" label="总库存" width="100" sortable>
        <template #default="scope">
          <el-tag :type="scope.row.totalStock > 50 ? 'success' : scope.row.totalStock > 0 ? 'warning' : 'danger'">
            {{ scope.row.totalStock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="supplier" label="供应商" min-width="150" show-overflow-tooltip />
      <el-table-column label="批次管理" width="180" fixed="right">
         <template #default="scope">
          <el-button type="primary" link size="small" @click="openViewBatchesDialog(scope.row)">查看批次</el-button>
          <el-button type="success" link size="small" @click="openAddBatchDialog(scope.row)" v-if="isAdmin || userInfo?.role === 'PHARMACIST'">添加批次</el-button>
         </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editDrug(scope.row)" v-if="isAdmin">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteDrug(scope.row.id)" v-if="isAdmin">删除</el-button>
          <el-button link size="small" @click="openHistoryDialog(scope.row)">记录</el-button>
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
      @close="resetForm"
    >
      <el-form :model="drugForm" label-width="100px" :rules="rules" ref="drugFormRef">
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="drugForm.name" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-autocomplete
            v-model="drugForm.spec"
            :fetch-suggestions="querySpecSuggestions"
            placeholder="请输入或选择规格"
            clearable
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="药品类别" prop="category">
          <el-select
            v-model="drugForm.category"
            placeholder="请选择或输入类别"
            filterable
            allow-create
            default-first-option
            style="width: 100%;"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-select
            v-model="drugForm.supplier"
            placeholder="请选择或输入供应商"
            filterable
            allow-create
            default-first-option
            style="width: 100%;"
          >
            <el-option
              v-for="item in supplierOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- 库存记录对话框 -->
    <el-dialog v-model="historyDialogVisible" title="库存记录" width="70%">
      <el-table :data="transactionHistory" border max-height="400px">
        <el-table-column prop="transactionTime" label="时间" width="180">
           <template #default="scope">{{ formatDateTime(scope.row.transactionTime) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
           <template #default="scope">
             <el-tag :type="getTransactionTypeTag(scope.row.type)">{{ getTransactionTypeText(scope.row.type) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="quantityChange" label="数量变化" width="100" />
        <el-table-column prop="stockAfterTransaction" label="交易后库存" width="110" />
        <el-table-column prop="user.username" label="操作人" width="120" />
        <el-table-column prop="remarks" label="备注" />
      </el-table>
      <template #footer>
        <el-button @click="historyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 查看批次对话框 -->
    <el-dialog v-model="viewBatchesDialogVisible" title="查看批次" width="75%">
       <p>药品: {{ currentDrugForBatchView?.name }}</p>
       <el-table :data="currentBatches" border size="small">
            <el-table-column prop="batchNumber" label="批号" />
            <el-table-column prop="productionDate" label="生产日期">
                <template #default="scope">{{ formatDate(scope.row.productionDate) }}</template>
            </el-table-column>
            <el-table-column prop="expirationDate" label="有效期至">
                <template #default="scope">{{ formatDate(scope.row.expirationDate) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" />
            <el-table-column prop="supplier" label="供应商" />
       </el-table>
      <template #footer>
        <el-button @click="viewBatchesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加批次对话框 -->
    <el-dialog 
      v-model="addBatchDialogVisible" 
      title="添加批次" 
      width="600px"
      :close-on-click-modal="false"
      @close="resetAddBatchForm"
    >
      <p v-if="currentDrugForBatchAdd">为药品 [<b>{{ currentDrugForBatchAdd.name }}</b>] 添加新批次</p>
      <el-form 
        :model="addBatchFormModel" 
        label-width="100px" 
        :rules="addBatchRules" 
        ref="addBatchFormRef" 
        style="margin-top: 20px;"
      >
        <el-form-item label="批号" prop="batchNumber">
          <el-input v-model="addBatchFormModel.batchNumber" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
            v-model="addBatchFormModel.productionDate"
            type="date"
            placeholder="选择生产日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="有效期至" prop="expirationDate">
          <el-date-picker
            v-model="addBatchFormModel.expirationDate"
            type="date"
            placeholder="选择有效期至"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="addBatchFormModel.quantity" :min="1" placeholder="请输入批次数量" style="width: 100%;" controls-position="right"/>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="addBatchFormModel.supplier" placeholder="请输入供应商（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addBatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddBatch" :loading="addBatchLoading">确认添加</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, Download } from '@element-plus/icons-vue'
import axios from 'axios'

// Inject user info and admin status from MainLayout
const userInfo = inject('userInfo');
const isAdmin = inject('isAdmin');

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
  supplier: ''
})
const submitLoading = ref(false) // 添加提交按钮的loading状态

// 新增：表单优化选项
const supplierOptions = ref([]) // 供应商选项
const categoryOptions = ref(['抗生素', '镇痛药', '抗病毒药', '营养补充剂', '心血管药物', '消化系统药物', '呼吸系统药物']) // 预定义类别
const specSuggestions = ref([
  { value: '片剂' }, { value: '胶囊' }, { value: '注射液' }, { value: '软膏' }, { value: '粉末' },
  { value: '10mg' }, { value: '50mg' }, { value: '100mg' }, { value: '250mg' }, { value: '500mg' }, { value: '1g' },
  { value: '10ml' }, { value: '100ml' }, { value: '500ml' },
  { value: '盒' }, { value: '瓶' }, { value: '支' }, { value: '袋' },
])

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
  supplier: [
    { required: true, message: '请输入供应商', trigger: 'blur' }
  ]
}

// --- 新增：库存操作相关 ---
const inboundDialogVisible = ref(false)
const outboundDialogVisible = ref(false)
const adjustDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const inboundFormRef = ref(null)
const outboundFormRef = ref(null)
const adjustFormRef = ref(null)

const inventoryForm = reactive({
  drugId: null,
  drugName: '',
  currentStock: 0, // 用于出库时校验
  quantity: null,
  remarks: ''
})

const adjustForm = reactive({
  drugId: null,
  drugName: '',
  currentStock: 0,
  newStock: null,
  remarks: ''
})

const transactionHistory = ref([])
const currentHistoryDrugName = ref('')

// 数据 for batch dialogs
const viewBatchesDialogVisible = ref(false)
const addBatchDialogVisible = ref(false)
const currentDrugForBatchView = ref(null)
const currentBatches = ref([])
const currentDrugForBatchAdd = ref(null)
const addBatchLoading = ref(false)

// --- Add Batch Form --- 
const addBatchFormRef = ref(null); // Ref for the new form
const addBatchFormModel = reactive({
  batchNumber: '',
  productionDate: null,
  expirationDate: null,
  quantity: null,
  supplier: ''
});

const addBatchRules = reactive({
  batchNumber: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  productionDate: [{ required: true, message: '请选择生产日期', trigger: 'change' }],
  expirationDate: [{ required: true, message: '请选择有效期至', trigger: 'change' }],
  quantity: [
      { required: true, message: '请输入批次数量', trigger: 'blur' },
      { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ]
  // Supplier is optional, no rule needed unless required
});

// 新增：获取供应商列表
const fetchSuppliers = async () => {
  try {
    const response = await axios.get('/api/drugs/suppliers');
    supplierOptions.value = response.data || [];
  } catch (error) {
    console.error('获取供应商列表失败:', error);
    // 可选：给用户提示
    // ElMessage.error('获取供应商列表失败');
  }
};

// 初始化获取数据
onMounted(() => {
  fetchDrugs()
  fetchExpiringDrugs()
  fetchSuppliers() // 获取供应商列表
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
    supplier: ''
  })
}

// 提交表单
const submitForm = () => {
  if (!drugFormRef.value) return
  
  submitLoading.value = true; // 开始提交，设置loading
  drugFormRef.value.validate(async (valid) => {
    if (!valid) {
      submitLoading.value = false; // 验证失败，取消loading
      return
    }
    
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
    } finally {
      submitLoading.value = false; // 无论成功或失败，取消loading
    }
  })
}

// 新增：规格自动完成查询
const querySpecSuggestions = (queryString, cb) => {
  const results = queryString
    ? specSuggestions.value.filter(item => item.value.toLowerCase().includes(queryString.toLowerCase()))
    : specSuggestions.value;
  cb(results);
};

// 获取即将过期的药品
const fetchExpiringDrugs = async () => {
  try {
    const token = localStorage.getItem('token'); // 获取token
    const response = await axios.get('/api/drugs/expiring?days=30', {
      headers: {
        'Authorization': `Bearer ${token}` // 添加认证头
      }
    });

    // 直接使用返回的DTO列表
    expiringDrugs.value = response.data || []; 

    // 之前的代码，假设返回结构是 { status: 'success', drugs: [...] }
    // if (response.data && response.data.status === 'success') {
    //   expiringDrugs.value = response.data.drugs;
    // }

  } catch (error) {
    console.error('获取过期预警药品失败:', error.response?.data || error.message);
    // 可以在这里添加用户提示，但避免在每次加载时都弹出错误
    // ElMessage.error('获取过期预警药品失败'); 
    expiringDrugs.value = []; // 出错时清空列表
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

// 打开库存记录对话框
const openHistoryDialog = async (drug) => {
  currentHistoryDrugName.value = drug.name // 可选：在标题或某处显示药品名
  historyDialogVisible.value = true
  transactionHistory.value = [] // 清空旧记录
  try {
    const response = await axios.get(`/api/inventory/transactions/${drug.id}`)
    transactionHistory.value = response.data
  } catch (error) {
    console.error('获取库存记录失败:', error)
    ElMessage.error('获取库存记录失败')
  }
}

// --- Helper 函数 ---
const formatDateTime = (dateTimeString) => {
   if (!dateTimeString) return '';
   try {
    // Spring Boot 返回的可能是数组或字符串，尝试兼容
    let dtStr = Array.isArray(dateTimeString) ? dateTimeString.join('-') : dateTimeString;
    dtStr = dtStr.replace('T', ' '); // 兼容 ISO 格式
    const date = new Date(dtStr);
    if (isNaN(date.getTime())) return dateTimeString; // 解析失败返回原始值

    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (e) {
    console.warn("解析日期时间失败:", dateTimeString, e);
    return dateTimeString; // 异常时返回原始字符串
  }
}

const getTransactionTypeTag = (type) => {
  switch (type) {
    case 'INBOUND': return 'success'
    case 'OUTBOUND': return 'warning'
    case 'ADJUSTMENT': return 'info'
    case 'INITIAL': return 'primary'
    default: return 'info'
  }
}

const getTransactionTypeText = (type) => {
  switch (type) {
    case 'INBOUND': return '入库'
    case 'OUTBOUND': return '出库'
    case 'ADJUSTMENT': return '调整'
    case 'INITIAL': return '初始'
    default: return type
  }
}

// --- 新增：提交库存操作的函数 ---
const submitInbound = async () => {
  if (!inboundFormRef.value) return
  await inboundFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload = {
          drugId: inventoryForm.drugId,
          quantity: inventoryForm.quantity,
          remarks: inventoryForm.remarks
        }
        await axios.post('/api/inventory/inbound', payload)
        ElMessage.success('入库成功')
        inboundDialogVisible.value = false
        fetchDrugs() // 刷新列表
      } catch (error) {
        console.error('入库失败:', error.response?.data?.message || error.message)
        ElMessage.error(error.response?.data?.message || '入库失败')
      }
    }
  })
}

const submitOutbound = async () => {
   if (!outboundFormRef.value) return
   await outboundFormRef.value.validate(async (valid) => {
     if (valid) {
        if (inventoryForm.quantity > inventoryForm.currentStock) {
           ElMessage.error('出库数量不能大于当前库存')
           return
        }
        try {
          const payload = {
            drugId: inventoryForm.drugId,
            quantity: inventoryForm.quantity,
            remarks: inventoryForm.remarks
          }
          await axios.post('/api/inventory/outbound', payload)
          ElMessage.success('出库成功')
          outboundDialogVisible.value = false
          fetchDrugs() // 刷新列表
        } catch (error) {
          console.error('出库失败:', error.response?.data?.message || error.message)
          ElMessage.error(error.response?.data?.message || '出库失败')
        }
     }
   })
}

const submitAdjust = async () => {
  if (!adjustFormRef.value) return
  await adjustFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload = {
          drugId: adjustForm.drugId,
          newStock: adjustForm.newStock,
          remarks: adjustForm.remarks
        }
        await axios.post('/api/inventory/adjust', payload)
        ElMessage.success('库存调整成功')
        adjustDialogVisible.value = false
        fetchDrugs() // 刷新列表
      } catch (error) {
        console.error('库存调整失败:', error.response?.data?.message || error.message)
        ElMessage.error(error.response?.data?.message || '库存调整失败')
      }
    }
  })
}

// Open View Batches Dialog
const openViewBatchesDialog = async (drug) => {
  currentDrugForBatchView.value = drug;
  currentBatches.value = []; // Clear previous batches
  viewBatchesDialogVisible.value = true;
  try {
    // Fetch batches for the selected drug
    const response = await axios.get(`/api/drugs/${drug.id}/batches`);
    currentBatches.value = response.data;
  } catch (error) {
    console.error('获取批次信息失败:', error);
    ElMessage.error('获取批次信息失败');
    viewBatchesDialogVisible.value = false; // Close dialog on error
  }
};

// Open Add Batch Dialog (Set default supplier)
const openAddBatchDialog = (drug) => {
  currentDrugForBatchAdd.value = drug;
  resetAddBatchForm(); // Reset form before opening
  // Set default supplier from the drug if available
  addBatchFormModel.supplier = drug.supplier || ''; 
  addBatchDialogVisible.value = true;
};

// Reset Add Batch Form
const resetAddBatchForm = () => {
    if (addBatchFormRef.value) {
        addBatchFormRef.value.resetFields();
    }
    // Explicitly clear fields as resetFields might not clear non-prop bound data fully
    Object.assign(addBatchFormModel, {
        batchNumber: '',
        productionDate: null,
        expirationDate: null,
        quantity: null,
        supplier: ''
    });
};

// Submit Add Batch (Implemented)
const submitAddBatch = async () => {
  if (!addBatchFormRef.value || !currentDrugForBatchAdd.value) return;

  await addBatchFormRef.value.validate(async (valid) => {
    if (valid) {
      addBatchLoading.value = true;
      try {
        const token = localStorage.getItem('token');
        const payload = {
          batchNumber: addBatchFormModel.batchNumber,
          productionDate: addBatchFormModel.productionDate,
          expirationDate: addBatchFormModel.expirationDate,
          quantity: addBatchFormModel.quantity,
          supplier: addBatchFormModel.supplier || null // Send null if empty
        };
        
        await axios.post(`/api/drugs/${currentDrugForBatchAdd.value.id}/batches`, payload, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json' // Ensure correct content type
          }
        });
        
        ElMessage.success('批次添加成功');
        addBatchDialogVisible.value = false;
        fetchDrugs(); // Refresh list to update total stock
      } catch (error) {
        console.error('添加批次失败:', error.response?.data || error.message);
        ElMessage.error(error.response?.data?.message || '添加批次失败');
      } finally {
        addBatchLoading.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

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

.el-table .el-button + .el-button {
    margin-left: 5px; /* Adjust button spacing in table */
}
</style> 