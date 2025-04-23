<template>
  <div class="inventory-report">
    <h1 class="page-title">库存报告</h1>

    <div class="operation-area">
       <el-button type="success" @click="exportReport" :loading="exportLoading">
         <el-icon><Download /></el-icon> 导出报告 (CSV)
       </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="reportData"
      border
      style="width: 100%"
      :default-sort="{ prop: 'drugName', order: 'ascending' }"
    >
      <el-table-column prop="drugId" label="药品ID" width="100" sortable />
      <el-table-column prop="drugName" label="药品名称" min-width="180" sortable show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" min-width="120" show-overflow-tooltip />
      <el-table-column prop="category" label="类别" min-width="120" sortable show-overflow-tooltip />
      <el-table-column prop="supplier" label="供应商" min-width="150" sortable show-overflow-tooltip />
      <el-table-column prop="totalStock" label="当前总库存" width="150" sortable align="right">
         <template #default="scope">
          <el-tag :type="scope.row.totalStock > 50 ? 'success' : scope.row.totalStock > 0 ? 'warning' : 'danger'">
            {{ scope.row.totalStock }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Download } from '@element-plus/icons-vue';

const reportData = ref([]);
const loading = ref(false);
const exportLoading = ref(false);

// 获取报告数据
const fetchReportData = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/reports/inventory');
    reportData.value = response.data;
  } catch (error) {
    console.error('获取库存报告失败:', error);
    ElMessage.error('获取库存报告失败');
    reportData.value = []; // Clear data on error
  } finally {
    loading.value = false;
  }
};

// 导出报告
const exportReport = async () => {
  exportLoading.value = true;
  try {
    const response = await axios.get('/api/reports/inventory/export', {
      responseType: 'blob' // 重要：告诉 axios 期望接收二进制数据 (Blob)
    });

    // 创建 Blob 对象
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });

    // 从响应头获取文件名 (如果后端设置了 Content-Disposition)
    const contentDisposition = response.headers['content-disposition'];
    let filename = 'inventory_report.csv'; // Default filename
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=UTF-8''(.+)|filename="?([^;"]+)"?/i);
       if (filenameMatch && filenameMatch.length > 1) {
         filename = decodeURIComponent(filenameMatch[1] || filenameMatch[2]).replace(/"/g, '');
       }
    }

    // 创建下载链接
    const downloadLink = document.createElement('a');
    const url = window.URL.createObjectURL(blob);
    downloadLink.href = url;
    downloadLink.download = filename; // 使用后端提供的或默认的文件名

    // 添加到文档并触发点击
    document.body.appendChild(downloadLink);
    downloadLink.click();

    // 清理
    document.body.removeChild(downloadLink);
    window.URL.revokeObjectURL(url);

    ElMessage.success('报告导出成功');

  } catch (error) {
    console.error('导出库存报告失败:', error);
    ElMessage.error('导出库存报告失败');
  } finally {
    exportLoading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchReportData();
});
</script>

<style scoped>
.inventory-report {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #303133; /* Slightly darker title */
}

.operation-area {
  display: flex;
  justify-content: flex-end; /* Align button to the right */
  margin-bottom: 20px;
}

.el-table .el-tag {
  cursor: default; /* Remove text cursor on tag */
}
</style> 