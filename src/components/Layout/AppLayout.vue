<template>
  <div class="layout">
    <!-- 头部导航 -->
    <header class="header" v-if="showHeader">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <div class="logo">
            <router-link to="/">
              <el-icon size="24"><Star /></el-icon>
              <span>志愿者服务平台</span>
            </router-link>
          </div>
          
          <!-- 导航菜单 -->
          <nav class="nav">
            <el-menu
              :default-active="activeMenu"
              mode="horizontal"
              :ellipsis="false"
              @select="handleMenuSelect"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/activities">活动</el-menu-item>
              <el-menu-item index="/recruitment">招募</el-menu-item>
              <el-menu-item index="/community">志愿社群</el-menu-item>
              <el-menu-item index="/insurance">保险服务</el-menu-item>
              <el-menu-item index="/star-volunteers">星级志愿者</el-menu-item>
              <!-- <el-menu-item index="/about">关于我们</el-menu-item> -->
            </el-menu>
          </nav>
          
          <!-- 用户操作 -->
          <div class="user-actions">
            <template v-if="authStore.isLoggedIn">
              <!-- 管理后台入口 -->
              <el-button @click="openAdmin" class="admin-btn">管理后台</el-button>
              
              <!-- 消息中心入口 -->
              <el-badge :value="unreadMessageCount" :hidden="unreadMessageCount === 0" class="message-badge">
                <el-button @click="$router.push('/user/messages')" icon="Bell" circle />
              </el-badge>
              
              <!-- 用户信息 -->
              <el-dropdown @command="handleUserCommand">
                <span class="user-info">
                  <el-avatar :size="32" :src="authStore.userInfo?.avatar">
                    {{ getFirstChar(authStore.userInfo) }}
                  </el-avatar>
                  <span class="username">{{ authStore.userInfo?.username }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">用户中心</el-dropdown-item>
                    <el-dropdown-item command="messages">消息中心</el-dropdown-item>
                    <el-dropdown-item command="registrations">我的报名</el-dropdown-item>
                    <el-dropdown-item command="recruitment-applications">招募申请</el-dropdown-item>
                    <el-dropdown-item command="service-records">服务记录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              
              <!-- 退出登录按钮 -->
              <el-button @click="handleLogout" class="logout-btn">退出登录</el-button>
            </template>
            <template v-else>
              <el-button @click="openAdmin" class="admin-btn">管理后台</el-button>
              <el-button type="primary" @click="$router.push('/login')">登录</el-button>
              <el-button @click="$router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </div>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="main" :class="{ 'no-padding': isAuthPage }">
      <div class="container">
        <router-view />
      </div>
    </main>
    
    <!-- 底部 -->
    <footer class="footer" v-if="showFooter">
      <div class="container">
        <div class="footer-content">
          <div class="footer-info">
            <p>&copy; 2025 志愿者服务平台. All rights reserved.</p>
            <p>让志愿服务更简单，让爱心传递更便捷</p>
          </div>
          <div class="footer-links">
            <a href="#">联系我们</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { messageApi } from '@/api/message'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const adminLoginUrl = 'http://localhost:3001/login'

// 未读消息数量
const unreadMessageCount = ref(0)

// 定时器
let messageTimer: number | null = null

// 获取未读消息数量
const fetchUnreadCount = async () => {
  if (!authStore.isLoggedIn) return
  
  try {
    const res = await messageApi.getUnreadCount()
    if (res && res.data !== undefined) {
      unreadMessageCount.value = res.data
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// 启动定时器
const startMessageTimer = () => {
  if (messageTimer) return
  
  // 立即获取一次
  fetchUnreadCount()
  
  // 每30秒获取一次
  messageTimer = setInterval(fetchUnreadCount, 30000)
}

// 停止定时器
const stopMessageTimer = () => {
  if (messageTimer) {
    clearInterval(messageTimer)
    messageTimer = null
  }
}

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/activities')) return '/activities'
  if (path.startsWith('/recruitment')) return '/recruitment'
  if (path.startsWith('/community')) return '/community'
  if (path.startsWith('/insurance')) return '/insurance'
  if (path.startsWith('/star-volunteers')) return '/star-volunteers'
  // if (path.startsWith('/about')) return '/about'
  if (path.startsWith('/user')) return '/user'
  return path
})

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  router.push(index)
}

// 获取用户姓名第一个字符
const getFirstChar = (userInfo: any) => {
  // 优先使用真实姓名
  if (userInfo?.realName) {
    return userInfo.realName.charAt(0)
  }
  // 其次使用userInfo.username
  if (userInfo?.username) {
    return userInfo.username.charAt(0)
  }
  // 最后使用authStore中的username（从localStorage获取）
  if (authStore.username) {
    return authStore.username.charAt(0)
  }
  return ''
}

// 处理用户操作
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'messages':
      router.push('/user/messages')
      break
    case 'registrations':
      router.push('/user/registrations')
      break
    case 'recruitment-applications':
      router.push('/user/recruitment-applications')
      break
    case 'service-records':
      router.push('/user/service-records')
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 用户取消
  }
}

// 打开管理后台登录页
const openAdmin = () => {
  window.open(adminLoginUrl, '_blank')
}

// 是否展示页脚（登录/注册页不展示）
const showFooter = computed(() => {
  const path = route.path
  return !(path.startsWith('/login') || path.startsWith('/register'))
})

// 登录/注册页：main 取消默认上下 padding
const isAuthPage = computed(() => !showFooter.value)

// 登录页隐藏顶部菜单栏
const showHeader = computed(() => route.path !== '/login')

// 生命周期钩子
onMounted(async () => {
  if (authStore.isLoggedIn) {
    startMessageTimer()
    // 如果用户信息未加载，则获取用户信息
    if (!authStore.userInfo) {
      try {
        await authStore.getUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
  }
})

onUnmounted(() => {
  stopMessageTimer()
})

// 监听登录状态变化
const isLoggedIn = computed(() => authStore.isLoggedIn)
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    startMessageTimer()
  } else {
    stopMessageTimer()
    unreadMessageCount.value = 0
  }
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo a {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #409eff;
  font-weight: bold;
  font-size: 18px;
}

.nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav :deep(.el-menu) {
  border-bottom: none;
}

.nav :deep(.el-menu-item) {
  border-bottom: none;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main {
  flex: 1;
  padding: 20px 0;
}

.main.no-padding { padding: 0; }

.footer {
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info p {
  margin: 4px 0;
  color: #6c757d;
  font-size: 14px;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: #6c757d;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #409eff;
}

.message-badge {
  margin-right: 12px;
}

.message-badge .el-badge__content {
  font-size: 12px;
}

/* 管理后台按钮样式 - 红色 */
.admin-btn {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
  color: #fff !important;
}

.admin-btn:hover {
  background-color: #f78989 !important;
  border-color: #f78989 !important;
}

.admin-btn:focus {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
}

/* 退出登录按钮样式 - 蓝色 */
.logout-btn {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
}

.logout-btn:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

.logout-btn:focus {
  background-color: #409eff !important;
  border-color: #409eff !important;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 10px 0;
  }
  
  .nav {
    order: 3;
    width: 100%;
    margin-top: 10px;
  }
  
  .user-actions {
    order: 2;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}
</style>
