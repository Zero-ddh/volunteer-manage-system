<template>
  <div class="activity-summary-management">
    <div class="page-header">
      <h2>活动总结管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        发布活动总结
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="所属社群">
          <el-select
            v-model="searchForm.communityId"
            placeholder="请选择社群"
            clearable
            filterable
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
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="草稿" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="已删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入关键词"
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

    <!-- 活动总结列表 -->
    <div class="table-section">
      <el-table :data="summaryList" v-loading="loading" stripe>
        <el-table-column prop="title" label="总结标题" width="200" show-overflow-tooltip />
        <el-table-column prop="communityName" label="所属社群" width="150" show-overflow-tooltip />
        <el-table-column prop="publisherName" label="发布人" width="120" show-overflow-tooltip />
        <el-table-column prop="content" label="总结内容" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 2 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 2 ? '下架' : '发布' }}
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

    <!-- 发布/编辑活动总结对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动总结' : '发布活动总结'" width="800px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="所属社群" prop="communityId">
          <el-select 
            v-model="form.communityId" 
            placeholder="请选择所属社群" 
            style="width: 100%"
            filterable
            :loading="communityLoading"
            @change="handleCommunityChange"
          >
            <el-option 
              v-for="community in communityList" 
              :key="community.id" 
              :label="community.name" 
              :value="community.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="总结标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入总结标题"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="发布人" prop="publisherName">
          <el-input 
            v-model="form.publisherName" 
            placeholder="请输入发布人姓名"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="总结内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6"
            placeholder="请详细描述活动过程、成果、感受等..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="success" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <!-- 查看活动总结对话框 -->
    <el-dialog v-model="viewDialogVisible" title="活动总结详情" width="800px">
      <div v-if="currentSummary" class="summary-detail">
        <div class="summary-header">
          <h3>{{ currentSummary.title }}</h3>
          <div class="summary-meta">
            <span class="publisher">发布人：{{ currentSummary.publisherName }}</span>
            <span class="time">发布时间：{{ currentSummary.publishTime }}</span>
            <el-tag :type="getStatusTagType(currentSummary.status)">
              {{ getStatusText(currentSummary.status) }}
            </el-tag>
          </div>
        </div>
        
        <el-divider />
        
        <div class="summary-content">
          <h4>活动总结</h4>
          <p>{{ currentSummary.content }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

// 定义接口类型
interface Community {
  id: number
  name: string
}

interface ActivitySummary {
  id: number
  communityId: number
  publisherId: number
  publisherName: string
  title: string
  content: string
  status: number
  publishTime: string
  communityName?: string
}

interface SearchForm {
  communityId: number | null
  status: number | null
  keyword: string
}

interface FormData {
  id: number | null
  communityId: number | null
  publisherId: number
  title: string
  publisherName: string
  content: string
}

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const communityLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const summaryList = ref<ActivitySummary[]>([])
const communityList = ref<Community[]>([])
const currentSummary = ref<ActivitySummary | null>(null)
const selectedCommunity = ref<Community | null>(null)
const formRef = ref()

const searchForm = ref<SearchForm>({
  communityId: null,
  status: null,
  keyword: ''
})

const pagination = ref({
  pageNo: 1,
  pageSize: 20,
  total: 0
})

const form = ref<FormData>({
  id: null,
  communityId: null,
  publisherId: 1, // 设置默认发布人ID
  title: '',
  publisherName: '',
  content: ''
})

const rules = {
  communityId: [{ required: true, message: '请选择所属社群', trigger: 'change' }],
  title: [{ required: true, message: '请输入总结标题', trigger: 'blur' }],
  publisherName: [{ required: true, message: '请输入发布人姓名', trigger: 'blur' }],
  content: [{ required: true, message: '请输入总结内容', trigger: 'blur' }]
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'info',
    2: 'success',
    3: 'danger'
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

// 加载社群列表
const loadCommunities = async () => {
  communityLoading.value = true
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
  } finally {
    communityLoading.value = false
  }
}


// 加载活动总结数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
            const response = await fetch('/api/admin/activity-summaries/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(params)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      summaryList.value = data.data.records || []
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

// 社群选择改变
const handleCommunityChange = (communityId: number) => {
  selectedCommunity.value = communityList.value.find(c => c.id === communityId) || null
}

// 新增活动总结
const handleCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    communityId: null,
    publisherId: 1,
    title: '',
    publisherName: '',
    content: ''
  }
  selectedCommunity.value = null
  dialogVisible.value = true
}

// 编辑活动总结
const handleEdit = (row: ActivitySummary) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    communityId: row.communityId,
    publisherId: 1,
    title: row.title,
    publisherName: row.publisherName,
    content: row.content
  }
  
  // 设置选中的社群
  selectedCommunity.value = communityList.value.find(c => c.id === row.communityId) || null
  
  dialogVisible.value = true
}

// 查看活动总结
const handleView = (row: ActivitySummary) => {
  currentSummary.value = row
  viewDialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row: ActivitySummary) => {
  const newStatus = row.status === 2 ? 1 : 2
  const action = newStatus === 2 ? '发布' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${action}活动总结吗？`, '确认操作', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/activity-summaries/${row.id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ status: newStatus })
    })
    
    const data = await response.json()
    if (data.code === 200) {
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

// 删除活动总结
const handleDelete = async (row: ActivitySummary) => {
  try {
    await ElMessageBox.confirm('确定要删除活动总结吗？删除后不可恢复！', '确认删除', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/activity-summaries/${row.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
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

// 保存草稿
const handleSaveDraft = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    const url = isEdit.value ? `/api/admin/activity-summaries/${form.value.id}` : '/api/admin/activity-summaries'
    const method = isEdit.value ? 'PUT' : 'POST'
    
    // 准备数据，编辑时不清空ID，新建时清空ID
    const requestData = {
      ...form.value,
      status: 1 // 草稿
    }
    
    // 如果是新建，清空ID字段
    if (!isEdit.value) {
      delete (requestData as Record<string, unknown>).id
    }
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(requestData)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(isEdit.value ? '更新草稿成功' : '保存草稿成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 发布
const handlePublish = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    publishing.value = true
    
    const url = isEdit.value ? `/api/admin/activity-summaries/${form.value.id}` : '/api/admin/activity-summaries'
    const method = isEdit.value ? 'PUT' : 'POST'
    
    // 准备数据，编辑时不清空ID，新建时清空ID
    const requestData = {
      ...form.value,
      status: 2 // 已发布
    }
    
    // 如果是新建，清空ID字段
    if (!isEdit.value) {
      delete (requestData as Record<string, unknown>).id
    }
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(requestData)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    publishing.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = { communityId: null, status: null, keyword: '' }
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

.activity-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.info-item {
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: bold;
  color: #606266;
}

.value {
  color: #303133;
}

.summary-detail {
  padding: 20px;
}

.summary-header h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.summary-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.summary-content {
  margin-bottom: 20px;
}

.summary-content h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.summary-content p {
  line-height: 1.6;
  color: #606266;
}

.summary-photos {
  margin-bottom: 20px;
}

.summary-photos h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.photo-gallery {
  display: flex;
  flex-wrap: wrap;
}

.summary-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}
</style>


