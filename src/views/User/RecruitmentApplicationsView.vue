<template>
  <div class="recruitment-applications-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h2>我的申请记录</h2>
          <p>查看您提交的招募申请状态和处理结果</p>
        </div>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <el-card class="search-card">
      <div class="search-toolbar">
        <el-input 
          v-model="searchParams.keyword" 
          placeholder="搜索招募标题" 
          style="width: 240px; margin-right: 12px;" 
          @keyup.enter="handleSearch"
          clearable
        />
        <el-select 
          v-model="searchParams.status" 
          clearable 
          placeholder="申请状态" 
          style="width: 160px; margin-right: 12px;"
        >
          <el-option :value="0" label="待审核" />
          <el-option :value="1" label="已通过" />
          <el-option :value="2" label="已拒绝" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 申请记录列表 -->
    <el-card class="applications-card">
      <el-table :data="applications" v-loading="loading" stripe>
        <el-table-column prop="recruitmentTitle" label="招募标题" min-width="200" />
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="申请状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intro" label="自我介绍" min-width="300" show-overflow-tooltip />
        <el-table-column prop="skills" label="相关技能" min-width="200" show-overflow-tooltip />
        <el-table-column prop="experience" label="相关经验" min-width="250" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="审核时间" width="180">
          <template #default="{ row }">
            {{ row.status !== 0 && row.reviewTime ? formatDate(row.reviewTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" min-width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="viewApplicationDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="招募标题">
            <div class="field-content">{{ currentApplication.recruitmentTitle }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(currentApplication.status)" size="small">
              {{ getStatusText(currentApplication.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            <div class="field-content">{{ formatDate(currentApplication.applyTime) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            <div class="field-content">{{ currentApplication.status !== 0 && currentApplication.reviewTime ? formatDate(currentApplication.reviewTime) : '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="相关技能">
            <div class="field-content skills-content">{{ currentApplication.skills || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="相关经验">
            <div class="field-content experience-content">{{ currentApplication.experience || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="自我介绍">
            <div class="field-content intro-content">{{ currentApplication.intro || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审核备注">
            <div class="field-content remark-content">{{ currentApplication.reviewRemark || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { recruitmentApi } from '@/api'
import { ElMessage } from 'element-plus'

// 类型定义
interface RecruitmentApplication {
  id: number
  recruitmentId: number
  recruitmentTitle: string
  applyTime: string
  status: number
  intro: string
  skills: string
  experience: string
  reviewTime?: string
  reviewRemark?: string
}

// 响应式数据
const loading = ref(false)
const applications = ref<RecruitmentApplication[]>([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索参数
const searchParams = ref({
  keyword: '',
  status: undefined as number | undefined
})

// 对话框相关
const detailDialogVisible = ref(false)
const currentApplication = ref<RecruitmentApplication | null>(null)

// 获取申请列表
const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await recruitmentApi.getMyApplications()
    console.log('API响应数据:', res) // 调试日志
    
    // 根据实际返回的数据结构处理
    let allApplications = []
    if (res?.records) {
      // 如果 res 直接包含 records
      allApplications = res.records
      total.value = res.total || 0
    } else if (res?.data?.records) {
      // 如果 res.data 包含 records
      allApplications = res.data.records
      total.value = res.data.total || 0
    } else if (res?.data && Array.isArray(res.data)) {
      allApplications = res.data
      total.value = res.data.length
    } else if (Array.isArray(res)) {
      allApplications = res
      total.value = res.length
    }
    
    console.log('处理后的申请数据:', allApplications) // 调试日志
    
    // 前端筛选
    if (searchParams.value.keyword || searchParams.value.status !== undefined) {
      allApplications = allApplications.filter((app: RecruitmentApplication) => {
        const keywordMatch = !searchParams.value.keyword || 
          app.recruitmentTitle.toLowerCase().includes(searchParams.value.keyword.toLowerCase())
        const statusMatch = searchParams.value.status === undefined || 
          app.status === searchParams.value.status
        return keywordMatch && statusMatch
      })
      total.value = allApplications.length
    }
    
    // 分页处理
    const startIndex = (pageNo.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    applications.value = allApplications.slice(startIndex, endIndex)
    
    console.log('最终显示的申请数据:', applications.value) // 调试日志
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pageNo.value = 1
  fetchApplications()
}

// 重置搜索
const resetSearch = () => {
  searchParams.value.keyword = ''
  searchParams.value.status = undefined
  pageNo.value = 1
  fetchApplications()
}

// 查看申请详情
const viewApplicationDetail = (application: RecruitmentApplication) => {
  currentApplication.value = application
  detailDialogVisible.value = true
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  pageNo.value = 1
  fetchApplications()
}

const handleCurrentChange = (val: number) => {
  pageNo.value = val
  fetchApplications()
}

// 状态相关
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'      // 待审核
    case 1: return 'success'   // 已通过
    case 2: return 'danger'    // 已拒绝
    default: return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.recruitment-applications-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-toolbar {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-text h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.applications-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.application-detail {
  padding: 10px 0;
}

.field-content {
  word-break: break-word;
  line-height: 1.6;
  color: #303133;
}

.skills-content,
.intro-content,
.experience-content,
.remark-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  max-width: 100%;
}

.intro-content {
  min-height: 60px;
  padding: 8px 0;
}

.experience-content {
  min-height: 50px;
  padding: 8px 0;
}

.remark-content {
  min-height: 40px;
  padding: 8px 0;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
  width: 120px;
  text-align: right;
  padding-right: 16px;
}

:deep(.el-descriptions__content) {
  color: #303133;
  padding-left: 16px;
}

:deep(.el-descriptions__body) {
  background-color: #fafafa;
}

:deep(.el-descriptions__table) {
  width: 100%;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  :deep(.el-descriptions__label) {
    width: 100px;
    font-size: 14px;
  }
  
  :deep(.el-descriptions__content) {
    font-size: 14px;
  }
}
</style>
