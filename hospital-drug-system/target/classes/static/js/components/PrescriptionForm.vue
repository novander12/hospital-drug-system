<template>
  <el-dialog
    :model-value="visible"
    title="新增处方"
    width="70%"
    @close="closeDialog"
    :close-on-click-modal="false"
  >
    <el-form :model="prescriptionForm" ref="prescriptionFormRef" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="患者姓名" prop="patientName">
            <el-input v-model="prescriptionForm.patientName" placeholder="请输入患者姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="患者年龄" prop="patientAge">
            <el-input-number v-model="prescriptionForm.patientAge" :min="0" :max="150" placeholder="年龄" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="患者性别" prop="patientGender">
            <el-select v-model="prescriptionForm.patientGender" placeholder="请选择性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
         <el-col :span="8">
           <el-form-item label="身份证号" prop="patientIdNumber">
             <el-input v-model="prescriptionForm.patientIdNumber" placeholder="选填" />
           </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="开方医生" prop="prescribingDoctor">
            <el-input v-model="prescriptionForm.prescribingDoctor" placeholder="请输入医生姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="开方日期" prop="prescriptionDate">
            <el-date-picker
              v-model="prescriptionForm.prescriptionDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
      </el-row>
       <el-form-item label="诊断信息" prop="diagnosis">
          <el-input v-model="prescriptionForm.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断信息" />
        </el-form-item>

      <el-divider>药品信息</el-divider>

      <div v-for="(item, index) in prescriptionForm.items" :key="index" class="prescription-item">
        <el-row :gutter="10" align="middle">
          <el-col :span="8">
            <el-form-item 
              :label="`药品 ${index + 1}`" 
              :prop="`items.${index}.drugId`" 
              :rules="{ required: true, message: '请选择药品', trigger: 'change' }"
            >
             <!-- 使用远程搜索选择药品 -->
             <el-select
                v-model="item.drugId"
                filterable
                remote
                reserve-keyword
                placeholder="搜索药品名称"
                :remote-method="searchDrugs"
                :loading="drugSearchLoading"
                style="width: 100%;"
                @change="(drugId) => handleDrugSelection(index, drugId)"
              >
                <el-option
                  v-for="drug in availableDrugs"
                  :key="drug.id"
                  :label="`${drug.name} (${drug.spec}) - 库存: ${drug.stock}`"
                  :value="drug.id"
                  :disabled="drug.stock <= 0"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="数量" label-width="50px" :prop="`items.${index}.quantity`" :rules="rules.itemQuantity">
              <el-input-number 
                 v-model="item.quantity" 
                 :min="1" 
                 :max="item.maxQuantity || 999" 
                 controls-position="right" 
                 style="width: 100%;" 
               />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="用法" label-width="50px" :prop="`items.${index}.dosage`">
              <el-input v-model="item.dosage" placeholder="如：每日3次，每次1片" />
            </el-form-item>
          </el-col>
           <el-col :span="5">
            <el-form-item label="频次" label-width="50px" :prop="`items.${index}.frequency`">
              <el-input v-model="item.frequency" placeholder="如：饭后服用" />
            </el-form-item>
          </el-col>
          <el-col :span="2">
            <el-button type="danger" :icon="Delete" circle @click="removeItem(index)" :disabled="prescriptionForm.items.length <= 1" />
          </el-col>
        </el-row>
         <el-form-item label="备注" label-width="100px" :prop="`items.${index}.notes`" class="item-notes">
            <el-input v-model="item.notes" placeholder="药品注意事项 (选填)" size="small"/>
          </el-form-item>
      </div>

      <el-form-item>
        <el-button type="success" @click="addItem">添加药品</el-button>
      </el-form-item>

    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submitPrescription" :loading="submitLoading">提交处方</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import _ from 'lodash'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close', 'created'])

const prescriptionFormRef = ref(null)
const submitLoading = ref(false)
const drugSearchLoading = ref(false)
const availableDrugs = ref([])

