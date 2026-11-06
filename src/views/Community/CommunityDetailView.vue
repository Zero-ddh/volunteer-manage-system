<template>
  <div class="community-detail">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="$router.back()" icon="ArrowLeft">
        返回社群列表
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 社群详情 -->
    <div v-else-if="community" class="detail-content">
      <!-- 社群基本信息 -->
      <div class="community-header">
        <div class="community-cover">
          <img 
            v-if="community.coverImage" 
            :src="community.coverImage" 
            :alt="community.name"
            @error="handleImageError"
          />
          <div v-else class="default-cover">
            <el-icon size="80"><ChatDotRound /></el-icon>
          </div>
        </div>
        <div class="community-info">
          <h1 class="community-name">{{ community.name }}</h1>
          <el-tag :type="getCategoryTagType(community.category)" size="large">
            {{ community.category }}
          </el-tag>
          <p class="community-description">{{ community.description }}</p>
          <div class="community-stats">
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ community.memberCount || 0 }} 成员</span>
            </div>
            <div class="stat-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(community.createTime) }} 创建</span>
            </div>
          </div>
        </div>
        <div class="community-actions">
          <el-button 
            v-if="!isLoggedIn" 
            type="primary" 
            size="large"
            @click="$router.push('/login')"
          >
            登录后加入
          </el-button>
          <el-button 
            v-else-if="!isMember" 
            type="primary" 
            size="large"
            @click="handleJoin"
            :loading="joining"
          >
            加入社群
          </el-button>
          <el-button 
            v-else 
            type="success" 
            size="large"
            @click="handleQuit"
            :loading="quitting"
          >
            已加入
          </el-button>
        </div>
      </div>

      <!-- 标签信息 -->
      <div v-if="community.tags" class="tags-section">
        <h3>社群标签</h3>
        <div class="tags-list">
          <el-tag 
            v-for="tag in getTagsList(community.tags)" 
            :key="tag" 
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-section">
        <!-- 活动总结列表 -->
        <div class="summary-section">
          <div class="summary-header">
            <h3>活动总结</h3>
            <!-- 搜索框 -->
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索活动总结..."
                clearable
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button 
                type="primary" 
                @click="handleSearch"
                :loading="summaryLoading"
              >
                查询
              </el-button>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="summaryLoading" class="summary-loading">
            <el-skeleton :rows="3" animated />
          </div>
          
          <!-- 活动总结列表 -->
          <div v-else-if="summaryList.length > 0" class="summary-list">
            <div 
              v-for="summary in summaryList" 
              :key="summary.id" 
              class="summary-item"
              @click="handleViewSummary(summary)"
            >
              <div class="summary-content">
                <h4 class="summary-title">{{ summary.title }}</h4>
                <p class="summary-text">{{ summary.content }}</p>
                <div class="summary-meta">
                  <div class="meta-left">
                    <span class="summary-author">
                      <el-icon><User /></el-icon>
                      {{ summary.publisherName }}
                    </span>
                    <span class="summary-time">
                      <el-icon><Calendar /></el-icon>
                      {{ formatDate(summary.publishTime) }}
                    </span>
                  </div>
                  <div class="meta-right">
                    <span class="summary-status">
                      <el-tag :type="getStatusTagType(summary.status)" size="small">
                        {{ getStatusText(summary.status) }}
                      </el-tag>
                    </span>
                  </div>
                </div>
              </div>
              <div class="summary-actions">
                <el-button type="primary" size="small" plain>
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else class="empty-summary">
            <el-empty description="暂无活动总结" :image-size="80" />
          </div>
          
          <!-- 分页 -->
          <div v-if="summaryList.length > 0" class="pagination-section">
            <el-pagination
              v-model:current-page="pagination.pageNo"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-section">
      <el-result
        icon="error"
        title="社群不存在"
        sub-title="请检查链接是否正确"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/community')">
            返回社群列表
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 活动总结详情弹窗 -->
    <el-dialog 
      v-model="summaryDialogVisible" 
      title="活动总结详情" 
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentSummary" class="summary-detail">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentSummary.title }}</h3>
          <div class="detail-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>发布人：{{ currentSummary.publisherName }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>发布时间：{{ formatDateTime(currentSummary.publishTime) }}</span>
            </div>
            <div class="meta-item">
              <el-tag :type="getStatusTagType(currentSummary.status)" size="small">
                {{ getStatusText(currentSummary.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-content">
          <h4>活动总结内容</h4>
          <div class="content-text">{{ currentSummary.content }}</div>
        </div>
        
        <div v-if="currentSummary.photos" class="detail-photos">
          <h4>活动照片</h4>
          <div class="photo-gallery">
            <div 
              v-for="(photo, index) in getPhotoList(currentSummary.photos)" 
              :key="index"
              class="photo-item"
            >
              <el-image
                :src="photo"
                :preview-src-list="getPhotoList(currentSummary.photos)"
                :initial-index="index"
                fit="cover"
                class="photo-image"
              />
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="summaryDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  ChatDotRound, 
  User, 
  Calendar,
  Search
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { communityApi, activitySummaryApi } from '@/api/index'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const joining = ref(false)
const quitting = ref(false)
const isMember = ref(false)
const community = ref<any>(null)

const summaryLoading = ref(false)
const summaryList = ref<any[]>([])
const searchKeyword = ref('')
const summaryDialogVisible = ref(false)
const currentSummary = ref<any>(null)
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)
const communityId = computed(() => Number(route.params.id))

// 获取分类标签类型
const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, string> = {
    '环保公益': 'success',
    '教育助学': 'primary',
    '扶贫助困': 'warning',
    '医疗健康': 'danger',
    '文化传承': 'info',
    '应急救援': 'warning',
    '社区服务': 'success',
    '其他': 'info'
  }
  return typeMap[category] || 'info'
}


// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 获取标签列表
const getTagsList = (tags: string) => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'info',    // 草稿
    2: 'success', // 已发布
    3: 'danger'   // 已删除
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    1: '草稿',
    2: '已发布',
    3: '已删除'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 获取照片列表
const getPhotoList = (photos: string) => {
  if (!photos) return []
  try {
    const photoArray = JSON.parse(photos)
    return Array.isArray(photoArray) ? photoArray : []
  } catch {
    return []
  }
}

// 加载社群详情
const loadCommunityDetail = async () => {
  loading.value = true
  try {
    const response = await communityApi.getCommunityDetail(communityId.value)
    console.log('社群详情API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是社群数据本身
    if (response && response.id) {
      community.value = response
      
      // 如果用户已登录，检查成员状态
      if (isLoggedIn.value) {
        await checkMemberStatus()
      }
    } else {
      ElMessage.error('社群不存在')
    }
  } catch (error) {
    console.error('加载社群详情失败:', error)
    ElMessage.error('加载社群详情失败')
  } finally {
    loading.value = false
  }
}

// 检查成员状态
const checkMemberStatus = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await communityApi.isMember(communityId.value)
    console.log('检查成员状态API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是布尔值
    isMember.value = response || false
  } catch (error) {
    console.error('检查成员状态失败:', error)
    isMember.value = false
  }
}

// 加载活动总结列表
const loadActivitySummaryList = async () => {
  summaryLoading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value || undefined,
      status: 2 // 只查询已发布状态的数据
    }
    
    const response = await activitySummaryApi.getListByCommunityId(communityId.value, params)
    console.log('活动总结列表API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是分页数据
    if (response && response.records) {
      summaryList.value = response.records
      pagination.value.total = response.total || 0
    } else {
      summaryList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('加载活动总结列表失败:', error)
    summaryList.value = []
    pagination.value.total = 0
  } finally {
    summaryLoading.value = false
  }
}

// 加入社群
const handleJoin = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要加入社群"${community.value.name}"吗？`,
      '确认加入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    joining.value = true
    
    const response = await communityApi.joinCommunity(communityId.value)
    console.log('加入社群API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是布尔值
    if (response === true) {
      ElMessage.success('加入社群成功')
      isMember.value = true
      community.value.memberCount = (community.value.memberCount || 0) + 1
    } else {
      ElMessage.error('加入社群失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('加入社群失败:', error)
      ElMessage.error('加入社群失败')
    }
  } finally {
    joining.value = false
  }
}

// 退出社群
const handleQuit = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要退出社群"${community.value.name}"吗？`,
      '确认退出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    quitting.value = true
    
    const response = await communityApi.quitCommunity(communityId.value)
    console.log('退出社群API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是布尔值
    if (response === true) {
      ElMessage.success('退出社群成功')
      isMember.value = false
      community.value.memberCount = Math.max((community.value.memberCount || 0) - 1, 0)
    } else {
      ElMessage.error('退出社群失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出社群失败:', error)
      ElMessage.error('退出社群失败')
    }
  } finally {
    quitting.value = false
  }
}

