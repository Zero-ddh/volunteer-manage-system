<template>
  <el-card>
    <BaseTable :data="list" :total="total" :current-page="pageNo" :page-size="pageSize" @current-change="(p:number)=>{pageNo=p;load()}">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="activityId" label="活动ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
      <template #actions="{ row }">
        <el-dropdown @command="(cmd:string)=>review(row, cmd)">
          <el-button link type="primary">审核</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="approve">通过</el-dropdown-item>
              <el-dropdown-item command="reject">拒绝</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </BaseTable>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseTable from '@/components/Table/BaseTable.vue'
import request from '@/utils/request'

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])

const load = async () => {
  // 示例：固定查看activityId=1的报名列表，可按需改造为筛选
  const res: any = await request.get('/v1/activities/1/registrations', { params: { pageNo: pageNo.value, pageSize: pageSize.value } })
  list.value = res.records || res.list || []
  total.value = res.total || 0
}

const review = async (row: any, cmd: string) => {
  // 审核接口待后端实现，这里先占位
  // await request.post(`/v1/admin/registrations/${row.id}/review`, { action: cmd })
}

onMounted(load)
</script>


