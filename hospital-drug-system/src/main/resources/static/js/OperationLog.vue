<template>
  <div class="operation-log">
    <h1 class="page-title">操作日志</h1>
    
    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" label-width="80px">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]"
            style="width: 380px;"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="filters.type" placeholder="请选择" clearable style="width: 180px;">
            <el-option
              v-for="item in actionTypes"
              :key="item"
              :label="getActionText(item)"
              :value="item">
              <span>{{ getActionText(item) }} ({{ item }})</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户" v-if="isAdmin">
          <el-input v-model="filters.username" placeholder="输入用户名" clearable style="width: 180px;"/>
        </el-form-item>
         <el-form-item label="操作结果">
          <el-select v-model="filters.result" placeholder="请选择" clearable style="width: 120px;">
             <el-option label="所有" value="" />
             <el-option label="成功" value="SUCCESS" />
             <el-option label="失败" value="FAILURE" />
          </el-select>
        </el-form-item>
         <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="搜索详情或药品名" clearable style="width: 200px;" @keyup.enter="handleFilter"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter" :loading="loading">筛选</el-button>
          <el-button @click="handleResetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作区域 -->
    <div class="operation-area">
      <el-radio-group v-model="logType" @change="handleLogTypeChange">
        <el-radio-button value="all" v-if="isAdmin">所有日志</el-radio-button>
        <el-radio-button value="my">我的日志</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 日志表格 -->
    <el-table
      v-loading="loading"
      :data="logs"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作用户" min-width="120" />
      <el-table-column label="操作类型" min-width="120">
        <template #default="scope">
          <el-tag
            :type="getActionTagType(scope.row.action)"
          >
            {{ getActionText(scope.row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="drugName" label="相关名称" min-width="150" />
      <el-table-column prop="operationResult" label="结果" width="80">
         <template #default="scope">
            <el-tag :type="scope.row.operationResult === 'SUCCESS' ? 'success' : 'danger'">
              {{ scope.row.operationResult === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
      </el-table-column>
      <el-table-column prop="details" label="操作详情" min-width="200" show-overflow-tooltip/>
      <el-table-column label="操作时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.timestamp) }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// --- State ---
const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logType = ref('my')
const actionTypes = ref([])

// 筛选条件
const filters = reactive({
  dateTimeRange: [],
  type: '',
  username: '',
  result: '',
  keyword: ''
})

// --- User Info & Permissions ---
const currentUserInfo = computed(() => {
  const userStr = localStorage.getItem('user');
  try {
    return userStr ? JSON.parse(userStr) : { username: '', role: '' };
  } catch (e) {
    console.error('解析用户数据失败', e);
    return { username: '', role: '' };
  }
});

const isAdmin = computed(() => {
  return currentUserInfo.value.role && currentUserInfo.value.role.toUpperCase() === 'ADMIN';
});

// --- Lifecycle Hooks ---
onMounted(() => {
  console.log('操作日志组件已挂载');
  if (isAdmin.value && logType.value !== 'all') {
    logType.value = 'all';
  } else if (!isAdmin.value) {
    logType.value = 'my';
  }
  fetchActionTypes();
  fetchLogs();
});

// --- Watchers ---
watch(isAdmin, (newIsAdmin, oldIsAdmin) => {
    if (newIsAdmin && !oldIsAdmin) {
        logType.value = 'all';
        handleFilter(); 
    } else if (!newIsAdmin && oldIsAdmin) {
        logType.value = 'my';
        filters.username = ''; 
        handleFilter();
    }
});

// 监听用户角色变化，自动调整日志类型
watch(currentUserInfo, (newUserInfo, oldUserInfo) => {
  console.log('currentUserInfo 发生变化:', newUserInfo);
  if (newUserInfo.role !== oldUserInfo?.role) {
    // Role changed, adjust logType if necessary
     if (isAdmin.value) {
      logType.value = 'all';
    } else {
      logType.value = 'my';
    }
    fetchLogs(); // Re-fetch logs based on new role/logType
  } else if (!newUserInfo.username && oldUserInfo?.username) {
     // User logged out (no username anymore)
     logs.value = [];
     total.value = 0;
  }
}, { deep: true });

// 初始化 logType (Moved before onMounted)
if (isAdmin.value) {
  logType.value = 'all';
} else {
  logType.value = 'my';
};

// Initialization on mount (Ensure correct syntax)
onMounted(() => {
  console.log('操作日志组件已挂载');
  fetchLogs();
});

// Cleanup before unmount
onBeforeUnmount(() => {
  console.log('操作日志组件即将卸载，清理资源');
});

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      // Handle not logged in state if necessary, though router guard should prevent this page access
      console.warn('尝试获取日志但未登录');
      logs.value = [];
      total.value = 0;
      loading.value = false;
      return;
    }

    // Determine endpoint based on role and selected type
    let effectiveLogType = logType.value;
    if (effectiveLogType === 'all' && !isAdmin.value) {
      console.warn('非管理员尝试获取所有日志，强制切换为 \'my\'');
      effectiveLogType = 'my';
      // Update the radio button state if needed, though watcher should handle it
      logType.value = 'my'; 
    }
    const endpoint = effectiveLogType === 'my' ? '/api/logs/my' : '/api/logs';
    
    // 准备查询参数
    const params = {
      page: currentPage.value - 1, // 后端 Pageable 页码从 0 开始
      size: pageSize.value,
      // 传递筛选参数 (只有非空值才传递，或由后端处理 null)
      startDate: filters.dateTimeRange?.[0] || null,
      endDate: filters.dateTimeRange?.[1] || null,
      type: filters.type || null,
      result: filters.result || null, // 空字符串代表所有，后端会处理
      keyword: filters.keyword || null,
      // sort: 'timestamp,desc' // 让后端处理默认排序
    };

    // 只有管理员查看所有日志时，才允许传递 username 筛选
    if (effectiveLogType === 'all' && isAdmin.value) {
        params.username = filters.username || null;
    }

    // 清理 null 或空字符串参数，减少 URL 长度 (可选)
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key];
      }
    });

    console.log(`获取日志: 类型=${effectiveLogType}, 端点=${endpoint}, IsAdmin=${isAdmin.value}, 参数:`, params);

    const response = await axios.get(endpoint, {
      headers: { 'Authorization': `Bearer ${token}` },
      params: params,
      timeout: 15000
    });

    // 处理 Page 对象
    if (response.data && typeof response.data === 'object') {
      logs.value = response.data.content || []; // 获取内容数组
      total.value = response.data.totalElements || 0; // 获取总元素数
      // 可以在这里检查 response.data.totalPages, response.data.number (当前页码, 0-based) 等
      if (total.value === 0 && currentPage.value === 1) {
           // 避免在切换页码时也提示
           // ElMessage.info('没有查询到符合条件的操作日志数据');
      }
    } else {
      console.warn('服务器返回的日志数据格式不正确:', response.data);
      logs.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取操作日志失败:', error);
    if (error.response?.status !== 401) { // 避免登录失效时重复提示
        ElMessage.error('获取操作日志失败: ' + (error.response?.data?.message || error.message));
    }
    logs.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理日志类型变化
const handleLogTypeChange = (newType) => {
  console.log('手动切换日志类型为:', newType);
  // 清除非管理员不能使用的筛选条件
  if (newType === 'my' && !isAdmin.value) {
      filters.username = '';
  }
  currentPage.value = 1;
  fetchLogs();
};

// 分页
const handleCurrentChange = (page) => {
  console.log(`页码切换到: ${page}`);
  currentPage.value = page;
  fetchLogs(); // 页码改变，调用 fetchLogs 获取新页数据
};

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
  });
};

