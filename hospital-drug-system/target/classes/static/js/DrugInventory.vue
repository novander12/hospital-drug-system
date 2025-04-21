<template>
  <div class="drug-inventory">
    <h1 class="page-title">药品库存查询</h1>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="药品名称/编码" clearable />
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplier" placeholder="选择供应商" clearable>
            <el-option v-for="item in supplierOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 药品表格 -->
    <el-table
      v-loading="loading"
      :data="paginatedDrugs"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="id" label="药品编码" min-width="120" />
      <el-table-column prop="name" label="药品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" min-width="120" />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column prop="supplier" label="供应商" min-width="150" />
      <el-table-column label="价格(元)" min-width="100">
        <template #default="scope">
          {{ formatPrice(scope.row.price || 0) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存数量" min-width="100" />
      <el-table-column label="单位" min-width="80">
        <template #default="scope">
          {{ scope.row.unit || '件' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="scope">
          <el-tag :type="scope.row.stock > 10 ? 'success' : (scope.row.stock > 0 ? 'warning' : 'danger')">
            {{ scope.row.stock > 10 ? '充足' : (scope.row.stock > 0 ? '偏低' : '缺货') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="过期日期" min-width="120">
        <template #default="scope">
          {{ scope.row.expirationDate }}
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  supplier: ''
})

// 数据列表
const drugs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 分类选项
const categoryOptions = ref([])
// 供应商选项
const supplierOptions = ref([])

// 初始化获取数据
onMounted(async () => {
  console.log('药品库存组件已挂载')
  await Promise.all([
    fetchCategories(),
    fetchSuppliers()
  ])
  fetchDrugs()
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get('/api/categories', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      categoryOptions.value = response.data.map(category => ({
        value: category.id,
        label: category.name
      }))
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 获取供应商列表
const fetchSuppliers = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get('/api/suppliers', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      supplierOptions.value = response.data.map(supplier => ({
        value: supplier.id,
        label: supplier.name
      }))
    }
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败')
  }
}

// 获取药品列表
const fetchDrugs = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      drugs.value = []
      total.value = 0
      loading.value = false
      return
    }
    
    // 构建查询参数
    const params = {}
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.category) params.categoryId = searchForm.category
    if (searchForm.supplier) params.supplierId = searchForm.supplier
    
    const response = await axios.get('/api/drugs', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params
    })
    
    if (Array.isArray(response.data)) {
      drugs.value = response.data
      total.value = response.data.length
      
      if (drugs.value.length === 0) {
        ElMessage.info('没有查询到符合条件的药品')
      }
    } else {
      console.warn('服务器返回的药品数据不是数组格式:', response.data)
      drugs.value = []
      total.value = 0
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取药品库存失败:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '获取药品库存失败')
    }
    
    drugs.value = []
    total.value = 0
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDrugs()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.supplier = ''
  currentPage.value = 1
  fetchDrugs()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 计算分页后的数据
const paginatedDrugs = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return drugs.value.slice(startIndex, startIndex + pageSize.value)
})

// 格式化价格
const formatPrice = (price) => {
  return price ? `¥${parseFloat(price).toFixed(2)}` : '¥0.00'
}
</script>

<style scoped>
.drug-inventory {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
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
</style> 