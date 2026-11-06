<template>
  <div class="insurance-order-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>保险订单管理</h2>
      <p>查看和管理保险订单的支付状态、订单状态等信息</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalOrders || 0 }}</div>
              <div class="stat-label">总订单数</div>
            </div>
            <el-icon class="stat-icon"><Document /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingOrders || 0 }}</div>
              <div class="stat-label">待支付</div>
            </div>
            <el-icon class="stat-icon pending"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.paidOrders || 0 }}</div>
              <div class="stat-label">已支付</div>
            </div>
            <el-icon class="stat-icon success"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.todayOrders || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
            <el-icon class="stat-icon info"><Calendar /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号/产品">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入订单号或产品名称" 
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="请选择支付状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="支付失败" :value="3" />
            <el-option label="已退款" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择订单状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="待支付" :value="1" />
            <el-option label="已生效" :value="2" />
            <el-option label="已过期" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
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
    </el-card>

    <!-- 订单列表 -->
    <el-card class="table-card">
      <el-table :data="orderList" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="productName" label="保险产品" min-width="150" />
        <el-table-column prop="price" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getPaymentMethodType(row.paymentMethod)">
              {{ getPaymentMethodText(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.orderStatus)">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效时间" width="160">
          <template #default="{ row }">
            {{ row.effectiveTime ? formatDateTime(row.effectiveTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="160">
          <template #default="{ row }">
            {{ row.expireTime ? formatDateTime(row.expireTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
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
          :total="pagination.total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="订单详情" 
      width="800px"
    >
      <div v-if="orderDetail" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="保险产品">{{ orderDetail.productName }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ orderDetail.price }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(orderDetail.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(orderDetail.paymentStatus)">
              {{ getPaymentStatusText(orderDetail.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(orderDetail.orderStatus)">
              {{ getOrderStatusText(orderDetail.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生效时间">
            {{ orderDetail.effectiveTime ? formatDateTime(orderDetail.effectiveTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="过期时间">
            {{ orderDetail.expireTime ? formatDateTime(orderDetail.expireTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(orderDetail.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(orderDetail.updatedTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">用户信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户姓名">{{ orderDetail.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱地址">{{ orderDetail.userEmail }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ orderDetail.userIdCard }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document, Clock, Check, Calendar, View } from '@element-plus/icons-vue'
import { insuranceApi } from '@/api/index'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const orderDetail = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  paymentStatus: null as number | null,
  orderStatus: null as number | null
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 订单列表
const orderList = ref([])

// 统计数据
const statistics = ref({
  totalOrders: 0,
  pendingOrders: 0,
  paidOrders: 0,
  effectiveOrders: 0,
  expiredOrders: 0,
  todayOrders: 0
})

// 获取订单列表
const getOrderList = async () => {
  try {
    loading.value = true
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      paymentStatus: searchForm.paymentStatus || undefined,
      orderStatus: searchForm.orderStatus || undefined
    }
    
    const response = await insuranceApi.getOrderList(params)
    if (response.code === 200) {
      orderList.value = response.data.records
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    const response = await insuranceApi.getOrderStatistics()
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getOrderList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.paymentStatus = null
  searchForm.orderStatus = null
  pagination.pageNo = 1
  getOrderList()
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    const response = await insuranceApi.getOrderDetail(row.id)
    if (response.code === 200) {
      orderDetail.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 获取支付方式文本
const getPaymentMethodText = (method: string) => {
  switch (method) {
    case 'bankcard': return '银行卡'
    case 'alipay': return '支付宝'
    case 'wechat': return '微信支付'
    case 'mock': return '模拟支付'
    default: return '未知'
  }
}

// 获取支付方式类型
const getPaymentMethodType = (method: string) => {
  switch (method) {
    case 'bankcard': return 'primary'
    case 'alipay': return 'primary'
    case 'wechat': return 'success'
    case 'mock': return 'info'
    default: return ''
  }
}

// 获取支付状态文本
const getPaymentStatusText = (status: number) => {
  switch (status) {
    case 1: return '待支付'
    case 2: return '已支付'
    case 3: return '支付失败'
    case 4: return '已退款'
    default: return '未知'
  }
}

// 获取支付状态类型
const getPaymentStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'danger'
    case 4: return 'info'
    default: return ''
  }
}

// 获取订单状态文本
const getOrderStatusText = (status: number) => {
  switch (status) {
    case 1: return '待支付'
    case 2: return '已生效'
    case 3: return '已过期'
    case 4: return '已取消'
    default: return '未知'
  }
}

// 获取订单状态类型
const getOrderStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'info'
    case 4: return 'danger'
    default: return ''
  }
}

// 分页相关
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.pageNo = 1
  getOrderList()
}

const handleCurrentChange = (val: number) => {
  pagination.pageNo = val
  getOrderList()
}

// 页面加载时获取数据
onMounted(() => {
  getOrderList()
  getStatistics()
})
</script>

<style scoped>
.insurance-order-list {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.3;
}

.stat-icon.pending {
  color: #E6A23C;
}

.stat-icon.success {
  color: #67C23A;
}

.stat-icon.info {
  color: #409EFF;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.price-text {
  color: #E6A23C;
  font-weight: 600;
}

.order-detail {
  padding: 10px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
