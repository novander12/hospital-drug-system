<template>
  <div class="prescription-list">
    <h1 class="page-title">处方管理</h1>

    <!-- 搜索/筛选区域 -->
    <div class="operation-area">
      <el-input 
        v-model="searchKeyword" 
        placeholder="按患者姓名或医生搜索"
        class="search-input"
        clearable
        @clear="fetchPrescriptions"
      >
        <template #append>
          <el-button @click="fetchPrescriptions">
             <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="fetchPrescriptions" style="width: 150px; margin-left: 10px;">
        <el-option label="待处理" value="PENDING"></el-option>
        <el-option label="已审核" value="APPROVED"></el-option>
        <el-option label="已发药" value="DISPENSED"></el-option>
        <el-option label="已取消" value="CANCELLED"></el-option>
      </el-select>
      <el-button type="primary" @click="openCreateDialog" style="margin-left: auto;" v-if="isAdmin || userInfo?.role === 'PHARMACIST'">
         <el-icon><Plus /></el-icon> 新增处方
      </el-button>
    </div>

    <!-- 处方表格 -->
    <el-table v-loading="loading" :data="prescriptions" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" sortable />
      <el-table-column prop="patientName" label="患者姓名" min-width="100" show-overflow-tooltip />
      <el-table-column prop="prescribingDoctor" label="开方医生" min-width="100" show-overflow-tooltip />
      <el-table-column prop="prescriptionDate" label="开方日期" min-width="110" sortable>
        <template #default="scope">{{ formatDate(scope.row.prescriptionDate) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100" sortable>
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="itemCount" label="药品数" width="80">
         <template #default="scope">{{ scope.row.items?.length || 0 }}</template>
      </el-table-column>
       <el-table-column prop="createdAt" label="创建时间" min-width="160" sortable>
        <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button link size="small" @click="viewDetails(scope.row)">详情</el-button>
          <el-button link type="primary" size="small" @click="openStatusUpdateDialog(scope.row, 'APPROVED')" 
                     :disabled="scope.row.status !== 'PENDING'" v-if="isAdmin || userInfo?.role === 'PHARMACIST'">审核通过</el-button>
          
          <!-- Debugging Info -->
          <span style="font-size: 10px; color: grey; margin-left: 5px;">
            (S:{{ scope.row.status }}/R:{{ userInfo?.role }})
          </span>
          
          <el-button link type="success" size="small" @click="openStatusUpdateDialog(scope.row, 'DISPENSED')" 
                     :disabled="scope.row.status !== 'APPROVED'" v-if="userInfo?.role === 'PHARMACIST' || userInfo?.role === 'NURSE'">确认发药</el-button>
          <el-button link type="danger" size="small" @click="openStatusUpdateDialog(scope.row, 'CANCELLED')" 
                     :disabled="scope.row.status === 'DISPENSED' || scope.row.status === 'CANCELLED'" v-if="isAdmin">取消处方</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 (如果需要，后期添加) -->
    <!-- 
    <div class="pagination-container">
      <el-pagination ... />
    </div> 
    -->

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailsDialogVisible" title="处方详情" width="60%">
       <el-descriptions :column="2" border>
        <el-descriptions-item label="处方ID">{{ currentPrescription?.id }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ currentPrescription?.patientName }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPrescription?.patientAge }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentPrescription?.patientGender }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentPrescription?.patientIdNumber }}</el-descriptions-item>
        <el-descriptions-item label="开方医生">{{ currentPrescription?.prescribingDoctor }}</el-descriptions-item>
        <el-descriptions-item label="开方日期">{{ formatDate(currentPrescription?.prescriptionDate) }}</el-descriptions-item>
        <el-descriptions-item label="诊断信息">{{ currentPrescription?.diagnosis }}</el-descriptions-item>
         <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentPrescription?.status)">{{ getStatusText(currentPrescription?.status) }}</el-tag>
        </el-descriptions-item>
         <el-descriptions-item label="创建人">{{ currentPrescription?.createdByUser?.username }}</el-descriptions-item>
         <el-descriptions-item label="创建时间">{{ formatDateTime(currentPrescription?.createdAt) }}</el-descriptions-item>
         <el-descriptions-item label="更新时间">{{ formatDateTime(currentPrescription?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>药品列表</el-divider>
      <el-table :data="currentPrescription?.items" border size="small">
        <el-table-column prop="drug.name" label="药品名称" />
        <el-table-column prop="drug.spec" label="规格" />
        <el-table-column prop="quantity" label="数量" width="80"/>
        <el-table-column prop="dosage" label="用法用量" />
        <el-table-column prop="frequency" label="频次" />
        <el-table-column prop="notes" label="备注" />
      </el-table>
      <template #footer>
        <el-button @click="detailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
     <!-- 更新状态对话框 -->
    <el-dialog v-model="statusUpdateDialogVisible" :title="`更新处方状态为: ${getStatusText(targetStatus)}`" width="400px">
      <el-form label-width="80px">
         <el-form-item label="处方ID">
          <span>{{ currentPrescription?.id }}</span>
        </el-form-item>
        <el-form-item label="患者">
          <span>{{ currentPrescription?.patientName }}</span>
        </el-form-item>
         <el-form-item label="当前状态">
            <el-tag :type="getStatusTagType(currentPrescription?.status)">{{ getStatusText(currentPrescription?.status) }}</el-tag>
        </el-form-item>
         <el-form-item label="目标状态">
             <el-tag :type="getStatusTagType(targetStatus)">{{ getStatusText(targetStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="statusUpdateRemarks" type="textarea" placeholder="选填，例如取消原因、发药说明等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusUpdateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatusUpdate">确认更新</el-button>
      </template>
    </el-dialog>

    <!-- 新增处方组件 -->
    <PrescriptionForm 
      :visible="createDialogVisible" 
      @close="createDialogVisible = false" 
      @created="handlePrescriptionCreated" 
    />

  </div>
</template>

<script setup>
import { ref, onMounted, computed, inject } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus } from '@element-plus/icons-vue'; // Import icons
import axios from 'axios';
import PrescriptionForm from './components/PrescriptionForm.vue'; // Import the form component

// Inject user info and admin status
const userInfo = inject('userInfo');
const isAdmin = inject('isAdmin');

const prescriptions = ref([]);
const loading = ref(false);
const searchKeyword = ref('');
const filterStatus = ref(''); // 用于状态筛选

const detailsDialogVisible = ref(false);
const statusUpdateDialogVisible = ref(false);
const createDialogVisible = ref(false); // 控制新增表单显示
const currentPrescription = ref(null); // 当前操作或查看的处方
const targetStatus = ref(null); // 目标更新状态
const statusUpdateRemarks = ref(''); // 更新状态的备注

// 获取处方列表
const fetchPrescriptions = async () => {
  loading.value = true;
  try {
    // TODO: 后期可以添加分页和过滤参数到请求中
    const params = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value; // 后端需要支持按关键字搜索
    if (filterStatus.value) params.status = filterStatus.value; // 后端需要支持按状态过滤
    
    const response = await axios.get('/api/prescriptions', { params });
    prescriptions.value = response.data;
  } catch (error) {
    console.error('获取处方列表失败:', error);
    ElMessage.error('获取处方列表失败');
    prescriptions.value = []; // 出错时清空列表
  } finally {
    loading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchPrescriptions();
});

// 查看详情
const viewDetails = (prescription) => {
  currentPrescription.value = prescription;
  detailsDialogVisible.value = true;
};

// 打开状态更新对话框
const openStatusUpdateDialog = (prescription, newStatus) => {
  currentPrescription.value = prescription;
  targetStatus.value = newStatus;
  statusUpdateRemarks.value = ''; // 清空备注
  statusUpdateDialogVisible.value = true;
};

// 提交状态更新
const submitStatusUpdate = async () => {
  if (!currentPrescription.value || !targetStatus.value) return;

  try {
    const payload = {
      newStatus: targetStatus.value,
      remarks: statusUpdateRemarks.value
    };
    await axios.put(`/api/prescriptions/${currentPrescription.value.id}/status`, payload);
    ElMessage.success(`处方状态已更新为: ${getStatusText(targetStatus.value)}`);
    statusUpdateDialogVisible.value = false;
    fetchPrescriptions(); // 刷新列表
  } catch (error) {
     console.error('更新处方状态失败:', error.response?.data?.message || error.message);
     // 显示后端返回的错误信息，或者通用错误信息
     ElMessage.error(error.response?.data?.message || '更新处方状态失败');
  }
};

// 打开新增处方对话框
const openCreateDialog = () => {
  createDialogVisible.value = true; 
};

// --- Added: Handler for when a prescription is created by the form ---
const handlePrescriptionCreated = () => {
  createDialogVisible.value = false; // Close the dialog
  fetchPrescriptions(); // Refresh the list
};

// --- Helper Functions ---
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
     // Spring Boot LocalDate might be returned as array [year, month, day]
     if (Array.isArray(dateString)) {
       return dateString.join('-');
     }
     const date = new Date(dateString);
     return date.toLocaleDateString('zh-CN');
   } catch (e) {
     return dateString; // Return original if formatting fails
   }
};

const formatDateTime = (dateTimeString) => {
   if (!dateTimeString) return '';
   try {
     let dtStr = Array.isArray(dateTimeString) ? dateTimeString.join('-') : dateTimeString;
     dtStr = dtStr.replace('T', ' ');
     const date = new Date(dtStr);
     if (isNaN(date.getTime())) return dateTimeString;
     return date.toLocaleString('zh-CN', { hour12: false });
   } catch (e) {
     return dateTimeString;
   }
};

const getStatusTagType = (status) => {
  switch (status) {
    case 'PENDING': return 'info';
    case 'APPROVED': return 'warning';
    case 'DISPENSED': return 'success';
    case 'CANCELLED': return 'danger';
    default: return 'info';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待处理';
    case 'APPROVED': return '已审核';
    case 'DISPENSED': return '已发药';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};

</script>

<style scoped>
.prescription-list {
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
  align-items: center;
  margin-bottom: 20px;
}
.search-input {
  width: 300px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.el-button + .el-button {
    margin-left: 8px; /* Adjust button spacing */
}
.el-table .el-button.is-link {
    padding: 0 5px; /* Adjust link button padding */
}
</style> 