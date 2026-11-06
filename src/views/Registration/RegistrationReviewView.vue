<template>
  <div class="registration-review">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">报名审核管理</h1>
      <p class="page-subtitle">审核志愿者活动报名申请</p>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-bar">
      <div class="search-group">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索报名者姓名、手机号码、活动标题..." 
          clearable 
          style="width: 300px"
          @keyup.enter="load"
        />
        <el-select 
          v-model="searchForm.activityId" 
          placeholder="选择活动" 
          clearable 
          filterable
          style="width: 200px"
        >
          <el-option 
            v-for="activity in activityList" 
            :key="activity.id" 
            :label="activity.title" 
            :value="activity.id" 
          />
        </el-select>
        <el-select 
          v-model="searchForm.status" 
          placeholder="审核状态" 
          clearable 
          style="width: 140px"
        >
          <el-option label="待审核" :value="1" />
          <el-option label="已通过" :value="2" />
          <el-option label="已拒绝" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 报名列表 -->
    <el-card class="registration-list">
      <el-table 
        :data="list" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="活动标题" min-width="200">
          <template #default="{ row }">
            <div class="activity-title">{{ getActivityTitle(row.activityId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.activityCategoryId)" size="small">
              {{ getCategoryName(row.activityCategoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名者" width="120">
          <template #default="{ row }">
            <div class="user-name">{{ row.realName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="手机号码" width="130">
          <template #default="{ row }">
            <div class="user-phone">{{ row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="registrationTime" label="报名时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationTime || '') }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="success" 
                size="small" 
                v-if="row.status === 1"
                @click="approveRegistration(row.id)"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                v-if="row.status === 1"
                @click="rejectRegistration(row.id)"
              >
                拒绝
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="viewRegistrationDetail(row.id)"
              >
                详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="审核报名" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input 
            v-model="reviewForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReview">确认</el-button>
      </template>
    </el-dialog>

    <!-- 报名详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="报名详情" width="900px">
      <div v-if="currentRegistration" class="registration-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="报名者姓名">{{ currentRegistration.realName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentRegistration.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentRegistration.email }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ currentRegistration.idCard }}</el-descriptions-item>
            <el-descriptions-item label="报名时间">{{ formatDateTime(currentRegistration.registrationTime || '') }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="getStatusType(currentRegistration.status)">
                {{ getStatusName(currentRegistration.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 活动信息 -->
        <div class="detail-section">
          <h3>活动信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="活动标题">{{ currentRegistration.activityTitle }}</el-descriptions-item>
            <el-descriptions-item label="活动地点">{{ currentRegistration.activityLocation }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(currentRegistration.activityStartTime || '') }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(currentRegistration.activityEndTime || '') }}</el-descriptions-item>
            <el-descriptions-item label="活动描述" :span="2">
              {{ currentRegistration.activityDescription || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 技能匹配度 - 已屏蔽 -->
        <!-- <div v-if="currentRegistration.skillMatchScore !== undefined" class="detail-section">
          <h3>技能匹配度</h3>
          <div class="skill-match-score">
            <el-progress 
              :percentage="Math.round(currentRegistration.skillMatchScore)" 
              :color="getMatchScoreColor(currentRegistration.skillMatchScore)"
              :stroke-width="20"
            />
            <p class="score-text">
              匹配度：{{ Math.round(currentRegistration.skillMatchScore) }}%
              <el-tag :type="getMatchScoreType(currentRegistration.skillMatchScore)" size="small">
                {{ getMatchScoreText(currentRegistration.skillMatchScore) }}
              </el-tag>
            </p>
          </div>
        </div> -->

        <!-- 用户技能信息 -->
        <div v-if="currentRegistration.userSkills && currentRegistration.userSkills.length > 0" class="detail-section">
          <h3>用户技能信息</h3>
          <el-table :data="currentRegistration.userSkills" size="small" border>
            <el-table-column prop="skillName" label="技能名称" width="120" />
            <el-table-column prop="skillCategoryName" label="技能分类" width="100" />
            <el-table-column prop="skillLevelText" label="技能等级" width="80">
              <template #default="{ row }">
                <el-tag :type="getSkillLevelType(row.skillLevel)" size="small">
                  {{ row.skillLevelText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="verifiedText" label="验证状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.verified === 1 ? 'success' : 'warning'" size="small">
                  {{ row.verifiedText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="certificateName" label="关联证书" />
            <el-table-column prop="description" label="技能描述" />
          </el-table>
        </div>

        <!-- 用户证书信息 -->
        <div v-if="getFilteredCertificates().length > 0" class="detail-section">
          <h3>用户证书信息</h3>
          <el-table :data="getFilteredCertificates()" size="small" border>
            <el-table-column prop="certificateName" label="证书名称" width="150" />
            <el-table-column prop="issuingAuthority" label="颁发机构" width="120" />
            <el-table-column prop="certificateNumber" label="证书编号" width="120" />
            <el-table-column prop="issueDate" label="颁发日期" width="100" />
            <el-table-column prop="expiryDate" label="有效期至" width="100" />
            <el-table-column prop="statusText" label="审核状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getCertificateStatusType(row.status)" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  v-if="row.certificateFile" 
                  type="primary" 
                  size="small" 
                  @click="previewCertificate(row)"
                >
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 活动要求 -->
        <div v-if="currentRegistration.activityRequirements && currentRegistration.activityRequirements.trim()" class="detail-section">
          <h3>活动要求</h3>
          <div class="requirements-content">
            <pre>{{ currentRegistration.activityRequirements }}</pre>
          </div>
        </div>

        <!-- 报名备注 -->
        <div class="detail-section">
          <h3>报名备注</h3>
          <p>{{ currentRegistration.remark || '无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { registrationApi, activityApi, activityCategoryApi } from '@/api'

// 类型定义
interface RegistrationVO {
  id: number
  activityId: number
  userId: number
  status: number
  registrationTime: string
  remark?: string
  realName?: string
  phone?: string
  email?: string
  idCard?: string
  activityTitle?: string
  activityDescription?: string
  activityStartTime?: string
  activityEndTime?: string
  activityLocation?: string
  activityCategoryId?: number
  activityCategoryName?: string
  activityRequirements?: string
  skillMatchScore?: number
  userSkills?: UserSkillInfo[]
  userCertificates?: UserCertificateInfo[]
  activitySkillRequirements?: ActivitySkillRequirementInfo[]
}

interface Activity {
  id: number
  title: string
  description?: string
  startTime?: string
  endTime?: string
  location?: string
}

interface UserCertificate {
  certificateName: string
  certificateFile?: string
  fileType?: string
}

interface UserSkillInfo {
  skillName: string
  skillCategoryName?: string
  skillLevel: number
  skillLevelText: string
  verified: number
  verifiedText: string
  certificateName?: string
  description?: string
}

interface UserCertificateInfo {
  certificateName: string
  issuingAuthority: string
  certificateNumber: string
  issueDate: string
  expiryDate: string
  status: number
  statusText: string
  certificateFile?: string
  fileType?: string
}

interface ActivitySkillRequirementInfo {
  skillName: string
  skillCategoryName?: string
  requiredLevel: number
  requiredLevelText: string
  isRequired: number
  isRequiredText: string
  description?: string
}

// 响应式数据
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<RegistrationVO[]>([])
const activityList = ref<Activity[]>([])
const selectedIds = ref<number[]>([])
const reviewDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRegistration = ref<RegistrationVO | null>(null)
const currentReviewId = ref<number | null>(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  activityId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.activityId = undefined
  searchForm.status = undefined
  pageNo.value = 1
  load()
}

// 审核表单
const reviewForm = reactive({
  status: 2,
  remark: ''
})

// 状态映射
const statusMap = {
  1: { name: '待审核', type: 'warning' },
  2: { name: '已通过', type: 'success' },
  3: { name: '已拒绝', type: 'danger' },
  4: { name: '已取消', type: 'info' }
}

// 获取状态名称
const getStatusName = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.name || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.type || 'info'
}

// 获取活动标题
const getActivityTitle = (activityId: number) => {
  const activity = activityList.value.find(a => a.id === activityId)
  return activity?.title || `活动${activityId}`
}

// 分类映射
const categoryMap = ref<Record<number, { name: string; type: string }>>({})

// 加载分类映射
const loadCategoryMap = async () => {
  try {
    const res = await activityCategoryApi.getEnabledCategories()
    const categories = res.data || []
    const map: Record<number, { name: string; type: string }> = {}
    const typeColors = ['success', 'primary', 'warning', 'danger', 'info'] as const
    categories.forEach((category: { id: number; categoryName: string }, index: number) => {
      if (category.id && category.categoryName) {
        map[category.id] = {
          name: category.categoryName,
          type: typeColors[index % typeColors.length] as string
        }
      }
    })
    categoryMap.value = map
  } catch (error) {
    console.error('加载分类数据失败:', error)
  }
}

// 获取分类名称
const getCategoryName = (categoryId: number | undefined) => {
  if (!categoryId) return '未分类'
  return categoryMap.value[categoryId]?.name || '未知'
}

// 获取分类类型
const getCategoryType = (categoryId: number | undefined) => {
  if (!categoryId) return 'info'
  return categoryMap.value[categoryId]?.type || 'info'
}

// 格式化日期时间
const formatDateTime = (date: string | undefined) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 加载数据
const load = async () => {
  try {
    loading.value = true
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await registrationApi.getRegistrationList(params)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载活动列表
const loadActivityList = async () => {
  try {
    const res = await activityApi.getActivityList({ pageNo: 1, pageSize: 1000 })
    activityList.value = res.data?.records || res.data?.list || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 选择变化
const handleSelectionChange = (selection: RegistrationVO[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 审核报名
const approveRegistration = (id: number) => {
  currentReviewId.value = id
  reviewForm.status = 2
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

// 拒绝报名
const rejectRegistration = (id: number) => {
  currentReviewId.value = id
  reviewForm.status = 3
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

// 确认审核
const confirmReview = async () => {
  if (!currentReviewId.value) return
  
  try {
    await registrationApi.reviewRegistration(currentReviewId.value, {
      status: reviewForm.status,
      remark: reviewForm.remark
    })
    ElMessage.success('审核成功')
    reviewDialogVisible.value = false
    await load()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 查看报名详情
const viewRegistrationDetail = async (id: number) => {
  try {
    const res = await registrationApi.getRegistrationDetail(id)
    currentRegistration.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取报名详情失败:', error)
    ElMessage.error('获取报名详情失败')
  }
}

// 技能匹配度相关函数 - 已屏蔽
// const getMatchScoreColor = (score: number) => {
//   if (score >= 80) return '#67c23a'
//   if (score >= 60) return '#e6a23c'
//   if (score >= 40) return '#f56c6c'
//   return '#909399'
// }

// const getMatchScoreType = (score: number) => {
//   if (score >= 80) return 'success'
//   if (score >= 60) return 'warning'
//   if (score >= 40) return 'danger'
//   return 'info'
// }

// const getMatchScoreText = (score: number) => {
//   if (score >= 80) return '优秀'
//   if (score >= 60) return '良好'
//   if (score >= 40) return '一般'
//   return '较差'
// }

// 获取技能等级类型
const getSkillLevelType = (level: number) => {
  switch (level) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

// 获取证书状态类型
const getCertificateStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 预览证书
const previewCertificate = (certificate: UserCertificate) => {
  if (!certificate.certificateFile) {
    ElMessage.warning('证书文件不存在')
    return
  }
  
  // 构建证书文件URL - 修复URL路径问题
  let fileUrl = certificate.certificateFile
  if (!fileUrl.startsWith('http') && !fileUrl.startsWith('/')) {
    fileUrl = `/api/uploads/certificates/${fileUrl}`
  }
  
  // 根据文件类型决定操作方式
  if (certificate.fileType === 'pdf') {
    // PDF文件直接下载
    downloadFile(fileUrl, certificate.certificateName || 'certificate.pdf')
  } else if (certificate.fileType && ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(certificate.fileType.toLowerCase())) {
    // 图片文件直接下载
    downloadFile(fileUrl, certificate.certificateName || 'certificate.jpg')
  } else {
    ElMessage.warning('不支持的文件类型')
  }
}

// 下载文件
const downloadFile = (fileUrl: string, filename: string) => {
  try {
    // 创建下载链接
    const link = document.createElement('a')
    link.href = fileUrl
    link.download = filename
    link.target = '_blank'
    
    // 添加到页面并触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    
    ElMessage.success('开始下载证书文件')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请检查文件是否存在')
  }
}

// 获取过滤后的证书信息（排除"官方志愿服务平台"）
const getFilteredCertificates = () => {
  if (!currentRegistration.value || !currentRegistration.value.userCertificates) {
    return []
  }
  return currentRegistration.value.userCertificates.filter(
    cert => cert.issuingAuthority !== '官方志愿服务平台'
  )
}

// 页面加载时获取数据
onMounted(() => {
  loadCategoryMap()
  loadActivityList()
  load()
})
</script>

<style scoped>
.registration-review {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.registration-list {
  margin-bottom: 20px;
}

.activity-info {
  line-height: 1.4;
}

.activity-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #666;
}

.user-name {
  font-weight: bold;
  color: #333;
}

.user-phone {
  font-size: 12px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

/* 报名详情样式 */
.registration-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4e7ed;
}

.detail-section h4 {
  font-size: 14px;
  font-weight: bold;
  color: #666;
  margin-bottom: 8px;
}

.detail-section p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* 技能匹配度样式 */
.skill-match-score {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.score-text {
  margin-top: 12px;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 活动要求样式 */
.requirements-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.requirements-content pre {
  margin: 0;
  padding: 0;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 表格样式优化 */
.detail-section .el-table {
  margin-top: 8px;
}

.detail-section .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .registration-detail {
    max-height: 60vh;
  }
  
  .detail-section {
    margin-bottom: 16px;
  }
  
  .detail-section h3 {
    font-size: 14px;
    margin-bottom: 8px;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.registration-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 10px;
  color: #333;
}

.detail-section p {
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .search-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-group .el-input,
  .search-group .el-select {
    width: 100% !important;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>
