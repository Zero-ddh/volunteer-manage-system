<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { recruitmentApi } from '@/api'

const loading = ref(false)
const list = ref<RecruitmentInfo[]>([])
const params = ref({ keyword: '', status: null }) // 搜索参数

// 状态选项
const statusOptions = ref([
  { label: '全部', value: null },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 }
])

// 类型定义
interface RecruitmentInfo {
  id: number
  title: string
  recruitCount: number
  status: number
  startTime: string
  endTime: string
}

async function fetchList() {
  loading.value = true
  try {
    if (params.value.status === null) {
      // 如果选择"全部"，分别获取进行中(2)和已结束(3)状态的数据
      const [res1, res2] = await Promise.all([
        recruitmentApi.getList({ ...params.value, status: 2 }),
        recruitmentApi.getList({ ...params.value, status: 3 })
      ])
      
      const page1 = res1?.data ?? res1
      const page2 = res2?.data ?? res2
      
      const list1 = page1?.records ?? page1 ?? []
      const list2 = page2?.records ?? page2 ?? []
      
      // 合并两个列表
      list.value = [...list1, ...list2]
    } else {
      // 如果选择了特定状态，只获取该状态的数据
      const res = await recruitmentApi.getList(params.value)
      const page = res?.data ?? res
      list.value = page?.records ?? page ?? []
    }
  } finally {
    loading.value = false
  }
}

// 状态转换函数
function getStatusText(status: number) {
  switch (status) {
    case 1: return '未开始'
    case 2: return '进行中'
    case 3: return '已结束'
    default: return '未知'
  }
}

// 状态类型函数
function getStatusType(status: number) {
  switch (status) {
    case 1: return 'info'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="recruitment-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-content">
        <h1 class="page-title">招募信息</h1>
        <p class="page-subtitle">发现更多招募机会，展现您的专业技能，为社会贡献自己的力量</p>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <el-card class="search-card mb-16">
      <div class="toolbar">
        <el-input v-model="params.keyword" placeholder="搜索招募标题" style="width:240px;margin-right:12px;" />
        <el-select v-model="params.status" placeholder="选择状态" style="width:120px;margin-right:12px;" clearable>
          <el-option 
            v-for="option in statusOptions" 
            :key="option.value" 
            :label="option.label" 
            :value="option.value" 
          />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>
    </el-card>

    <!-- 招募列表 -->
    <el-card class="recruitment-card">
      <div class="section-header">
        <div class="section-title">招募列表</div>
      </div>
      
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="recruitCount" label="招募人数" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <router-link :to="`/recruitment/${row.id}`">
              <el-button type="primary" text>查看详情</el-button>
            </router-link>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.recruitment-list {
  padding: 20px;
}

.page-header { 
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: #fff;
  padding: 60px 0;
  text-align: center;
  margin-bottom: 20px;
  /* 全宽铺满屏幕两侧（突破父级容器宽度限制） */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}

.page-header-content { 
  max-width: 800px; 
  margin: 0 auto; 
  padding: 0 20px; 
}

.page-title { 
  font-size: 32px; 
  font-weight: 700; 
  color: #fff; 
  margin: 0 0 12px 0; 
}

.page-subtitle { 
  color: #fff; 
  opacity: 0.95; 
  font-size: 16px; 
}

.search-card {
  border-radius: 10px;
  margin-bottom: 16px;
}

.toolbar { 
  display: flex;
  align-items: center;
  gap: 12px;
}

.recruitment-card {
  border-radius: 10px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
}

.section-title {
  font-weight: 700;
  color: #333;
}

.mb-16 {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .page-header {
    padding: 40px 0;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar .el-input {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 12px;
  }
}
</style>


