<template>
  <el-container class="main-container">
    <!-- 侧边菜单 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="app-aside">
      <el-menu
        :default-active="activeMenu"
        router
        class="el-menu-vertical"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
         <!-- Logo Area Removed -->
         <!-- <div class="logo-container" :class="{'is-collapse': isCollapse}">
            <img src="/assets/logo.png" class="logo" alt="Logo" v-if="!isCollapse"/>
            <img src="/assets/logo-small.png" class="logo-small" alt="Logo" v-else/>
            <span v-if="!isCollapse" class="system-title">医院药品系统</span>
          </div> -->

        <!-- 菜单项 -->
        <el-menu-item index="/drug-management">
          <el-icon><Box /></el-icon>
          <template #title><span>药品管理</span></template>
        </el-menu-item>
        <el-menu-item index="/drug-statistics">
          <el-icon><DataAnalysis /></el-icon>
           <template #title><span>药品统计</span></template>
        </el-menu-item>
        <el-menu-item index="/prescriptions">
          <el-icon><DocumentChecked /></el-icon>
          <template #title><span>处方管理</span></template>
        </el-menu-item>
        <el-menu-item index="/inventory-report" v-if="isAdmin">
          <el-icon><DataAnalysis /></el-icon>
          <template #title><span>库存报告</span></template>
        </el-menu-item>
        <el-menu-item index="/drug-consumption-report" v-if="isAdmin">
          <el-icon><Histogram /></el-icon>
          <template #title><span>药品消耗统计</span></template>
        </el-menu-item>
        <el-menu-item index="/operation-log" v-if="canViewOperationLog">
          <el-icon><Document /></el-icon>
           <template #title><span>操作日志</span></template>
        </el-menu-item>
        <!-- Admin only menu items -->
        <template v-if="isAdmin">
           <el-menu-item index="/user-management">
             <el-icon><User /></el-icon>
             <template #title><span>用户管理</span></template>
           </el-menu-item>
           <el-menu-item index="/system-settings">
            <el-icon><Setting /></el-icon>
            <template #title><span>系统设置</span></template>
           </el-menu-item>
        </template>
        <!-- Add User Settings menu item accessible to all users -->
        <el-menu-item index="/user-settings">
          <el-icon><Setting /></el-icon>
          <template #title><span>用户设置</span></template>
        </el-menu-item>
         <!-- Add other non-admin menu items here later -->
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
         <div class="header-left">
           <el-icon @click="toggleCollapse" class="collapse-icon">
             <component :is="isCollapse ? 'Expand' : 'Fold'" />
           </el-icon>
           <span class="header-title">医院药品管理系统</span>
         </div>
        <div class="user-info">
          <span>{{ userInfo.username }}</span>
          <span class="user-role">({{ displayedUserRole }})</span>
          <el-dropdown trigger="click" @command="handleCommand">
            <el-avatar :size="32" :icon="UserFilled" style="cursor: pointer;"/>
            <template #dropdown>
              <!-- <el-dropdown-item command="profile">个人信息</el-dropdown-item> -->
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="app-main">
        <!-- 路由视图 -->
        <router-view v-slot="{ Component }">
          <!-- <keep-alive> --> <!-- Temporarily remove keep-alive -->
            <component :is="Component" />
          <!-- </keep-alive> -->
        </router-view>
      </el-main>

      <!-- Footer -->
       <el-footer class="app-footer">
          © {{ new Date().getFullYear() }} 医院药品管理系统 | 版权所有
        </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, onActivated, watch, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
   UserFilled, User, Box, DataAnalysis, Document, Setting, 
   Expand, Fold, DocumentChecked, Histogram // Import collapse icons and new icon
} from '@element-plus/icons-vue'
// Removed axios import as it's likely handled within child components

const router = useRouter()
const route = useRoute()

// --- Moved state from App.vue --- 
const isCollapse = ref(false)
const userInfo = reactive({
  id: null,
  username: '',
  role: ''
})

// Check if user is admin
const isAdmin = computed(() => {
   return userInfo.role && userInfo.role.toUpperCase() === 'ADMIN';
});

// New computed property to check if user can view logs
const canViewOperationLog = computed(() => {
  const role = userInfo.role ? userInfo.role.toUpperCase() : '';
  return ['ADMIN', 'PHARMACIST', 'NURSE'].includes(role);
});

// New computed property for displaying user role name
const displayedUserRole = computed(() => {
  const role = userInfo.role ? userInfo.role.toUpperCase() : 'USER';
  switch (role) {
    case 'ADMIN': return '管理员';
    case 'PHARMACIST': return '药师';
    case 'NURSE': return '护士';
    default: return '普通用户';
  }
});

