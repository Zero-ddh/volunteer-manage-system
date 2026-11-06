/**
 * 组织者权限管理工具
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */

// 组织者权限常量
export const ORGANIZER_PERMISSIONS = {
  ACTIVITY_CREATE: 'activity:create',
  ACTIVITY_EDIT: 'activity:edit',
  ACTIVITY_PUBLISH: 'activity:publish',
  ACTIVITY_CANCEL: 'activity:cancel',
  REGISTRATION_REVIEW: 'registration:review',
  CHECKIN_MANAGE: 'checkin:manage',
  SUMMARY_PUBLISH: 'summary:publish'
} as const

// 组织者权限检查
export const checkOrganizerPermission = (permission: string, userRoles: string[]): boolean => {
  // 超级管理员拥有所有权限
  if (userRoles.includes('admin')) {
    return true
  }
  
  // 组织者角色检查
  if (userRoles.includes('organizer')) {
    const organizerPermissions = Object.values(ORGANIZER_PERMISSIONS)
    return organizerPermissions.includes(permission as any)
  }
  
  return false
}

// 检查是否为组织者
export const isOrganizer = (userRoles: string[]): boolean => {
  return userRoles.includes('organizer') || userRoles.includes('admin')
}

// 检查是否可以管理活动
export const canManageActivity = (activityOrganizerId: number, currentUserId: number, userRoles: string[]): boolean => {
  // 超级管理员可以管理所有活动
  if (userRoles.includes('admin')) {
    return true
  }
  
  // 组织者只能管理自己创建的活动
  if (userRoles.includes('organizer')) {
    return activityOrganizerId === currentUserId
  }
  
  return false
}

// 组织者权限路由守卫
export const organizerRouteGuard = (to: any, from: any, next: any, userInfo: any) => {
  const userRoles = userInfo?.roles || []
  
  // 组织者相关路由
  const organizerRoutes = [
    '/activities/create',
    '/activities/:id/edit',
    '/registrations/review',
    '/checkins/manage',
    '/activity-summaries/create'
  ]
  
  const isOrganizerRoute = organizerRoutes.some(route => {
    const pattern = route.replace(/:id/g, '\\d+')
    return new RegExp(`^${pattern}$`).test(to.path)
  })
  
  if (isOrganizerRoute && !isOrganizer(userRoles)) {
    next('/403') // 权限不足页面
    return
  }
  
  next()
}

// 组织者菜单权限过滤
export const filterOrganizerMenus = (menus: any[], userRoles: string[]): any[] => {
  if (userRoles.includes('admin')) {
    return menus // 管理员看到所有菜单
  }
  
  if (!userRoles.includes('organizer')) {
    return [] // 非组织者不显示组织者菜单
  }
  
  // 组织者菜单配置
  const organizerMenuCodes = [
    'activity-management',
    'registration-review', 
    'checkin-management',
    'activity-summary'
  ]
  
  return menus.filter(menu => 
    organizerMenuCodes.includes(menu.menuCode) || 
    (menu.children && menu.children.some((child: any) => organizerMenuCodes.includes(child.menuCode)))
  )
}

// 组织者功能权限检查
export const organizerFeatureChecks = {
  // 是否可以创建活动
  canCreateActivity: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.ACTIVITY_CREATE, userRoles),
  
  // 是否可以编辑活动
  canEditActivity: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.ACTIVITY_EDIT, userRoles),
  
  // 是否可以发布活动
  canPublishActivity: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.ACTIVITY_PUBLISH, userRoles),
  
  // 是否可以取消活动
  canCancelActivity: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.ACTIVITY_CANCEL, userRoles),
  
  // 是否可以审核报名
  canReviewRegistration: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.REGISTRATION_REVIEW, userRoles),
  
  // 是否可以管理签到
  canManageCheckin: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.CHECKIN_MANAGE, userRoles),
  
  // 是否可以发布活动总结
  canPublishSummary: (userRoles: string[]) => checkOrganizerPermission(ORGANIZER_PERMISSIONS.SUMMARY_PUBLISH, userRoles)
}
