<template>
  <div class="service-case-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h2>典型案例管理</h2>
          <p>记录和分享您的志愿服务典型案例，传播正能量</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openAddCaseDialog">
            <el-icon><Plus /></el-icon>
            添加案例
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">📝</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCases }}</div>
              <div class="stat-label">总案例数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">⭐</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.featuredCases }}</div>
              <div class="stat-label">精选案例</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">👀</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalViews }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">👍</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalLikes }}</div>
              <div class="stat-label">总点赞数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="案例类型">
          <el-select v-model="filterForm.caseType" placeholder="选择类型" clearable>
            <el-option label="感人故事" :value="1" />
            <el-option label="技能应用" :value="2" />
            <el-option label="团队协作" :value="3" />
            <el-option label="成长感悟" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="搜索标题或内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCases">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 案例列表 -->
    <el-card class="case-list-card">
      <div v-if="cases.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无典型案例" />
      </div>
      
      <div v-else class="case-grid">
        <div v-for="case in cases" :key="case.id" class="case-item" @click="viewCaseDetail(case)">
          <div class="case-image">
            <el-image
              v-if="getCaseImage(case.photos)"
              :src="getCaseImage(case.photos)"
              fit="cover"
              style="width: 100%; height: 200px;"
            />
            <div v-else class="no-image">
              <el-icon size="48"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </div>
          
          <div class="case-content">
            <div class="case-header">
              <h3 class="case-title">{{ case.title }}</h3>
              <el-tag :type="getCaseTypeTag(case.caseType)" size="small">
                {{ getCaseTypeText(case.caseType) }}
              </el-tag>
            </div>
            
            <p class="case-summary">{{ getCaseSummary(case.content) }}</p>
            
            <div class="case-meta">
              <div class="case-stats">
                <span><el-icon><View /></el-icon> {{ case.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ case.likeCount }}</span>
              </div>
              <div class="case-date">{{ formatDate(case.createdTime) }}</div>
            </div>
            
            <div class="case-actions">
              <el-button size="small" @click.stop="editCase(case)">编辑</el-button>
              <el-button size="small" type="primary" @click.stop="viewCaseDetail(case)">查看</el-button>
              <el-button size="small" type="danger" @click.stop="deleteCase(case)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadCases"
        @current-change="loadCases"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <!-- 添加/编辑案例对话框 -->
    <el-dialog 
      v-model="caseDialogVisible" 
      :title="isEdit ? '编辑典型案例' : '添加典型案例'"
      width="900px"
    >
      <el-form :model="caseForm" :rules="caseRules" ref="caseFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="案例标题" prop="title">
              <el-input v-model="caseForm.title" placeholder="请输入案例标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="案例类型" prop="caseType">
              <el-select v-model="caseForm.caseType" placeholder="选择类型" style="width: 100%">
                <el-option label="感人故事" :value="1" />
                <el-option label="技能应用" :value="2" />
                <el-option label="团队协作" :value="3" />
                <el-option label="成长感悟" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="关联活动" prop="activityId">
          <el-select v-model="caseForm.activityId" placeholder="选择活动" style="width: 100%">
            <el-option 
              v-for="activity in activities" 
              :key="activity.id" 
              :label="activity.title" 
              :value="activity.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="案例标签" prop="tags">
          <el-input v-model="caseForm.tags" placeholder="请输入标签，用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="案例内容" prop="content">
          <el-input 
            v-model="caseForm.content" 
            type="textarea" 
            :rows="8"
            placeholder="请详细描述您的典型案例..."
          />
        </el-form-item>
        
        <el-form-item label="案例照片">
          <el-upload
            v-model:file-list="photoFileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handlePhotoSuccess"
            :on-error="handlePhotoError"
            :before-upload="beforePhotoUpload"
            list-type="picture-card"
            :limit="6"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="caseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCase" :loading="saving">
          {{ isEdit ? '更新' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 案例详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="案例详情"
      width="1000px"
    >
      <div v-if="currentCase" class="case-detail">
        <div class="detail-header">
          <h2>{{ currentCase.title }}</h2>
          <div class="detail-meta">
            <el-tag :type="getCaseTypeTag(currentCase.caseType)">
              {{ getCaseTypeText(currentCase.caseType) }}
            </el-tag>
            <span class="detail-date">{{ formatDate(currentCase.createdTime) }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="detail-text">
            <p>{{ currentCase.content }}</p>
          </div>
          
          <div v-if="getCaseImages(currentCase.photos).length > 0" class="detail-images">
            <h3>案例照片</h3>
            <div class="image-gallery">
              <el-image
                v-for="(image, index) in getCaseImages(currentCase.photos)"
                :key="index"
                :src="image"
                :preview-src-list="getCaseImages(currentCase.photos)"
                fit="cover"
                style="width: 200px; height: 150px; margin-right: 10px; margin-bottom: 10px;"
              />
            </div>
          </div>
        </div>
        
        <div class="detail-footer">
          <div class="detail-stats">
            <span><el-icon><View /></el-icon> {{ currentCase.viewCount }} 次浏览</span>
            <span><el-icon><Star /></el-icon> {{ currentCase.likeCount }} 个赞</span>
          </div>
          <div class="detail-actions">
            <el-button @click="likeCase(currentCase)">
              <el-icon><Star /></el-icon>
              点赞
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, View, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const cases = ref<any[]>([])
const stats = ref({
  totalCases: 0,
  featuredCases: 0,
  totalViews: 0,
  totalLikes: 0
})

// 分页数据
const pageNo = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 筛选表单
const filterForm = ref({
  caseType: null,
  keyword: ''
})

// 对话框相关
const caseDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

// 表单数据
const caseForm = ref({
  id: 0,
  activityId: null,
  serviceRecordId: null,
  title: '',
  content: '',
  caseType: 1,
  tags: ''
})

const caseFormRef = ref()

// 活动列表
const activities = ref<any[]>([])

// 当前案例
const currentCase = ref<any>(null)

// 照片上传
const photoFileList = ref<any[]>([])
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/v1/upload/image`)
const uploadHeaders = computed(() => {
  const authStore = useAuthStore()
  return {
    'Authorization': `Bearer ${authStore.token}`
  }
})

// 表单验证规则
const caseRules = {
  title: [{ required: true, message: '请输入案例标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入案例内容', trigger: 'blur' }],
  caseType: [{ required: true, message: '请选择案例类型', trigger: 'change' }]
}

// 加载数据
const load = async () => {
  await Promise.all([
    loadCases(),
    loadCaseStats(),
    loadActivities()
  ])
}

// 加载案例列表
const loadCases = async () => {
  loading.value = true
  try {
    const response = await request.get('/v1/service-case/user/list', {
      params: {
        pageNo: pageNo.value,
        pageSize: pageSize.value,
        caseType: filterForm.value.caseType,
        keyword: filterForm.value.keyword
      }
    })
    cases.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    console.error('加载案例列表失败:', error)
    ElMessage.error('加载案例列表失败')
  } finally {
    loading.value = false
  }
}

// 加载案例统计
const loadCaseStats = async () => {
  try {
    const response = await request.get('/v1/service-case/user/stats')
    stats.value = response || { totalCases: 0, featuredCases: 0, totalViews: 0, totalLikes: 0 }
  } catch (error) {
    console.error('加载案例统计失败:', error)
  }
}

// 加载活动列表
const loadActivities = async () => {
  try {
    const response = await request.get('/v1/activities/list')
    activities.value = response.records || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 打开添加案例对话框
const openAddCaseDialog = async () => {
  isEdit.value = false
  caseForm.value = {
    id: 0,
    activityId: null,
    serviceRecordId: null,
    title: '',
    content: '',
    caseType: 1,
    tags: ''
  }
  photoFileList.value = []
  await loadActivities()
  caseDialogVisible.value = true
}

// 编辑案例
const editCase = async (caseItem: any) => {
  isEdit.value = true
  caseForm.value = { ...caseItem }
  photoFileList.value = getCaseImages(caseItem.photos)
    .filter((url: string) => !url.startsWith('blob:')) // 过滤掉blob URL
    .map((url: string) => ({
      url,
      name: url.split('/').pop()
    }))
  await loadActivities()
  caseDialogVisible.value = true
}

// 查看案例详情
const viewCaseDetail = async (caseItem: any) => {
  try {
    const response = await request.get(`/v1/service-case/${caseItem.id}`)
    currentCase.value = response
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取案例详情失败:', error)
    ElMessage.error('获取案例详情失败')
  }
}

// 删除案例
const deleteCase = async (caseItem: any) => {
  try {
    await ElMessageBox.confirm('确认删除此案例吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/v1/service-case/${caseItem.id}`)
    ElMessage.success('案例删除成功')
    await loadCases()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除案例失败:', error)
      ElMessage.error('删除案例失败')
    }
  }
}

