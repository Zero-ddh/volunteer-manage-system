<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { serviceApi } from '@/api'
import { ElMessage } from 'element-plus'

// 积分记录类型定义
interface PointsRecord {
  id: number
  userId: number
  points: number
  pointsType: number  // 1-获得，2-消费
  source: string
  description?: string
  balance: number
  // serviceId?: number  // 服务记录ID（可选）- 已屏蔽
  createdTime: string
}

// 分页信息类型定义
interface PaginationInfo {
  pageNo: number
  pageSize: number
  total: number
}

const loading = ref(false)
const points = ref<PointsRecord[]>([])
const pagination = reactive<PaginationInfo>({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 搜索条件
const searchForm = reactive({
  pointsType: '' as string | number
})

// 积分类型选项
const pointsTypeOptions = [
  { label: '全部', value: '' },
  { label: '获得', value: 1 },
  { label: '消费', value: 2 }
]

// 获取积分类型文本
const getPointsTypeText = (type: number) => {
  return type === 1 ? '获得' : '消费'
}

// 获取积分类型标签样式
const getPointsTypeTag = (type: number) => {
  return type === 1 ? 'success' : 'warning'
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化积分显示
const formatPoints = (points: number, type: number) => {
  const prefix = type === 1 ? '+' : '-'
  return `${prefix}${points}`
}

// 获取积分记录数据
async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize
    }
    
    // 添加积分类型搜索条件
    if (searchForm.pointsType !== '') {
      params.pointsType = searchForm.pointsType
    }
    
    const res = await serviceApi.getPointsRecords(params)
    console.log('积分记录API响应:', res) // 添加调试日志
    
    if (res && res.records) {
      points.value = res.records
      pagination.total = res.total || 0
    } else if (Array.isArray(res)) {
      points.value = res
      pagination.total = res.length
    } else {
      points.value = []
      pagination.total = 0
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
  pagination.pageNo = 1 // 重置到第一页
  fetchData()
}

// 重置搜索
const handleReset = () => {
  searchForm.pointsType = ''
  pagination.pageNo = 1
  fetchData()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.pageNo = page
  fetchData()
}

// 页面大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNo = 1
  fetchData()
}

// 查看服务记录功能已屏蔽
// const viewServiceRecord = (serviceId: number) => {
//   // 跳转到服务记录页面，可以通过路由参数传递serviceId
//   // 这里可以根据实际需求实现跳转逻辑
//   console.log('查看服务记录:', serviceId)
//   ElMessage.info(`查看服务记录ID: ${serviceId}`)
// }

onMounted(fetchData)
</script>

<template>
  <div class="points-page">
    <div class="page-header">
      <h2>积分记录</h2>
      <p>查看您的积分获得和消费记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="积分类型">
          <el-select v-model="searchForm.pointsType" placeholder="请选择积分类型" clearable style="width: 160px">
            <el-option
              v-for="option in pointsTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 积分记录表格 -->
    <el-table :data="points" v-loading="loading" stripe>
      <el-table-column prop="pointsType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getPointsTypeTag(row.pointsType)" size="small">
            {{ getPointsTypeText(row.pointsType) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="points" label="积分变动" width="120">
        <template #default="{ row }">
          <span :class="row.pointsType === 1 ? 'points-earn' : 'points-spend'">
            {{ formatPoints(row.points, row.pointsType) }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column prop="source" label="来源" min-width="150" />
      
      <!-- 屏蔽服务记录字段 -->
      <!-- <el-table-column prop="serviceId" label="服务记录" width="120">
        <template #default="{ row }">
          <el-link v-if="row.serviceId" type="primary" @click="viewServiceRecord(row.serviceId)">
            {{ row.serviceId }}
          </el-link>
          <span v-else style="color: #c0c4cc;">-</span>
        </template>
      </el-table-column> -->
      
      <el-table-column prop="description" label="描述" min-width="200">
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column prop="balance" label="余额" width="120">
        <template #default="{ row }">
          <span class="balance">{{ row.balance }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="createdTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNo"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 如果没有数据 -->
    <div v-if="!loading && points.length === 0" class="empty-state">
      <el-empty description="暂无积分记录" />
    </div>
  </div>
</template>

<style scoped>
.points-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.points-earn {
  color: #67c23a;
  font-weight: bold;
}

.points-spend {
  color: #e6a23c;
  font-weight: bold;
}

.balance {
  color: #409eff;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.empty-state {
  margin-top: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .points-page {
    padding: 16px;
  }
  
  .page-header h2 {
    font-size: 20px;
  }
}
</style>


