<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { activityApi } from '@/api'

const authStore = useAuthStore()
const router = useRouter()

const categories = ref([
  { icon: '🌱', name: '环保公益', desc: '保护环境，共建绿色家园' },
  { icon: '📚', name: '教育助学', desc: '关爱儿童，助力教育发展' },
  { icon: '👴', name: '敬老爱老', desc: '关爱老人，传递温暖' },
  { icon: '🏥', name: '医疗健康', desc: '健康服务，关爱生命' }
])

// 定义活动数据接口
interface ActivityData {
  id: number
  title: string
  time: string
  statusText: string
  statusType: string
  location: string
  progress: number
  enrolled: string
  category: string
}

interface ActivityItem {
  id: number
  title: string
  startTime?: string
  status: number
  location?: string
  maxParticipants?: number
  currentParticipants?: number
  categoryName?: string
}

interface ApiResponse {
  code?: number
  data?: {
    records?: ActivityItem[]
  }
  records?: ActivityItem[]
}

const hotActivities = ref<ActivityData[]>([])
const loading = ref(false)

// 获取活动状态文字
const getStatusText = (status: number): string => {
  const statusMap: { [key: number]: string } = {
    2: '即将开始',
    3: '进行中',
    4: '已结束'
  }
  return statusMap[status] || '进行中'
}

// 获取活动状态类型
const getStatusType = (status: number): string => {
  if (status === 3) return 'ongoing'
  if (status === 2) return 'upcoming'
  if (status === 4) return 'ended'
  return 'ongoing'
}

