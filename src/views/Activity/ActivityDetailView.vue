<template>
  <div class="activity-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">活动详情</h1>
      </div>
      <div class="header-right" v-if="activity">
        <el-button type="warning" @click="editActivity">编辑活动</el-button>
        <el-button type="danger" @click="deleteActivity" v-if="canDelete">删除活动</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 活动详情内容 -->
    <div v-else-if="activity" class="activity-content">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(activity.status)">
              {{ getStatusText(activity.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动标题" :span="2">
            {{ activity.title }}
          </el-descriptions-item>
          <el-descriptions-item label="活动分类">
            {{ activity.categoryName || '未分类' }}
          </el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag :type="getStatusType(activity.status)">
              {{ getStatusText(activity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动时间">
            {{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="活动地点">
            {{ activity.location }}
          </el-descriptions-item>
          <el-descriptions-item label="参与人数">
            {{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants || '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="组织者">
            {{ activity.organizerName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系方式">
            {{ activity.contactInfo || '未提供' }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急联系人">
            {{ activity.emergencyContactName || '未提供' }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急联系电话">
            {{ activity.emergencyContactPhone || '未提供' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(activity.createdTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(activity.updatedTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 活动描述 -->
      <el-card class="description-card">
        <template #header>
          <span>活动描述</span>
        </template>
        <div class="description-content" v-html="formatDescription(activity.description)"></div>
      </el-card>

      <!-- 活动参与条件 -->
      <el-card class="requirements-card" v-if="activity.participationConditions">
        <template #header>
          <span>活动参与条件</span>
        </template>
        <div class="requirements-content" v-html="formatDescription(activity.participationConditions)"></div>
      </el-card>

      <!-- 技能要求 -->
      <el-card class="requirements-card" v-if="activity.requirements">
        <template #header>
          <span>技能要求</span>
        </template>
        <div class="requirements-content" v-html="formatDescription(activity.requirements)"></div>
      </el-card>

      <!-- 活动图片 -->
      <el-card class="images-card" v-if="activity.images && activity.images.length > 0">
        <template #header>
          <span>活动图片</span>
        </template>
        <div class="images-grid">
          <el-image
            v-for="(image, index) in activity.images"
            :key="index"
            :src="image"
            :preview-src-list="activity.images"
            :initial-index="index"
            fit="cover"
            class="activity-image"
          />
        </div>
      </el-card>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <el-empty description="活动不存在或已被删除">
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activityApi } from '@/api'

// 路由和导航
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const activity = ref<any>(null)

// 获取活动ID
const activityId = route.params.id as string

// 获取活动详情
const getActivityDetail = async () => {
  loading.value = true
  try {
    const response = await activityApi.getActivityDetail(parseInt(activityId))
    activity.value = response.data
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 编辑活动
const editActivity = () => {
  router.push(`/activities/${activityId}/edit`)
}

// 删除活动
const deleteActivity = async () => {
  try {
    await ElMessageBox.confirm(
      '确认删除此活动吗？删除后无法恢复！',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await activityApi.deleteActivity(parseInt(activityId))
    ElMessage.success('删除成功')
    router.push('/activities/list')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: 'info',    // 草稿
    2: 'success', // 已发布
    3: 'warning', // 进行中
    4: 'info',    // 已结束
    5: 'danger'   // 已取消
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: '草稿',
    2: '已发布',
    3: '进行中',
    4: '已结束',
    5: '已取消'
  }
  return statusMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '未设置'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 格式化描述内容
const formatDescription = (description: string) => {
  if (!description) return '暂无描述'
  return description.replace(/\n/g, '<br>')
}

// 判断是否可以删除
const canDelete = computed(() => {
  return activity.value && activity.value.status === 1 // 只有草稿状态可以删除
})

// 页面加载
onMounted(() => {
  getActivityDetail()
})
</script>

<style scoped>
.activity-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 10px;
}

.loading-container {
  padding: 20px;
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.description-card,
.requirements-card,
.images-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-content,
.requirements-content {
  line-height: 1.6;
  color: #666;
  min-height: 100px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.activity-image {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  cursor: pointer;
}

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-descriptions__content) {
  color: #666;
}
</style>
