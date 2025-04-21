 <template>
  <div class="prescription-form">
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-width="100px"
      status-icon
    >
      <el-divider content-position="left">患者信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="患者姓名" prop="patientName">
            <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="患者ID" prop="patientId">
            <el-input v-model="form.patientId" placeholder="请输入患者ID" />
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="年龄" prop="age">
            <el-input-number v-model="form.age" :min="0" :max="150" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别">
              <el-option label="男" value="male" />
              <el-option label="女" value="female" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="科室" prop="department">
            <el-select v-model="form.department" placeholder="请选择科室">
              <el-option v-for="dept in departments" :key="dept.value" :label="dept.label" :value="dept.value" />
            </el-select>
          </el-form-item>
        </el-col>
        
        <el-col :span="8">
          <el-form-item label="医生" prop="doctor">
            <el-select v-model="form.doctor" placeholder="请选择医生">
              <el-option v-for="doc in doctors" :key="doc.value" :label="doc.label" :value="doc.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="诊断" prop="diagnosis">
        <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断信息" />
      </el-form-item>
      
      <el-divider content-position="left">药品信息</el-divider>
      
      <div v-for="(drug, index) in form.drugs" :key="index" class="drug-item">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item :label="index === 0 ? '药品' : ''" :prop="`drugs.${index}.drugId`" :rules="rules.drugId">
              <el-select 
                v-model="drug.drugId" 
                filterable 
                remote 
                reserve-keyword 
                placeholder="搜索药品" 
                :remote-method="searchDrugs" 
                :loading="loading"
              >
                <el-option
                  v-for="item in drugOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="6">
            <el-form-item :label="index === 0 ? '数量' : ''" :prop="`drugs.${index}.quantity`" :rules="rules.quantity">
              <el-input-number v-model="drug.quantity" :min="1" />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item :label="index === 0 ? '用法用量' : ''" :prop="`drugs.${index}.usage`" :rules="rules.usage">
              <el-input v-model="drug.usage" placeholder="例：一日三次，每次一片" />
            </el-form-item>
          </el-col>
          
          <el-col :span="2" class="drug-action">
            <el-button 
              type="danger" 
              circle 
              icon="Delete" 
              @click="removeDrug(index)" 
              v-if="form.drugs.length > 1"
            />
          </el-col>
        </el-row>
      </div>
      
      <div class="add-drug">
        <el-button type="primary" plain icon="Plus" @click="addDrug">添加药品</el-button>
      </div>
      
      <el-divider content-position="left">备注信息</el-divider>
      
      <el-form-item label="备注" prop="remarks">
        <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="可选备注信息" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm(formRef)">提交处方</el-button>
        <el-button @click="resetForm(formRef)">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const emit = defineEmits(['submit-success'])
const formRef = ref(null)
const loading = ref(false)
const departments = ref([])
const doctors = ref([])
const drugOptions = ref([])

// 表单数据
const form = reactive({
  patientName: '',
  patientId: '',
  age: 30,
  gender: '',
  department: '',
  doctor: '',
  diagnosis: '',
  drugs: [
    { drugId: '', quantity: 1, usage: '' }
  ],
  remarks: ''
})

// 验证规则
const rules = {
  patientName: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  patientId: [
    { required: true, message: '请输入患者ID', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  doctor: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  diagnosis: [
    { required: true, message: '请输入诊断信息', trigger: 'blur' }
  ],
  drugId: [
    { required: true, message: '请选择药品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' }
  ],
  usage: [
    { required: true, message: '请输入用法用量', trigger: 'blur' }
  ]
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchDepartments(),
    fetchDoctors()
  ])
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get('/api/departments', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      departments.value = response.data.map(dept => ({
        value: dept.id,
        label: dept.name
      }))
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      return
    }
    
    const response = await axios.get('/api/doctors', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (Array.isArray(response.data)) {
      doctors.value = response.data.map(doctor => ({
        value: doctor.id,
        label: doctor.name
      }))
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 搜索药品
const searchDrugs = async (query) => {
  if (query.length < 2) return
  
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录令牌不存在，请重新登录')
      loading.value = false
      return
    }
    
    const response = await axios.get('/api/drugs/search', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params: { keyword: query }
    })
    
    if (Array.isArray(response.data)) {
      drugOptions.value = response.data.map(drug => ({
        value: drug.id,
        label: `${drug.name} (${drug.specification}) - 库存:${drug.stock}${drug.unit}`,
        stock: drug.stock
      }))
    }
    
    loading.value = false
  } catch (error) {
    console.error('搜索药品失败:', error)
    ElMessage.error('搜索药品失败')
    loading.value = false
  }
}

// 添加药品
const addDrug = () => {
  form.drugs.push({ drugId: '', quantity: 1, usage: '' })
}

// 移除药品
const removeDrug = (index) => {
  form.drugs.splice(index, 1)
}

// 提交表单
const submitForm = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        const token = localStorage.getItem('token')
        if (!token) {
          ElMessage.error('登录令牌不存在，请重新登录')
          return
        }
        
        const response = await axios.post('/api/prescriptions', form, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        console.log('处方创建成功:', response.data)
        emit('submit-success')
        resetForm(formEl)
      } catch (error) {
        console.error('创建处方失败:', error)
        ElMessage.error(error.response?.data?.message || '创建处方失败')
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
  form.drugs = [{ drugId: '', quantity: 1, usage: '' }]
}
</script>

<style scoped>
.prescription-form {
  margin: 20px 0;
}

.drug-item {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.drug-action {
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-drug {
  margin: 20px 0;
  text-align: center;
}
</style>