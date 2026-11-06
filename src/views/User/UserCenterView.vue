<template>
  <div class="user-center">
    <div class="grid">
      <!-- 左侧侧栏 -->
      <aside class="sidebar">
        <div class="profile">
          <div class="avatar">{{ getFirstChar() }}</div>
          <div class="name">{{ userInfo?.realName || username || '志愿者' }}</div>
          <div class="level">志愿者 · {{ getStarLevelText(userInfo?.starLevel || 0) }}</div>
        </div>
        <ul class="menu">
          <li><a :class="{active: isActive('user-profile')}" @click.prevent="go('/user/profile')">👤 个人信息</a></li>
          <li><a :class="{active: isActive('user-messages')}" @click.prevent="go('/user/messages')">📬 消息中心</a></li>
          <li><a :class="{active: isActive('user-registrations')}" @click.prevent="go('/user/registrations')">📋 我的活动</a></li>
          <li><a :class="{active: isActive('user-recruitment-applications')}" @click.prevent="go('/user/recruitment-applications')">📝 招募申请</a></li>
          <li><a :class="{active: isActive('user-communities')}" @click.prevent="go('/user/communities')">👥 我的社群</a></li>
          <li><a :class="{active: isActive('user-points')}" @click.prevent="go('/user/points')">⭐ 积分记录</a></li>
          <li><a :class="{active: isActive('user-points-mall')}" @click.prevent="go('/user/points-mall')">🛒 积分商城</a></li>
          <li><a :class="{active: isActive('user-skills')}" @click.prevent="go('/user/skills')">🎯 技能管理</a></li>
          <li><a :class="{active: isActive('user-certificates')}" @click.prevent="go('/user/certificates')">📜 技能证书</a></li>
          <li><a :class="{active: isActive('user-service-records')}" @click.prevent="go('/user/service-records')">📊 服务记录</a></li>
          <li><a :class="{active: isActive('user-checkins')}" @click.prevent="go('/user/checkins')">🕒 签到记录</a></li>
          <li><a :class="{active: isActive('user-emergency-contacts')}" @click.prevent="go('/user/safety/emergency-contacts')">📞 紧急联系人</a></li>
          <li><a :class="{active: isActive('user-insurance')}" @click.prevent="go('/user/safety/insurance')">🛡️ 我的保险</a></li>
        </ul>
      </aside>

      <!-- 右侧内容 -->
      <section class="content">
        <header class="content-header">
          <h1 class="title">{{ getPageTitle() }}</h1>
          <p class="subtitle">管理个人资料、查看活动与积分</p>
        </header>
        <div class="content-body">
          <router-view />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const username = computed(() => authStore.username || '')
const userInfo = computed(() => authStore.userInfo || {})

const go = (path: string) => router.push(path)
const isActive = (name: string) => route.name === name

// 获取用户姓名第一个字符
const getFirstChar = () => {
  const info = userInfo.value as any
  // 优先使用真实姓名
  if (info?.realName) {
    return info.realName.charAt(0)
  }
  // 其次使用userInfo.username
  if (info?.username) {
    return info.username.charAt(0)
  }
  // 最后使用authStore中的username（从localStorage获取）
  if (username.value) {
    return username.value.charAt(0)
  }
  return ''
}

// 获取星级文本（与管理后台逻辑一致）
const getStarLevelText = (starLevel: number) => {
  switch (starLevel) {
    case 5: return '五星'
    case 4: return '四星'
    case 3: return '三星'
    case 2: return '二星'
    case 1: return '一星'
    case 0: return '无星'
    default: return '无星'
  }
}

// 获取页面标题，技能管理页面显示"个人信息"
const getPageTitle = () => {
  if (route.name === 'user-skills') {
    return '个人信息'
  }
  return route.meta.title || '用户中心'
}

// 组件挂载时获取用户信息
onMounted(async () => {
  if (!authStore.userInfo && authStore.isLoggedIn) {
    try {
      await authStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.user-center { padding: 16px; }
.grid { display: grid; grid-template-columns: 220px 1fr; gap: 16px; }
.sidebar { background: #fff; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.06); overflow: hidden; }
.profile { padding: 24px 16px; text-align: center; background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%); color: #fff; }
.avatar { width: 80px; height: 80px; border-radius: 50%; background: rgba(255,255,255,.2); margin: 0 auto 10px; display:flex; align-items:center; justify-content:center; font-size: 32px; }
.name { font-size: 18px; font-weight: 700; }
.level { opacity: .9; }
.menu { list-style: none; padding: 0; margin: 0; }
.menu li { border-bottom: 1px solid #eee; }
.menu li:last-child { border-bottom: none; }
.menu a { display: block; padding: 14px 16px; color: #333; text-decoration: none; }
.menu a:hover { background: #f8f9fa; color: #2c5aa0; }
.menu a.active { background: #2c5aa0; color: #fff; position: relative; }
.menu a.active:before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: #1e3d6f; }

.content { background: #fff; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.06); overflow: hidden; }
.content-header { padding: 20px; border-bottom: 1px solid #eee; background: #f8f9fa; }
.content-body { padding: 20px; }

@media (max-width: 768px) {
  .grid { grid-template-columns: 1fr; }
  .sidebar { order: 2; }
  .content { order: 1; }
}
</style>


