import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const username = ref<string>(localStorage.getItem('username') || '')
  const userInfo = ref<any>(null)
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const login = async (loginData: any) => {
    const response: any = await authApi.login(loginData)
    token.value = response.token
    username.value = loginData.username || loginData.phone || ''
    localStorage.setItem('token', response.token)
    localStorage.setItem('username', username.value)
    return response
  }

  // 注册
  const register = async (registerData: any) => {
    return await authApi.register(registerData)
  }

  // 登出
  const logout = async () => {
    try { await authApi.logout() } catch {}
    token.value = ''
    username.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  // 获取用户信息
  const getUserInfo = async () => {
    const response = await authApi.getUserInfo()
    userInfo.value = response
    return response
  }

  // 更新用户信息
  const updateUserInfo = async (userData: any) => {
    const response = await authApi.updateUserInfo(userData)
    userInfo.value = response
    return response
  }

  // 恢复状态
  const restore = () => {
    token.value = localStorage.getItem('token') || ''
    username.value = localStorage.getItem('username') || ''
  }

  return { token, username, userInfo, isLoggedIn, login, register, logout, getUserInfo, updateUserInfo, restore }
})
