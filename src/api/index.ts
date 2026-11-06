import request from '@/utils/request'

// 管理员认证相关接口
export const adminAuthApi = {
  // 管理员登录
  login: (data: any) => request.post('/auth/admin/login', data),
  
  // 管理员登出
  logout: () => request.post('/auth/logout'),
  
  // 获取管理员信息
  getAdminInfo: () => request.get('/admin/profile'),
  
  // 更新管理员信息
  updateAdminInfo: (data: any) => request.put('/admin/profile', data),
  
  // 获取用户菜单权限
  getUserMenus: () => request.get('/admin/menu/user-menus')
}

// 用户管理相关接口
export const userApi = {
  // 获取用户列表
  getUserList: (params: any) => request.get('/admin/users', { params }),
  
  // 获取用户详情
  getUserDetail: (id: number) => request.get(`/admin/users/${id}`),
  
  // 更新用户状态
  updateUserStatus: (id: number, status: number) => request.put(`/admin/users/${id}/status`, { status }),
  
  // 删除用户
  deleteUser: (id: number) => request.delete(`/admin/users/${id}`)
}

// 角色管理相关接口
export const roleApi = {
  // 获取角色列表
  getRoleList: (params: any) => request.get('/v1/admin/roles', { params }),
  
  // 获取角色详情
  getRoleDetail: (id: number) => request.get(`/v1/admin/roles/${id}`),
  
  // 创建角色
  createRole: (data: any) => request.post('/v1/admin/roles', data),
  
  // 更新角色
  updateRole: (id: number, data: any) => request.put(`/v1/admin/roles/${id}`, data),
  
  // 删除角色
  deleteRole: (id: number) => request.delete(`/v1/admin/roles/${id}`),
  
  // 分配权限
  assignPermissions: (id: number, permissions: number[]) => request.post(`/v1/admin/roles/${id}/permissions`, { permissions })
}

// 菜单管理相关接口
export const menuApi = {
  // 获取菜单列表
  getMenuList: () => request.get('/v1/admin/menus'),
  
  // 获取菜单树
  getMenuTree: () => request.get('/v1/admin/menus/tree'),
  
  // 创建菜单
  createMenu: (data: any) => request.post('/v1/admin/menus', data),
  
  // 更新菜单
  updateMenu: (id: number, data: any) => request.put(`/v1/admin/menus/${id}`, data),
  
  // 删除菜单
  deleteMenu: (id: number) => request.delete(`/v1/admin/menus/${id}`)
}

// 活动管理相关接口
export const activityApi = {
  // 获取活动列表
  getActivityList: (params: any) => request.get('/v1/admin/activities', { params }),
  
  // 获取活动详情
  getActivityDetail: (id: number) => request.get(`/v1/admin/activities/${id}`),
  
  // 创建活动
  createActivity: (data: any) => request.post('/v1/admin/activities', data),
  
  // 更新活动
  updateActivity: (id: number, data: any) => request.put(`/v1/admin/activities/${id}`, data),
  
  // 删除活动
  deleteActivity: (id: number) => request.delete(`/v1/admin/activities/${id}`),
  
  // 批量删除活动
  batchDelete: (ids: number[]) => request.delete('/v1/admin/activities/batch', { data: ids }),
  
  // 更新活动状态
  updateActivityStatus: (id: number, status: number) => request.put(`/v1/admin/activities/${id}/status`, { status }),
  
  // 批量更新活动状态
  batchUpdateStatus: (ids: number[], status: number) => request.put('/v1/admin/activities/batch/status', { ids, status }),
  
  // 导出活动数据
  exportActivities: (params: any) => request.get('/v1/admin/activities/export', { params }),
  
  // 获取活动技能要求
  getActivitySkillRequirements: (activityId: number) => request.get(`/v1/activities/${activityId}/skill-requirements`),
  
  // 设置活动技能要求
  setActivitySkillRequirements: (activityId: number, requirements: any[]) => request.post(`/v1/activities/${activityId}/skill-requirements`, requirements)
}

// 招募管理相关接口（管理后台）
export const recruitmentApi = {
  // 创建招募
  create: (data: any) => request.post('/v1/admin/recruitment', data),
  // 获取招募列表
  getList: (params: any) => request.get('/v1/admin/recruitment', { params }),
  // 更新招募
  update: (id: number, data: any) => request.put(`/v1/admin/recruitment/${id}`, data),
  // 获取招募详情
  getDetail: (id: number) => request.get(`/v1/admin/recruitment/${id}`),
  // 获取报名者列表
  getApplicants: (id: number, params?: any) => request.get(`/v1/admin/recruitment/${id}/applicants`, { params }),
  // 审核报名者申请
  reviewApplication: (id: number, data: any) => request.post(`/v1/admin/recruitment/applications/${id}/review`, data)
}

