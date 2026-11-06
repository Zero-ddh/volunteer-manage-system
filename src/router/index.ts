import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Auth/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Auth/RegisterView.vue'),
      meta: { title: '注册', requiresAuth: false }
    },
    {
      path: '/activities',
      name: 'activities',
      component: () => import('../views/Activity/ActivityListView.vue'),
      meta: { title: '活动列表' }
    },
    {
      path: '/activities/:id',
      name: 'activity-detail',
      component: () => import('../views/Activity/ActivityDetailView.vue'),
      meta: { title: '活动详情' }
    },
    {
      path: '/insurance',
      name: 'insurance-service',
      component: () => import('../views/Insurance/InsuranceServiceView.vue'),
      meta: { title: '保险服务' }
    },
    {
      path: '/demo/:id',
      name: 'demo-scan',
      component: () => import('../views/Demo/DemoScanPage.vue'),
      meta: { title: '扫码演示', requiresAuth: false }
    },
    {
      path: '/checkin/:qrCode',
      name: 'checkin-page',
      component: () => import('../views/Checkin/CheckinPage.vue'),
      meta: { title: '活动签到', requiresAuth: false }
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/User/UserCenterView.vue'),
      meta: { title: '用户中心', requiresAuth: true },
      children: [
        {
          path: 'profile',
          name: 'user-profile',
          component: () => import('../views/User/ProfileView.vue'),
          meta: { title: '个人信息' }
        },
        {
          path: 'messages',
          name: 'user-messages',
          component: () => import('../views/User/MessageCenterView.vue'),
          meta: { title: '消息中心' }
        },
        {
          path: 'registrations',
          name: 'user-registrations',
          component: () => import('../views/User/RegistrationsView.vue'),
          meta: { title: '我的报名' }
        },
        {
          path: 'recruitment-applications',
          name: 'user-recruitment-applications',
          component: () => import('../views/User/RecruitmentApplicationsView.vue'),
          meta: { title: '我的申请' }
        },
        {
          path: 'communities',
          name: 'user-communities',
          component: () => import('../views/User/MyCommunitiesView.vue'),
          meta: { title: '我的社群' }
        },
        {
          path: 'service-records',
          name: 'user-service-records',
          component: () => import('../views/User/ServiceRecordsView.vue'),
          meta: { title: '服务记录' }
        },
        {
          path: 'service-cases',
          name: 'user-service-cases',
          component: () => import('../views/User/ServiceCaseView.vue'),
          meta: { title: '典型案例' }
        }
        ,
        {
          path: 'checkins',
          name: 'user-checkins',
          component: () => import('../views/User/CheckinsView.vue'),
          meta: { title: '签到记录' }
        },
        {
          path: 'points',
          name: 'user-points',
          component: () => import('../views/User/PointsView.vue'),
          meta: { title: '积分记录' }
        },
        {
          path: 'points-mall',
          name: 'user-points-mall',
          component: () => import('../views/User/PointsMallView.vue'),
          meta: { title: '积分商城' }
        },
        {
          path: 'skills',
          name: 'user-skills',
          component: () => import('../views/User/SkillManagementView.vue'),
          meta: { title: '技能管理' }
        },
        {
          path: 'certificates',
          name: 'user-certificates',
          component: () => import('../views/User/CertificateManagementView.vue'),
          meta: { title: '技能证书' }
        },
        {
          path: 'safety/emergency-contacts',
          name: 'user-emergency-contacts',
          component: () => import('../views/User/EmergencyContactsView.vue'),
          meta: { title: '紧急联系人', requiresAuth: true }
        },
        {
          path: 'safety/insurance',
          name: 'user-insurance',
          component: () => import('../views/User/InsuranceView.vue'),
          meta: { title: '保险购买', requiresAuth: true }
        }
      ]
    },
    // {
    //   path: '/about',
    //   name: 'about',
    //   component: () => import('../views/AboutView.vue'),
    //   meta: { title: '关于我们' }
    // },
    {
      path: '/star-volunteers',
      name: 'star-volunteers',
      component: () => import('../views/StarVolunteersView.vue'),
      meta: { title: '星级志愿者' }
    }
    ,
    {
      path: '/recruitment',
      name: 'recruitment-list',
      component: () => import('../views/Recruitment/ListView.vue'),
      meta: { title: '招募信息' }
    },
    {
      path: '/recruitment/:id',
      name: 'recruitment-detail',
      component: () => import('../views/Recruitment/DetailView.vue'),
      meta: { title: '招募详情' }
    },
    {
      path: '/recruitment/:id/apply',
      name: 'recruitment-apply',
      component: () => import('../views/Recruitment/ApplyView.vue'),
      meta: { title: '申请招募', requiresAuth: true }
    }
    ,
    {
      path: '/community',
      name: 'community-list',
      component: () => import('../views/Community/CommunityListView.vue'),
      meta: { title: '社群列表' }
    },
    {
      path: '/community/:id',
      name: 'community-detail',
      component: () => import('../views/Community/CommunityDetailView.vue'),
      meta: { title: '社群详情' }
    }
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 志愿者服务平台`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth === true) {
    if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
  }
  
  // 如果已登录且访问登录/注册页面，重定向到首页
  if (authStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    next('/')
    return
  }
  
  next()
})

export default router
