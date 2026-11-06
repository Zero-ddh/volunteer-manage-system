<template>
  <div class="activity-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-content">
        <h1 class="page-title">志愿活动</h1>
        <p class="page-subtitle">发现更多有意义的志愿活动，为社会贡献自己的力量</p>
      </div>
    </div>

    <!-- 筛选区（参照原型） -->
    <el-card class="filter-card mb-16">
      <el-row :gutter="16" class="mb-16">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="filter-group">
            <div class="filter-label">搜索关键词</div>
            <el-input v-model="keyword" placeholder="输入活动标题或关键词" clearable />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="filter-group">
            <div class="filter-label">活动分类</div>
            <el-select v-model="category" placeholder="全部分类" clearable class="w-full" :loading="categoryLoading">
              <el-option 
                v-for="c in categories" 
                :key="c.id" 
                :label="c.categoryName" 
                :value="c.id" 
              />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="filter-group">
            <div class="filter-label">活动状态</div>
            <el-select v-model="status" placeholder="全部状态" clearable class="w-full">
              <el-option label="即将开始" value="upcoming" />
              <el-option label="进行中" value="ongoing" />
              <el-option label="已结束" value="ended" />
            </el-select>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="filter-group">
            <div class="filter-label">活动地址</div>
            <el-input v-model="location" placeholder="输入活动地址关键词" clearable />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="16" class="mb-16">
        <el-col :xs="24" :sm="12" :md="12">
          <div class="filter-group">
            <div class="filter-label">活动时间</div>
            <el-date-picker
              v-model="startTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </div>
        </el-col>
      </el-row>
      <div class="filter-actions">
        <el-button @click="reset">重置</el-button>
        <el-button type="primary" @click="load">搜索</el-button>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="activities-card">
      <div class="section-header">
        <div class="section-title">活动列表</div>
        <!-- 排序选择器（仅在登录状态下显示） -->
        <div v-if="isLoggedIn" class="sort-options">
          <span class="sort-label">排序方式：</span>
          <el-radio-group v-model="sortType" @change="handleSortChange">
            <el-radio label="latest">最新发布</el-radio>
            <el-radio label="recommended">智能推荐</el-radio>
          </el-radio-group>
        </div>
      </div>

      <div v-loading="loading">
        <!-- 活动列表 -->
        <div v-if="activities && activities.length > 0">
          <div v-for="item in activities" :key="item.id" class="activity-item" @click="$router.push(`/activities/${item.id}`)">
            <!-- 活动图片已屏蔽 -->
            <!-- <div class="activity-image">{{ item.coverImage ? '' : '活动图片' }}<img v-if="item.coverImage" :src="item.coverImage" alt="cover" /></div> -->
            <div class="activity-info">
              <div class="activity-header">
                <div class="activity-title">{{ item.title }}</div>
                <div class="activity-description">{{ item.description }}</div>
              </div>
              <div class="activity-meta">
                <span>👥 需要{{ item.maxParticipants || 0 }}人</span>
                <span>📅 {{ item.startTime }}</span>
                <span>⏰ 截止：{{ item.endTime || '-' }}</span>
                <!-- 显示推荐匹配分数（仅在智能推荐模式下） -->
                <span v-if="sortType === 'recommended' && item.matchScore !== undefined" class="match-score">
                  🎯 匹配度 {{ Math.round(item.matchScore) }}%
                </span>
              </div>
              <div class="activity-location">📍 {{ item.location }}</div>
              <div class="activity-tags">
                <span class="tag" v-if="item.categoryName">{{ item.categoryName }}</span>
              </div>
            </div>
            <div class="activity-stats">
              <div class="activity-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</div>
              <div class="activity-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercent(item) + '%' }"></div>
                </div>
                <div class="progress-text">{{ (item.currentParticipants || 0) }}/{{ item.maxParticipants || 0 }} 人</div>
              </div>
              <div class="activity-count">已报名 {{ item.currentParticipants || 0 }} 人</div>
            </div>
          </div>
        </div>
        
        <!-- 暂无数据提示 -->
        <div v-else class="no-data">
          <div class="no-data-icon">📋</div>
          <div class="no-data-text">暂无数据</div>
          <div class="no-data-desc">当前没有符合条件的志愿活动，请尝试调整筛选条件</div>
        </div>
      </div>

      <div class="flex-center pagination">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useActivityStore } from '@/stores/activity'
import { useAuthStore } from '@/stores/auth'
import { activityCategoryApi, activityApi } from '@/api'
import { ElMessage } from 'element-plus'

const activityStore = useActivityStore()
const authStore = useAuthStore()
const { getActivityList } = activityStore