// 点赞案例
const likeCase = async (caseItem: any) => {
  try {
    await request.post(`/v1/service-case/${caseItem.id}/like`)
    ElMessage.success('点赞成功')
    // 更新点赞数
    currentCase.value.likeCount++
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

// 保存案例
const saveCase = async () => {
  try {
    await caseFormRef.value.validate()
    saving.value = true
    
    const caseData = { ...caseForm.value }
    
    // 处理照片URL - 只使用有效的服务器URL
    if (photoFileList.value.length > 0) {
      const photoUrls = photoFileList.value
        .map(file => file.url || file.response?.data?.url)
        .filter(url => url && !url.startsWith('blob:')) // 过滤掉blob URL
      caseData.photos = JSON.stringify(photoUrls)
    }
    
    if (isEdit.value) {
      await request.put(`/v1/service-case/${caseData.id}`, caseData)
      ElMessage.success('案例更新成功')
    } else {
      await request.post('/v1/service-case', caseData)
      ElMessage.success('案例创建成功')
    }
    
    caseDialogVisible.value = false
    await loadCases()
  } catch (error) {
    console.error('保存案例失败:', error)
    ElMessage.error('保存案例失败')
  } finally {
    saving.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    caseType: null,
    keyword: ''
  }
  loadCases()
}

// 照片上传成功
const handlePhotoSuccess = (response: { code: number; data: { url: string } }, file: UploadFile, fileList: UploadFile[]) => {
  ElMessage.success('照片上传成功')
  // 确保使用服务器返回的URL而不是blob URL
  if (response && response.data && response.data.url) {
    // 将相对URL转换为完整URL用于显示缩略图
    let imageUrl = response.data.url
    if (imageUrl.startsWith('/uploads/')) {
      imageUrl = `http://localhost:8080/api${imageUrl}`
    }
    
    // 更新文件列表中的URL为完整的显示URL
    const targetFile = fileList.find(f => f.uid === file.uid)
    if (targetFile) {
      targetFile.url = imageUrl
      console.log('照片上传成功，URL已更新:', imageUrl)
    }
  }
}

// 照片上传失败
const handlePhotoError = (error: any) => {
  console.error('照片上传失败:', error)
  ElMessage.error('照片上传失败')
}

// 照片上传前验证
const beforePhotoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  
  return true
}

// 获取案例图片
const getCaseImages = (photos: string) => {
  if (!photos) return []
  try {
    return JSON.parse(photos)
  } catch {
    return []
  }
}

// 获取案例第一张图片
const getCaseImage = (photos: string) => {
  const images = getCaseImages(photos)
  return images.length > 0 ? images[0] : null
}

// 获取案例类型文本
const getCaseTypeText = (type: number) => {
  const typeMap: { [key: number]: string } = {
    1: '感人故事',
    2: '技能应用',
    3: '团队协作',
    4: '成长感悟'
  }
  return typeMap[type] || '未知'
}

// 获取案例类型标签
const getCaseTypeTag = (type: number) => {
  const tagMap: { [key: number]: string } = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: 'info'
  }
  return tagMap[type] || 'info'
}

// 获取案例摘要
const getCaseSummary = (content: string) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.service-case-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  flex-shrink: 0;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.filter-card {
  margin-bottom: 24px;
}

.case-list-card {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.case-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.case-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.case-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.case-image {
  position: relative;
  height: 200px;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f5f5;
  color: #999;
}

.case-content {
  padding: 16px;
}

.case-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.case-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 12px;
}

.case-summary {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.5;
  font-size: 14px;
}

.case-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.case-stats {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
}

.case-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.case-date {
  color: #909399;
  font-size: 12px;
}

.case-actions {
  display: flex;
  gap: 8px;
}

.case-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-date {
  color: #909399;
  font-size: 14px;
}

.detail-content {
  margin-bottom: 24px;
}

.detail-text p {
  margin: 0;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.detail-images {
  margin-top: 24px;
}

.detail-images h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.detail-stats {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 14px;
}

.detail-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .service-case-page {
    padding: 0 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    align-self: flex-end;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
  
  .case-grid {
    grid-template-columns: 1fr;
  }
  
  .case-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .case-title {
    margin-right: 0;
  }
}
</style>
