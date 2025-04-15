<template>
  <div class="settings-container">
    <h1 class="page-title">系统设置</h1>
    
    <!-- 操作区域 -->
    <div class="operation-area">
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 添加设置
      </el-button>
    </div>
    
    <!-- 设置表格 -->
    <el-table
      v-loading="loading"
      :data="settings"
      border
      style="width: 100%"
    >
      <el-table-column prop="settingKey" label="设置键" min-width="150" />
      <el-table-column prop="settingValue" label="设置值" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="editSetting(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="deleteSetting(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑设置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑设置' : '添加设置'"
      width="500px"
    >
      <el-form :model="settingForm" label-width="100px" :rules="rules" ref="settingFormRef">
        <el-form-item label="设置键" prop="settingKey" :disabled="isEdit">
          <el-input v-model="settingForm.settingKey" placeholder="请输入设置键" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="设置值" prop="settingValue">
          <el-input v-model="settingForm.settingValue" placeholder="请输入设置值" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="settingForm.description" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSetting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'

// 数据列表
const settings = ref([])
const loading = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const settingFormRef = ref(null)
const settingForm = reactive({
  settingKey: '',
  settingValue: '',
  description: ''
})

// 表单验证规则
const rules = {
  settingKey: [
    { required: true, message: '请输入设置键', trigger: 'blur' },
    { pattern: /^[a-z0-9.]+$/, message: '设置键只能包含小写字母、数字和点', trigger: 'blur' }
  ],
  settingValue: [
    { required: true, message: '请输入设置值', trigger: 'blur' }
  ]
}

// 初始化获取数据
onMounted(() => {
  fetchSettings()
})

// 获取设置列表
const fetchSettings = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/settings', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      settings.value = response.data
    } else {
      settings.value = []
      ElMessage.warning('获取设置列表数据格式不正确')
    }
    
    loading.value = false
  } catch (error) {
    console.error('获取设置列表失败:', error)
    ElMessage.error('获取设置列表失败')
    settings.value = []
    loading.value = false
  }
}

// 打开添加设置对话框
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑设置对话框
const editSetting = (setting) => {
  isEdit.value = true
  resetForm()
  
  // 填充表单数据
  settingForm.settingKey = setting.settingKey
  settingForm.settingValue = setting.settingValue
  settingForm.description = setting.description
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (settingFormRef.value) {
    settingFormRef.value.resetFields()
  }
  
  // 手动重置表单属性
  Object.assign(settingForm, {
    settingKey: '',
    settingValue: '',
    description: ''
  })
}

// 保存设置
const saveSetting = () => {
  if (!settingFormRef.value) return
  
  settingFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const token = localStorage.getItem('token')
      const response = await axios.post('/api/settings', settingForm, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success(isEdit.value ? '设置更新成功' : '设置添加成功')
        dialogVisible.value = false
        fetchSettings()
      } else {
        ElMessage.error(response.data?.message || '操作失败')
      }
    } catch (error) {
      console.error('保存设置失败:', error)
      ElMessage.error(error.response?.data?.message || '保存设置失败')
    }
  })
}

// 删除设置
const deleteSetting = (setting) => {
  ElMessageBox.confirm(`确定要删除设置 ${setting.settingKey} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await axios.delete(`/api/settings/${setting.settingKey}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success('设置删除成功')
        fetchSettings()
      } else {
        ElMessage.error(response.data?.message || '删除失败')
      }
    } catch (error) {
      console.error('删除设置失败:', error)
      ElMessage.error(error.response?.data?.message || '删除设置失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.settings-container {
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
</style> 