// 数据统计相关接口
export const statisticsApi = {
  // 获取仪表盘数据
  getDashboardData: () => request.get('/v1/admin/statistics/dashboard'),
  
  // 获取用户统计
  getUserStatistics: (params: any) => request.get('/v1/admin/statistics/users', { params }),
  
  // 获取活动统计
  getActivityStatistics: (params: any) => request.get('/v1/admin/statistics/activities', { params }),
  
  // 导出活动统计数据
  exportActivityStatistics: (params: any) => request.get('/v1/admin/statistics/activities/export', { 
    params, 
    responseType: 'blob' 
  }),
  
  // 获取活动统计详情
  getActivityStatisticsDetail: (activityId: number) => request.get(`/v1/admin/statistics/activities/${activityId}`),
  
  // 获取组织者统计
  getOrganizerStatistics: (params: any) => request.get('/v1/admin/statistics/organizers', { params }),
  
  // 获取分类统计
  getCategoryStatistics: (params: any) => request.get('/v1/admin/statistics/categories', { params }),
  
  // 获取志愿者排名
  getVolunteerRanking: (params: any) => request.get('/v1/admin/statistics/volunteers/ranking', { params }),
  
  // 导出志愿者排名数据
  exportVolunteerRanking: (params: any) => request.get('/v1/admin/statistics/volunteers/ranking/export', { 
    params, 
    responseType: 'blob' 
  }),
  
  // 导出活动统计数据
  exportActivityStatistics: (params: any) => request.get('/v1/admin/statistics/activities/export', { 
    params, 
    responseType: 'blob' 
  }),
  
  // 导出组织者统计数据
  exportOrganizerStatistics: (params: any) => request.get('/v1/admin/statistics/organizers/export', { 
    params, 
    responseType: 'blob' 
  }),
  
  // 导出分类统计数据
  exportCategoryStatistics: (params: any) => request.get('/v1/admin/statistics/categories/export', { 
    params, 
    responseType: 'blob' 
  }),
  
  // 获取服务记录统计
  getServiceStatistics: (params: any) => request.get('/v1/admin/statistics/services', { params }),
  
  // 获取志愿者服务领域统计
  getVolunteerFieldStatistics: (params: any) => request.get('/v1/admin/statistics/volunteers/field-statistics', { params }),
  
  // 导出志愿者服务领域统计数据
  exportVolunteerFieldStatistics: (params: any) => request.get('/v1/admin/statistics/volunteers/field-statistics/export', { 
    params, 
    responseType: 'blob' 
  })
}

// 报名管理相关接口
export const registrationApi = {
  // 获取报名列表
  getRegistrationList: (params: any) => request.get('/v1/admin/registrations', { params }),
  
  // 获取报名详情
  getRegistrationDetail: (id: number) => request.get(`/v1/admin/registrations/${id}`),
  
  // 审核报名
  reviewRegistration: (id: number, data: any) => request.post(`/v1/admin/registrations/${id}/review`, data),
  
  // 批量审核报名
  batchReviewRegistrations: (data: any) => request.post('/v1/admin/registrations/batch-review', data),
  
  // 获取活动报名列表
  getActivityRegistrations: (activityId: number, params: any) => request.get(`/v1/admin/activities/${activityId}/registrations`, { params })
}

// 签到管理相关接口
export const checkinApi = {
  // 获取签到列表
  getCheckinList: (params: any) => request.get('/v1/admin/checkins', { params }),
  
  // 获取活动签到列表
  getActivityCheckins: (activityId: number, params: any) => request.get(`/v1/admin/activities/${activityId}/checkins`, { params }),
  
  // 更新服务时长
  updateServiceHours: (id: number, data: any) => request.put(`/v1/admin/checkins/${id}/service-hours`, data),
  
  // 更新签到状态
  updateCheckinStatus: (id: number, data: any) => request.put(`/v1/admin/checkins/${id}/status`, data),
  
  // 批量更新服务时长
  batchUpdateServiceHours: (data: any) => request.put('/v1/admin/checkins/batch-service-hours', data),
  
  // 自动计算服务时长
  autoCalculateServiceHours: (checkinId: number) => request.post(`/v1/checkins/${checkinId}/auto-calculate-hours`),
  
  // 更新用户总服务时长
  updateTotalServiceHours: (userIds: number[]) => request.post('/v1/admin/checkins/update-total-service-hours', { userIds }),
  
  // 导出签到数据
  exportCheckins: (params: any) => request.get('/v1/admin/checkins/export', { params })
}

// 活动总结相关接口
export const activitySummaryApi = {
  // 获取活动总结列表
  getSummaryList: (params: any) => request.get('/v1/admin/activity-summaries', { params }),
  
  // 获取活动总结详情
  getSummaryDetail: (id: number) => request.get(`/v1/admin/activity-summaries/${id}`),
  
  // 创建活动总结
  createSummary: (data: any) => request.post('/v1/admin/activity-summaries', data),
  
  // 更新活动总结
  updateSummary: (id: number, data: any) => request.put(`/v1/admin/activity-summaries/${id}`, data),
  
  // 删除活动总结
  deleteSummary: (id: number) => request.delete(`/v1/admin/activity-summaries/${id}`),
  
  // 根据活动ID获取总结
  getSummaryByActivityId: (activityId: number) => request.get(`/v1/activities/${activityId}/summary`)
}

