<template>
  <div class="organizer-statistics">
    <div class="page-header">
      <h1>组织者统计</h1>
      <p>查看和分析各组织者的活动组织情况</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
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
        <el-table-column prop="organizerName" label="组织者" min-width="150" />
        <el-table-column prop="totalActivities" label="活动总数" width="100" align="center" />
        <el-table-column prop="completedActivities" label="已完成" width="100" align="center" />
        <el-table-column prop="ongoingActivities" label="进行中" width="100" align="center" />
        <el-table-column prop="totalRegistrations" label="总报名数" width="100" align="center" />
        <el-table-column prop="checkinCount" label="已签到" width="100" align="center" />
        <el-table-column prop="totalServiceHours" label="总时长" width="100" align="center" />
        <el-table-column prop="uniqueVolunteers" label="志愿者数" width="100" align="center" />
        <el-table-column prop="avgCompletionRate" label="平均完成率" width="120" align="center">
          <template #default="{ row }">
            <span :style="{ color: getCompletionRateColor(row.avgCompletionRate) }">
              {{ row.avgCompletionRate }}%
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
    const response = await statisticsApi.getOrganizerStatistics({
      ...filterForm.value
    })
    
    if (response.code === 200) {
      tableData.value = response.data || []
      total.value = tableData.value.length
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载组织者统计数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.value = {
    year: new Date().getFullYear().toString()
  }
  loadData()
}

// 导出数据
const exportData = async () => {
  try {
    await statisticsApi.exportOrganizerStatistics(filterForm.value)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 获取完成率颜色
const getCompletionRateColor = (rate: number) => {
  if (rate >= 80) return '#67C23A'
  if (rate >= 60) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.organizer-statistics {
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

