import { defineStore } from 'pinia'
import { ref } from 'vue'
import { activityApi } from '@/api'

export const useActivityStore = defineStore('activity', () => {
  // 状态
  const activityList = ref<any[]>([])
  const currentActivity = ref<any>(null)
  const myRegistrations = ref<any[]>([])
  const loading = ref(false)

  // 获取活动列表
  const getActivityList = async (params: any = {}) => {
    loading.value = true
    try {
      const response = await activityApi.getActivityList(params)
      // response 已经是后端返回的 data 字段内容，直接使用
      activityList.value = response.records || response.list || []
      return response
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取活动详情
  const getActivityDetail = async (id: number) => {
    try {
      const response = await activityApi.getActivityDetail(id)
      currentActivity.value = response
      return response
    } catch (error) {
      throw error
    }
  }

  // 报名活动
  const registerActivity = async (id: number, data: any) => {
    try {
      const response = await activityApi.registerActivity(id, data)
      // 更新活动详情中的报名状态
      if (currentActivity.value && currentActivity.value.id === id) {
        currentActivity.value.isRegistered = true
      }
      return response
    } catch (error) {
      throw error
    }
  }

  // 取消报名
  const cancelRegistration = async (id: number) => {
    try {
      const response = await activityApi.cancelRegistration(id)
      // 更新活动详情中的报名状态
      if (currentActivity.value && currentActivity.value.id === id) {
        currentActivity.value.isRegistered = false
      }
      return response
    } catch (error) {
      throw error
    }
  }

  // 获取我的报名
  const getMyRegistrations = async () => {
    try {
      const response = await activityApi.getMyRegistrations()
      // response 已经是后端返回的 data 字段内容，直接使用
      myRegistrations.value = response.records || response.list || []
      return response
    } catch (error) {
      throw error
    }
  }

  return {
    activityList,
    currentActivity,
    myRegistrations,
    loading,
    getActivityList,
    getActivityDetail,
    registerActivity,
    cancelRegistration,
    getMyRegistrations
  }
})
