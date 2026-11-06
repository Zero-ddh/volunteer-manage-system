<template>
  <div class="admin-activity-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">活动管理</h1>
      <p class="page-subtitle">管理志愿活动的发布、编辑和审核</p>
    </div>

    <!-- 操作栏 -->
    <el-card class="action-bar">
      <div class="search-group">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索活动标题、组织者..." 
          clearable 
          style="width: 250px"
          @keyup.enter="load"
        />
        <el-select 
          v-model="searchForm.categoryId" 
          placeholder="全部分类" 
          clearable 
          style="width: 140px"
        >
          <el-option 
            v-for="category in categoryList" 
            :key="category.id" 
            :label="category.categoryName" 
            :value="category.id" 
          />
        </el-select>
        <el-select 
          v-model="searchForm.status" 
          placeholder="全部状态" 
          clearable 
          style="width: 140px"
        >
          <el-option label="草稿" :value="1" />
          <el-option label="已发布" :value="2" />
          <el-option label="进行中" :value="3" />
          <el-option label="已结束" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="addActivity">发布活动</el-button>
      </div>
    </el-card>

    <!-- 活动列表 -->
    <el-card class="activity-list">
      <el-table 
        :data="list" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column label="组织者" width="150">
          <template #default="{ row }">
            {{ getOrganizerName(row.organizerId) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.categoryId)" size="small">
              {{ getCategoryName(row.categoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名人数" width="120">
          <template #default="{ row }">
            <div class="participants-info">
              {{ row.currentParticipants }}/{{ row.maxParticipants }}人
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="viewActivity(row.id)">
                查看
              </el-button>
              <el-button type="warning" size="small" @click="editActivity(row.id)">
                编辑
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                v-if="row.status === 1"
                @click="publishActivity(row.id)"
              >
                发布
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteActivity(row.id)"
              >
                删除
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

    <!-- 批量审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="批量审核" width="400px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核状态">
          <el-select v-model="auditForm.status" placeholder="请选择状态">
            <el-option label="发布" :value="2" />
            <el-option label="取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="auditForm.remark" 
            type="textarea" 
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { activityApi, activityCategoryApi } from '@/api'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const selectedIds = ref<number[]>([])
const auditDialogVisible = ref(false)
const categoryList = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 审核表单
const auditForm = reactive({
  status: 2,
  remark: ''
})

// 分类映射 - 动态生成
const categoryMap = ref<{ [key: number]: { name: string; type: string } }>({})

// 状态映射
const statusMap = {
  1: { name: '草稿', type: 'info' },
  2: { name: '已发布', type: 'success' },
  3: { name: '进行中', type: 'primary' },
  4: { name: '已结束', type: 'warning' },
  5: { name: '已取消', type: 'danger' }
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  return categoryMap.value[categoryId]?.name || '未知'
}

// 获取分类类型
const getCategoryType = (categoryId: number) => {
  return categoryMap.value[categoryId]?.type || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.name || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.type || 'info'
}

// 获取组织者名称
const getOrganizerName = (organizerId: number) => {
  // 这里可以根据organizerId从用户列表或API获取组织者名称
  // 暂时返回一个默认值，实际项目中应该从后端获取
  const organizerMap: { [key: number]: string } = {
    1: '系统管理员',
    2: '活动组织者1',
    3: '活动组织者2'
  }
  return organizerMap[organizerId] || `组织者${organizerId}`
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (date: string) => {
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
    const res = await activityApi.getActivityList(params)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = undefined
  searchForm.status = undefined
  pageNo.value = 1
  load()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 发布活动
const addActivity = () => {
  // 跳转到创建活动页面
  router.push('/activities/create')
}

// 查看活动
const viewActivity = (id: number) => {
  // 跳转到活动详情页面
  router.push(`/activities/${id}`)
}

// 编辑活动
const editActivity = (id: number) => {
  // 跳转到编辑活动页面
  router.push(`/activities/${id}/edit`)
}

// 发布活动
const publishActivity = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认发布此活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await activityApi.updateActivityStatus(id, 2)
    ElMessage.success('发布成功')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

// 删除活动
const deleteActivity = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此活动吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await activityApi.deleteActivity(id)
    ElMessage.success('删除成功')
    await load()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 显示后端返回的具体错误信息
      const errorMessage = error?.message || error?.response?.data?.message || '删除失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 批量审核
const batchAudit = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要审核的活动')
    return
  }
  auditDialogVisible.value = true
}

// 确认批量审核
const confirmBatchAudit = async () => {
  try {
    await activityApi.batchUpdateStatus(selectedIds.value, auditForm.status)
    ElMessage.success('批量审核成功')
    auditDialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('批量审核失败')
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的活动')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个活动吗？删除后无法恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await activityApi.batchDelete(selectedIds.value)
    ElMessage.success('批量删除成功')
    await load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await activityCategoryApi.getEnabledCategories()
    if (res.code === 200) {
      categoryList.value = res.data || []
      // 构建分类映射
      const map: { [key: number]: { name: string; type: string } } = {}
      const typeColors = ['success', 'primary', 'warning', 'danger', 'info']
      categoryList.value.forEach((category: any, index: number) => {
        map[category.id] = {
          name: category.categoryName,
          type: typeColors[index % typeColors.length]
        }
      })
      categoryMap.value = map
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
    ElMessage.error('加载分类数据失败')
  }
}

// 导出活动数据
const exportActivities = async () => {
  try {
    const res = await activityApi.exportActivities(searchForm)
    if (res.data) {
      // 创建下载链接
      const link = document.createElement('a')
      link.href = res.data
      link.download = `活动数据_${new Date().toLocaleDateString()}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.admin-activity-list {
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

.action-bar {
  margin-bottom: 20px;
}

.search-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: nowrap;
}


.activity-list {
  margin-bottom: 20px;
}


.participants-info {
  text-align: center;
}

.participants-count {
  font-weight: bold;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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
  
  .btn-group {
    justify-content: center;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>