// 获取热门活动列表
const fetchHotActivities = async (): Promise<void> => {
  try {
    loading.value = true
    const params = {
      pageNo: 1,
      pageSize: 6,
      status: 3 // 只显示进行中的活动
    }
    
    const res = await activityApi.getActivityList(params) as ApiResponse
    
    // 处理响应数据
    let activities: ActivityItem[] = []
    if (res && res.code === 200 && res.data) {
      activities = res.data.records || []
    } else if (res && res.records) {
      activities = res.records
    }
    
    // 转换为首页所需格式
    hotActivities.value = activities.map((item: ActivityItem) => {
      const progress = item.maxParticipants 
        ? Math.round((item.currentParticipants || 0) / item.maxParticipants * 100)
        : 0
        
      return {
        id: item.id,
        title: item.title,
        time: item.startTime?.replace('T', ' ') || '',
        statusText: getStatusText(item.status),
        statusType: getStatusType(item.status),
        location: item.location || '待定',
        progress: progress,
        enrolled: `${item.currentParticipants || 0}/${item.maxParticipants || 0}`,
        category: item.categoryName || '未分类'
      }
    })
  } catch (error) {
    console.error('获取热门活动失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到活动详情
const goToActivityDetail = (activityId: number) => {
  router.push(`/activities/${activityId}`)
}

const goActivities = () => {
  router.push('/activities')
}

const goLogin = () => {
  router.push('/login')
}

// 页面加载时获取数据
onMounted(() => {
  fetchHotActivities()
})
</script>

<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">志愿者服务平台</h1>
        <p class="hero-subtitle">连接志愿者与公益组织，让志愿服务更便捷、更高效</p>
        <div class="hero-buttons" v-if="!authStore.isLoggedIn">
          <el-button size="large" type="primary" @click="goActivities">浏览活动</el-button>
          <el-button size="large" @click="goLogin">立即登录</el-button>
        </div>
      </div>
    </section>

    <!-- 活动分类导航 -->
    <section class="category-section">
      <div class="container">
        <h2 class="section-title">活动分类</h2>
        <div class="category-grid">
          <div v-for="c in categories" :key="c.name" class="category-card" @click="goActivities">
            <div class="category-icon">{{ c.icon }}</div>
            <h3 class="category-name">{{ c.name }}</h3>
            <p class="category-desc">{{ c.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门活动展示 -->
    <section class="activities-section">
      <div class="container">
        <h2 class="section-title">热门活动</h2>
        <div v-loading="loading" class="activities-grid">
          <div v-for="a in hotActivities" :key="a.id" class="activity-card" @click="goToActivityDetail(a.id)">
            <!-- 活动图片已屏蔽 -->
            <!-- <div class="activity-image">活动图片</div> -->
            <div class="activity-content">
              <h3 class="activity-title">{{ a.title }}</h3>
              <div class="activity-meta">
                <span>{{ a.time }}</span>
                <span class="activity-status" :class="a.statusType === 'ongoing' ? 'status-ongoing' : 'status-upcoming'">{{ a.statusText }}</span>
              </div>
              <div class="activity-location">📍 {{ a.location }}</div>
              <div class="activity-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: a.progress + '%' }"></div>
                </div>
              </div>
              <div class="activity-stats">
                <span>已报名 {{ a.enrolled }} 人</span>
                <span>{{ a.category }}</span>
              </div>
            </div>
          </div>
          <!-- 暂无活动提示 -->
          <div v-if="!loading && hotActivities.length === 0" class="no-activities">
            <div class="no-data-icon">📋</div>
            <div class="no-data-text">暂无活动</div>
            <div class="no-data-desc">当前没有进行中的活动</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 积分排行榜 - 已屏蔽 -->
    <!-- <section class="ranking-section">
      <div class="container">
        <h2 class="section-title">积分排行榜</h2>
        <div class="ranking-content">
          <div class="ranking-list">
            <div v-for="(volunteer, index) in pointsRanking" :key="volunteer.id" class="ranking-item">
              <div class="ranking-number" :class="getRankingClass(index + 1)">
                {{ index + 1 }}
              </div>
              <div class="ranking-info">
                <div class="volunteer-name">{{ volunteer.realName }}</div>
                <div class="volunteer-stats">
                  <span class="points">{{ volunteer.totalPoints }} 积分</span>
                  <span class="hours">{{ volunteer.totalServiceHours }}h</span>
                </div>
              </div>
            </div>
          </div>
          <div class="ranking-actions">
            <el-button type="primary" @click="goStarVolunteers">查看完整排行榜</el-button>
          </div>
        </div>
      </div>
    </section> -->

    <!-- 平台介绍 -->
    <section class="intro-section">
      <div class="container">
        <div class="intro-content">
          <div class="intro-header">
            <h3>为什么选择我们？</h3>
            <p class="intro-description">志愿者活动服务平台致力于为志愿者和公益组织搭建一个高效、便捷的服务桥梁。我们提供全方位的志愿服务管理解决方案。</p>
          </div>
          <div class="intro-features-grid">
            <div class="feature-card">
              <div class="feature-icon">📋</div>
              <div class="feature-title">丰富的活动类型</div>
              <div class="feature-desc">满足不同志愿服务需求</div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">⚡</div>
              <div class="feature-title">便捷的报名流程</div>
              <div class="feature-desc">一键参与志愿活动</div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">✓</div>
              <div class="feature-title">完善的签到系统</div>
              <div class="feature-desc">确保服务真实性</div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">⭐</div>
              <div class="feature-title">积分奖励机制</div>
              <div class="feature-desc">激励持续参与</div>
            </div>
            <div class="feature-card">
              <div class="feature-icon">📁</div>
              <div class="feature-title">服务记录管理</div>
              <div class="feature-desc">建立个人志愿服务档案</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: #fff;
  padding: 80px 0;
  text-align: center;
  /* 全宽铺满屏幕两侧（突破父级容器宽度限制） */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}
.hero-content { max-width: 800px; margin: 0 auto; padding: 0 20px; }
.hero-title { font-size: 40px; font-weight: bold; margin-bottom: 16px; }
.hero-subtitle { font-size: 18px; opacity: 0.95; margin-bottom: 20px; }
.hero-buttons { display: flex; gap: 16px; justify-content: center; flex-wrap: wrap; }

.category-section { padding: 60px 0; background: #f8f9fa; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.section-title { text-align: center; font-size: 32px; font-weight: bold; margin-bottom: 36px; color: #333; }
.category-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 24px; }
.category-card { background: #fff; padding: 24px; border-radius: 10px; text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.08); transition: transform .3s, box-shadow .3s; cursor: pointer; }
.category-card:hover { transform: translateY(-4px); box-shadow: 0 10px 25px rgba(0,0,0,0.12); }
.category-icon { font-size: 42px; margin-bottom: 12px; }
.category-name { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 6px; }
.category-desc { color: #666; font-size: 14px; }

.activities-section { padding: 60px 0; }
.activities-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px; }
.activity-card { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.08); transition: transform .3s, box-shadow .3s; cursor: pointer; }
.activity-card:hover { transform: translateY(-4px); box-shadow: 0 10px 25px rgba(0,0,0,0.12); }
.activity-image { height: 200px; background: #e9ecef; display: flex; align-items: center; justify-content: center; color: #666; }
.activity-content { padding: 16px; }
.activity-title { font-size: 18px; font-weight: bold; margin-bottom: 8px; color: #333; }
.activity-meta { display: flex; justify-content: space-between; font-size: 13px; color: #666; margin-bottom: 10px; }
.activity-location { color: #666; font-size: 14px; margin-bottom: 10px; }
.progress-bar { width: 100%; height: 6px; background: #e9ecef; border-radius: 3px; overflow: hidden; margin-bottom: 10px; }
.progress-fill { height: 100%; background: #2c5aa0; }
.activity-stats { display: flex; justify-content: space-between; color: #666; font-size: 13px; }
.activity-status { padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 700; }
.status-ongoing { background: #d4edda; color: #155724; }
.status-upcoming { background: #fff3cd; color: #856404; }

.ranking-section { padding: 60px 0; background: #fff; }
.ranking-content { display: flex; gap: 40px; align-items: flex-start; }
.ranking-list { flex: 1; }
.ranking-item { display: flex; align-items: center; gap: 16px; padding: 16px; margin-bottom: 12px; background: #f8f9fa; border-radius: 8px; transition: background .3s; }
.ranking-item:hover { background: #e9ecef; }
.ranking-number { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; color: white; font-size: 14px; }
.rank-first { background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%); color: #333; }
.rank-second { background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%); color: #333; }
.rank-third { background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%); }
.rank-normal { background: #6c757d; }
.ranking-info { flex: 1; }
.volunteer-name { font-weight: bold; color: #333; margin-bottom: 4px; }
.volunteer-stats { display: flex; gap: 16px; font-size: 13px; color: #666; }
.points { color: #e74c3c; font-weight: bold; }
.ranking-actions { display: flex; align-items: center; }

.intro-section { padding: 60px 0; background: #f8f9fa; }
.intro-content { max-width: 1000px; margin: 0 auto; }
.intro-header { text-align: center; margin-bottom: 48px; }
.intro-header h3 { font-size: 32px; font-weight: bold; color: #333; margin-bottom: 16px; }
.intro-description { font-size: 16px; color: #666; line-height: 1.8; max-width: 700px; margin: 0 auto; }

.intro-features-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 24px; }
.feature-card { background: #fff; padding: 32px 24px; border-radius: 12px; text-align: center; box-shadow: 0 2px 12px rgba(0,0,0,0.06); transition: all .3s ease; cursor: pointer; }
.feature-card:hover { transform: translateY(-6px); box-shadow: 0 8px 24px rgba(44, 90, 160, 0.15); }
.feature-icon { font-size: 48px; margin-bottom: 16px; line-height: 1; }
.feature-title { font-size: 16px; font-weight: bold; color: #333; margin-bottom: 8px; }
.feature-desc { font-size: 14px; color: #666; line-height: 1.5; }

/* 暂无活动样式 */
.no-activities {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #999;
}
.no-data-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}
.no-data-text {
  font-size: 18px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}
.no-data-desc {
  font-size: 14px;
  color: #999;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .hero-title { font-size: 32px; }
  .hero-subtitle { font-size: 16px; }
  .intro-header h3 { font-size: 28px; }
  .intro-description { font-size: 15px; }
  .intro-features-grid { grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .feature-card { padding: 24px 16px; }
  .feature-icon { font-size: 36px; margin-bottom: 12px; }
  .feature-title { font-size: 14px; }
  .feature-desc { font-size: 12px; }
}
</style>
