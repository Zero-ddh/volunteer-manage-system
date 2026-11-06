<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recruitmentApi } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const applicants = ref<any[]>([])
const recruitmentId = ref<number>(Number(route.params.id))

// 分页相关
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索参数
const searchParams = ref({
  name: '',
  phone: '',
  status: undefined as number | undefined
})

// 状态映射
const statusMap = {
  1: { text: '待审核', type: 'warning' },
  2: { text: '已通过', type: 'success' },
  3: { text: '已拒绝', type: 'danger' }
}

async function fetchApplicants() {
  loading.value = true
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...(searchParams.value.name && { name: searchParams.value.name }),
      ...(searchParams.value.phone && { phone: searchParams.value.phone }),
      ...(searchParams.value.status !== undefined && { status: searchParams.value.status })
    }
    
    const res = await recruitmentApi.getApplicants(recruitmentId.value, params)
    // 修复数据格式问题：后端返回的是 {code, message, data: {records: [], total: 1}}
    if (res && res.data) {
      applicants.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      applicants.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取报名者列表失败:', error)
    ElMessage.error('获取报名者列表失败')
    applicants.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/recruitment')
}

// 搜索处理
function handleSearch() {
  pageNo.value = 1
  fetchApplicants()
}

// 重置搜索
function resetSearch() {
  searchParams.value.name = ''
  searchParams.value.phone = ''
  searchParams.value.status = undefined
  pageNo.value = 1
  fetchApplicants()
}

// 分页处理
function handleSizeChange(val: number) {
  pageSize.value = val
  pageNo.value = 1
  fetchApplicants()
}

function handleCurrentChange(val: number) {
  pageNo.value = val
  fetchApplicants()
}

// 审核相关
const reviewDialogVisible = ref(false)
const reviewForm = ref({
  admissionId: 0,
  status: 1,
  remark: ''
})
const reviewFormRef = ref()

// 查看详情相关
const detailDialogVisible = ref(false)
const currentApplicant = ref<any>(null)

// 审核表单验证规则
const reviewRules = {
  remark: [
    { required: true, message: '请输入审核备注', trigger: 'blur' },
    { min: 2, max: 500, message: '审核备注长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

// 打开审核弹窗
function openReviewDialog(row: any, status: number) {
  reviewForm.value = {
    admissionId: row.id,
    status: status,
    remark: ''
  }
  reviewDialogVisible.value = true
}

// 提交审核
async function submitReview() {
  if (!reviewFormRef.value) return
  
  try {
    await reviewFormRef.value.validate()
    
    const data = {
      status: reviewForm.value.status,
      remark: reviewForm.value.remark
    }
    
    await recruitmentApi.reviewApplication(reviewForm.value.admissionId, data)
    
    ElMessage.success('审核成功')
    reviewDialogVisible.value = false
    fetchApplicants() // 重新获取数据
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 取消审核
function cancelReview() {
  reviewDialogVisible.value = false
  reviewForm.value = {
    admissionId: 0,
    status: 1,
    remark: ''
  }
}

// 查看报名者详情
function viewApplicantDetail(row: any) {
  currentApplicant.value = row
  detailDialogVisible.value = true
}

// 关闭详情弹窗
function closeDetailDialog() {
  detailDialogVisible.value = false
  currentApplicant.value = null
}

onMounted(() => {
  fetchApplicants()
})
</script>

<template>
  <div class="applicants-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">报名者管理</h1>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <el-card class="search-card">
      <div class="search-toolbar">
        <el-input 
          v-model="searchParams.name" 
          placeholder="搜索姓名" 
          style="width: 200px; margin-right: 12px;" 
          @keyup.enter="handleSearch"
          clearable
        />
        <el-input 
          v-model="searchParams.phone" 
          placeholder="搜索手机号" 
          style="width: 200px; margin-right: 12px;" 
          @keyup.enter="handleSearch"
          clearable
        />
        <el-select 
          v-model="searchParams.status" 
          clearable 
          placeholder="申请状态" 
          style="width: 160px; margin-right: 12px;"
        >
          <el-option :value="1" label="待审核" />
          <el-option :value="2" label="已通过" />
          <el-option :value="3" label="已拒绝" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 报名者列表 -->
    <el-card>
      <el-table :data="applicants" v-loading="loading" stripe>
        <el-table-column prop="userName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="skills" label="技能" width="200" show-overflow-tooltip />
        <el-table-column prop="experience" label="相关经验" width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="个人介绍" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status as keyof typeof statusMap]?.type">
              {{ statusMap[row.status as keyof typeof statusMap]?.text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="申请时间" width="180" />
        <el-table-column prop="reviewTime" label="审核时间" width="180">
          <template #default="{ row }">
            {{ row.reviewTime ? new Date(row.reviewTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewApplicantDetail(row)"
            >
              查看
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="success" 
              size="small" 
              @click="openReviewDialog(row, 2)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="danger" 
              size="small" 
              @click="openReviewDialog(row, 3)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              type="warning" 
              size="small" 
              @click="openReviewDialog(row, 1)"
            >
              重新审核
            </el-button>
            <el-button 
              v-if="row.status === 3" 
              type="success" 
              size="small" 
              @click="openReviewDialog(row, 1)"
            >
              重新审核
            </el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewForm.status === 2 ? '审核通过' : reviewForm.status === 3 ? '审核拒绝' : '重新审核'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-form-item label="审核结果">
          <el-tag :type="reviewForm.status === 2 ? 'success' : reviewForm.status === 3 ? 'danger' : 'warning'">
            {{ reviewForm.status === 2 ? '通过' : reviewForm.status === 3 ? '拒绝' : '重新审核' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注" prop="remark">
          <el-input
            v-model="reviewForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelReview">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitReview"
            :loading="false"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报名者详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="报名者详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentApplicant" class="applicant-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            <div class="field-content">{{ currentApplicant.userName || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            <div class="field-content">{{ currentApplicant.phone || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            <div class="field-content">{{ currentApplicant.email || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="statusMap[currentApplicant.status as keyof typeof statusMap]?.type">
              {{ statusMap[currentApplicant.status as keyof typeof statusMap]?.text }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            <div class="field-content">{{ currentApplicant.createdTime ? new Date(currentApplicant.createdTime).toLocaleString('zh-CN') : '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            <div class="field-content">{{ currentApplicant.reviewTime ? new Date(currentApplicant.reviewTime).toLocaleString('zh-CN') : '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="相关技能" :span="2">
            <div class="field-content skills-content">{{ currentApplicant.skills || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="相关经验" :span="2">
            <div class="field-content experience-content">{{ currentApplicant.experience || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="个人介绍" :span="2">
            <div class="field-content intro-content">{{ currentApplicant.remark || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">
            <div class="field-content remark-content">{{ currentApplicant.reviewRemark || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDetailDialog">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.applicants-list {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 16px;
}

.search-toolbar {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-dialog__body) {
  padding: 20px 20px 10px 20px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
}

.applicant-detail {
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
</style>