// 获取操作类型对应的标签类型
const getActionTagType = (action) => {
  switch (action) {
    case 'ADD':
      return 'success'
    case 'UPDATE':
      return 'warning'
    case 'DELETE':
      return 'danger'
    case 'BATCH_DELETE':
      return 'danger'
    default:
      return 'info'
  }
};

// 获取操作类型对应的文本
const getActionText = (action) => {
 switch (action) {
    case 'ADD':
      return '添加'
    case 'UPDATE':
      return '更新'
    case 'DELETE':
      return '删除'
    case 'BATCH_DELETE':
      return '批量删除'
    default:
      return action
  }
};

// 获取操作类型
const fetchActionTypes = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) return;
    const response = await axios.get('/api/logs/types', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    actionTypes.value = response.data || [];
    console.log('Action types loaded:', actionTypes.value);
  } catch (error) {
    console.error('获取操作类型列表失败:', error);
    // ElMessage.error('获取操作类型列表失败'); // Avoid double message if fetchLogs also fails
  }
};

// 添加handleFilter函数
const handleFilter = () => {
    currentPage.value = 1; // 筛选时总是回到第一页
    fetchLogs();
};

// 添加handleResetFilters函数
const handleResetFilters = () => {
    filters.dateTimeRange = [];
    filters.type = '';
    filters.username = '';
    filters.result = '';
    filters.keyword = '';
    currentPage.value = 1;
    fetchLogs(); 
};

</script>

<style scoped>
.operation-log {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.operation-area {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.filter-card {
  margin-bottom: 20px;
  background-color: #f9f9f9; 
}
.filter-card .el-form-item {
    margin-bottom: 10px; 
    margin-right: 15px; /* Add some right margin for inline items */
}
</style> 