const initialItem = () => ({ drugId: null, quantity: 1, dosage: '', frequency: '', notes: '', maxQuantity: 999 })
const prescriptionForm = reactive({
  patientName: '',
  patientAge: null,
  patientGender: '',
  patientIdNumber: '',
  prescribingDoctor: '',
  prescriptionDate: new Date(),
  diagnosis: '',
  items: [initialItem()],
})

const rules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  prescribingDoctor: [{ required: true, message: '请输入开方医生姓名', trigger: 'blur' }],
  prescriptionDate: [{ required: true, message: '请选择开方日期', trigger: 'change' }],
  items: [{ required: true, message: '请至少添加一种药品', trigger: 'blur' }],
  itemQuantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ]
}

const closeDialog = () => {
  resetForm()
  emit('close')
}

const resetForm = () => {
  if (prescriptionFormRef.value) {
    prescriptionFormRef.value.resetFields()
  }
  Object.assign(prescriptionForm, {
    patientName: '',
    patientAge: null,
    patientGender: '',
    patientIdNumber: '',
    prescribingDoctor: '',
    prescriptionDate: new Date(),
    diagnosis: '',
    items: [initialItem()],
  })
  availableDrugs.value = []
}

const addItem = () => {
  prescriptionForm.items.push(initialItem())
}

const removeItem = (index) => {
  if (prescriptionForm.items.length > 1) {
    prescriptionForm.items.splice(index, 1)
  }
}

const searchDrugs = _.debounce(async (query) => {
  if (query) {
    drugSearchLoading.value = true
    try {
      const response = await axios.get('/api/drugs/search', { params: { name: query } })
      if (response.data && Array.isArray(response.data.drugs)) {
        availableDrugs.value = response.data.drugs
      } else if (Array.isArray(response.data)) {
        availableDrugs.value = response.data
      } else {
        availableDrugs.value = []
      }
    } catch (error) {
      console.error('搜索药品失败:', error)
      availableDrugs.value = []
    } finally {
      drugSearchLoading.value = false
    }
  } else {
    availableDrugs.value = []
  }
}, 300)

const handleDrugSelection = (index, drugId) => {
  const selectedDrug = availableDrugs.value.find(d => d.id === drugId)
  if (selectedDrug) {
    prescriptionForm.items[index].maxQuantity = selectedDrug.stock
    if (prescriptionForm.items[index].quantity > selectedDrug.stock) {
      prescriptionForm.items[index].quantity = selectedDrug.stock
    }
  } else {
    prescriptionForm.items[index].maxQuantity = 999
  }
}

const submitPrescription = async () => {
  if (!prescriptionFormRef.value) return

  await prescriptionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const formattedDate = prescriptionForm.prescriptionDate instanceof Date
          ? prescriptionForm.prescriptionDate.toISOString().split('T')[0]
          : prescriptionForm.prescriptionDate
        
        const payload = {
          ...prescriptionForm,
          prescriptionDate: formattedDate,
          items: prescriptionForm.items.map(item => ({
            drugId: item.drugId,
            quantity: item.quantity,
            dosage: item.dosage,
            frequency: item.frequency,
            notes: item.notes
          }))
        }
        
        console.log("Submitting payload:", JSON.stringify(payload, null, 2))

        await axios.post('/api/prescriptions', payload)
        ElMessage.success('处方创建成功')
        emit('created')
        closeDialog()
      } catch (error) {
        console.error('创建处方失败:', error.response?.data || error.message)
        ElMessage.error(error.response?.data?.message || '创建处方失败')
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.warning('请检查表单填写是否完整且正确')
    }
  })
}

watch(() => props.visible, (newValue) => {
  if (newValue) {
    nextTick(() => {
      resetForm()
    })
  }
})
</script>

<style scoped>
.prescription-item {
  border: 1px solid #eee;
  padding: 15px 15px 0 15px;
  margin-bottom: 15px;
  border-radius: 4px;
  position: relative;
}

.item-notes {
    margin-top: -10px;
    margin-bottom: 10px;
}

.el-col .el-button {
    margin-top: 8px;
}

.el-form-item {
    margin-bottom: 18px;
}

.el-input-number .el-input__inner {
  text-align: left !important; 
}
</style>