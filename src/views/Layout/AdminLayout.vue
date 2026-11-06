<template>
  <div class="layout-root">
    <header class="header">
      <div class="nav-container">
        <a class="logo" href="/">志愿者管理后台</a>
        <div class="nav-right">
          <div class="user-info">
            <div class="user-avatar">管</div>
            <span>{{ authStore.adminInfo?.username || '系统管理员' }}</span>
          </div>
          <a class="btn btn-outline" href="http://localhost:3000/">返回前台</a>
          <button class="btn btn-outline" @click="onLogout">退出登录</button>
        </div>
      </div>
    </header>
    <aside class="sidebar">
      <ul class="sidebar-menu">
        <!-- 动态渲染菜单 -->
        <li 
          v-for="menu in userMenus" 
          :key="(menu as any).id" 
          class="menu-item"
        >
          <!-- 有子菜单的菜单项 -->
          <div 
            v-if="(menu as any).children && (menu as any).children.length > 0"
            class="menu-link-container"
          >
            <div 
              class="menu-link menu-link-parent" 
              @click="toggleMenu((menu as any).id)"
            >
              <span class="menu-icon">{{ getMenuIcon((menu as any).icon) }}</span>
              <span class="menu-text">{{ (menu as any).menuName }}</span>
              <span class="menu-arrow" :class="{ 'expanded': isMenuExpanded((menu as any).id) }">
                ▼
              </span>
            </div>
            
            <!-- 子菜单 -->
            <ul 
              v-show="isMenuExpanded((menu as any).id)" 
              class="submenu"
            >
              <li 
                v-for="submenu in (menu as any).children" 
                :key="(submenu as any).id" 
                class="submenu-item"
              >
                <router-link 
                  class="submenu-link" 
                  :to="(submenu as any).path || '#'"
                  :class="{ 'disabled': !(submenu as any).path }"
                >
                  {{ (submenu as any).menuName }}
                </router-link>
              </li>
            </ul>
          </div>
          
          <!-- 没有子菜单的菜单项 -->
          <router-link 
            v-else
            class="menu-link" 
            :to="(menu as any).path || '#'"
            :class="{ 'disabled': !(menu as any).path }"
          >
            <span class="menu-icon">{{ getMenuIcon((menu as any).icon) }}</span>
            <span class="menu-text">{{ (menu as any).menuName }}</span>
          </router-link>
        </li>
      </ul>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
  
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const authStore = useAdminAuthStore()
const router = useRouter()
const userMenus = ref<Record<string, unknown>[]>([])

// 菜单展开状态管理
const expandedMenus = ref<Set<number>>(new Set())

// 切换菜单展开状态
const toggleMenu = (menuId: number) => {
  if (expandedMenus.value.has(menuId)) {
    expandedMenus.value.delete(menuId)
  } else {
    expandedMenus.value.add(menuId)
  }
}

// 检查菜单是否展开
const isMenuExpanded = (menuId: number) => {
  return expandedMenus.value.has(menuId)
}

// 获取菜单图标
const getMenuIcon = (icon: string) => {
  const iconMap: Record<string, string> = {
    'HomeFilled': '🏠',
    'User': '👥',
    'UserFilled': '👤',
    'Menu': '📝',
    'Calendar': '📋',
    'DataAnalysis': '📈',
    'Lock': '🔐'
  }
  return iconMap[icon] || '📄'
}

// 加载用户菜单
const loadUserMenus = async () => {
  try {
    await authStore.fetchMenus()
    userMenus.value = authStore.menus
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败')
  }
}

const onLogout = async () => {
  await authStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}

onMounted(() => {
  loadUserMenus()
})
</script>

<style scoped>
.layout-root { background: #f8f9fa; min-height: 100vh; }
.header { position: fixed; top: 0; left: 0; right: 0; background: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.1); z-index: 1000; }
.nav-container { display: flex; align-items: center; justify-content: space-between; height: 60px; padding: 0 20px; }
.logo { color: #667eea; text-decoration: none; font-weight: bold; }
.nav-right { display: flex; align-items: center; gap: 16px; }
.btn { padding: 6px 14px; border: none; border-radius: 5px; cursor: pointer; text-decoration: none; transition: all .2s; }
.btn-outline { background: transparent; color: #667eea; border: 1px solid #667eea; }
.btn-outline:hover { background: #667eea; color: #fff; }
.user-info { display: flex; align-items: center; gap: 8px; color: #666; }
.user-avatar { width: 28px; height: 28px; border-radius: 50%; background: #667eea; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.sidebar { position: fixed; top: 60px; left: 0; bottom: 0; width: 220px; background: #2c3e50; overflow: auto; }
.sidebar-menu { list-style: none; padding: 12px 0; margin: 0; }
.menu-item { margin: 4px 0; }
.menu-link { display: flex; align-items: center; padding: 12px 18px; color: #bdc3c7; text-decoration: none; transition: all 0.3s; }
.menu-link:hover { background: #34495e; color: #fff; }
.menu-link.router-link-active { background: #667eea; color: #fff; }
.menu-link.disabled { cursor: not-allowed; opacity: 0.6; }
.menu-icon { margin-right: 8px; font-size: 16px; }
.menu-text { flex: 1; }
.main-content { margin-left: 220px; padding: 80px 24px 24px; }

/* 菜单容器样式 */
.menu-link-container { position: relative; }

/* 父菜单样式 */
.menu-link-parent { cursor: pointer; position: relative; }
.menu-link-parent:hover { background: #34495e; color: #fff; }

/* 菜单箭头样式 */
.menu-arrow { 
  position: absolute; 
  right: 18px; 
  top: 50%; 
  transform: translateY(-50%); 
  font-size: 12px; 
  transition: transform 0.3s ease;
  color: #bdc3c7;
}
.menu-arrow.expanded { 
  transform: translateY(-50%) rotate(180deg); 
}

/* 子菜单样式 */
.submenu { 
  list-style: none; 
  padding: 0; 
  margin: 0; 
  background: #34495e; 
  overflow: hidden;
  transition: max-height 0.3s ease;
}
.submenu-item { margin: 0; }
.submenu-link { 
  display: block; 
  padding: 8px 18px 8px 36px; 
  color: #bdc3c7; 
  text-decoration: none; 
  font-size: 14px; 
  transition: all 0.3s; 
}
.submenu-link:hover { background: #2c3e50; color: #fff; }
.submenu-link.router-link-active { background: #667eea; color: #fff; }
.submenu-link.disabled { cursor: not-allowed; opacity: 0.6; }
</style>


