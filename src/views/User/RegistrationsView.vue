<template>
  <el-card>
    <div class="registrations-page">
      <h2>我的活动</h2>
      <BaseTable :data="list" :total="total" :current-page="pageNo" :page-size="pageSize" @current-change="(p:number)=>{pageNo=p;load()}" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityId" label="活动信息" min-width="300">
          <template #default="{ row }">
            <div class="activity-info">
              <div class="activity-title">{{ row.activityTitle || `活动ID: ${row.activityId}` }}</div>
              <div class="activity-location" v-if="row.activityLocation">
                <el-icon><Location /></el-icon>
                {{ row.activityLocation }}
              </div>
              <div v-else-if="row.activityId" class="activity-id">
                活动ID: {{ row.activityId }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registrationTime" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="viewActivity(row.activityId)">查看活动</el-button>
              <el-popconfirm 
                title="确定取消报名吗？" 
                @confirm="onCancel(row.activityId)" 
                v-if="row.status === 1"
                width="300"
                confirm-button-text="确定"
                cancel-button-text="取消"
                confirm-button-type="danger"
              >
                <template #reference>
                  <el-button size="small" type="danger">取消报名</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </BaseTable>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseTable from '@/components/Table/BaseTable.vue'
import { activityApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'

const router = useRouter()
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const list = ref<any[]>([])

const load = async () => {
  loading.value = true
  try {
    const res: any = await activityApi.getMyRegistrationsWithDetails({ 
      pageNo: pageNo.value, 
      pageSize: pageSize.value 
    })
    list.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载报名记录失败:', error)
    ElMessage.error('加载报名记录失败')
  } finally {
    loading.value = false
  }
}

const onCancel = async (activityId: number) => {
  try {
    await activityApi.cancelRegistration(activityId)
    ElMessage.success('已取消报名')
    await load()
  } catch (error) {
    console.error('取消报名失败:', error)
    ElMessage.error('取消报名失败')
  }
}

const viewActivity = (activityId: number) => {
  router.push(`/activities/${activityId}`)
}

const statusText = (s: number) => ({ 1: '待审核', 2: '已通过', 3: '已拒绝', 4: '已取消' } as any)[s] || '未知'
const statusType = (s: number) => ({ 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' } as any)[s] || 'info'

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(load)
</script>

<style scoped>
.registrations-page { max-width: 1200px; margin: 20px auto; }

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-title {
  font-weight: 600;
  color: #303133;
}

.activity-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.activity-id {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.text-gray {
  color: #909399;
}
</style>


