<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recruitmentApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 类型定义
interface RecruitmentDetail {
  id: number
  title: string
  recruitCount: number
  status: number
  startTime: string
  endTime: string
  contactPerson?: string
  contactInfo?: string
  description?: string
  requirements?: string
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const detail = ref<RecruitmentDetail>({} as RecruitmentDetail)

async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await recruitmentApi.getDetail(id)
    detail.value = res?.data ?? res ?? {}
  } finally {
    loading.value = false
  }
}

function goApply() {
  // 检查用户是否已登录
  if (!authStore.isLoggedIn) {
    ElMessage.warning('您好,请先登录后再操作！')
    router.push('/login')
    return
  }
  router.push(`/recruitment/${route.params.id}/apply`)
}

function goBack() {
  router.back()
}

// 状态转换函数
function getStatusText(status: number) {
  switch (status) {
    case 1: return '未开始'
    case 2: return '进行中'
    case 3: return '已结束'
    default: return '未知'
  }
}

// 状态类型函数
function getStatusType(status: number) {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回上一级
      </el-button>
      <h2>{{ detail.title || '招募详情' }}</h2>
    </div>
    <!-- 基本信息 -->
    <el-descriptions :column="2" border>
      <el-descriptions-item label="招募人数">{{ detail.recruitCount }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="getStatusType(detail.status)" size="small">
          {{ getStatusText(detail.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
      <el-descriptions-item label="联系人">{{ detail.contactPerson || '-' }}</el-descriptions-item>
      <el-descriptions-item label="联系方式">{{ detail.contactInfo || '-' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 招募描述 -->
    <el-card class="detail-card" style="margin-top: 16px;">
      <template #header>
        <span class="card-title">招募描述</span>
      </template>
      <div class="description-content">
        <p v-if="detail.description" class="description-text">{{ detail.description }}</p>
        <p v-else class="no-content">暂无描述</p>
      </div>
    </el-card>

    <!-- 招募要求 -->
    <el-card class="detail-card" style="margin-top: 16px;">
      <template #header>
        <span class="card-title">招募要求</span>
      </template>
      <div class="requirements-content">
        <p v-if="detail.requirements" class="requirements-text">{{ detail.requirements }}</p>
        <p v-else class="no-content">暂无要求</p>
      </div>
    </el-card>
    <div style="margin-top:12px;">
      <el-button type="primary" @click="goApply">申请</el-button>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.page-header { margin-bottom: 16px; }
.back-button { margin-bottom: 8px; }

/* 详情卡片样式 */
.detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.description-content,
.requirements-content {
  min-height: 60px;
}

.description-text,
.requirements-text {
  margin: 0;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.no-content {
  margin: 0;
  color: #909399;
  font-style: italic;
}
</style>


