import request from '@/utils/request'

// 用户认证相关接口（对接后端 /auth 与 /user 接口）
export const authApi = {
  // 用户注册
  register: (data: any) => request.post('/auth/register', data),
  
  // 用户登录
  login: (data: any) => request.post('/auth/login', data),
  
  // 用户登出
  logout: () => request.post('/auth/logout'),
  
  // 忘记密码
  forgotPassword: (data: any) => request.post('/auth/forgot-password', data),
  
  // 获取用户信息（基于本地存储的用户名）
  getUserInfo: () => {
    const username = localStorage.getItem('username') || ''
    return request.get('/user/profile', { params: { username } })
  },
  
  // 更新用户信息
  updateUserInfo: (data: any) => request.put('/user/profile', data),
  
  // 修改密码
  changePassword: (data: any) => request.put('/user/change-password', data)
}

// 活动分类相关接口
export const activityCategoryApi = {
  // 获取所有启用的活动分类
  getEnabledCategories: () => request.get('/v1/activity-categories')
}

// 活动相关接口
export const activityApi = {
  // 获取活动列表
  getActivityList: (params: any) => request.get('/v1/activities', { params }),
  
  // 获取活动详情
  getActivityDetail: (id: number) => request.get(`/v1/activities/${id}`),
  
  // 报名活动
  registerActivity: (id: number, data: any) => request.post(`/v1/activities/${id}/register`, data),
  
  // 取消报名
  cancelRegistration: (id: number) => request.delete(`/v1/activities/${id}/register`),
  
  // 获取我的报名
  getMyRegistrations: () => request.get('/v1/user/registrations'),
  
  // 获取我的报名（包含活动详情）
  getMyRegistrationsWithDetails: (params: any) => request.get('/v1/user/registrations/details', { params }),
  
  // 获取报名表数据（前端生成PDF）
  getRegistrationFormData: (registrationId: number) => request.get(`/v1/registrations/${registrationId}/form-data`),
  // 生成报名表（后端生成，保留接口）
  generateRegistrationForm: (registrationId: number) => request.get(`/v1/registrations/${registrationId}/form`, { responseType: 'blob' }) as Promise<Blob>,
  
  // 获取推荐活动列表
  getRecommendedActivities: (params: any) => request.get('/v1/activity-recommendation/list', { params }),
  
  // 计算活动匹配分数
  getActivityMatchScore: (activityId: number) => request.get(`/v1/activity-recommendation/match-score/${activityId}`),
  
  // 获取活动详细匹配信息
  getActivityMatchDetails: (activityId: number) => request.get(`/v1/activity-recommendation/match-details/${activityId}`),
  
  // 获取活动技能要求
  getActivitySkillRequirements: (activityId: number) => request.get(`/v1/activities/${activityId}/skill-requirements`)
}

// 签到相关接口
export const checkinApi = {
  // 活动签到
  checkin: (id: number, data: any) => request.post(`/v1/activities/${id}/checkin`, data),
  
  // 活动签退
  checkout: (id: number, data: any) => request.post(`/v1/activities/${id}/checkout`, data),
  
  // 获取签到记录
  getCheckinRecords: () => request.get('/v1/user/checkins'),
  
  // 获取签到记录（包含活动信息）
  getCheckinRecordsWithActivity: (pageNo = 1, pageSize = 10, activityTitle?: string) => {
    const params = new URLSearchParams()
    params.append('pageNo', pageNo.toString())
    params.append('pageSize', pageSize.toString())
    if (activityTitle) {
      params.append('activityTitle', activityTitle)
    }
    return request.get(`/v1/user/checkins-with-activity?${params.toString()}`)
  },
  
  // 自动计算服务时长
  autoCalculateServiceHours: (checkinId: number) => 
    request.post(`/v1/checkins/${checkinId}/auto-calculate-hours`),
  
  // 获取活动二维码
  getActivityQRCode: (id: number) => request.get(`/v1/activities/${id}/qr-code`),
  
  // 检查用户今天是否已签到指定活动
  checkTodayCheckinStatus: (activityId: number) => 
    request.get(`/v1/activities/${activityId}/checkin-status`),
  
  // 获取当前用户基本信息（用于签到页面预填充）
  getCurrentUserCheckinInfo: () => request.get('/v1/user/checkin-info')
}

// 服务记录相关接口
export const serviceApi = {
  // 获取服务记录
  getServiceRecords: () => request.get('/v1/user/service-records'),
  
  // 获取积分记录（支持分页和搜索）
  getPointsRecords: (params?: { pageNo?: number; pageSize?: number; pointsType?: number }) => 
    request.get('/v1/service-record/user/points', { params })
}

// 社群相关接口
export const communityApi = {
  // 社群列表
  getCommunityList: (params: any) => request.get('/v1/community/list', { params }),
  // 社群详情
  getCommunityDetail: (id: number) => request.get(`/v1/community/${id}`),
  // 社群帖子列表
  getCommunityPosts: (id: number, params: any) => request.get(`/v1/community/${id}/posts`, { params }),
  // 加入/退出社群
  joinCommunity: (id: number) => request.post(`/v1/community/${id}/join`),
  quitCommunity: (id: number) => request.post(`/v1/community/${id}/quit`),
  // 检查是否为社群成员
  isMember: (id: number) => request.get(`/v1/community/${id}/is-member`),
  // 获取用户加入的社群列表
  getMyCommunities: (params: any) => request.get('/v1/community/my-communities', { params }),
  // 发帖/帖子详情/评论
  createPost: (data: any) => request.post('/v1/community/post/create', data),
  getPostDetail: (id: number) => request.get(`/v1/community/post/${id}`),
  getPostComments: (id: number, params: any) => request.get(`/v1/community/post/${id}/comments`, { params }),
  createComment: (id: number, data: any) => request.post(`/v1/community/post/${id}/comment`, data)
}

// 活动总结相关接口
export const activitySummaryApi = {
  // 获取活动总结列表
  getList: (params: any) => request.get('/v1/activity-summaries', { params }),
  // 获取活动总结详情
  getDetail: (id: number) => request.get(`/v1/activity-summaries/${id}`),
  // 根据活动ID获取活动总结
  getByActivityId: (activityId: number) => request.get(`/v1/activities/${activityId}/summary`),
  // 根据社群ID获取活动总结
  getByCommunityId: (communityId: number) => request.get(`/v1/community/${communityId}/summary`),
  // 根据社群ID获取活动总结列表（支持分页和搜索）
  getListByCommunityId: (communityId: number, params: any) => request.get(`/v1/activity-summaries`, { 
    params: { ...params, communityId } 
  })
}

// 招募相关接口（C端）
export const recruitmentApi = {
  // 获取招募列表
  getList: (params: any) => request.get('/v1/recruitment', { params }),
  // 获取招募详情
  getDetail: (id: number) => request.get(`/v1/recruitment/${id}`),
  // 申请招募
  apply: (id: number, data: any) => request.post(`/v1/recruitment/${id}/apply`, data),
  // 我的申请列表
  getMyApplications: () => request.get('/v1/user/recruitment-applications')
}

// 数据统计相关接口（志愿者端）
export const statisticsApi = {
  // 获取志愿者年度报告
  getVolunteerYearlyReport: (userId: number, year?: number) => 
    request.get(`/v1/statistics/volunteers/${userId}/yearly-report`, { params: { year } })
}