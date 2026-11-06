<template>
  <div class="community-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>志愿社群</h1>
      <p class="page-description">加入感兴趣的社群，与志同道合的志愿者一起交流分享，组织线下分享会</p>
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
    <div class="community-grid" v-loading="loading">
      <div 
        v-for="community in communityList" 
        :key="community.id" 
        class="community-card"
        @click="handleViewDetail(community)"
      >
        <div class="card-header">
          <div class="community-cover">
            <img 
              v-if="community.coverImage" 
              :src="community.coverImage" 
              :alt="community.name"
              @error="handleImageError"
            />
            <div v-else class="default-cover">
              <el-icon size="48"><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="community-info">
            <h3 class="community-name">{{ community.name }}</h3>
            <el-tag :type="getCategoryTagType(community.category)" size="small">
              {{ community.category }}
            </el-tag>
          </div>
        </div>
        
        <div class="card-body">
          <p class="community-description">{{ community.description }}</p>
          <div class="community-stats">
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ community.memberCount || 0 }} 成员</span>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="action-buttons">
            <el-button 
              v-if="!isLoggedIn" 
              type="primary" 
              size="small"
              @click.stop="$router.push('/login')"
            >
              登录后加入
            </el-button>
            <el-button 
              v-else-if="!isMember(community.id)" 
              type="primary" 
              size="small"
              @click.stop="handleJoin(community)"
              :loading="joiningCommunity === community.id"
            >
              加入社群
            </el-button>
            <el-button 
              v-else 
              type="success" 
              size="small"
              @click.stop="handleViewDetail(community)"
            >
              已加入
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && communityList.length === 0" class="empty-state">
      <el-empty description="暂无社群数据">
        <el-button type="primary" @click="handleReset">刷新页面</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="communityList.length > 0" class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.pageNo"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[12, 24, 48, 96]"
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
import { Search, Refresh, ChatDotRound, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { communityApi } from '@/api/index'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const joiningCommunity = ref<number | null>(null)
const communityList = ref<any[]>([])
const memberStatusMap = ref<Record<number, boolean>>({})

const searchForm = ref({
  keyword: '',
  category: ''
})

const pagination = ref({
  pageNo: 1,
  pageSize: 12,
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

// 检查是否为社群成员
const isMember = (communityId: number) => {
  return memberStatusMap.value[communityId] || false
}

// 加载社群列表
const loadCommunityList = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
    const response = await communityApi.getCommunityList(params)
    console.log('社群列表API响应:', response)
    
    // 后端现在返回的是 Result<Page<Community>> 格式
    // response 就是 Page<Community> 对象
    if (response && response.records) {
      communityList.value = response.records || []
      pagination.value.total = response.total || 0
      
      // 如果用户已登录，检查成员状态
      if (isLoggedIn.value) {
        await checkMemberStatus()
      }
    } else {
      console.error('社群列表响应格式异常:', response)
      ElMessage.error('加载社群列表失败')
    }
  } catch (error) {
    console.error('加载社群列表失败:', error)
    ElMessage.error('加载社群列表失败')
  } finally {
    loading.value = false
  }
}

// 检查成员状态
const checkMemberStatus = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const promises = communityList.value.map(async (community) => {
      try {
        const response = await communityApi.isMember(community.id)
        console.log(`社群${community.id}成员状态API响应:`, response)
        
        // request拦截器已经提取了data部分，所以response就是布尔值
        memberStatusMap.value[community.id] = response || false
      } catch (error) {
        console.error(`检查社群${community.id}成员状态失败:`, error)
        memberStatusMap.value[community.id] = false
      }
    })
    
    await Promise.all(promises)
  } catch (error) {
    console.error('检查成员状态失败:', error)
  }
}

// 加入社群
const handleJoin = async (community: any) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要加入社群"${community.name}"吗？`,
      '确认加入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    joiningCommunity.value = community.id
    
    const response = await communityApi.joinCommunity(community.id)
    console.log('加入社群API响应:', response)
    
    // request拦截器已经提取了data部分，所以response就是布尔值
    if (response === true) {
      ElMessage.success('加入社群成功')
      memberStatusMap.value[community.id] = true
      // 更新成员数量
      community.memberCount = (community.memberCount || 0) + 1
    } else {
      ElMessage.error('加入社群失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('加入社群失败:', error)
      ElMessage.error('加入社群失败')
    }
  } finally {
    joiningCommunity.value = null
  }
}

// 查看社群详情
const handleViewDetail = (community: any) => {
  router.push(`/community/${community.id}`)
}

// 搜索
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadCommunityList()
}

// 重置
const handleReset = () => {
  searchForm.value = { keyword: '', category: '' }
  pagination.value.pageNo = 1
  loadCommunityList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNo = 1
  loadCommunityList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.pageNo = page
  loadCommunityList()
}

// 图片加载错误处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

onMounted(() => {
  loadCommunityList()
})
</script>

<style scoped>
.community-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: #fff;
  padding: 60px 0;
  text-align: center;
  margin-bottom: 20px;
  /* 全宽铺满屏幕两侧（突破父级容器宽度限制） */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}

.page-header h1 {
  font-size: 32px;
  color: #fff;
  margin-bottom: 10px;
  font-weight: 700;
}

.page-description {
  font-size: 16px;
  color: #fff;
  opacity: 0.95;
  margin: 0;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.community-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.community-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px 20px 0;
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.community-cover {
  width: 60px;
  height: 60px;
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

.community-info {
  flex: 1;
  min-width: 0;
}

.community-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-body {
  padding: 15px 20px;
}

.community-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.community-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}

.card-footer {
  padding: 15px 20px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
}

.join-type {
  flex: 1;
}

.action-buttons {
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
  .community-list {
    padding: 15px;
  }
  
  .page-header {
    padding: 40px 0;
  }
  
  .page-header h1 {
    font-size: 28px;
  }
  
  .page-description {
    font-size: 14px;
  }
  
  .community-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .search-section {
    padding: 15px;
  }
  
  .card-header {
    padding: 15px 15px 0;
  }
  
  .card-body {
    padding: 10px 15px;
  }
  
  .card-footer {
    padding: 10px 15px 15px;
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>