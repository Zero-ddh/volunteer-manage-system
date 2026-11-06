<template>
  <div class="community-post-management">
    <div class="page-header">
      <h2>社群帖子管理</h2>
      <el-button type="primary" @click="handleCreatePost">
        <el-icon><Plus /></el-icon>
        发布官方公告
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="社群">
          <el-select
            v-model="searchForm.communityId"
            placeholder="请选择社群"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="community in communityList"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="帖子类型">
          <el-select
            v-model="searchForm.postType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="普通帖子" :value="1" />
            <el-option label="活动组织" :value="2" />
            <el-option label="经验分享" :value="3" />
            <el-option label="公告" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="隐藏" :value="2" />
            <el-option label="删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入标题或内容"
            clearable
            style="width: 200px"
          />
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

    <!-- 帖子列表 -->
    <div class="table-section">
      <el-table :data="postList" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="communityName" label="所属社群" width="150" />
        <el-table-column prop="postType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPostTypeTagType(row.postType)">
              {{ getPostTypeText(row.postType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEssence" label="精华" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isEssence" type="warning" size="small">精华</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              size="small" 
              :type="row.isTop ? 'info' : 'warning'"
              @click="handleToggleTop(row)"
            >
              {{ row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button 
              size="small" 
              :type="row.isEssence ? 'info' : 'success'"
              @click="handleToggleEssence(row)"
            >
              {{ row.isEssence ? '取消精华' : '精华' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
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

    <!-- 发布官方公告对话框 -->
    <el-dialog v-model="postDialogVisible" title="发布官方公告" width="600px">
      <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="100px">
        <el-form-item label="选择社群" prop="communityId">
          <el-select v-model="postForm.communityId" placeholder="请选择社群" style="width: 100%">
            <el-option
              v-for="community in communityList"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" :rows="5" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="postForm.isTop" />
        </el-form-item>
        <el-form-item label="是否精华">
          <el-switch v-model="postForm.isEssence" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPost" :loading="submitLoading">发布</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="帖子详情" width="800px">
      <div v-if="currentPost" class="post-detail">
        <div class="post-header">
          <h3>{{ currentPost.title }}</h3>
          <div class="post-meta">
            <el-tag :type="getPostTypeTagType(currentPost.postType)">
              {{ getPostTypeText(currentPost.postType) }}
            </el-tag>
            <span class="author">作者：{{ currentPost.authorName }}</span>
            <span class="time">发布时间：{{ currentPost.createTime }}</span>
          </div>
        </div>
        <div class="post-content">
          <p>{{ currentPost.content }}</p>
        </div>
        <div class="post-stats">
          <span>👍 {{ currentPost.likeCount || 0 }}</span>
          <span>💬 {{ currentPost.commentCount || 0 }}</span>
          <span>👁 {{ currentPost.viewCount || 0 }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const postDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const postList = ref([])
const communityList = ref([])
const currentPost = ref(null)
const postFormRef = ref()

const searchForm = ref({
  communityId: null,
  postType: null,
  status: null,
  keyword: ''
})

const pagination = ref({
  pageNo: 1,
  pageSize: 20,
  total: 0
})

const postForm = ref({
  communityId: null,
  title: '',
  content: '',
  postType: 4, // 公告
  isTop: false,
  isEssence: false
})

const postRules = {
  communityId: [{ required: true, message: '请选择社群', trigger: 'change' }],
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

// 获取帖子类型标签类型
const getPostTypeTagType = (type: number) => {
  const typeMap: Record<number, string> = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取帖子类型文本
const getPostTypeText = (type: number) => {
  const textMap: Record<number, string> = {
    1: '普通帖子',
    2: '活动组织',
    3: '经验分享',
    4: '公告'
  }
  return textMap[type] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    1: '正常',
    2: '隐藏',
    3: '删除'
  }
  return textMap[status] || '未知'
}

// 加载社群列表
const loadCommunities = async () => {
  try {
    const response = await fetch('/api/admin/community/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ pageNo: 1, pageSize: 1000 })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      communityList.value = data.data.records || []
    }
  } catch (error) {
    console.error('加载社群列表失败:', error)
  }
}

// 加载帖子数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
            const response = await fetch('/api/admin/community-posts', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(params)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      postList.value = data.data.records || []
      pagination.value.total = data.data.total || 0
    } else {
      ElMessage.error(data.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 发布官方公告
const handleCreatePost = () => {
  postForm.value = {
    communityId: null,
    title: '',
    content: '',
    postType: 4,
    isTop: false,
    isEssence: false
  }
  postDialogVisible.value = true
}

// 查看帖子详情
const handleView = (row: any) => {
  currentPost.value = row
  viewDialogVisible.value = true
}

// 切换置顶状态
const handleToggleTop = async (row: any) => {
  const newTop = !row.isTop
  const action = newTop ? '置顶' : '取消置顶'
  
  try {
    await ElMessageBox.confirm(`确定要${action}帖子"${row.title}"吗？`, '确认操作', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community/posts/${row.id}/top`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ isTop: newTop })
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.error(data.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 切换精华状态
const handleToggleEssence = async (row: any) => {
  const newEssence = !row.isEssence
  const action = newEssence ? '设为精华' : '取消精华'
  
  try {
    await ElMessageBox.confirm(`确定要${action}帖子"${row.title}"吗？`, '确认操作', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community/posts/${row.id}/essence`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ isEssence: newEssence })
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.error(data.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除帖子
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除帖子"${row.title}"吗？删除后不可恢复！`, '确认删除', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community/posts/${row.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      }
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交帖子
const handleSubmitPost = async () => {
  if (!postFormRef.value) return
  
  try {
    await postFormRef.value.validate()
    submitLoading.value = true
    
    const response = await fetch('/api/admin/community/posts/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(postForm.value)
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success('发布成功')
      postDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  } finally {
    submitLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = { communityId: null, postType: null, status: null, keyword: '' }
  pagination.value.pageNo = 1
  loadData()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNo = 1
  loadData()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.pageNo = page
  loadData()
}

onMounted(() => {
  loadCommunities()
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.post-detail {
  padding: 20px;
}

.post-header {
  margin-bottom: 20px;
}

.post-header h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #909399;
  font-size: 14px;
}

.post-content {
  margin-bottom: 20px;
  line-height: 1.6;
  color: #606266;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}
</style>


