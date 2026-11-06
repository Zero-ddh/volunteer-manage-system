import { createRouter, createWebHistory } from 'vue-router'
import { useAdminAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login/LoginView.vue'),
      meta: { title: '管理员登录', requiresAuth: false }
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/Layout/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/Home/HomeView.vue'),
          meta: { title: '系统首页', icon: 'HomeFilled' }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('../views/User/UserListView.vue'),
          meta: { title: '用户管理', icon: 'User' }
        },
        {
          path: 'users/:id',
          name: 'user-detail',
          component: () => import('../views/User/UserDetailView.vue'),
          meta: { title: '用户详情', hidden: true }
        },
        {
          path: 'volunteers',
          name: 'volunteers',
          component: () => import('../views/Volunteer/VolunteerListView.vue'),
          meta: { title: '志愿者列表', icon: 'UserFilled' }
        },
        {
          path: 'volunteers/certificate-review',
          name: 'certificate-review',
          component: () => import('../views/Volunteer/CertificateReviewView.vue'),
          meta: { title: '证书审核', icon: 'DocumentChecked' }
        },
        {
          path: 'roles',
          name: 'roles',
          component: () => import('../views/Role/RoleListView.vue'),
          meta: { title: '角色管理', icon: 'UserFilled' }
        },
        {
          path: 'menus',
          name: 'menus',
          component: () => import('../views/Menu/MenuListView.vue'),
          meta: { title: '菜单管理', icon: 'Menu' }
        },
        {
          path: 'activity-categories',
          name: 'activity-categories',
          component: () => import('../views/System/ActivityCategory/Index.vue'),
          meta: { title: '活动分类管理', icon: 'Menu' }
        },
        {
          path: 'activities/list',
          name: 'activities',
          component: () => import('../views/Activity/ActivityListView.vue'),
          meta: { title: '活动列表', icon: 'Calendar' }
        },
        {
          path: 'activities/create',
          name: 'activity-create',
          component: () => import('../views/Activity/ActivityFormView.vue'),
          meta: { title: '创建活动', hidden: true }
        },
        {
          path: 'activities/:id/edit',
          name: 'activity-edit',
          component: () => import('../views/Activity/ActivityFormView.vue'),
          meta: { title: '编辑活动', hidden: true }
        },
        {
          path: 'activities/:id',
          name: 'activity-detail',
          component: () => import('../views/Activity/ActivityDetailView.vue'),
          meta: { title: '活动详情', hidden: true }
        },
        {
          path: 'registrations/review',
          name: 'registration-review',
          component: () => import('../views/Registration/RegistrationReviewView.vue'),
          meta: { title: '报名审核', icon: 'Check' }
        },
        {
          path: 'checkins/manage',
          name: 'checkin-management',
          component: () => import('../views/Checkin/CheckinManagementView.vue'),
          meta: { title: '签到管理', icon: 'Clock' }
        },
        {
          path: 'activity-summaries',
          name: 'activity-summaries',
          component: () => import('../views/ActivitySummary/ActivitySummaryFormView.vue'),
          meta: { title: '活动总结', icon: 'Document' }
        },
        {
          path: 'activity-summaries/:id/edit',
          name: 'activity-summary-edit',
          component: () => import('../views/ActivitySummary/ActivitySummaryFormView.vue'),
          meta: { title: '编辑活动总结', hidden: true }
        },
        {
          path: 'recruitment',
          name: 'recruitment',
          component: () => import('../views/Recruitment/RecruitmentListView.vue'),
          meta: { title: '招募管理', icon: 'User' }
        },
        {
          path: 'recruitment/create',
          name: 'recruitment-create',
          component: () => import('../views/Recruitment/RecruitmentFormView.vue'),
          meta: { title: '创建招募', hidden: true }
        },
        {
          path: 'recruitment/view/:id',
          name: 'recruitment-detail',
          component: () => import('../views/Recruitment/RecruitmentDetailView.vue'),
          meta: { title: '招募详情', hidden: true }
        },
        {
          path: 'recruitment/:id/applicants',
          name: 'recruitment-applicants',
          component: () => import('../views/Recruitment/ApplicantsListView.vue'),
          meta: { title: '报名者管理', hidden: true }
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('../views/Statistics/StatisticsView.vue'),
          meta: { title: '数据统计', icon: 'DataAnalysis' }
        },
        {
          path: 'statistics/activity',
          name: 'statistics-activity',
          component: () => import('../views/Statistics/ActivityStatistics.vue'),
          meta: { title: '活动统计', hidden: true }
        },
        {
          path: 'statistics/category',
          name: 'statistics-category',
          component: () => import('../views/Statistics/CategoryStatistics.vue'),
          meta: { title: '分类统计', hidden: true }
        },
        {
          path: 'statistics/ranking',
          name: 'statistics-ranking',
          component: () => import('../views/Statistics/VolunteerRanking.vue'),
          meta: { title: '志愿者排名', hidden: true }
        },
        {
          path: 'system/points-rules',
          name: 'points-rules',
          component: () => import('../views/System/PointsRuleConfigView.vue'),
          meta: { title: '积分规则配置', icon: 'Star' }
        },
        {
          path: 'goods',
          name: 'goods',
          component: () => import('../views/Goods/GoodsListView.vue'),
          meta: { title: '商品管理', icon: 'Goods' }
        },
        {
          path: 'points-exchange',
          name: 'points-exchange',
          component: () => import('../views/PointsExchange/PointsExchangeListView.vue'),
          meta: { title: '积分兑换记录', icon: 'ShoppingCart' }
        },
        {
          path: 'insurance/products',
          name: 'insurance-products',
          component: () => import('../views/Insurance/InsuranceProductListView.vue'),
          meta: { title: '保险产品管理', icon: 'Box' }
        },
        {
          path: 'insurance/orders',
          name: 'insurance-orders',
          component: () => import('../views/Insurance/InsuranceOrderListView.vue'),
          meta: { title: '保险订单管理', icon: 'Document' }
        },
        {
          path: 'points-record',
          name: 'points-record',
          component: () => import('../views/PointsRecord/PointsRecordListView.vue'),
          meta: { title: '积分记录', icon: 'Coin' }
        },
        // 社群管理相关路由
        {
          path: 'community/list',
          name: 'community-list',
          component: () => import('../views/Community/CommunityListView.vue'),
          meta: { title: '社群列表', icon: 'ChatDotRound' }
        },
        {
          path: 'community/posts',
          name: 'community-posts',
          component: () => import('../views/Community/CommunityPostListView.vue'),
          meta: { title: '帖子管理', icon: 'Document' }
        },
        {
          path: 'community/members',
          name: 'community-members',
          component: () => import('../views/Community/CommunityMemberListView.vue'),
          meta: { title: '成员管理', icon: 'UserFilled' }
        },
        {
          path: 'community/summaries',
          name: 'community-summaries',
          component: () => import('../views/Community/ActivitySummaryListView.vue'),
          meta: { title: '活动总结管理', icon: 'DocumentChecked' }
        }
      ]
    }
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAdminAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 管理后台`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth === true) {
    // 检查token是否存在
    const token = localStorage.getItem('admin-token')
    if (!token || !authStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 如果菜单未加载，先加载菜单
    if (authStore.menus.length === 0) {
      try {
        await authStore.fetchMenus()
      } catch (error) {
        console.error('加载菜单失败:', error)
        // 菜单加载失败时，允许访问首页，但限制其他页面
        if (to.name !== 'home') {
          ElMessage.error('菜单加载失败，权限验证异常')
          next('/')
          return
        }
      }
    }
    
    // 检查菜单权限（仅对有图标的路由进行权限检查）
    if (to.meta.icon && to.name !== 'home' && !authStore.hasMenuPermission(to.name as string)) {
      ElMessage.error('没有访问权限')
      next('/')
      return
    }
  }
  
  // 如果已登录且访问登录页面，重定向到首页
  if (authStore.isLoggedIn && to.name === 'login') {
    next('/')
    return
  }
  
  next()
})

export default router
