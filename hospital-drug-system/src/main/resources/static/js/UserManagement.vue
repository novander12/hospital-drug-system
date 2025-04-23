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
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small"
            @click="openResetPasswordDialog(scope.row)"
            :disabled="scope.row.id === 1 || scope.row.username === currentUsername"
          >
            重置密码
          </el-button>

          <el-dropdown
            trigger="click"
            @command="(command) => handleRoleCommand(scope.row, command)"
            :disabled="scope.row.id === 1 || scope.row.username === currentUsername"
            style="margin-left: 10px;"
          >
            <el-button type="warning" size="small">
              修改角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="role in availableRoles" 
                  :key="role"
                  :command="role"
                  :disabled="scope.row.role === role"
                >
                  {{ role }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button 
            type="danger" 
            size="small" 
            @click="deleteUser(scope.row)"
            :disabled="scope.row.id === 1 || scope.row.username === currentUsername"
            style="margin-left: 10px;"
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

    <!-- 新增：重置密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置用户密码"
      width="450px"
      :close-on-click-modal="false"
      @close="resetPasswordForm"
    >
      <el-form 
        :model="resetPasswordFormModel" 
        label-width="100px" 
        :rules="resetPasswordRules" 
        ref="resetPasswordFormRef"
      >
        <el-form-item label="用户名">
          <span>{{ selectedUserForReset?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="resetPasswordFormModel.newPassword" 
            type="password" 
            placeholder="请输入新密码（6-20位）" 
            show-password 
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmNewPassword">
          <el-input 
            v-model="resetPasswordFormModel.confirmNewPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword" :loading="resetLoading">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Lock, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

// 数据列表
const users = ref([])
const loading = ref(false)
const currentUsername = ref('') // 当前登录用户名

// 可用角色列表
const availableRoles = ref(['USER', 'NURSE', 'PHARMACIST', 'ADMIN']);

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

// 新增：重置密码相关
const resetDialogVisible = ref(false);
const resetPasswordFormRef = ref(null);
const selectedUserForReset = ref(null);
const resetLoading = ref(false);
const resetPasswordFormModel = reactive({
  newPassword: '',
  confirmNewPassword: '',
});

// 自定义验证规则：确认密码
const validateConfirmResetPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== resetPasswordFormModel.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const resetPasswordRules = reactive({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在 6 到 20 个字符之间', trigger: 'blur' },
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmResetPassword, trigger: 'blur' },
  ],
});

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

// 打开重置密码对话框
const openResetPasswordDialog = (user) => {
  selectedUserForReset.value = user;
  resetDialogVisible.value = true;
};

// 提交重置密码
const submitResetPassword = async () => {
  if (!resetPasswordFormRef.value) return;

  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      resetLoading.value = true;
      try {
        const token = localStorage.getItem('token');
        await axios.put(
          `/api/users/${selectedUserForReset.value.id}/reset-password`,
          { newPassword: resetPasswordFormModel.newPassword },
          {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          }
        );
        ElMessage.success('密码重置成功');
        resetDialogVisible.value = false;
      } catch (error) {
        console.error('重置密码失败:', error.response?.data || error.message);
        ElMessage.error(error.response?.data?.message || '重置密码失败');
      } finally {
        resetLoading.value = false;
      }
    } else {
      return false;
    }
  });
};

// 重置密码表单
const resetPasswordForm = () => {
    if(resetPasswordFormRef.value) {
        resetPasswordFormRef.value.resetFields();
    }
    resetPasswordFormModel.newPassword = '';
    resetPasswordFormModel.confirmNewPassword = '';
    selectedUserForReset.value = null;
}

// 删除用户
const deleteUser = (user) => {
  // 添加额外保护，防止删除 ID 为 1 的用户
  if (user.id === 1) {
    ElMessage.warning('不能删除主管理员账户');
    return;
  }
  // 之前的逻辑，防止删除自己
  if (user.username === currentUsername.value) {
     ElMessage.warning('不能删除当前登录的用户');
     return;
  }

  ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const token = localStorage.getItem('token')
      await axios.delete(`/api/users/${user.id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      ElMessage.success('用户删除成功')
      fetchUsers() // 重新获取列表
    } catch (error) {
      console.error('删除用户失败:', error.response?.data || error.message)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// --- 新增：处理角色修改命令 ---
const handleRoleCommand = (user, newRole) => {
  // 添加额外保护
  if (user.id === 1 || user.username === currentUsername.value) {
    ElMessage.warning('不能修改主管理员或自己的角色');
    return;
  }
  if (user.role === newRole) {
    return; // 角色未改变
  }

  ElMessageBox.confirm(`确定要将用户 ${user.username} 的角色修改为 ${newRole} 吗？`, '确认修改角色', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await updateUserRoleApi(user.id, newRole);
  }).catch(() => {
    // 取消操作
    ElMessage.info('已取消角色修改');
  });
};

// --- 新增：调用后端 API 更新角色 ---
const updateUserRoleApi = async (userId, newRole) => {
  try {
    const token = localStorage.getItem('token');
    const response = await axios.put(
      `/api/users/${userId}/role`,
      { role: newRole }, // 请求体中包含 role 字段
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json' // 明确指定 Content-Type
        }
      }
    );

    if (response.data && response.data.status === 'success') {
      ElMessage.success('用户角色更新成功');
      // 更新前端列表中的数据，避免重新请求整个列表
      const index = users.value.findIndex(u => u.id === userId);
      if (index !== -1) {
        users.value[index].role = newRole;
      }
      // 或者调用 fetchUsers() 重新加载列表
      // fetchUsers();
    } else {
      ElMessage.error(response.data?.message || '角色更新失败');
    }
  } catch (error) {
    console.error('更新用户角色失败:', error.response?.data || error.message);
    ElMessage.error(error.response?.data?.message || '角色更新失败');
  }
};
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