// 搜索活动总结
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadActivitySummaryList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNo = 1
  loadActivitySummaryList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.pageNo = page
  loadActivitySummaryList()
}

// 查看活动总结详情
const handleViewSummary = (summary: any) => {
  currentSummary.value = summary
  summaryDialogVisible.value = true
}

// 图片加载错误处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

onMounted(() => {
  loadCommunityDetail()
  loadActivitySummaryList()
})
</script>

<style scoped>
.community-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-section {
  margin-bottom: 20px;
}

.loading-section {
  padding: 40px;
}

.detail-content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.community-header {
  padding: 30px;
  display: flex;
  gap: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.community-cover {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.community-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.community-info {
  flex: 1;
  min-width: 0;
}

.community-name {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.community-description {
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
  margin: 15px 0;
}

.community-stats {
  display: flex;
  gap: 30px;
  margin-top: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
}

.community-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.tags-section {
  padding: 20px 30px;
  border-bottom: 1px solid #f0f0f0;
}

.tags-section h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.content-section {
  padding: 30px;
}

.summary-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  width: 100%;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.summary-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.search-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-box .el-input {
  width: 300px;
}

.summary-loading {
  padding: 20px 0;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.summary-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 15px;
}

.summary-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.summary-content {
  flex: 1;
  min-width: 0;
}

.summary-title {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.summary-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.summary-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.meta-left {
  display: flex;
  gap: 15px;
  align-items: center;
}

.meta-right {
  display: flex;
  align-items: center;
}

.summary-author,
.summary-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.summary-actions {
  flex-shrink: 0;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-summary {
  text-align: center;
  padding: 40px 0;
}

/* 弹窗样式 */
.summary-detail {
  padding: 20px 0;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-title {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.detail-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.detail-content {
  margin-bottom: 20px;
}

.detail-content h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.content-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  padding-left: 20px;
  margin-left: 10px;
}

.detail-photos {
  margin-top: 20px;
}

.detail-photos h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.photo-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.photo-item {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.photo-image {
  width: 100%;
  height: 100%;
}

.summary-actions {
  flex-shrink: 0;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-summary {
  text-align: center;
  padding: 40px 0;
}

.error-section {
  padding: 60px 20px;
}

@media (max-width: 768px) {
  .community-detail {
    padding: 15px;
  }
  
  .community-header {
    flex-direction: column;
    padding: 20px;
    text-align: center;
  }
  
  .community-cover {
    width: 100px;
    height: 100px;
    margin: 0 auto;
  }
  
  .community-name {
    font-size: 24px;
  }
  
  .community-stats {
    justify-content: center;
    gap: 20px;
  }
  
  .content-section {
    padding: 20px;
  }
  
  .summary-section {
    padding: 15px;
  }
  
  .summary-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .search-box {
    width: 100%;
    flex-direction: column;
    gap: 10px;
  }
  
  .search-box .el-input {
    width: 100%;
  }
  
  .summary-item {
    flex-direction: column;
    gap: 15px;
  }
  
  .summary-actions {
    align-self: flex-end;
  }
  
  .detail-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .photo-gallery {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;
  }
}
</style>