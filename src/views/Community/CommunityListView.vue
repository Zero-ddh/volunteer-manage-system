<template>
  <div class="community-management">
    <div class="page-header">
      <h2>社群列表管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增社群
      </el-button>
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
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="2" />
            <el-option label="解散" :value="3" />
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
    <div class="table-section">
      <el-table :data="communityList" v-loading="loading" stripe>
        <el-table-column prop="name" label="社群名称" width="200" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              :disabled="row.memberCount && row.memberCount > 0"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑社群' : '新增社群'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="社群名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社群名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
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
        <el-form-item label="社群描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入社群描述" />
        </el-form-item>
        <el-form-item label="最大成员数">
          <el-input-number v-model="form.maxMembers" :min="10" :max="5000" />
        </el-form-item>
        <el-form-item label="社群标签">
          <el-input v-model="form.tags" placeholder="请输入标签，多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看社群对话框 -->
    <el-dialog v-model="viewDialogVisible" title="社群详情" width="600px">
      <div v-if="viewData" class="community-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="社群名称">
            {{ viewData.name }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag :type="getCategoryTagType(viewData.category)">
              {{ viewData.category }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="社群描述" :span="2">
            {{ viewData.description }}
          </el-descriptions-item>
          <el-descriptions-item label="当前成员数">
            {{ viewData.memberCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="最大成员数">
            {{ viewData.maxMembers || 1000 }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(viewData.status)">
              {{ getStatusText(viewData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="社群标签">
            {{ viewData.tags || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ viewData.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ viewData.updateTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const communityList = ref([])
const formRef = ref()

// 查看对话框相关
const viewDialogVisible = ref(false)
const viewData = ref<any>(null)

const searchForm = ref({
  keyword: '',
  category: '',
  status: null
})

const pagination = ref({
  pageNo: 1,
  pageSize: 20,
  total: 0
})

const form = ref({
  id: null,
  name: '',
  category: '',
  description: '',
  maxMembers: 1000,
  tags: ''
})

const rules = {
  name: [{ required: true, message: '请输入社群名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入社群描述', trigger: 'blur' }]
}


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
    2: '禁用',
    3: '解散'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  // 将 "2025-10-25T22:30:31" 格式化为 "2025-10-25 22:30:31"
  return dateTimeStr.replace('T', ' ')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
            const response = await fetch('/api/admin/community/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(params)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      communityList.value = data.data.records || []
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

// 新增社群
const handleCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    name: '',
    category: '',
    description: '',
    maxMembers: 1000,
    tags: ''
  }
  dialogVisible.value = true
}

// 编辑社群
const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 查看社群
const handleView = (row: any) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}社群"${row.name}"吗？`, '确认操作', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community/status/${row.id}`, {
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

// 删除社群
const handleDelete = async (row: any) => {
  try {
    // 检查社群是否有成员
    if (row.memberCount && row.memberCount > 0) {
      ElMessage.warning(`社群"${row.name}"当前有 ${row.memberCount} 名成员，无法删除。请先移除所有成员后再删除社群。`)
      return
    }
    
    await ElMessageBox.confirm(`确定要删除社群"${row.name}"吗？删除后不可恢复！`, '确认删除', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community/delete/${row.id}`, {
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


// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const url = isEdit.value ? '/api/admin/community/update' : '/api/admin/community/create'
    const method = isEdit.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(form.value)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
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
  searchForm.value = { keyword: '', category: '', status: null }
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

.community-detail {
  padding: 10px 0;
}
</style>


