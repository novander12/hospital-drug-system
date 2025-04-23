<template>
  <div class="user-settings-page">
    <h1 class="page-title">用户设置</h1>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      <ChangePasswordForm />
    </el-card>

    <!-- 未来可以添加其他设置项，例如修改个人信息等 -->
    <!-- 
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>修改个人信息</span>
        </div>
      </template>
      <div>...</div>
    </el-card> 
    -->

  </div>
</template>

<script setup>
import ChangePasswordForm from './components/ChangePasswordForm.vue';

// 如果需要加载当前用户信息等，可以在这里添加逻辑

</script>

<style scoped>
.user-settings-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF; /* Or your theme color */
}

.box-card {
  max-width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 