 <template>
  <div class="user-profile">
    <h1 class="page-title">个人信息管理</h1>
    
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="个人信息" name="info">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-button class="edit-button" type="primary" size="small" @click="startEditing">
                  {{ editing ? '取消编辑' : '编辑资料' }}
                </el-button>
              </div>
            </template>
            
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              :disabled="!editing"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" />
              </el-form-item>
              
              <el-form-item label="角色">
                <el-input v-model="profileForm.role" disabled />
              </el-form-item>
              
              <el-form-item label="部门" prop="department">
                <el-input v-model="profileForm.department" />
              </el-form-item>
              
              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="profileForm.email" />
              </el-form-item>
              
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
              
              <el-form-item v-if="editing">
                <el-button type="primary" @click="submitProfileForm(profileFormRef)">保存</el-button>
                <el-button @click="resetForm(profileFormRef)">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>修改密码</span>
              </div>
            </template>
            
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
            >
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  placeholder="请输入当前密码"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  placeholder="请输入新密码"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  placeholder="请再次输入新密码"
                  type="password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="submitPasswordForm(passwordFormRef)">修改密码</el-button>
                <el-button @click="resetForm(passwordFormRef)">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="登录历史" name="loginHistory">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>登录历史记录</span>
                <el-button type="primary" size="small" @click="refreshLoginHistory">
                  刷新
                </el-button>
              </div>
            </template>
            
            <el-table
              v-loading="historyLoading"
              :data="loginHistory"
              style="width: 100%"
              border
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="loginTime" label="登录时间" min-width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.loginTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="ipAddress" label="IP地址" width="150" />
              <el-table-column prop="device" label="设备信息" min-width="200" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'">
                    {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
                  </el-tag>
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
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 选项卡
const activeTab = ref('info')
const editing = ref(false)

// 表单引用
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 个人信息表单
const profileForm = reactive({
  username: '',
  realName: '',
  role: '',
  department: '',
  email: '',
  phone: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 登录历史
const loginHistory = ref([])
const historyLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 个人信息表单验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 初始化
onMounted(() => {
  loadUserProfile()
  if (activeTab.value === 'loginHistory') {
    fetchLoginHistory()
  }
})

// 处理选项卡切换
const handleTabClick = () => {
  if (activeTab.value === 'loginHistory') {
    fetchLoginHistory()
  }
}

// 加载用户信息
const loadUserProfile = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get('/api/users/profile', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const userProfile = response.data
    profileForm.username = userProfile.username || ''
    profileForm.realName = userProfile.realName || ''
    profileForm.role = userProfile.role || ''
    profileForm.department = userProfile.department || ''
    profileForm.email = userProfile.email || ''
    profileForm.phone = userProfile.phone || ''
    
  } catch (error) {
    console.error('获取用户信息失败:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '获取用户信息失败')
    }
  }
}

// 获取登录历史
const fetchLoginHistory = async () => {
  historyLoading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      loginHistory.value = []
      total.value = 0
      historyLoading.value = false
      return
    }
    
    const response = await axios.get('/api/users/login-history', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      loginHistory.value = response.data
      total.value = response.data.length
    } else {
      console.warn('服务器返回的登录历史数据不是数组格式:', response.data)
      loginHistory.value = []
      total.value = 0
    }
    
    historyLoading.value = false
  } catch (error) {
    console.error('获取登录历史失败:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '获取登录历史失败')
    }
    
    loginHistory.value = []
    total.value = 0
    historyLoading.value = false
  }
}

// 刷新登录历史
const refreshLoginHistory = () => {
  currentPage.value = 1
  fetchLoginHistory()
}

// 开始编辑个人信息
const startEditing = () => {
  editing.value = !editing.value
  if (!editing.value) {
    // 如果取消编辑，恢复原始数据
    loadUserProfile()
  }
}

// 提交个人信息表单
const submitProfileForm = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('登录令牌不存在，请重新登录')
          return
        }
        
        const response = await axios.put('/api/users/profile', {
          realName: profileForm.realName,
          department: profileForm.department,
          email: profileForm.email,
          phone: profileForm.phone
        }, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        ElMessage.success('个人信息更新成功')
        editing.value = false
      } catch (error) {
        console.error('更新个人信息失败:', error)
        
        if (error.response?.status === 401 || error.response?.status === 403) {
          ElMessage.error('登录已过期或权限不足，请重新登录')
        } else {
          ElMessage.error(error.response?.data?.message || '更新个人信息失败')
        }
      }
    } else {
      ElMessage.error('请完善表单信息')
    }
  })
}

// 提交密码表单
const submitPasswordForm = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('登录令牌不存在，请重新登录')
          return
        }
        
        const response = await axios.put('/api/users/change-password', {
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        }, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        ElMessage.success('密码修改成功，请重新登录')
        resetForm(formEl)
        
        // 可以选择自动登出
        // localStorage.removeItem('token')
        // localStorage.removeItem('user')
        // 重定向到登录页面
        // window.location.href = '/login'
      } catch (error) {
        console.error('修改密码失败:', error)
        
        if (error.response?.status === 401 || error.response?.status === 403) {
          ElMessage.error('当前密码错误或登录已过期')
        } else {
          ElMessage.error(error.response?.data?.message || '修改密码失败')
        }
      }
    } else {
      ElMessage.error('请完善表单信息')
    }
  })
}

// 重置表单
const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 计算分页后的数据
const paginatedHistory = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  return loginHistory.value.slice(startIndex, startIndex + pageSize.value)
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
.user-profile {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.tab-content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style>