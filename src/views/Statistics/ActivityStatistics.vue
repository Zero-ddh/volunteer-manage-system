<template>
  <div class="activity-statistics">
    <div class="page-header">
      <h1>活动统计</h1>
      <p>查看和分析活动的报名、签到、完成率等数据</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="活动标题">
          <el-input v-model="filterForm.activityTitle" placeholder="请输入活动标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="年份">
          <el-date-picker
            v-model="filterForm.year"
            type="year"
            placeholder="选择年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="success" @click="exportData">导出数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="activityId" label="活动ID" width="100" align="center" />
        <el-table-column prop="activityTitle" label="活动标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="totalRegistrations" label="总报名" width="100" align="center" />
        <el-table-column prop="approvedRegistrations" label="已通过" width="100" align="center" />
        <el-table-column prop="checkinCount" label="已签到" width="100" align="center" />
        <el-table-column prop="completionRate" label="完成率" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: getCompletionRateColor(row.completionRate) }">
              {{ row.completionRate }}%
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { statisticsApi } from '@/api'

// 筛选表单
const filterForm = ref({
  activityTitle: '',
  year: new Date().getFullYear().toString()
})

// 列表数据
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await statisticsApi.getActivityStatistics({
      ...filterForm.value,
      pageNo: page.value,
      pageSize: pageSize.value
    })
    
    if (response.code === 200) {
      tableData.value = response.data?.records || response.data || []
      total.value = response.data?.total || tableData.value.length
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载活动统计数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.value = {
    activityTitle: '',
    year: new Date().getFullYear().toString()
  }
  loadData()
}

// 获取完成率颜色
const getCompletionRateColor = (rate: number) => {
  if (rate >= 80) return '#67C23A'
  if (rate >= 60) return '#E6A23C'
  return '#F56C6C'
}

// 导出数据
const exportData = async () => {
  try {
    const response = await statisticsApi.exportActivityStatistics({
      activityTitle: filterForm.value.activityTitle,
      year: filterForm.value.year
    })
    
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `活动统计_${filterForm.value.year}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.activity-statistics {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card .pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

