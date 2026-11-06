<template>
  <div class="volunteer-ranking">
    <div class="page-header">
      <h1>志愿者服务时长排名</h1>
      <p>按服务时长查看志愿者排名（统计口径：剔除服务时长为0的志愿者）</p>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="年度">
          <el-date-picker
            v-model="filterForm.year"
            type="year"
            placeholder="选择年度"
            format="YYYY"
            value-format="YYYY"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filterForm.realName" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filterForm.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="success" @click="exportData">导出数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排名列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="ranking" label="排名" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="totalServiceHours" label="总服务时长" width="130" align="center" />
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
  year: new Date().getFullYear().toString(),
  username: '',
  realName: '',
  phone: ''
})

// 列表数据
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await statisticsApi.getVolunteerRanking({
      year: filterForm.value.year,
      username: filterForm.value.username,
      realName: filterForm.value.realName,
      phone: filterForm.value.phone,
      pageNo: page.value,
      pageSize: pageSize.value
    })
    
    if (response.code === 200) {
      tableData.value = response.data?.records || []
      total.value = response.data?.total || tableData.value.length
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载志愿者排名失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.value = {
    year: new Date().getFullYear().toString(),
    username: '',
    realName: '',
    phone: ''
  }
  loadData()
}

// 导出数据
const exportData = async () => {
  try {
    const response = await statisticsApi.exportVolunteerRanking({
      username: filterForm.value.username,
      realName: filterForm.value.realName,
      phone: filterForm.value.phone,
      year: filterForm.value.year
    })
    
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `志愿者排名_${filterForm.value.year}.xlsx`
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
.volunteer-ranking {
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

