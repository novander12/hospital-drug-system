<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>医院药品管理系统</h2>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名" 
                prefix-icon="User"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                class="action-button" 
                :loading="loading" 
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0">
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="用户名" 
                prefix-icon="User"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="密码" 
                prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password" 
                placeholder="确认密码" 
                prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                class="action-button" 
                :loading="loading" 
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const emit = defineEmits(['login-success'])

// 当前激活的标签页
const activeTab = ref('login')

// 登录表单
const loginFormRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerFormRef = ref(null)
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 加载状态
const loading = ref(false)

// 登录处理
const handleLogin = () => {
  if (!loginFormRef.value) return
  
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const response = await axios.post('/auth/login', loginForm)
      
      // 登录成功
      if (response.data) {
        // 从响应中提取用户信息
        const userData = {
          id: response.data.id,
          username: response.data.username || loginForm.username,
          role: response.data.role
        }
        
        // 存储用户信息到本地存储
        localStorage.setItem('user', JSON.stringify(userData))
        // 存储JWT令牌
        localStorage.setItem('token', response.data.token)
        
        ElMessage.success('登录成功')
        
        // 通知父组件登录成功
        emit('login-success', userData)
      } else {
        ElMessage.error('登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

// 注册处理
const handleRegister = () => {
  if (!registerFormRef.value) return
  
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const response = await axios.post('/auth/register', {
        username: registerForm.username,
        password: registerForm.password
      })
      
      if (response.data) {
        ElMessage.success('注册成功，请登录')
        // 切换到登录界面并填充用户名
        activeTab.value = 'login'
        loginForm.username = registerForm.username
        loginForm.password = ''
        
        // 重置注册表单
        if (registerFormRef.value) {
          registerFormRef.value.resetFields()
        }
      }
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.response?.data?.message || '注册失败，用户名可能已存在')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 380px;
}

.login-header {
  text-align: center;
  margin-bottom: 20px;
}

.login-header h2 {
  color: #409EFF;
  font-weight: 600;
}

.action-button {
  width: 100%;
}
</style> 