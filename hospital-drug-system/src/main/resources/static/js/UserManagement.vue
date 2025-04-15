<template>
  <div class="user-management">
    <h1 class="page-title">用户管理</h1>
    
    <!-- 操作区域 -->
    <div class="operation-area">
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增用户
      </el-button>
    </div>
    
    <!-- 用户列表 -->
    <el-table
      v-loading="loading"
      :data="users"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      <el-table-column label="角色" min-width="150">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ scope.row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button 
            type="warning" 
            size="small" 
            @click="changeRole(scope.row)"
            :disabled="scope.row.username === currentUsername"
          >
            {{ scope.row.role === 'ADMIN' ? '设为普通用户' : '设为管理员' }}
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="deleteUser(scope.row)"
            :disabled="scope.row.username === currentUsername"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增用户"
      width="500px"
    >
      <el-form :model="userForm" label-width="100px" :rules="rules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
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
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'

// 数据列表
const users = ref([])
const loading = ref(false)
const currentUsername = ref('') // 当前登录用户名

// 弹窗相关
const dialogVisible = ref(false)
const userFormRef = ref(null)
const userForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'USER'
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 初始化获取数据
onMounted(() => {
  // 获取当前用户信息
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      currentUsername.value = user.username
    } catch (e) {
      console.error('解析用户数据失败', e)
    }
  }
  
  fetchUsers()
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/users', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    users.value = response.data
    loading.value = false
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
    loading.value = false
  }
}

// 打开新增用户对话框
const openAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  
  // 手动重置表单属性
  Object.assign(userForm, {
    username: '',
    password: '',
    confirmPassword: '',
    role: 'USER'
  })
}

// 提交表单
const submitForm = () => {
  if (!userFormRef.value) return
  
  userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const token = localStorage.getItem('token')
      const response = await axios.post('/api/users', {
        username: userForm.username,
        password: userForm.password,
        role: userForm.role
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success('用户创建成功')
        dialogVisible.value = false
        fetchUsers()
      } else {
        ElMessage.error(response.data?.message || '创建失败')
      }
    } catch (error) {
      console.error('创建用户失败:', error)
      ElMessage.error(error.response?.data?.message || '创建用户失败')
    }
  })
}

// 更改用户角色
const changeRole = (user) => {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const roleText = newRole === 'ADMIN' ? '管理员' : '普通用户'
  
  ElMessageBox.confirm(`确定将用户 ${user.username} 角色更改为${roleText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await axios.put(`/api/users/${user.id}`, {
        role: newRole
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success('角色更新成功')
        fetchUsers()
      } else {
        ElMessage.error(response.data?.message || '更新失败')
      }
    } catch (error) {
      console.error('更新用户角色失败:', error)
      ElMessage.error(error.response?.data?.message || '更新用户角色失败')
    }
  }).catch(() => {})
}

// 删除用户
const deleteUser = (user) => {
  ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await axios.delete(`/api/users/${user.id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.data && response.data.status === 'success') {
        ElMessage.success('用户删除成功')
        fetchUsers()
      } else {
        ElMessage.error(response.data?.message || '删除失败')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.user-management {
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