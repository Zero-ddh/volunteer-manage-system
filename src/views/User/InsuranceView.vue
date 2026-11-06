<template>
  <div class="insurance-page">
    <div class="page-header">
      <h2>志愿服务保险</h2>
      <p>为您的志愿服务提供安全保障</p>
    </div>

    <!-- 保险产品列表 -->
    <div class="products-section">
      <h3>保险产品</h3>
      <div class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-header">
            <h4>{{ product.name }}</h4>
            <div class="price">¥{{ product.price }}</div>
          </div>
          <div class="product-content">
            <p class="description">{{ product.description }}</p>
            <div class="coverage-info">
              <div class="coverage-item">
                <span class="label">保障金额：</span>
                <span class="value">¥{{ product.coverageAmount }}</span>
              </div>
              <div class="coverage-item">
                <span class="label">保障期限：</span>
                <span class="value">{{ product.coveragePeriod }}天</span>
              </div>
            </div>
          </div>
          <div class="product-footer">
            <el-button 
              type="primary" 
              @click="purchaseInsurance(product)"
              :loading="purchasing"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的保险订单 -->
    <div class="orders-section">
      <h3>我的保险订单</h3>
      
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input
              v-model="searchForm.orderNo"
              placeholder="请输入订单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="保险产品">
            <el-input
              v-model="searchForm.productName"
              placeholder="请输入保险产品名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 订单列表 -->
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="productName" label="保险产品" />
        <el-table-column prop="price" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.orderStatus)">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效时间" width="180">
          <template #default="{ row }">
            {{ row.effectiveTime ? formatTime(row.effectiveTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expireTime ? formatTime(row.expireTime) : '-' }}
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <el-dialog
      v-model="purchaseDialogVisible"
      title="确认购买保险"
      width="500px"
    >
      <div class="purchase-content">
        <div class="product-info">
          <h4>{{ selectedProduct?.name }}</h4>
          <p>{{ selectedProduct?.description }}</p>
          <div class="price-info">
            <span class="price-label">保险费用：</span>
            <span class="price-value">¥{{ selectedProduct?.price }}</span>
          </div>
          <div class="coverage-info">
            <div class="coverage-item">
              <span class="label">保障金额：</span>
              <span class="value">¥{{ selectedProduct?.coverageAmount }}</span>
            </div>
            <div class="coverage-item">
              <span class="label">保障期限：</span>
              <span class="value">{{ selectedProduct?.coveragePeriod }}天</span>
            </div>
          </div>
        </div>
        
        <div class="payment-method">
          <h5>支付方式</h5>
          <el-radio-group v-model="paymentMethod">
            <el-radio value="bankcard">银行卡</el-radio>
            <el-radio value="alipay">支付宝</el-radio>
            <el-radio value="wechat">微信支付</el-radio>
          </el-radio-group>
        </div>
        
        <div class="purchase-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>确认购买此保险产品吗？购买后立即生效。</span>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="purchaseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPurchase" :loading="purchasing">
            确认购买
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { getInsuranceProducts, createInsuranceOrder, mockPayment, getUserInsuranceOrders } from '@/api/insurance'

// 响应式数据
const loading = ref(false)
const purchasing = ref(false)
const products = ref<any[]>([])
const orders = ref<any[]>([])
const purchaseDialogVisible = ref(false)
const selectedProduct = ref<any>(null)
const paymentMethod = ref('bankcard')

// 搜索表单
const searchForm = ref({
  orderNo: '',
  productName: ''
})

// 分页数据
const pagination = ref({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadProducts(),
      loadOrders()
    ])
  } finally {
    loading.value = false
  }
}

// 加载保险产品
const loadProducts = async () => {
  try {
    const res = await getInsuranceProducts()
    products.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载保险产品失败:', error)
  }
}

// 加载用户订单
const loadOrders = async () => {
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.size,
      orderNo: searchForm.value.orderNo || undefined,
      productName: searchForm.value.productName || undefined
    }
    
    const res = await getUserInsuranceOrders(params)
    // 响应拦截器已经返回了data，不需要再访问.data属性
    if (res && res.records) {
      orders.value = res.records || []
      pagination.value.total = res.total || 0
      pagination.value.pages = res.pages || 0
    }
  } catch (error) {
    console.error('加载保险订单失败:', error)
    ElMessage.error('加载保险订单失败')
  }
}

// 购买保险
const purchaseInsurance = (product: any) => {
  selectedProduct.value = product
  purchaseDialogVisible.value = true
}

// 确认购买
const confirmPurchase = async () => {
  if (!selectedProduct.value) return
  
  purchasing.value = true
  try {
    // 1. 创建订单
    const orderRes = await createInsuranceOrder({
      productId: selectedProduct.value.id,
      paymentMethod: paymentMethod.value
    })
    
    // 响应拦截器已经返回了data，不需要再访问.data属性
    const order = orderRes
    ElMessage.success('订单创建成功')
    
    // 2. 模拟支付（所有支付方式都模拟成功）
    const paymentRes = await mockPayment({
      orderNo: order.orderNo,
      paymentMethod: paymentMethod.value
    })
    
    // 响应拦截器已经返回了data，不需要再访问.data属性
    if (paymentRes.success) {
      ElMessage.success('支付成功！保险已生效')
    } else {
      ElMessage.error('支付失败，请重试')
    }
    
    // 3. 刷新数据
    await loadOrders()
    purchaseDialogVisible.value = false
    
  } catch (error: any) {
    ElMessage.error(error.message || '购买失败')
  } finally {
    purchasing.value = false
  }
}

// 状态转换函数
const getPaymentStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '支付失败',
    4: '已退款'
  }
  return statusMap[status] || '未知'
}

const getPaymentStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

const getOrderStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已生效',
    3: '已过期',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const getOrderStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 搜索处理
const handleSearch = () => {
  pagination.value.current = 1
  loadOrders()
}

// 重置搜索
const handleReset = () => {
  searchForm.value.orderNo = ''
  searchForm.value.productName = ''
  pagination.value.current = 1
  loadOrders()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadOrders()
}

// 当前页改变
const handleCurrentChange = (current: number) => {
  pagination.value.current = current
  loadOrders()
}

onMounted(loadData)
</script>

<style scoped>
.insurance-page {
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
}

.products-section, .orders-section {
  margin-bottom: 40px;
}

.products-section h3, .orders-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
  transition: box-shadow 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-header h4 {
  margin: 0;
  color: #333;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.6;
}

.coverage-info {
  margin-bottom: 20px;
}

.coverage-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.coverage-item .label {
  color: #666;
}

.coverage-item .value {
  color: #333;
  font-weight: 500;
}

.purchase-content {
  padding: 20px 0;
}

.product-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.product-info p {
  color: #666;
  margin-bottom: 15px;
}

.price-info {
  margin-bottom: 15px;
}

.price-label {
  color: #666;
}

.price-value {
  font-size: 20px;
  font-weight: bold;
  color: #e6a23c;
}

.payment-method {
  margin-bottom: 20px;
}

.payment-method h5 {
  margin: 0 0 15px 0;
  color: #333;
}

.purchase-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  color: #1890ff;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

