<template>
  <div class="certificate-review">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">证书审核管理</h1>
      <p class="page-subtitle">审核志愿者技能证书申请</p>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-bar">
      <div class="search-group">
        <el-input 
          v-model="searchForm.certificateName" 
          placeholder="搜索证书名称..." 
          clearable 
          style="width: 200px"
          @keyup.enter="load"
        />
        <el-input 
          v-model="searchForm.userName" 
          placeholder="搜索用户姓名..." 
          clearable 
          style="width: 200px"
          @keyup.enter="load"
        />
        <el-select 
          v-model="searchForm.skillCategoryId" 
          placeholder="技能分类" 
          clearable 
          style="width: 200px"
        >
          <el-option 
            v-for="category in skillCategories" 
            :key="category.id" 
            :label="category.categoryName" 
            :value="category.id" 
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
        </el-select>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 证书列表 -->
    <el-card class="certificate-list">
      <el-table 
        :data="list" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="证书信息" min-width="250">
          <template #default="{ row }">
            <div class="certificate-info">
              <div class="certificate-name">{{ row.certificateName }}</div>
              <div class="issuing-authority">{{ row.issuingAuthority }}</div>
              <div class="issue-date">{{ formatDate(row.issueDate) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-name">{{ row.userName }}</div>
              <div class="user-contact">{{ row.userPhone }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="技能分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.skillCategoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="success" 
                size="small" 
                v-if="row.status === 1"
                @click="approveCertificate(row.id)"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                v-if="row.status === 1"
                @click="rejectCertificate(row.id)"
              >
                拒绝
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="viewCertificateDetail(row.id)"
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
    <el-dialog v-model="auditDialogVisible" title="证书审核" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input 
            v-model="auditForm.auditRemark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 证书详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="证书详情" width="900px">
      <div v-if="currentCertificate" class="certificate-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3>证书信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="证书名称">{{ currentCertificate.certificateName }}</el-descriptions-item>
            <el-descriptions-item label="颁发机构">{{ currentCertificate.issuingAuthority }}</el-descriptions-item>
            <el-descriptions-item label="颁发日期">{{ formatDate(currentCertificate.issueDate) }}</el-descriptions-item>
            <el-descriptions-item label="有效期至">{{ formatDate(currentCertificate.expiryDate) }}</el-descriptions-item>
            <el-descriptions-item label="文件类型">{{ currentCertificate.fileType }}</el-descriptions-item>
            <el-descriptions-item label="文件大小">{{ formatFileSize(currentCertificate.fileSize) }}</el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ formatDateTime(currentCertificate.createdTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="getStatusType(currentCertificate.status)">
                {{ getStatusName(currentCertificate.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 用户信息 -->
        <div class="detail-section">
          <h3>用户信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户姓名">{{ currentCertificate.userName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentCertificate.userPhone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentCertificate.userEmail }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ currentCertificate.userIdCard }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 技能信息 -->
        <div class="detail-section">
          <h3>技能信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="技能分类">{{ currentCertificate.skillCategoryName }}</el-descriptions-item>
            <el-descriptions-item label="技能名称">{{ currentCertificate.skillName }}</el-descriptions-item>
            <el-descriptions-item label="技能等级">{{ currentCertificate.skillLevel }}</el-descriptions-item>
            <el-descriptions-item label="技能描述">{{ currentCertificate.skillDescription || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 审核信息 -->
        <div v-if="currentCertificate.status !== 1" class="detail-section">
          <h3>审核信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审核时间">{{ formatDateTime(currentCertificate.auditTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核人">{{ currentCertificate.auditorName }}</el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2">
              {{ currentCertificate.auditRemark || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 证书文件 -->
        <div v-if="currentCertificate.certificateFile" class="detail-section">
          <h3>证书文件</h3>
          <div class="certificate-file">
            <div class="file-info">
              <div class="file-details">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ currentCertificate.originalFileName || '未知文件' }}</span>
                <el-tag size="small" :type="getFileTypeTag(currentCertificate.fileType)">
                  {{ currentCertificate.fileType?.toUpperCase() }}
                </el-tag>
              </div>
              
              <div class="file-actions">
                <el-button type="primary" size="small" @click="downloadCertificate">
                  <el-icon><Download /></el-icon>
                  下载证书
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 证书预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="证书预览" width="80%" top="5vh">
      <CertificatePreview 
        v-if="previewFileUrl"
        :file-url="previewFileUrl"
        :file-type="previewFileType"
        :original-name="previewOriginalName"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Download } from '@element-plus/icons-vue'
import request from '@/utils/request'
import CertificatePreview from '@/components/CertificatePreview.vue'

// 响应式数据
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)
const selectedIds = ref([])

// 搜索表单
const searchForm = reactive({
  certificateName: '',
  userName: '',
  skillCategoryId: null,
  status: null
})

// 技能分类列表
const skillCategories = ref([])

// 对话框状态
const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const previewDialogVisible = ref(false)

// 当前操作的证书
const currentCertificate = ref(null)
const currentAuditId = ref(null)

// 审核表单
const auditForm = reactive({
  status: 2,
  auditRemark: ''
})

// 预览相关
const previewFileUrl = ref('')
const previewFileType = ref('')
const previewOriginalName = ref('')

// 页面加载
onMounted(() => {
  load()
  loadSkillCategories()
})

// 加载证书列表
const load = async () => {
  try {
    loading.value = true
    
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      certificateName: searchForm.certificateName,
      userName: searchForm.userName,
      skillCategoryId: searchForm.skillCategoryId,
      status: searchForm.status
    }

    const response = await request.get('/v1/skills/certificate/pending', { params })
    
    if (response.code === 200) {
      // 处理分页数据格式
      if (response.data && response.data.records) {
        list.value = response.data.records || []
        total.value = response.data.total || 0
      } else if (Array.isArray(response.data)) {
        // 处理直接数组格式
        list.value = response.data || []
        total.value = response.data.length || 0
      } else {
        list.value = []
        total.value = 0
      }
    } else {
      ElMessage.error(response.message || '加载证书列表失败')
    }
  } catch (error) {
    console.error('加载证书列表失败:', error)
    ElMessage.error('加载证书列表失败')
  } finally {
    loading.value = false
  }
}

// 加载技能分类
const loadSkillCategories = async () => {
  try {
    const response = await request.get('/v1/skills/categories')
    if (response.code === 200) {
      skillCategories.value = response.data || []
    }
  } catch (error) {
    console.error('加载技能分类失败:', error)
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.certificateName = ''
  searchForm.userName = ''
  searchForm.skillCategoryId = null
  searchForm.status = null
  pageNo.value = 1
  load()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 审核证书
const approveCertificate = (id: number) => {
  currentAuditId.value = id
  auditForm.status = 2
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

// 拒绝证书
const rejectCertificate = (id: number) => {
  currentAuditId.value = id
  auditForm.status = 3
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

// 确认审核
const confirmAudit = async () => {
  try {
    const response = await request.put(`/v1/skills/certificate/${currentAuditId.value}/audit`, auditForm)
    
    if (response.code === 200) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      load()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 批量通过
const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确定要通过选中的 ${selectedIds.value.length} 个证书吗？`, '批量通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const promises = selectedIds.value.map(id => 
      request.put(`/v1/skills/certificate/${id}/audit`, {
        status: 2,
        auditRemark: '批量审核通过'
      })
    )

    await Promise.all(promises)
    ElMessage.success('批量审核完成')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

// 批量拒绝
const batchReject = async () => {
  try {
    await ElMessageBox.confirm(`确定要拒绝选中的 ${selectedIds.value.length} 个证书吗？`, '批量拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const promises = selectedIds.value.map(id => 
      request.put(`/v1/skills/certificate/${id}/audit`, {
        status: 3,
        auditRemark: '批量审核拒绝'
      })
    )

    await Promise.all(promises)
    ElMessage.success('批量审核完成')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

// 查看证书详情
const viewCertificateDetail = async (id: number) => {
  try {
    const response = await request.get(`/v1/skills/certificate/${id}`)
    
    if (response.code === 200) {
      currentCertificate.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取证书详情失败')
    }
  } catch (error) {
    console.error('获取证书详情失败:', error)
    ElMessage.error('获取证书详情失败')
  }
}

// 预览证书
const previewCertificate = (row: any) => {
  previewFileUrl.value = row.certificateFile
  previewFileType.value = row.fileType
  previewOriginalName.value = row.originalFileName
  previewDialogVisible.value = true
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const statusMap = {
    1: '待审核',
    2: '已通过',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}

// 下载证书
const downloadCertificate = () => {
  if (currentCertificate.value?.certificateFile) {
    const link = document.createElement('a')
    link.href = currentCertificate.value.certificateFile
    link.download = currentCertificate.value.originalFileName || 'certificate'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

// 获取文件类型标签
const getFileTypeTag = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'pdf': 'danger',
    'jpg': 'success',
    'jpeg': 'success', 
    'png': 'success',
    'gif': 'success',
    'bmp': 'success',
    'webp': 'success'
  }
  return typeMap[type?.toLowerCase()] || 'info'
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  let fileSize = size
  
  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024
    unitIndex++
  }
  
  return `${fileSize.toFixed(1)} ${units[unitIndex]}`
}
</script>

<style scoped>
.certificate-review {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
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

.certificate-list {
  margin-bottom: 20px;
}

.certificate-info {
  line-height: 1.5;
}

.certificate-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.issuing-authority {
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
}

.issue-date {
  font-size: 12px;
  color: #909399;
}

.user-info {
  line-height: 1.5;
}

.user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-contact {
  font-size: 12px;
  color: #606266;
}

.no-file {
  color: #909399;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #EBEEF5;
}

.certificate-file {
  margin-top: 12px;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.file-details {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-details span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.file-actions {
  display: flex;
  gap: 8px;
}
</style>
