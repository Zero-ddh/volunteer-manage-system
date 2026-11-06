<template>
  <div class="checkins-page">
    <div class="page-header">
      <h2>签到记录</h2>
      <p class="page-subtitle">查看和管理您的活动签到记录（系统会根据每日签到/签退时间生成服务时长数据）</p>
    </div>

    <el-card class="checkins-list">
      <!-- 搜索区域 -->
      <div class="search-container">
        <el-input
          v-model="searchForm.activityTitle"
          placeholder="请输入活动标题进行搜索"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 300px; margin-right: 10px;"
        >
          <template #append>
            <el-button @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="activityTitle" label="活动标题" min-width="200" />
        <el-table-column prop="checkinTime" label="签到时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.checkinTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkoutTime" label="签退时间" width="180">
          <template #default="{ row }">
            {{ row.checkoutTime ? formatDateTime(row.checkoutTime) : '未签退' }}
          </template>
        </el-table-column>
        <el-table-column label="服务时长" width="120">
          <template #default="{ row }">
            <span v-if="row.actualServiceHours">
              {{ row.actualServiceHours }}小时
            </span>
            <span v-else-if="row.serviceHours">
              {{ row.serviceHours }}小时
            </span>
            <span v-else class="text-gray">未计算</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="!row.checkoutTime" 
              type="primary" 
              size="small"
              @click="handleCheckout(row)"
            >
              签退
            </el-button>
            <el-button 
              v-if="row.checkoutTime"
              type="info" 
              size="small"
              disabled
            >
              已完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 签退对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="活动签退"
      width="500px"
    >
      <el-form :model="checkoutForm" :rules="checkoutRules" ref="checkoutFormRef" label-width="100px">
        <el-form-item label="活动" prop="activityTitle">
          <el-input v-model="checkoutForm.activityTitle" disabled />
        </el-form-item>
        <el-form-item label="签到时间" prop="checkinTime">
          <el-input v-model="checkoutForm.checkinTime" disabled />
        </el-form-item>
        <el-form-item label="签退地点" prop="location">
          <el-input 
            v-model="checkoutForm.location" 
            placeholder="请输入签退地点"
            type="textarea"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="checkoutForm.remark" 
            placeholder="请输入签退备注（可选）"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckout" :loading="checkoutLoading">
          确认签退
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { checkinApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const records = ref<any[]>([])
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = ref({
  activityTitle: ''
})

// 签退相关
const checkoutDialogVisible = ref(false)
const checkoutLoading = ref(false)
const checkoutForm = ref({
  activityId: null,
  activityTitle: '',
  checkinTime: '',
  location: '',
  remark: ''
})
const checkoutRules = {
  location: [
    { required: true, message: '请输入签退地点', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    const res = await checkinApi.getCheckinRecordsWithActivity(
      pagination.value.pageNo, 
      pagination.value.pageSize,
      searchForm.value.activityTitle || undefined
    )
    records.value = res.records || []
    pagination.value.total = res.total || 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
function handleSearch() {
  pagination.value.pageNo = 1 // 重置到第一页
  load()
}

function handleCheckout(row: any) {
  checkoutForm.value = {
    activityId: row.activityId,
    activityTitle: row.activityTitle,
    checkinTime: formatDateTime(row.checkinTime),
    location: '',
    remark: ''
  }
  checkoutDialogVisible.value = true
}

async function confirmCheckout() {
  checkoutLoading.value = true
  try {
    await checkinApi.checkout(checkoutForm.value.activityId, {
      location: checkoutForm.value.location,
      remark: checkoutForm.value.remark
    })
    ElMessage.success('签退成功')
    checkoutDialogVisible.value = false
    load() // 重新加载数据
  } catch (error) {
    ElMessage.error('签退失败：' + (error as any).message)
  } finally {
    checkoutLoading.value = false
  }
}

function formatDateTime(dateTime: string) {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

function getStatusType(status: number) {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status as keyof typeof types] || 'info'
}

function getStatusName(status: number) {
  const names = { 1: '正常', 2: '迟到', 3: '早退' }
  return names[status as keyof typeof names] || '未知'
}

onMounted(load)
</script>

<style scoped>
.checkins-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-subtitle {
  color: #666;
  margin-top: 5px;
}

.search-container {
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.text-gray {
  color: #999;
}
</style>


