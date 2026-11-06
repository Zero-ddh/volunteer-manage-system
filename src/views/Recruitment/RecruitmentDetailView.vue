<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recruitmentApi } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<any>({})

// 状态映射
const statusMap = {
  1: { text: '草稿', type: 'info' },
  2: { text: '发布中', type: 'success' },
  3: { text: '已结束', type: 'warning' }
}

async function fetchDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('招募ID不能为空')
    router.back()
    return
  }

  loading.value = true
  try {
    const res = await recruitmentApi.getDetail(Number(id))
    if (res && res.data) {
      detail.value = res.data
    } else {
      ElMessage.error('获取招募详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取招募详情失败:', error)
    ElMessage.error('获取招募详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

function goEdit() {
  router.push(`/recruitment/create?id=${detail.value.id}`)
}

onMounted(fetchDetail)
</script>

<template>
  <div class="recruitment-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" circle>
          <el-icon><arrow-left /></el-icon>
        </el-button>
        <div class="title-info">
          <h1 class="page-title">招募详情</h1>
          <p class="page-subtitle">查看招募信息详情</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="goEdit">编辑招募</el-button>
      </div>
    </div>

    <div v-loading="loading" class="detail-content">
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">基本信息</span>
            <el-tag :type="statusMap[detail.status as keyof typeof statusMap]?.type">
              {{ statusMap[detail.status as keyof typeof statusMap]?.text }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="招募标题" :span="2">
            <span class="title-text">{{ detail.title }}</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="招募人数">
            <el-tag type="primary">{{ detail.recruitCount }} 人</el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="招募状态">
            <el-tag :type="statusMap[detail.status as keyof typeof statusMap]?.type">
              {{ statusMap[detail.status as keyof typeof statusMap]?.text }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="开始时间">
            {{ detail.startTime }}
          </el-descriptions-item>
          
          <el-descriptions-item label="结束时间">
            {{ detail.endTime }}
          </el-descriptions-item>
          
          <el-descriptions-item label="联系人">
            {{ detail.contactPerson || '未设置' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="联系方式">
            {{ detail.contactInfo || '未设置' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="创建时间">
            {{ detail.createdTime }}
          </el-descriptions-item>
          
          <el-descriptions-item label="更新时间">
            {{ detail.updatedTime }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="detail-card" style="margin-top: 16px;">
        <template #header>
          <span class="card-title">招募描述</span>
        </template>
        <div class="description-content">
          <p v-if="detail.description" class="description-text">{{ detail.description }}</p>
          <p v-else class="no-content">暂无描述</p>
        </div>
      </el-card>

      <el-card class="detail-card" style="margin-top: 16px;">
        <template #header>
          <span class="card-title">招募要求</span>
        </template>
        <div class="requirements-content">
          <p v-if="detail.requirements" class="requirements-text">{{ detail.requirements }}</p>
          <p v-else class="no-content">暂无要求</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.recruitment-detail {
  padding: 16px;
}

/* 页面标题样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-info {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 详情内容样式 */
.detail-content {
  max-width: 1000px;
}

.detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.description-content,
.requirements-content {
  min-height: 60px;
}

.description-text,
.requirements-text {
  line-height: 1.6;
  color: #606266;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.no-content {
  color: #c0c4cc;
  font-style: italic;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-end;
  }
  
  .detail-content {
    max-width: 100%;
  }
}
</style>
