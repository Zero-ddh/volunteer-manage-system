<template>
  <div class="my-communities">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我的社群</h1>
      <p class="page-description">管理您加入的社群，查看社群动态</p>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入社群名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option label="环保公益" value="环保公益" />
            <el-option label="教育助学" value="教育助学" />
            <el-option label="扶贫助困" value="扶贫助困" />
            <el-option label="医疗健康" value="医疗健康" />
            <el-option label="文化传承" value="文化传承" />
            <el-option label="应急救援" value="应急救援" />
            <el-option label="社区服务" value="社区服务" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 社群列表 -->
    <div class="community-list" v-loading="loading">
      <div 
        v-for="community in communityList" 
        :key="community.id" 
        class="community-item"
      >
        <div class="community-info">
          <div class="community-cover">
            <img 
              v-if="community.coverImage" 
              :src="community.coverImage" 
              :alt="community.name"
              @error="handleImageError"
            />
            <div v-else class="default-cover">
              <el-icon size="32"><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="community-details">
            <h3 class="community-name">{{ community.name }}</h3>
            <el-tag :type="getCategoryTagType(community.category)" size="small">
              {{ community.category }}
            </el-tag>
            <p class="community-description">{{ community.description }}</p>
            <div class="community-stats">
              <!-- 成员字段已屏蔽 -->
              <div class="stat-item">
                <el-icon><Calendar /></el-icon>
                <span>加入时间：{{ formatDate(community.joinTime) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="community-actions">
          <el-button 
            type="primary" 
            @click="handleViewDetail(community)"
          >
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <el-button 
            type="danger" 
            @click="handleQuitCommunity(community)"
            :loading="quittingCommunity === community.id"
          >
            <el-icon><Close /></el-icon>
            退出社群
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && communityList.length === 0" class="empty-state">
      <el-empty description="您还没有加入任何社群">
        <el-button type="primary" @click="$router.push('/community')">
          去发现社群
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="communityList.length > 0" class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.pageNo"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ChatDotRound, User, Calendar, View, Close } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { communityApi } from '@/api/index'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const quittingCommunity = ref<number | null>(null)
const communityList = ref<any[]>([])

const searchForm = ref({
  keyword: '',
  category: ''
})

const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)

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
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 加载我的社群列表
const loadMyCommunities = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
    const response = await communityApi.getMyCommunities(params)
    console.log('我的社群列表API响应:', response)
    
    if (response && response.records) {
      communityList.value = response.records || []
      pagination.value.total = response.total || 0
    } else {
      console.error('我的社群列表响应格式异常:', response)
      ElMessage.error('加载社群列表失败')
    }
  } catch (error) {
    console.error('加载我的社群列表失败:', error)
    ElMessage.error('加载社群列表失败')
  } finally {
    loading.value = false
  }
}

// 查看社群详情
const handleViewDetail = (community: any) => {
  router.push(`/community/${community.id}`)
}

// 退出社群
const handleQuitCommunity = async (community: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出社群"${community.name}"吗？退出后将无法接收社群消息。`,
      '确认退出',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    quittingCommunity.value = community.id
    
    const response = await communityApi.quitCommunity(community.id)
    console.log('退出社群API响应:', response)
    
    if (response === true) {
      ElMessage.success('退出社群成功')
      // 重新加载列表
      await loadMyCommunities()
    } else {
      ElMessage.error('退出社群失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出社群失败:', error)
      ElMessage.error('退出社群失败')
    }
  } finally {
    quittingCommunity.value = null
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadMyCommunities()
}

// 重置
const handleReset = () => {
  searchForm.value = { keyword: '', category: '' }
  pagination.value.pageNo = 1
  loadMyCommunities()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNo = 1
  loadMyCommunities()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.pageNo = page
  loadMyCommunities()
}

// 图片加载错误处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

onMounted(() => {
  loadMyCommunities()
})
</script>

<style scoped>
.my-communities {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.community-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.community-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;
}

.community-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.community-info {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.community-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
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

.community-details {
  flex: 1;
  min-width: 0;
}

.community-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.community-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.community-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}

.community-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .my-communities {
    padding: 15px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .page-description {
    font-size: 14px;
  }
  
  .search-section {
    padding: 15px;
  }
  
  .community-item {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .community-info {
    gap: 15px;
  }
  
  .community-cover {
    width: 60px;
    height: 60px;
  }
  
  .community-name {
    font-size: 18px;
  }
  
  .community-stats {
    gap: 15px;
  }
  
  .community-actions {
    width: 100%;
    justify-content: center;
  }
  
  .community-actions .el-button {
    flex: 1;
  }
}
</style>