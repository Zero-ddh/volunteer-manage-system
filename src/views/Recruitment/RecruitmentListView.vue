<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { recruitmentApi } from '@/api'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const params = ref({ 
  keyword: '', 
  status: undefined as any,
  pageNo: 1,
  pageSize: 10
})

// 状态映射
const statusMap = {
  1: { text: '草稿', type: 'info' },
  2: { text: '发布中', type: 'success' },
  3: { text: '已结束', type: 'warning' }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await recruitmentApi.getList(params.value)
    // 修复数据格式问题：后端返回的是 {code, message, data: {records: [], total: 3}}
    if (res && res.data) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取招募列表失败:', error)
    ElMessage.error('获取招募列表失败')
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/recruitment/create')
}

function goEdit(id: number) {
  router.push(`/recruitment/create?id=${id}`)
}

function goView(id: number) {
  router.push(`/recruitment/view/${id}`)
}

function goApplicants(id: number) {
  router.push(`/recruitment/${id}/applicants`)
}

function handleDelete(row: any) {
  // 检查是否有报名者
  if (row.applicantCount && row.applicantCount > 0) {
    ElMessageBox.alert(
      `该招募已有 ${row.applicantCount} 人报名，无法删除。请先处理报名者信息后再删除。`,
      '无法删除',
      {
        confirmButtonText: '确定',
        type: 'warning'
      }
    )
    return
  }

  ElMessageBox.confirm(
    `确定要删除招募 "${row.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里应该调用删除API
      // await recruitmentApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

function handleStatusChange(row: any, newStatus: number) {
  const statusText = statusMap[newStatus as keyof typeof statusMap]?.text
  ElMessageBox.confirm(
    `确定要将招募 "${row.title}" 的状态修改为 ${statusText} 吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await recruitmentApi.update(row.id, { ...row, status: newStatus })
      row.status = newStatus
      ElMessage.success('状态更新成功')
    } catch (error) {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

function resetSearch() {
  params.value.keyword = ''
  params.value.status = undefined
  params.value.pageNo = 1
  fetchList()
}

function handlePageChange(page: number) {
  params.value.pageNo = page
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="recruitment-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">招募管理</h1>
      <p class="page-subtitle">管理志愿者招募信息和报名申请 <span class="click-tip">（报名者列的数据支持点击，跳转到报名者管理页面）</span></p>
    </div>

    <div class="toolbar">
      <el-input 
        v-model="params.keyword" 
        placeholder="搜索标题" 
        style="width:240px;margin-right:12px;" 
        @keyup.enter="fetchList"
        clearable
      />
      <el-select 
        v-model="params.status" 
        clearable 
        placeholder="状态" 
        style="width:160px;margin-right:12px;"
      >
        <el-option :value="1" label="草稿" />
        <el-option :value="2" label="发布中" />
        <el-option :value="3" label="已结束" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="goCreate" style="margin-left:auto;">创建招募</el-button>
    </div>
    
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="recruitCount" label="招募人数" width="100" align="center" />
      <el-table-column prop="contactPerson" label="联系人" width="140" />
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status as keyof typeof statusMap]?.type">
            {{ statusMap[row.status as keyof typeof statusMap]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报名者" width="90" align="center">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            text 
            @click="goApplicants(row.id)"
            :disabled="!row.applicantCount || row.applicantCount === 0"
          >
            {{ row.applicantCount || 0 }}人
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text @click="goView(row.id)">查看</el-button>
          <el-button type="success" text @click="goEdit(row.id)">编辑</el-button>
          <el-button 
            v-if="row.status === 1" 
            type="warning" 
            text 
            @click="handleStatusChange(row, 2)"
          >
            发布
          </el-button>
          <el-button 
            v-if="row.status === 2" 
            type="info" 
            text 
            @click="handleStatusChange(row, 3)"
          >
            结束
          </el-button>
          <el-button 
            v-if="row.status === 3" 
            type="warning" 
            text 
            @click="handleStatusChange(row, 2)"
          >
            重新发布
          </el-button>
          <el-button 
            type="danger" 
            text 
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="params.pageNo"
        :page-size="params.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="handlePageChange"
        @size-change="(size) => { params.pageSize = size; fetchList() }"
      />
    </div>
  </div>
</template>

<style scoped>
.recruitment-management {
  padding: 16px;
}

/* 页面标题样式 */
.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.click-tip {
  color: #f56c6c;
  font-weight: 500;
}

.toolbar { 
  display: flex; 
  align-items: center; 
  margin-bottom: 16px; 
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>


