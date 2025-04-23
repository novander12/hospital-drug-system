<template>
  <div class="drug-consumption-report">
    <h1 class="page-title">药品消耗统计报告</h1>

    <!-- 操作区域：日期选择和生成按钮 -->
    <div class="operation-area">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        :disabled-date="disabledDate"
        unlink-panels
        style="margin-right: 10px;"
      />
      <el-button type="primary" @click="generateReport" :loading="loading" :disabled="!dateRange || dateRange.length !== 2">
        <el-icon><Search /></el-icon> 生成报告
      </el-button>
       <!-- Optional: Add Export Button later -->
      
      <el-button type="success" @click="exportReport" :loading="exportLoading" :disabled="reportData.length === 0">
        <el-icon><Download /></el-icon> 导出报告 (CSV)
      </el-button> 
      
    </div>

    <!-- 结果表格 -->
    <el-table
      v-loading="loading"
      :data="reportData"
      border
      style="width: 100%"
      empty-text="请选择日期范围并生成报告"
      :default-sort="{ prop: 'totalConsumedQuantity', order: 'descending' }"
    >
      <el-table-column prop="drugId" label="药品ID" width="100" sortable />
      <el-table-column prop="drugName" label="药品名称" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" min-width="120" show-overflow-tooltip />
      <el-table-column prop="category" label="类别" min-width="120" sortable show-overflow-tooltip />
      <el-table-column prop="totalConsumedQuantity" label="消耗总量" width="150" sortable align="right"/>
    </el-table>

  </div>
</template>

<script setup>
import { ref, reactive, inject } from 'vue';
import axios from 'axios';
import { ElMessage, ElDatePicker, ElButton, ElTable, ElTableColumn, ElIcon } from 'element-plus';
import { Search, Download } from '@element-plus/icons-vue';
import dayjs from 'dayjs'; // Import dayjs for date manipulation

// Inject admin status (though this page is likely admin-only via routing)
const isAdmin = inject('isAdmin');

const dateRange = ref([]); // Stores [startDate, endDate] as strings 'YYYY-MM-DD'
const reportData = ref([]);
const loading = ref(false);
const exportLoading = ref(false); // For export button later

// Disable future dates in date picker
const disabledDate = (time) => {
  return time.getTime() > Date.now();
};

// 生成报告方法
const generateReport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择有效的开始和结束日期');
    return;
  }

  const [startDate, endDate] = dateRange.value;
  
  // Basic validation
   if (dayjs(endDate).isBefore(dayjs(startDate))) {
     ElMessage.error('结束日期不能早于开始日期');
     return;
   }

  loading.value = true;
  reportData.value = []; // Clear previous results

  try {
    // --- 获取 Token ---
    const token = localStorage.getItem('token'); // Corrected key name
    if (!token) {
        ElMessage.error('用户未认证，无法生成报告');
        loading.value = false;
        return;
    }

    const response = await axios.get('/api/reports/consumption', {
      params: {
        startDate: startDate, // 'YYYY-MM-DD'
        endDate: endDate    // 'YYYY-MM-DD'
      },
       headers: { // Add Authorization header
        'Authorization': `Bearer ${token}` // Adjust prefix if needed
      }
    });
    reportData.value = response.data;
     if (reportData.value.length === 0) {
       ElMessage.info('所选日期范围内没有药品消耗记录');
     }
  } catch (error) {
    console.error('生成药品消耗报告失败:', error);
    ElMessage.error(error.response?.data?.message || '生成报告失败');
  } finally {
    loading.value = false;
  }
};

// Placeholder for export function (to be implemented later) -> Updated Export Function

const exportReport = async () => {
  if (reportData.value.length === 0) {
    ElMessage.warning('没有数据可导出');
    return;
  }
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请先选择日期范围并生成报告以进行导出');
    return;
  }

  const [startDate, endDate] = dateRange.value;

  exportLoading.value = true;
  try {
    // 获取存储的 JWT Token
    const token = localStorage.getItem('token'); // Corrected key name
    if (!token) {
        ElMessage.error('用户未认证，无法导出');
        exportLoading.value = false;
        // 可能需要重定向到登录页
        return;
    }

    const response = await axios.get('/api/reports/consumption/export', {
      params: {
        startDate: startDate,
        endDate: endDate
      },
      headers: {
        // 在请求头中添加 Authorization
        'Authorization': `Bearer ${token}` // !重要: 确认 Token 前缀是否为 'Bearer '
      },
      responseType: 'blob' // !重要: 告诉 axios 期望接收二进制数据 (文件)
    });

    // --- 处理 Blob 响应以下载文件 ---

    // 1. 从响应头中获取文件名 (如果后端设置了 Content-Disposition)
    const contentDisposition = response.headers['content-disposition'];
    let filename = 'drug_consumption_report.csv'; // 默认文件名
    if (contentDisposition) {
      // 尝试更健壮地解析包含引号和非ASCII字符的文件名
      const filenameRegex = /filename\*?=['"]?([^'";]+)['"]?/;
      const filenameMatch = contentDisposition.match(filenameRegex);
      if (filenameMatch && filenameMatch[1]) {
           // 如果是 filename* (RFC 5987), 需要解码
          if (contentDisposition.includes("filename*")) {
             try {
                 filename = decodeURIComponent(filenameMatch[1].replace(/UTF-8''/i, ''));
             } catch (e) {
                 console.warn("无法解码 RFC 5987 文件名, 使用原始值:", filenameMatch[1]);
                 filename = filenameMatch[1]; // 回退到使用原始值
             }
          } else {
              filename = filenameMatch[1];
          }
      }
    }


    // 2. 创建 Blob URL
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'text/csv' });
    const downloadUrl = window.URL.createObjectURL(blob);

    // 3. 创建 <a> 标签并模拟点击
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', filename); // 设置下载文件名
    document.body.appendChild(link);
    link.click();

    // 4. 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);

    ElMessage.success('报告导出成功');

  } catch (error) {
    console.error('导出药品消耗报告失败:', error);
     // 尝试解析 Blob 错误信息 (如果响应是 JSON 错误而不是文件)
    if (error.response && error.response.data instanceof Blob && error.response.data.type.toLowerCase().indexOf('json') !== -1) {
        const reader = new FileReader();
        reader.onload = () => {
            try {
                const errorData = JSON.parse(reader.result);
                ElMessage.error(errorData.message || '导出报告失败');
            } catch (parseError) {
                 ElMessage.error('导出报告时发生未知错误(解析错误)');
            }
        };
        reader.onerror = () => {
             ElMessage.error('导出报告时发生未知错误(读取Blob错误)');
        };
        reader.readAsText(error.response.data);
    } else if (error.response && error.response.status === 403) {
         ElMessage.error('权限不足，无法导出报告');
    }
    else {
         ElMessage.error(error.response?.data?.message || '导出报告失败');
    }
  } finally {
    exportLoading.value = false;
  }
};

</script>

<style scoped>
.drug-consumption-report {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.operation-area {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.el-table {
  margin-top: 20px; /* Add some space above the table */
}
</style> 