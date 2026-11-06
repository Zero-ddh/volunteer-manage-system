<template>
  <div class="points-record-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>积分记录管理</h2>
      <p>查看和管理所有用户的积分记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" :inline="true" class="search-form-inline">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="积分类型">
          <el-select
            v-model="searchForm.pointsType"
            placeholder="请选择积分类型"
            clearable
            style="width: 150px"
          >
            <el-option label="获得积分" :value="1" />
            <el-option label="消费积分" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="积分来源">
          <el-input
            v-model="searchForm.source"
            placeholder="请输入积分来源"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
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

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :default-sort="{ prop: 'createdTime', order: 'descending' }"
      >
        <el-table-column prop="id" label="记录ID" width="80" align="center" />
        
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        
        <el-table-column prop="username" label="用户名" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.username">{{ row.username }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="realName" label="真实姓名" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.realName">{{ row.realName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="points" label="积分数量" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.pointsType === 1 ? 'points-earn' : 'points-spend'">
              {{ row.pointsType === 1 ? '+' : '-' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="pointsType" label="积分类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.pointsType === 1 ? 'success' : 'warning'">
              {{ row.pointsType === 1 ? '获得' : '消费' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="source" label="积分来源" width="150" align="center" />
        
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="balance" label="积分余额" width="100" align="center">
          <template #default="{ row }">
            <span class="balance-text">{{ row.balance }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="积分记录详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">
            {{ currentRecord.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentRecord.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentRecord.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ currentRecord.realName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="积分数量">
            <span :class="currentRecord.pointsType === 1 ? 'points-earn' : 'points-spend'">
              {{ currentRecord.pointsType === 1 ? '+' : '-' }}{{ currentRecord.points }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="积分类型">
            <el-tag :type="currentRecord.pointsType === 1 ? 'success' : 'warning'">
              {{ currentRecord.pointsType === 1 ? '获得' : '消费' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="积分来源">
            {{ currentRecord.source }}
          </el-descriptions-item>
          <el-descriptions-item label="积分余额">
            <span class="balance-text">{{ currentRecord.balance }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentRecord.createdTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentRecord.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  pointsType: null,
  source: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取积分记录列表
const fetchData = async () => {
  try {
    loading.value = true
    
    const params = {
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      pointsType: searchForm.pointsType || undefined,
      source: searchForm.source || undefined
    }
    
    const response = await adminApi.getPointsRecordsPage(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取积分记录失败')
    }
  } catch (error) {
    console.error('获取积分记录失败:', error)
    ElMessage.error('获取积分记录失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.pointsType = null
  searchForm.source = ''
  pagination.current = 1
  fetchData()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchData()
}

// 当前页改变
const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.points-record-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.search-form-inline {
  margin: 0;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-content {
  padding: 10px 0;
}

.points-earn {
  color: #67c23a;
  font-weight: 600;
}

.points-spend {
  color: #e6a23c;
  font-weight: 600;
}

.balance-text {
  color: #409eff;
  font-weight: 600;
}

.text-muted {
  color: #c0c4cc;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #303133;
}
</style>