// Provide userInfo and isAdmin to child components
provide('userInfo', userInfo);
provide('isAdmin', isAdmin);

// Active menu item based on current route
const activeMenu = computed(() => route.path);

// Load user info from localStorage
const loadUserInfo = () => {
  console.log('MainLayout: Loading user info...');
  const userStr = localStorage.getItem('user');
  const oldRole = userInfo.role; // Store old role to detect changes
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      userInfo.id = user.id || null;
      userInfo.username = user.username || '未知用户';
      userInfo.role = user.role || 'USER';
      console.log('MainLayout: User info loaded:', JSON.stringify(userInfo));
      // Redirect if role changed and current route is not allowed
      if (oldRole && oldRole !== userInfo.role && !isAdmin.value && (route.path === '/user-management' || route.path === '/system-settings')) {
         console.warn('MainLayout: Role changed, redirecting from admin page.');
         router.push('/drug-management');
      }
    } catch (e) {
      console.error('MainLayout: Failed to parse user data, logging out.', e);
      logout(false); // Logout without confirmation
    }
  } else {
    console.warn('MainLayout: No user data found, logging out.');
    logout(false); // Logout without confirmation
  }
};

// Toggle sidebar collapse state
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

// Handle dropdown commands
const handleCommand = (command) => {
  if (command === 'logout') {
    logout(); // Logout with confirmation
  } else if (command === 'profile') {
    // Navigate to profile page or show modal
    // Example: router.push('/profile');
     ElMessageBox.alert(`用户ID: ${userInfo.id}<br>用户名: ${userInfo.username}<br>角色: ${userInfo.role}`, '用户信息', {
       dangerouslyUseHTMLString: true,
       confirmButtonText: '确定'
     })
  }
};

// Logout function
const logout = (showConfirm = true) => {
   const performLogout = () => {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      ElMessage.success('已退出登录');
      router.push('/login');
   };

   if (showConfirm) {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(performLogout).catch(() => {});
   } else {
      performLogout();
   }
};

// --- Lifecycle and Watchers --- 
let storageListener = null;
let intervalId = null;

onMounted(() => {
  console.log('MainLayout Mounted');
  loadUserInfo(); // Load user info initially
  
  // Listen for storage changes from other tabs
  storageListener = (event) => {
    if (event.key === 'user' || event.key === 'token') {
      console.log('MainLayout: Detected storage change for', event.key);
      loadUserInfo();
    }
  };
  window.addEventListener('storage', storageListener);

  // Periodically check user info (optional, less frequent check)
  // intervalId = setInterval(loadUserInfo, 5000); 
});

onUnmounted(() => {
  console.log('MainLayout Unmounted');
  if (storageListener) {
    window.removeEventListener('storage', storageListener);
  }
  // if (intervalId) {
  //   clearInterval(intervalId);
  // }
});

// Watch route changes to ensure user info is current (might be redundant with interval/storage listener)
// watch(() => route.path, () => {
//   loadUserInfo();
// });

</script>

<style scoped>
.main-container {
  height: 100vh;
}

.app-aside {
  background-color: #304156; /* Dark background for sidebar */
  transition: width 0.3s ease;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  overflow-x: hidden; /* Hide horizontal scrollbar when collapsed */
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.el-menu {
  border-right: none; /* Remove default border */
}

.logo-container {
  height: 60px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: center; /* Center items */
  background-color: #2b2f3a; /* Slightly different background for logo area */
  overflow: hidden;
  white-space: nowrap; /* Prevent title wrapping */
}
.logo-container.is-collapse {
   justify-content: center;
}

.logo {
  height: 32px;
  width: 32px;
  vertical-align: middle;
  /* margin-right: 12px; */
}
.logo-small {
   height: 32px;
   width: 32px;
   vertical-align: middle;
}

.system-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  margin-left: 10px; /* Space between logo and title */
}

.app-header {
  background-color: #fff;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  height: 50px; /* Slightly smaller header */
}

.header-left {
   display: flex;
   align-items: center;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
  color: #606266;
}

.header-title {
  font-size: 18px; 
  font-weight: bold; 
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

.app-main {
  background-color: #f0f2f5; /* Lighter background for content */
  padding: 20px;
  height: calc(100vh - 110px); /* Full height minus header and footer */
  overflow-y: auto; /* Allow scrolling for content */
}

.app-footer {
  background-color: #f0f2f5;
  color: #909399;
  text-align: center;
  line-height: 60px;
  font-size: 12px;
  height: 60px;
}
</style> 