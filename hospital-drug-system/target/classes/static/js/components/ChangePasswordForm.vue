 <template>
  <el-form
    ref="passwordFormRef"
    :model="passwordForm"
    :rules="rules"
    label-width="100px"
    class="change-password-form"
  >
    <el-form-item label="旧密码" prop="oldPassword">
      <el-input
        v-model="passwordForm.oldPassword"
        type="password"
        placeholder="请输入当前密码"
        show-password
      />
    </el-form-item>
    <el-form-item label="新密码" prop="newPassword">
      <el-input
        v-model="passwordForm.newPassword"
        type="password"
        placeholder="请输入新密码（6-20位）"
        show-password
      />
    </el-form-item>
    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input
        v-model="passwordForm.confirmPassword"
        type="password"
        placeholder="请再次输入新密码"
        show-password
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm" :loading="loading">确认修改</el-button>
      <el-button @click="resetForm">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const passwordFormRef = ref(null);
const loading = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 自定义验证规则：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const rules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在 6 到 20 个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
});

const submitForm = async () => {
  if (!passwordFormRef.value) return;
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const payload = {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        };
        await axios.put('/api/users/change-password', payload);
        ElMessage.success('密码修改成功！');
        resetForm();
        // 可选：如果是在对话框中，可以 emit 一个事件通知父组件关闭
        // emit('success');
      } catch (error) {
        console.error('修改密码失败:', error.response?.data || error.message);
        ElMessage.error(error.response?.data?.message || '修改密码失败，请检查旧密码是否正确');
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确');
      return false;
    }
  });
};

const resetForm = () => {
  if (!passwordFormRef.value) return;
  passwordFormRef.value.resetFields();
};

// 如果这个组件是在对话框中使用，可以暴露 resetForm 方法
// defineExpose({ resetForm });

</script>

<style scoped>
.change-password-form {
  max-width: 400px;
  /* 如果不是在对话框中，可以添加一些边距 */
  /* margin: 20px auto; */
}
</style>
