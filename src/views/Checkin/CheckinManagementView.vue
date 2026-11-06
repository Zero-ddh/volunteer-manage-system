<template>
  <div class="checkin-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">签到管理</h1>
      <p class="page-subtitle">管理活动签到记录和服务时长</p>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-bar">
      <div class="search-group">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="搜索参与者姓名、活动标题..." 
          clearable 
          style="width: 250px"
          @keyup.enter="load"
        />
        <el-select 
          v-model="searchForm.activityId" 
          placeholder="选择活动" 
          clearable 
          style="width: 200px"
        >
          <el-option 
            v-for="activity in activityList" 
            :key="activity.id" 
            :label="activity.title" 
            :value="activity.id" 
          />
        </el-select>
        <el-select 
          v-model="searchForm.status" 
          placeholder="签到状态" 
          clearable 
          style="width: 140px"
        >
          <el-option label="正常" :value="1" />
          <el-option label="迟到" :value="2" />
          <el-option label="早退" :value="3" />
        </el-select>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- 签到列表 -->
    <el-card class="checkin-list">
      <!-- 操作按钮区域 -->
      <div class="action-bar">
        <div class="action-left">
          <el-button 
            type="success" 
            :loading="autoCalculateLoading"
            @click="autoCalculateServiceHours"
          >
            自动计算签到记录服务时长
          </el-button>
          <el-button 
            type="primary" 
            :loading="updateTotalHoursLoading"
            @click="updateTotalServiceHours"
          >
            更新用户的总服务时长
          </el-button>
          <span class="operation-tip">（执行顺序：先执行计算签到记录服务时长，再执行用户总服务时长）</span>
        </div>
        <div class="action-right">
          <!-- 批量更新时长按钮已屏蔽 -->
          <!-- <el-button 
            type="warning" 
            :disabled="selectedRows.length === 0"
            @click="showBatchHoursDialog"
          >
            批量更新时长
          </el-button> -->
        </div>
      </div>

      <el-table 
        :data="list" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="活动标题" min-width="250">
          <template #default="{ row }">
            <div class="activity-title">{{ getActivityTitle(row.activityId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="参与者" min-width="130">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-name">{{ row.userName }}</div>
              <div class="user-contact">{{ row.userPhone }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="签到时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.checkinTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkoutTime" label="签退时间" min-width="180">
          <template #default="{ row }">
            {{ row.checkoutTime ? formatDateTime(row.checkoutTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkinLocation" label="签到地点" min-width="260" />
        <el-table-column label="服务时长" width="120">
          <template #default="{ row }">
            {{ row.serviceHours ? row.serviceHours + '小时' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewCheckinDetail(row.id)"
              >
                详情
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="editCheckinStatus(row.id)"
              >
                编辑状态
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

    <!-- 批量更新时长对话框 -->
    <el-dialog v-model="batchHoursDialogVisible" title="批量更新服务时长" width="400px">
      <el-form :model="batchHoursForm" label-width="100px">
        <el-form-item label="服务时长">
          <el-input-number 
            v-model="batchHoursForm.serviceHours" 
            :min="0" 
            :max="24" 
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="更新说明">
          <el-input 
            v-model="batchHoursForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入更新说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchHoursDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchUpdateHours">确认</el-button>
      </template>
    </el-dialog>

    <!-- 编辑签到状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="编辑签到状态" width="400px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="签到状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="早退" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="statusForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus">确认</el-button>
      </template>
    </el-dialog>

    <!-- 签到详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="签到详情" width="600px">
      <div v-if="currentCheckin" class="checkin-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="参与者姓名">{{ currentCheckin.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentCheckin.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="活动名称">{{ getActivityTitle(currentCheckin.activityId) }}</el-descriptions-item>
          <el-descriptions-item label="签到时间">{{ formatDateTime(currentCheckin.checkinTime) }}</el-descriptions-item>
          <el-descriptions-item label="签到地点">{{ currentCheckin.checkinLocation }}</el-descriptions-item>
          <el-descriptions-item label="签退时间">{{ currentCheckin.checkoutTime ? formatDateTime(currentCheckin.checkoutTime) : '未签退' }}</el-descriptions-item>
          <el-descriptions-item label="签退地点">{{ currentCheckin.checkoutLocation || '未签退' }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentCheckin.serviceHours }}小时</el-descriptions-item>
          <el-descriptions-item label="签到状态">
            <el-tag :type="getStatusType(currentCheckin.status)">
              {{ getStatusName(currentCheckin.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkinApi, activityApi } from '@/api'

// 响应式数据
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const activityList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const selectedRows = ref<any[]>([])
const batchHoursDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentCheckin = ref<any>(null)
const currentEditId = ref<number | null>(null)
const autoCalculateLoading = ref(false)
const updateTotalHoursLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  activityId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 批量更新时长表单
const batchHoursForm = reactive({
  serviceHours: 0,
  remark: ''
})

// 状态编辑表单
const statusForm = reactive({
  status: 1,
  remark: ''
})

// 状态映射
const statusMap = {
  1: { name: '正常', type: 'success' },
  2: { name: '迟到', type: 'warning' },
  3: { name: '早退', type: 'danger' }
}

// 获取状态名称
const getStatusName = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.name || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  return statusMap[status as keyof typeof statusMap]?.type || 'info'
}

// 获取活动标题
const getActivityTitle = (activityId: number) => {
  const activity = activityList.value.find(a => a.id === activityId)
  return activity?.title || `活动${activityId}`
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.activityId = undefined
  searchForm.status = undefined
  pageNo.value = 1
  load()
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
    const res = await checkinApi.getCheckinList(params)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载活动列表
const loadActivityList = async () => {
  try {
    const res = await activityApi.getActivityList({ pageNo: 1, pageSize: 1000 })
    activityList.value = res.data?.records || res.data?.list || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
  selectedRows.value = selection
}

// 自动计算服务时长
const autoCalculateServiceHours = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将根据签到、签退时间自动计算服务时长。如果没有签退时间，将使用活动结束时间计算。确定继续吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    autoCalculateLoading.value = true
    
    // 批量调用自动计算接口
    const promises = list.value.map(item => 
      checkinApi.autoCalculateServiceHours(item.id)
    )
    
    await Promise.all(promises)
    
    ElMessage.success('自动计算服务时长完成')
    await load() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('自动计算服务时长失败')
    }
  } finally {
    autoCalculateLoading.value = false
  }
}

// 更新用户总服务时长
const updateTotalServiceHours = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将从签到表重新统计所有用户的总服务时长，并更新到志愿者表。确定继续吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    updateTotalHoursLoading.value = true
    
    // 批量更新所有用户的总服务时长
    const res = await checkinApi.updateTotalServiceHours([])
    
    if (res.data) {
      const { successCount, failCount, message } = res.data
      if (failCount === 0) {
        ElMessage.success(message)
      } else {
        ElMessage.warning(message)
      }
    } else {
      ElMessage.success('更新用户总服务时长完成')
    }
    
    await load() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('更新用户总服务时长失败')
    }
  } finally {
    updateTotalHoursLoading.value = false
  }
}

// 更新服务时长
const updateServiceHours = async (id: number, hours: number) => {
  try {
    await checkinApi.updateServiceHours(id, { serviceHours: hours })
    ElMessage.success('服务时长更新成功')
  } catch (error) {
    ElMessage.error('服务时长更新失败')
    await load() // 重新加载数据
  }
}

// 批量更新时长
const batchUpdateHours = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要更新的签到记录')
    return
  }
  batchHoursForm.serviceHours = 0
  batchHoursForm.remark = ''
  batchHoursDialogVisible.value = true
}

// 显示批量更新时长对话框
const showBatchHoursDialog = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要更新的签到记录')
    return
  }
  batchHoursForm.serviceHours = 0
  batchHoursForm.remark = ''
  batchHoursDialogVisible.value = true
}

// 确认批量更新时长
const confirmBatchUpdateHours = async () => {
  try {
    await checkinApi.batchUpdateServiceHours({
      ids: selectedIds.value,
      serviceHours: batchHoursForm.serviceHours,
      remark: batchHoursForm.remark
    })
    ElMessage.success('批量更新成功')
    batchHoursDialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('批量更新失败')
  }
}

// 编辑签到状态
const editCheckinStatus = (id: number) => {
  currentEditId.value = id
  const checkin = list.value.find(item => item.id === id)
  if (checkin) {
    statusForm.status = checkin.status
    statusForm.remark = ''
  }
  statusDialogVisible.value = true
}

// 确认更新状态
const confirmUpdateStatus = async () => {
  if (!currentEditId.value) return
  
  try {
    await checkinApi.updateCheckinStatus(currentEditId.value, {
      status: statusForm.status,
      remark: statusForm.remark
    })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 查看签到详情
const viewCheckinDetail = async (id: number) => {
  try {
    const checkin = list.value.find(item => item.id === id)
    if (checkin) {
      currentCheckin.value = checkin
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取签到详情失败')
  }
}

// 导出签到数据
const exportCheckins = async () => {
  try {
    const params = { ...searchForm, export: true }
    const res = await checkinApi.exportCheckins(params)
    if (res.data) {
      // 创建下载链接
      const link = document.createElement('a')
      link.href = res.data
      link.download = `签到数据_${new Date().toLocaleDateString()}.xlsx`
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
  loadActivityList()
  load()
})
</script>

<style scoped>
.checkin-management {
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

.search-bar {
  margin-bottom: 20px;
}

.search-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.checkin-list {
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  gap: 12px;
}

.activity-info {
  line-height: 1.4;
}

.activity-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #666;
}

.user-info {
  line-height: 1.4;
}

.user-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.user-contact {
  font-size: 12px;
  color: #666;
}

.operation-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
  font-style: italic;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.checkin-detail {
  padding: 20px 0;
}

.checkin-detail .el-descriptions-item__content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
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
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>