// 活动分类相关接口
export const activityCategoryApi = {
  // 获取所有启用的活动分类
  getEnabledCategories: () => request.get('/v1/admin/activity-categories/enabled'),
  
  // 分页查询活动分类列表
  getCategoryList: (params: any) => request.get('/v1/admin/activity-categories', { params }),
  
  // 获取分类详情
  getCategoryDetail: (id: number) => request.get(`/v1/admin/activity-categories/${id}`),
  
  // 创建分类
  createCategory: (data: any) => request.post('/v1/admin/activity-categories', data),
  
  // 更新分类
  updateCategory: (id: number, data: any) => request.put(`/v1/admin/activity-categories/${id}`, data),
  
  // 删除分类
  deleteCategory: (id: number) => request.delete(`/v1/admin/activity-categories/${id}`)
}

// 系统配置相关接口
export const systemApi = {
  // 积分规则配置
  getPointsRules: () => request.get('/v1/admin/points-rules/list'),
  getPointsRuleDetail: (id: number) => request.get(`/v1/admin/points-rules/${id}`),
  updatePointsRule: (id: number, data: any) => request.put(`/v1/admin/points-rules/${id}`, data),
  togglePointsRuleStatus: (id: number, status: number) => request.put(`/v1/admin/points-rules/${id}/status?status=${status}`),
  
  // 手动生成积分数据
  generatePointsData: () => request.post('/v1/admin/points-rules/generate-points')
}

// 商品管理相关接口
export const goodsApi = {
  // 获取商品列表
  getGoodsList: (params: any) => request.get('/v1/admin/goods', { params }),
  
  // 获取商品详情
  getGoodsDetail: (id: number) => request.get(`/v1/admin/goods/${id}`),
  
  // 创建商品
  createGoods: (data: any) => request.post('/v1/admin/goods', data),
  
  // 更新商品
  updateGoods: (id: number, data: any) => request.put(`/v1/admin/goods/${id}`, data),
  
  // 删除商品
  deleteGoods: (id: number) => request.delete(`/v1/admin/goods/${id}`),
  
  // 更新商品状态
  updateGoodsStatus: (id: number, status: number) => request.put(`/v1/admin/goods/${id}/status?status=${status}`),
  
  // 获取商品分类列表
  getGoodsCategories: () => request.get('/v1/admin/goods/categories')
}

// 积分兑换记录管理相关接口
export const pointsExchangeApi = {
  // 获取兑换记录列表
  getPointsExchangeRecords: (params: any) => request.get('/admin/points-exchange/list', { params }),
  
  // 获取兑换记录详情
  getPointsExchangeDetail: (id: number) => request.get(`/admin/points-exchange/${id}`),
  
  // 更新发货信息
  updateShippingInfo: (id: number, data: any) => request.put(`/admin/points-exchange/${id}/shipping`, data),
  
  // 更新订单状态
  updateOrderStatus: (id: number, status: number, adminRemark?: string) => 
    request.put(`/admin/points-exchange/${id}/status?status=${status}${adminRemark ? `&adminRemark=${encodeURIComponent(adminRemark)}` : ''}`),
  
  // 获取状态统计
  getPointsExchangeStats: () => request.get('/admin/points-exchange/stats')
}

// 保险管理相关接口
export const insuranceApi = {
  // 保险产品管理
  getProductList: (params: any) => request.get('/admin/insurance-products/list', { params }),
  createProduct: (data: any) => request.post('/admin/insurance-products', data),
  updateProduct: (id: number, data: any) => request.put(`/admin/insurance-products/${id}`, data),
  deleteProduct: (id: number) => request.delete(`/admin/insurance-products/${id}`),
  updateProductStatus: (id: number, status: number) => request.put(`/admin/insurance-products/${id}/status?status=${status}`),
  getProductDetail: (id: number) => request.get(`/admin/insurance-products/${id}`),
  getAllProducts: () => request.get('/admin/insurance-products/all'),
  
  // 保险订单管理
  getOrderList: (params: any) => request.get('/admin/insurance-orders/list', { params }),
  getOrderDetail: (id: number) => request.get(`/admin/insurance-orders/${id}`),
  updateInsuranceOrderStatus: (id: number, status: number) => request.put(`/admin/insurance-orders/${id}/status?status=${status}`),
  getOrderStatistics: () => request.get('/admin/insurance-orders/statistics')
}

// 积分记录管理相关接口
export const pointsRecordApi = {
  // 分页查询积分记录
  getPointsRecordsPage: (params: any) => request.get('/admin/points-record/page', { params }),
  
  // 获取积分记录详情
  getPointsRecordDetail: (id: number) => request.get(`/admin/points-record/${id}`),
  
  // 获取积分类型选项
  getPointsTypes: () => request.get('/admin/points-record/points-types')
}

// 统一导出所有API
export const adminApi = {
  ...adminAuthApi,
  ...userApi,
  ...roleApi,
  ...menuApi,
  ...activityApi,
  ...recruitmentApi,
  ...statisticsApi,
  ...registrationApi,
  ...checkinApi,
  ...activitySummaryApi,
  ...activityCategoryApi,
  ...systemApi,
  ...goodsApi,
  ...pointsExchangeApi,
  ...insuranceApi,
  ...pointsRecordApi
}