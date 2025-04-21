<template>
  <div class="prescription">
    <h1 class="page-title">处方管理</h1>
    
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="新增处方" name="new">
        <div class="tab-content">
          <PrescriptionForm @submit-success="onPrescriptionSubmitted" />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="处方查询" name="query">
        <div class="tab-content">
          <PrescriptionQuery />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="发药记录" name="dispensing">
        <div class="tab-content">
          <DispensingRecords />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PrescriptionForm from './components/PrescriptionForm.vue'
import PrescriptionQuery from './components/PrescriptionQuery.vue'
import DispensingRecords from './components/DispensingRecords.vue'

const activeTab = ref('new')

// 处理选项卡切换
const handleTabClick = () => {
  console.log('切换到选项卡:', activeTab.value)
}

// 处方提交成功后回调
const onPrescriptionSubmitted = () => {
  ElMessage.success('处方创建成功')
  // 切换到查询选项卡
  activeTab.value = 'query'
}
</script>

<style scoped>
.prescription {
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
</style> 