const pageNo = ref(1)
const pageSize = ref(9)
const total = ref(0)
const keyword = ref('')
const status = ref<string | undefined>()
const category = ref<number | undefined>()
const location = ref('') // 活动地址
const startTimeRange = ref<[string, string] | null>(null) // 活动时间范围

const categories = ref<any[]>([])
const categoryLoading = ref(false)

// 排序相关
const sortType = ref('latest') // 'latest' | 'recommended'
const activities = ref<any[]>([])
const loading = ref(false)

// 检查登录状态
const isLoggedIn = computed(() => authStore.isLoggedIn)

// 状态值映射：前端字符串 -> 后端整数
const statusMap: { [key: string]: number } = {
  'upcoming': 2,  // 即将开始
  'ongoing': 3,   // 进行中
  'ended': 4      // 已结束
}

// 加载活动列表
const load = async () => {
  // 游客状态下默认按发布时间排序，登录状态下根据选择的排序方式加载
  if (isLoggedIn.value && sortType.value === 'recommended') {
    await loadRecommendedActivities()
  } else {
    // 游客或选择"最新发布"时，使用默认的发布时间排序
    await loadLatestActivities()
  }
}

// 加载最新发布的活动的
const loadLatestActivities = async () => {
  try {
    loading.value = true
    console.log('开始加载活动列表，参数:', {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      status: status.value,
      category: category.value,
      location: location.value,
      startTimeRange: startTimeRange.value
    })
    
    // 构建查询参数
    const params: any = {
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    
    if (keyword.value) params.keyword = keyword.value
    if (status.value) params.status = statusMap[status.value]
    if (category.value) params.categoryId = category.value
    if (location.value) params.location = location.value
    
    // 处理活动时间范围（日期格式转换为全天时间范围）
    if (startTimeRange.value && startTimeRange.value.length === 2) {
      // 开始日期：yyyy-MM-dd 00:00:00
      params.startTimeBegin = startTimeRange.value[0] + ' 00:00:00'
      // 结束日期：yyyy-MM-dd 23:59:59
      params.startTimeEnd = startTimeRange.value[1] + ' 23:59:59'
    }
    
    const res: any = await getActivityList(params)
    
    console.log('API响应:', res)
    
    // 处理不同的响应数据结构
    if (res && res.code === 200 && res.data) {
      // 标准分页响应：{ code: 200, data: { records: [...], total: 39 } }
      activities.value = res.data.records || res.data || []
      total.value = res.data.total || 0
      console.log('活动列表数据:', activities.value, '总数:', total.value)
    } else if (res && res.records) {
      // 直接返回分页结构：{ records: [...], total: 39 }
      activities.value = res.records || []
      total.value = res.total || 0
      console.log('活动列表数据（records）:', activities.value, '总数:', total.value)
    } else if (res && res.data && Array.isArray(res.data)) {
      // 直接返回数组的情况
      activities.value = res.data
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      // 直接返回数组
      activities.value = res
      total.value = res.length
    } else {
      activities.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    activities.value = []
    total.value = 0
  } finally {
    loading.value = false
    console.log('加载完成，activities.value:', activities.value)
  }
}

// 加载智能推荐的活动
const loadRecommendedActivities = async () => {
  try {
    loading.value = true
    const params: any = {
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    
    if (category.value) {
      params.categoryId = category.value
    }
    
    if (keyword.value) {
      // 推荐接口可能不支持关键词搜索，先在本地筛选
      const res: any = await activityApi.getRecommendedActivities(params)
      const allActivities = res || []
      activities.value = allActivities.filter((item: any) => 
        item.title?.includes(keyword.value) || 
        item.description?.includes(keyword.value)
      )
    } else {
      const res: any = await activityApi.getRecommendedActivities(params)
      activities.value = res || []
    }
    
    total.value = activities.value.length
    
    // 再次应用状态筛选
    if (status.value && statusMap[status.value]) {
      const statusValue = statusMap[status.value]
      activities.value = activities.value.filter((item: any) => item.status === statusValue)
    }
  } catch (error) {
    console.error('加载推荐活动失败:', error)
    ElMessage.warning('推荐功能需要登录后才能使用')
    // 如果推荐失败，回退到最新发布
    sortType.value = 'latest'
    await loadLatestActivities()
  } finally {
    loading.value = false
  }
}

// 处理排序方式变化
const handleSortChange = async () => {
  pageNo.value = 1 // 重置页码
  await load()
}

const reset = () => {
  keyword.value = ''
  status.value = undefined
  category.value = undefined
  location.value = ''
  startTimeRange.value = null
  load()
}

const progressPercent = (item: any) => {
  const a = Number(item.currentParticipants || 0)
  const b = Number(item.maxParticipants || 0)
  if (!b) return 0
  return Math.min(100, Math.round((a / b) * 100))
}

const statusText = (s: any) => {
  const statusMap: { [key: number]: string } = {
    1: '草稿',
    2: '即将开始', 
    3: '进行中',
    4: '已结束',
    5: '已取消'
  }
  return statusMap[s] || '进行中'
}

const statusClass = (s: any) => {
  const classMap: { [key: number]: string } = {
    1: 'status-draft',
    2: 'status-upcoming',
    3: 'status-ongoing', 
    4: 'status-ended',
    5: 'status-cancelled'
  }
  return classMap[s] || 'status-ongoing'
}

// 加载活动分类
const loadCategories = async () => {
  try {
    categoryLoading.value = true
    const res = await activityCategoryApi.getEnabledCategories()
    categories.value = res || []
  } catch (error) {
    console.error('加载活动分类失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.page-header { 
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: #fff;
  padding: 60px 0;
  text-align: center;
  margin-bottom: 20px;
  /* 全宽铺满屏幕两侧（突破父级容器宽度限制） */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}
.page-header-content { 
  max-width: 800px; 
  margin: 0 auto; 
  padding: 0 20px; 
}
.page-title { 
  font-size: 32px; 
  font-weight: 700; 
  color: #fff; 
  margin: 0 0 12px 0; 
}
.page-subtitle { 
  color: #fff; 
  opacity: 0.95; 
  font-size: 16px; 
}
.filter-card { border-radius: 10px; }
.filter-group { display: flex; flex-direction: column; }
.filter-label { font-weight: 600; color: #333; margin-bottom: 6px; }
.w-full { width: 100%; }
.filter-actions { display: flex; justify-content: flex-end; gap: 12px; }

.activities-card { border-radius: 10px; overflow: hidden; }
.section-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 8px; border-bottom: 1px solid #eee; }
.section-title { font-weight: 700; color: #333; }
.sort-options { display: flex; align-items: center; gap: 8px; white-space: nowrap; }
.sort-label { color: #666; font-size: 13px; }
.match-score {
  color: #ff4757;
  font-weight: bold;
  font-size: 13px;
}
.match-tag { 
  background: #fff;
  color: #ff4757 !important; 
  font-weight: bold;
  font-size: 13px;
  border: 2px solid #ff4757;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.2);
}

.activity-item { display: grid; grid-template-columns: 1fr auto; gap: 16px; padding: 20px 8px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.activity-item:last-child { border-bottom: none; }
.activity-image { width: 200px; height: 120px; background: #e9ecef; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #666; position: relative; overflow: hidden; }
.activity-image img { width: 100%; height: 100%; object-fit: cover; }
.activity-header { display: flex; gap: 20px; align-items: flex-start; margin-bottom: 12px; }
.activity-title { font-size: 18px; font-weight: 700; color: #333; flex-shrink: 0; }
.activity-meta { display: flex; gap: 16px; color: #666; font-size: 13px; margin-bottom: 8px; }
.activity-location { color: #666; font-size: 13px; margin-bottom: 8px; }
.activity-description { color: #666; font-size: 14px; line-height: 1.5; flex: 1; }
.activity-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tag { padding: 4px 8px; background: #e9ecef; color: #666; border-radius: 12px; font-size: 12px; }

.activity-stats { text-align: right; min-width: 150px; }
.activity-status { padding: 5px 10px; border-radius: 15px; font-size: 12px; font-weight: 700; display: inline-block; margin-bottom: 10px; }
.status-draft { background: #e2e3e5; color: #6c757d; }
.status-upcoming { background: #fff3cd; color: #856404; }
.status-ongoing { background: #d4edda; color: #155724; }
.status-ended { background: #f8d7da; color: #721c24; }
.status-cancelled { background: #f5c6cb; color: #721c24; }
.activity-progress { margin-bottom: 10px; }
.progress-bar { width: 100%; height: 6px; background: #e9ecef; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: #2c5aa0; }
.progress-text { font-size: 12px; color: #666; text-align: center; }
.pagination { padding: 16px 0 8px; }

/* 暂无数据样式 */
.no-data {
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
  .page-header {
    padding: 40px 0;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .activity-item { grid-template-columns: 1fr; text-align: center; }
  .activity-image { width: 100%; height: 150px; }
  .activity-stats { text-align: center; }
  .activity-header { flex-direction: column; align-items: flex-start; text-align: left; }
  .activity-description { width: 100%; }
  
  .no-data {
    padding: 40px 20px;
  }
  
  .no-data-icon {
    font-size: 36px;
  }
  
  .no-data-text {
    font-size: 16px;
  }
}
</style>


