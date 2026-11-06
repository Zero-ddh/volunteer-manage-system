<template>
  <div class="points-exchange-management">
    <div class="page-header">
      <h2>积分兑换记录管理</h2>
      <div class="header-stats">
        <el-card class="stat-card">
          <div class="stat-item">
            <span class="stat-label">待发货</span>
            <span class="stat-value status-1">{{ stats.status1 || 0 }}</span>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-item">
            <span class="stat-label">已发货</span>
            <span class="stat-value status-2">{{ stats.status2 || 0 }}</span>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-item">
            <span class="stat-label">运输中</span>
            <span class="stat-value status-3">{{ stats.status3 || 0 }}</span>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-item">
            <span class="stat-label">已签收</span>
            <span class="stat-value status-4">{{ stats.status4 || 0 }}</span>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-item">
            <span class="stat-label">已完成</span>
            <span class="stat-value status-5">{{ stats.status5 || 0 }}</span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="运输中" :value="3" />
            <el-option label="已签收" :value="4" />
            <el-option label="已完成" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.goodsName"
            placeholder="搜索商品名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.userName"
            placeholder="搜索用户名"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户电话">
          <el-input
            v-model="searchForm.userPhone"
            placeholder="搜索用户电话"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单ID" width="80" align="center" />
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="userPhone" label="用户电话" width="120" />
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="pointsUsed" label="消耗积分" width="100" align="center">
          <template #default="{ row }">
            <span class="points-text">{{ row.pointsUsed }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeTime" label="兑换时间" width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column prop="receiverPhone" label="收货电话" width="120" />
        <el-table-column prop="receiverAddress" label="收货地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="shippingCompany" label="物流公司" width="120">
          <template #default="{ row }">
            <span v-if="row.shippingCompany">{{ row.shippingCompany }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="trackingNumber" label="快递单号" width="150">
          <template #default="{ row }">
            <span v-if="row.trackingNumber">{{ row.trackingNumber }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="shippingTime" label="发货时间" width="180">
          <template #default="{ row }">
            <span v-if="row.shippingTime">{{ formatDateTime(row.shippingTime) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="handleShipping(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status >= 2"
              type="success"
              size="small"
              @click="handleUpdateStatus(row)"
            >
              更新状态
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发货信息弹窗 -->
    <el-dialog
      v-model="shippingDialogVisible"
      title="填写发货信息"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="shippingFormRef"
        :model="shippingForm"
        :rules="shippingFormRules"
        label-width="100px"
      >
        <el-form-item label="物流公司" prop="shippingCompany">
          <el-select v-model="shippingForm.shippingCompany" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="韵达速递" value="韵达速递" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="邮政EMS" value="邮政EMS" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNumber">
          <el-input
            v-model="shippingForm.trackingNumber"
            placeholder="请输入快递单号"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="管理员备注">
          <el-input
            v-model="shippingForm.adminRemark"
            type="textarea"
            placeholder="可选，填写发货备注信息"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shippingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitShipping" :loading="submitting">
            确认发货
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 状态更新弹窗 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新订单状态"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="statusFormRef"
        :model="statusForm"
        :rules="statusFormRules"
        label-width="100px"
      >
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentRecord?.status)">
            {{ getStatusText(currentRecord?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="更新为" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择新状态" style="width: 100%">
            <el-option label="运输中" :value="3" />
            <el-option label="已签收" :value="4" />
            <el-option label="已完成" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理员备注">
          <el-input
            v-model="statusForm.adminRemark"
            type="textarea"
            placeholder="可选，填写状态更新备注"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusUpdate" :loading="submitting">
            确认更新
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="兑换记录详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentRecord.userName }}</el-descriptions-item>
          <el-descriptions-item label="用户电话">{{ currentRecord.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ currentRecord.goodsName }}</el-descriptions-item>
          <el-descriptions-item label="消耗积分">{{ currentRecord.pointsUsed }}</el-descriptions-item>
          <el-descriptions-item label="兑换时间">{{ formatDateTime(currentRecord.exchangeTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentRecord.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收货电话">{{ currentRecord.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentRecord.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="物流公司">{{ currentRecord.shippingCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ currentRecord.trackingNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentRecord.shippingTime ? formatDateTime(currentRecord.shippingTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">{{ currentRecord.deliveryTime ? formatDateTime(currentRecord.deliveryTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentRecord.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="管理员备注">{{ currentRecord.adminRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

// 类型定义
interface ExchangeRecord {
  id: number
  userId: number
  goodsId: number
  goodsName: string
  pointsUsed: number
  exchangeTime: string
  status: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  shippingCompany?: string
  trackingNumber?: string
  shippingTime?: string
  deliveryTime?: string
  remark?: string
  adminRemark?: string
  userName?: string
  userPhone?: string
}

interface Stats {
  status1: number
  status2: number
  status3: number
  status4: number
  status5: number
  total: number
}

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ExchangeRecord[]>([])
const stats = ref<Stats>({
  status1: 0,
  status2: 0,
  status3: 0,
  status4: 0,
  status5: 0,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  status: null as number | null,
  goodsName: '',
  userName: '',
  userPhone: ''
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const shippingDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref<ExchangeRecord | null>(null)

// 发货表单
const shippingForm = ref({
  shippingCompany: '',
  trackingNumber: '',
  adminRemark: ''
})

const shippingFormRules = {
  shippingCompany: [
    { required: true, message: '请选择物流公司', trigger: 'change' }
  ],
  trackingNumber: [
    { required: true, message: '请输入快递单号', trigger: 'blur' },
    { min: 5, max: 50, message: '快递单号长度在 5 到 50 个字符', trigger: 'blur' }
  ]
}

// 状态更新表单
const statusForm = ref({
  status: null as number | null,
  adminRemark: ''
})

const statusFormRules = {
  status: [
    { required: true, message: '请选择新状态', trigger: 'change' }
  ]
}

// 表单引用
const shippingFormRef = ref()
const statusFormRef = ref()

// 获取兑换记录列表
async function fetchExchangeRecords() {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      status: searchForm.status,
      goodsName: searchForm.goodsName,
      userName: searchForm.userName,
      userPhone: searchForm.userPhone
    }
    
    const res = await adminApi.getPointsExchangeRecords(params) as any
    if (res && res.data && res.data.records) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取兑换记录失败:', error)
    ElMessage.error('获取兑换记录失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 获取统计数据
async function fetchStats() {
  try {
    const res = await adminApi.getPointsExchangeStats() as any
    if (res && res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索
function handleSearch() {
  pagination.pageNo = 1
  fetchExchangeRecords()
}

// 重置搜索
function handleReset() {
  searchForm.status = null
  searchForm.goodsName = ''
  searchForm.userName = ''
  searchForm.userPhone = ''
  pagination.pageNo = 1
  fetchExchangeRecords()
}

// 分页处理
function handleSizeChange(val: number) {
  pagination.pageSize = val
  pagination.pageNo = 1
  fetchExchangeRecords()
}

function handleCurrentChange(val: number) {
  pagination.pageNo = val
  fetchExchangeRecords()
}

// 发货操作
function handleShipping(record: ExchangeRecord) {
  currentRecord.value = record
  shippingForm.value = {
    shippingCompany: '',
    trackingNumber: '',
    adminRemark: ''
  }
  shippingDialogVisible.value = true
}

// 提交发货信息
async function submitShipping() {
  if (!shippingFormRef.value) return
  
  try {
    await shippingFormRef.value.validate()
    
    if (!currentRecord.value) {
      ElMessage.error('记录信息丢失')
      return
    }
    
    submitting.value = true
    
    const res = await adminApi.updateShippingInfo(currentRecord.value.id, shippingForm.value) as any
    
    if (res && res.data === '发货信息更新成功') {
      ElMessage.success('发货成功！')
      shippingDialogVisible.value = false
      await Promise.all([fetchExchangeRecords(), fetchStats()])
    } else {
      ElMessage.error(res?.data || res?.message || '发货失败')
    }
  } catch (error: any) {
    if (error !== false) {
      console.error('发货失败:', error)
      ElMessage.error('发货失败')
    }
  } finally {
    submitting.value = false
  }
}

// 更新状态操作
function handleUpdateStatus(record: ExchangeRecord) {
  currentRecord.value = record
  statusForm.value = {
    status: null,
    adminRemark: ''
  }
  statusDialogVisible.value = true
}

// 提交状态更新
async function submitStatusUpdate() {
  if (!statusFormRef.value) return
  
  try {
    await statusFormRef.value.validate()
    
    if (!currentRecord.value) {
      ElMessage.error('记录信息丢失')
      return
    }
    
    submitting.value = true
    
    const res = await adminApi.updateOrderStatus(
      currentRecord.value.id,
      statusForm.value.status!,
      statusForm.value.adminRemark
    ) as any
    
    if (res && res.data === '订单状态更新成功') {
      ElMessage.success('状态更新成功！')
      statusDialogVisible.value = false
      await Promise.all([fetchExchangeRecords(), fetchStats()])
    } else {
      ElMessage.error(res?.data || res?.message || '状态更新失败')
    }
  } catch (error: any) {
    if (error !== false) {
      console.error('状态更新失败:', error)
      ElMessage.error('状态更新失败')
    }
  } finally {
    submitting.value = false
  }
}

// 查看详情
async function handleViewDetail(record: ExchangeRecord) {
  try {
    const res = await adminApi.getPointsExchangeDetail(record.id) as any
    if (res && res.data) {
      currentRecord.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error('获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 获取状态类型
function getStatusType(status: number) {
  switch (status) {
    case 1: return 'warning'  // 待发货
    case 2: return 'primary'   // 已发货
    case 3: return 'info'      // 运输中
    case 4: return 'success'   // 已签收
    case 5: return 'success'   // 已完成
    default: return 'info'
  }
}

// 获取状态文本
function getStatusText(status: number) {
  switch (status) {
    case 1: return '待发货'
    case 2: return '已发货'
    case 3: return '运输中'
    case 4: return '已签收'
    case 5: return '已完成'
    default: return '未知状态'
  }
}

// 格式化日期时间
function formatDateTime(dateTime: string) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  fetchExchangeRecords()
  fetchStats()
})
</script>

<style scoped>
.points-exchange-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 20px 0;
  color: #303133;
}

.header-stats {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 120px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
}

.status-1 { color: #e6a23c; }
.status-2 { color: #409eff; }
.status-3 { color: #909399; }
.status-4 { color: #67c23a; }
.status-5 { color: #67c23a; }

.search-card {
  margin-bottom: 20px;
}

.search-card .el-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.search-card .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.table-card {
  margin-bottom: 20px;
}

.points-text {
  color: #e6a23c;
  font-weight: bold;
}

.text-muted {
  color: #c0c4cc;
  font-style: italic;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

.detail-content {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-stats {
    flex-direction: column;
  }
  
  .stat-card {
    min-width: 100%;
  }
  
  .points-exchange-management {
    padding: 10px;
  }
}
</style>
