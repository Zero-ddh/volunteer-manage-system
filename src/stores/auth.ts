import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminAuthApi } from '@/api'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('admin-token') || '')
  const adminInfo = ref<any>(null)
  const permissions = ref<string[]>([])
  const menus = ref<any[]>([])
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const login = async (loginData: any) => {
    try {
      const response = await adminAuthApi.login(loginData)
      
      // 检查响应状态
      if (response.code === 200) {
        token.value = response.data.token
        adminInfo.value = response.data.adminInfo
        permissions.value = response.data.permissions || []
        menus.value = response.data.menus || []
        localStorage.setItem('admin-token', response.data.token)
        return response.data
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      await adminAuthApi.logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      adminInfo.value = null
      permissions.value = []
      menus.value = []
      localStorage.removeItem('admin-token')
    }
  }

  // 获取管理员信息
  const getAdminInfo = async () => {
    try {
      const response = await adminAuthApi.getAdminInfo()
      adminInfo.value = response
      return response
    } catch (error) {
      throw error
    }
  }

  // 更新管理员信息
  const updateAdminInfo = async (adminData: any) => {
    try {
      const response = await adminAuthApi.updateAdminInfo(adminData)
      adminInfo.value = response
      return response
    } catch (error) {
      throw error
    }
  }

  // 检查权限
  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission)
  }

  // 动态拉取菜单（基于角色）
  const fetchMenus = async () => {
    try {
      const response = await adminAuthApi.getUserMenus()
      menus.value = response.data || []
      return menus.value
    } catch (error) {
      console.error('获取用户菜单失败:', error)
      menus.value = []
      return []
    }
  }

  const hasMenuPermission = (routeName: string) => {
    const flat = (arr: any[]): any[] => arr.flatMap(i => [i, ...(i.children ? flat(i.children) : [])])
    return flat(menus.value).some((m: any) => {
      // 检查菜单路径是否匹配路由名称
      if (m.path && m.path === `/${routeName}`) return true
      // 检查菜单路径是否匹配路由路径（处理嵌套路径）
      if (m.path && m.path.includes(`/${routeName}`)) return true
      // 特殊处理：活动列表路由名称是 'activities'，但路径是 '/activities/list'
      if (routeName === 'activities' && m.path === '/activities/list') return true
      if (routeName === 'registration-review' && m.path === '/registrations/review') return true
      if (routeName === 'checkin-management' && m.path === '/checkins/manage') return true
      if (routeName === 'activity-summaries' && m.path === '/activity-summaries') return true
      // 特殊处理：保险相关路由
      if (routeName === 'insurance-products' && m.path === '/insurance/products') return true
      if (routeName === 'insurance-orders' && m.path === '/insurance/orders') return true
      // 特殊处理：社群管理相关路由
      if (routeName === 'community-list' && m.path === '/community/list') return true
      if (routeName === 'community-posts' && m.path === '/community/posts') return true
      if (routeName === 'community-members' && m.path === '/community/members') return true
      if (routeName === 'community-summaries' && m.path === '/community/summaries') return true
      // 检查子菜单
      if (m.children) {
        return m.children.some((child: any) => 
          child.path === `/${routeName}` || 
          (child.path && child.path.includes(`/${routeName}`)) ||
          (routeName === 'activities' && child.path === '/activities/list') ||
          (routeName === 'registration-review' && child.path === '/registrations/review') ||
          (routeName === 'checkin-management' && child.path === '/checkins/manage') ||
          (routeName === 'activity-summaries' && child.path === '/activity-summaries') ||
          (routeName === 'insurance-products' && child.path === '/insurance/products') ||
          (routeName === 'insurance-orders' && child.path === '/insurance/orders') ||
          (routeName === 'community-list' && child.path === '/community/list') ||
          (routeName === 'community-posts' && child.path === '/community/posts') ||
          (routeName === 'community-members' && child.path === '/community/members') ||
          (routeName === 'community-summaries' && child.path === '/community/summaries')
        )
      }
      return false
    })
  }

  const buildTree = (list: any[]) => {
    const map: any = {}
    list.forEach(i => { i.children = []; map[i.id] = i })
    const roots: any[] = []
    list.forEach(i => {
      if (!i.parentId || i.parentId === 0) roots.push(i)
      else map[i.parentId]?.children.push(i)
    })
    return roots
  }

  return {
    token,
    adminInfo,
    permissions,
    menus,
    isLoggedIn,
    login,
    logout,
    getAdminInfo,
    updateAdminInfo,
    fetchMenus,
    hasPermission,
    hasMenuPermission
  }
})
