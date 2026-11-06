<template>
  <div class="points-mall">
    <div class="mall-header">
      <div class="header-left">
        <h2>积分商城</h2>
      </div>
      <div class="header-center">
        <div class="user-points">
          <span class="points-label">我的积分：</span>
          <span class="points-value">{{ userPoints }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showExchangeRecords" :icon="Document">
          我的兑换记录
        </el-button>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="goods-grid">
      <div v-for="goods in goodsList" :key="goods.id" class="goods-card" @click="showGoodsDetail(goods)">
        <div class="goods-image">
          <div v-if="getGoodsImages(goods).length > 0" class="image-carousel">
            <!-- 图片轮播区域 -->
            <div class="carousel-container" :style="{ transform: `translateX(-${getCurrentImageIndex(goods.id) * 100}%)` }">
              <div 
                v-for="(image, index) in getGoodsImages(goods)" 
                :key="index" 
                class="carousel-item"
              >
                <img :src="image" :alt="goods.name" />
              </div>
            </div>
            
            <!-- 左右切换按钮 -->
            <div v-if="getGoodsImages(goods).length > 1" class="carousel-controls">
              <button 
                class="carousel-btn prev" 
                @click.stop="prevImage(goods.id)"
                :disabled="getCurrentImageIndex(goods.id) === 0"
              >
                ‹
              </button>
              <button 
                class="carousel-btn next" 
                @click.stop="nextImage(goods.id)"
                :disabled="getCurrentImageIndex(goods.id) === getGoodsImages(goods).length - 1"
              >
                ›
              </button>
            </div>
            
            <!-- 图片指示器 -->
            <div v-if="getGoodsImages(goods).length > 1" class="carousel-indicators">
              <span 
                v-for="(image, index) in getGoodsImages(goods)" 
                :key="index"
                class="indicator"
                :class="{ active: getCurrentImageIndex(goods.id) === index }"
                @click.stop="setCurrentImage(goods.id, index)"
              ></span>
            </div>
          </div>
          
          <!-- 无图片提示 -->
          <div v-else class="no-image">
            <span>暂无图片</span>
          </div>
        </div>
        <div class="goods-info">
          <h3 class="goods-name">{{ goods.name }}</h3>
          <p class="goods-description">{{ goods.description }}</p>
          <div class="goods-meta">
            <span class="goods-category">{{ goods.category }}</span>
            <span class="goods-stock">库存：{{ goods.stock }}</span>
          </div>
          <div class="goods-footer">
            <span class="points-required">{{ goods.pointsRequired }} 积分</span>
            <el-button 
              type="primary" 
              @click.stop="exchangeGoods(goods)"
              :disabled="userPoints < goods.pointsRequired || goods.stock <= 0"
              :loading="exchanging"
            >
              {{ goods.stock <= 0 ? '缺货' : '兑换' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="selectedGoods?.name"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedGoods" class="goods-detail">
        <!-- 商品图片轮播 -->
        <div class="detail-image-section">
          <div class="detail-image-carousel">
            <div class="detail-carousel-container" :style="{ transform: `translateX(-${detailCurrentImageIndex * 100}%)` }">
              <div 
                v-for="(image, index) in getGoodsImages(selectedGoods)" 
                :key="index" 
                class="detail-carousel-item"
              >
                <img :src="image" :alt="selectedGoods.name" />
              </div>
            </div>
            
            <!-- 左右切换按钮 -->
            <div v-if="getGoodsImages(selectedGoods).length > 1" class="detail-carousel-controls">
              <button 
                class="detail-carousel-btn prev" 
                @click="prevDetailImage"
                :disabled="detailCurrentImageIndex === 0"
              >
                ‹
              </button>
              <button 
                class="detail-carousel-btn next" 
                @click="nextDetailImage"
                :disabled="detailCurrentImageIndex === getGoodsImages(selectedGoods).length - 1"
              >
                ›
              </button>
            </div>
            
            <!-- 图片指示器 -->
            <div v-if="getGoodsImages(selectedGoods).length > 1" class="detail-carousel-indicators">
              <span 
                v-for="(image, index) in getGoodsImages(selectedGoods)" 
                :key="index"
                class="detail-indicator"
                :class="{ active: detailCurrentImageIndex === index }"
                @click="setDetailCurrentImage(index)"
              ></span>
            </div>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="detail-info-section">
          <h2 class="detail-goods-name">{{ selectedGoods.name }}</h2>
          <p class="detail-goods-description">{{ selectedGoods.description }}</p>
          
          <div class="detail-goods-meta">
            <div class="meta-item">
              <span class="meta-label">分类：</span>
              <span class="meta-value">{{ selectedGoods.category }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">库存：</span>
              <span class="meta-value">{{ selectedGoods.stock }} 件</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">所需积分：</span>
              <span class="meta-value points">{{ selectedGoods.pointsRequired }} 积分</span>
            </div>
          </div>
          
          <div class="detail-actions">
            <el-button 
              type="primary" 
              size="large"
              @click="exchangeGoods(selectedGoods)"
              :disabled="userPoints < selectedGoods.pointsRequired || selectedGoods.stock <= 0"
              :loading="exchanging"
            >
              {{ selectedGoods.stock <= 0 ? '缺货' : `立即兑换 (${selectedGoods.pointsRequired} 积分)` }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 兑换记录弹窗 -->
    <el-dialog
      v-model="exchangeRecordsVisible"
      title="我的兑换记录"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="exchange-records-content">
        <el-table :data="exchangeRecords" v-loading="recordsLoading" stripe>
          <el-table-column prop="goodsName" label="商品名称" min-width="150" />
          <el-table-column prop="pointsUsed" label="消耗积分" width="120" align="center">
            <template #default="{ row }">
              <span class="points-used">{{ row.pointsUsed }} 积分</span>
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
          <el-table-column prop="trackingNumber" label="快递单号" width="150">
            <template #default="{ row }">
              <span v-if="row.trackingNumber">{{ row.trackingNumber }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 2 || row.status === 3" 
                type="primary" 
                size="small" 
                @click="confirmReceipt(row.id)"
                :loading="confirmingReceipt === row.id"
              >
                确认收货
              </el-button>
              <span v-else-if="row.status === 4" class="text-success">已签收</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalRecords"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 收货信息填写弹窗 -->
    <el-dialog
      v-model="receiverFormVisible"
      title="填写收货信息"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div v-if="currentExchangeGoods" class="receiver-form-content">
        <!-- 商品信息展示 -->
        <div class="goods-summary">
          <h4>兑换商品：{{ currentExchangeGoods.name }}</h4>
          <p>所需积分：<span class="points-highlight">{{ currentExchangeGoods.pointsRequired }}</span> 积分</p>
        </div>
        
        <!-- 收货信息表单 -->
        <el-form
          ref="receiverFormRef"
          :model="receiverForm"
          :rules="receiverFormRules"
          label-width="100px"
          class="receiver-form"
        >
          <el-form-item label="收货人姓名" prop="receiverName">
            <el-input
              v-model="receiverForm.receiverName"
              placeholder="请输入收货人姓名"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="收货人电话" prop="receiverPhone">
            <el-input
              v-model="receiverForm.receiverPhone"
              placeholder="请输入收货人手机号码"
              maxlength="11"
            />
          </el-form-item>
          
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input
              v-model="receiverForm.receiverAddress"
              type="textarea"
              placeholder="请输入详细的收货地址，包括省市区和具体地址"
              :rows="3"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelReceiverForm">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitReceiverInfo"
            :loading="exchanging"
          >
            确认兑换
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { goodsApi } from '@/api/goods'
import { serviceApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

// 类型定义
interface Goods {
  id: number
  name: string
  description: string
  pointsRequired: number
  stock: number
  imageUrl: string
  category: string
}

interface ExchangeRecord {
  id: number
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
}

// 响应式数据
const loading = ref(false)
const exchanging = ref(false)
const goodsList = ref<Goods[]>([])
const exchangeRecords = ref<ExchangeRecord[]>([])
const userPoints = ref(0)

// 兑换记录弹窗相关数据
const exchangeRecordsVisible = ref(false)
const recordsLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)
const confirmingReceipt = ref<number | null>(null)

// 轮播图相关数据
const goodsImageIndices = ref<Map<number, number>>(new Map())

// 详情弹窗相关数据
const detailDialogVisible = ref(false)
const selectedGoods = ref<Goods | null>(null)
const detailCurrentImageIndex = ref(0)

// 收货信息表单相关数据
const receiverFormVisible = ref(false)
const receiverForm = ref({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: ''
})
const receiverFormRules = ref({
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入收货人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 10, max: 200, message: '地址长度在 10 到 200 个字符', trigger: 'blur' }
  ]
})
const receiverFormRef = ref()
const currentExchangeGoods = ref<Goods | null>(null)

// 获取商品列表
async function fetchGoodsList() {
  loading.value = true
  try {
    const res = await goodsApi.getGoodsList() as any
    console.log('商品列表响应:', res)
    if (res) {
      goodsList.value = res
    } else {
      goodsList.value = []
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
    goodsList.value = []
  } finally {
    loading.value = false
  }
}

// 获取用户积分
async function fetchUserPoints() {
  try {
    const res = await goodsApi.getUserTotalPoints() as any
    console.log('用户积分响应:', res)
    if (res !== undefined) {
      userPoints.value = res
    } else {
      userPoints.value = 0
    }
  } catch (error) {
    console.error('获取用户积分失败:', error)
    userPoints.value = 0
  }
}

// 获取兑换记录（支持分页）
async function fetchExchangeRecords() {
  recordsLoading.value = true
  try {
    const res = await goodsApi.getExchangeRecords({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    console.log('兑换记录响应:', res)
    if (res && res.records) {
      exchangeRecords.value = res.records
      totalRecords.value = res.total || 0
    } else if (res && Array.isArray(res)) {
      // 兼容旧版本API，直接返回数组
      exchangeRecords.value = res
      totalRecords.value = res.length
    } else {
      exchangeRecords.value = []
      totalRecords.value = 0
    }
  } catch (error) {
    console.error('获取兑换记录失败:', error)
    ElMessage.error('获取兑换记录失败')
    exchangeRecords.value = []
    totalRecords.value = 0
  } finally {
    recordsLoading.value = false
  }
}

// 显示兑换记录弹窗
function showExchangeRecords() {
  exchangeRecordsVisible.value = true
  currentPage.value = 1
  fetchExchangeRecords()
}

// 分页处理
function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  fetchExchangeRecords()
}

function handleCurrentChange(val: number) {
  currentPage.value = val
  fetchExchangeRecords()
}

// 确认收货
async function confirmReceipt(exchangeId: number) {
  try {
    await ElMessageBox.confirm(
      '确定要确认收货吗？确认后将无法撤销。',
      '确认收货',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    confirmingReceipt.value = exchangeId
    console.log('开始确认收货:', exchangeId)
    
    const res = await goodsApi.confirmReceipt(exchangeId) as any
    console.log('确认收货响应:', res)
    
    // 由于响应拦截器会返回data部分，成功时res就是"确认收货成功"字符串
    if (res === '确认收货成功' || (res && res.code === 200)) {
      console.log('确认收货成功，准备刷新列表')
      ElMessage.success('确认收货成功')
      // 刷新兑换记录列表
      await fetchExchangeRecords()
      console.log('列表刷新完成')
    } else {
      console.log('确认收货失败，响应:', res)
      ElMessage.error(typeof res === 'string' ? res : (res?.message || '确认收货失败'))
    }
  } catch (error: any) {
    console.error('确认收货异常:', error)
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  } finally {
    confirmingReceipt.value = null
  }
}

// 兑换商品
async function exchangeGoods(goods: Goods) {
  try {
    await ElMessageBox.confirm(
      `确定要用 ${goods.pointsRequired} 积分兑换 ${goods.name} 吗？`,
      '确认兑换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 设置当前要兑换的商品
    currentExchangeGoods.value = goods
    
    // 重置表单
    receiverForm.value = {
      receiverName: '',
      receiverPhone: '',
      receiverAddress: ''
    }
    
    // 显示收货信息填写表单
    receiverFormVisible.value = true
    
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('兑换商品失败:', error)
      ElMessage.error('兑换失败')
    }
  }
}

// 提交收货信息并完成兑换
async function submitReceiverInfo() {
  if (!receiverFormRef.value) return
  
  try {
    await receiverFormRef.value.validate()
    
    if (!currentExchangeGoods.value) {
      ElMessage.error('商品信息丢失，请重新选择')
      return
    }
    
    exchanging.value = true
    
    const res = await goodsApi.exchangeGoods({
      goodsId: currentExchangeGoods.value.id,
      receiverName: receiverForm.value.receiverName,
      receiverPhone: receiverForm.value.receiverPhone,
      receiverAddress: receiverForm.value.receiverAddress
    }) as any
    
    if (res === '兑换成功') {
      ElMessage.success('兑换成功！')
      receiverFormVisible.value = false
      
      // 刷新数据
      await Promise.all([
        fetchGoodsList(),
        fetchUserPoints(),
        fetchExchangeRecords()
      ])
    } else {
      ElMessage.error(res || '兑换失败')
    }
  } catch (error: any) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('提交收货信息失败:', error)
      ElMessage.error('兑换失败')
    }
  } finally {
    exchanging.value = false
  }
}

// 取消收货信息填写
function cancelReceiverForm() {
  receiverFormVisible.value = false
  currentExchangeGoods.value = null
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

// 获取商品图片数组（处理JSON格式的多图片数据）
function getGoodsImages(goods: Goods): string[] {
  if (!goods.imageUrl) return []
  
  try {
    // 尝试解析JSON格式的图片数据
    const images = JSON.parse(goods.imageUrl)
    if (Array.isArray(images) && images.length > 0) {
      return images
    }
  } catch (error) {
    // 如果JSON解析失败，说明是单个图片URL
    return [goods.imageUrl]
  }
  
  return []
}

// 轮播图控制函数
function getCurrentImageIndex(goodsId: number): number {
  return goodsImageIndices.value.get(goodsId) || 0
}

function setCurrentImage(goodsId: number, index: number) {
  goodsImageIndices.value.set(goodsId, index)
}

function nextImage(goodsId: number) {
  const images = getGoodsImages(goodsList.value.find(g => g.id === goodsId)!)
  const currentIndex = getCurrentImageIndex(goodsId)
  if (currentIndex < images.length - 1) {
    setCurrentImage(goodsId, currentIndex + 1)
  }
}

function prevImage(goodsId: number) {
  const currentIndex = getCurrentImageIndex(goodsId)
  if (currentIndex > 0) {
    setCurrentImage(goodsId, currentIndex - 1)
  }
}

// 详情弹窗控制函数
function showGoodsDetail(goods: Goods) {
  selectedGoods.value = goods
  detailCurrentImageIndex.value = 0
  detailDialogVisible.value = true
}

function nextDetailImage() {
  if (selectedGoods.value) {
    const images = getGoodsImages(selectedGoods.value)
    if (detailCurrentImageIndex.value < images.length - 1) {
      detailCurrentImageIndex.value++
    }
  }
}

function prevDetailImage() {
  if (detailCurrentImageIndex.value > 0) {
    detailCurrentImageIndex.value--
  }
}

function setDetailCurrentImage(index: number) {
  detailCurrentImageIndex.value = index
}

// 页面加载时获取数据
onMounted(async () => {
  await Promise.all([
    fetchGoodsList(),
    fetchUserPoints()
  ])
})
</script>

<style scoped>
.points-mall {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.mall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-left h2 {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-points {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right {
  display: flex;
  align-items: center;
}

.points-label {
  font-size: 16px;
}

.points-value {
  font-size: 24px;
  font-weight: bold;
  color: #ffd700;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.goods-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.goods-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.goods-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  position: relative;
}

.image-carousel {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.carousel-container {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.carousel-item {
  min-width: 100%;
  height: 100%;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-controls {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 10px;
  transform: translateY(-50%);
  pointer-events: none;
}

.carousel-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  pointer-events: auto;
  transition: background 0.3s ease;
}

.carousel-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.7);
}

.carousel-btn:disabled {
  background: rgba(0, 0, 0, 0.2);
  cursor: not-allowed;
}

.carousel-indicators {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 5px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.3s ease;
}

.indicator.active {
  background: rgba(255, 255, 255, 0.9);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #999;
  font-size: 14px;
  border: 1px dashed #ddd;
}

.goods-info {
  padding: 20px;
}

.goods-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.goods-description {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 12px;
  color: #999;
}

.goods-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-required {
  font-size: 16px;
  font-weight: bold;
  color: #e74c3c;
}

.exchange-records {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.exchange-records h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
}

/* 商品详情弹窗样式 */
.goods-detail {
  display: flex;
  gap: 30px;
}

.detail-image-section {
  flex: 1;
  max-width: 400px;
}

.detail-image-carousel {
  width: 100%;
  aspect-ratio: 1;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.detail-carousel-container {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.detail-carousel-item {
  min-width: 100%;
  height: 100%;
}

.detail-carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-carousel-controls {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 15px;
  transform: translateY(-50%);
  pointer-events: none;
}

.detail-carousel-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  pointer-events: auto;
  transition: background 0.3s ease;
}

.detail-carousel-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.8);
}

.detail-carousel-btn:disabled {
  background: rgba(0, 0, 0, 0.3);
  cursor: not-allowed;
}

.detail-carousel-indicators {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.detail-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.3s ease;
}

.detail-indicator.active {
  background: rgba(255, 255, 255, 0.9);
}

.detail-info-section {
  flex: 1;
  padding: 20px 0;
}

.detail-goods-name {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.detail-goods-description {
  margin: 0 0 25px 0;
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.detail-goods-meta {
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}

.meta-label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.meta-value {
  color: #333;
  font-weight: 500;
}

.meta-value.points {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.detail-actions {
  margin-top: 30px;
}

/* 兑换记录弹窗样式 */
.exchange-records-content {
  padding: 20px 0;
}

.points-used {
  color: #e74c3c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: 1fr;
  }
  
  .mall-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .header-center {
    order: -1;
  }
  
  .goods-detail {
    flex-direction: column;
  }
  
  .detail-image-section {
    max-width: 100%;
  }
}

/* 收货信息表单样式 */
.receiver-form-content {
  padding: 20px 0;
}

.goods-summary {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
}

.goods-summary h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
}

.goods-summary p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.points-highlight {
  color: #e6a23c;
  font-weight: bold;
  font-size: 16px;
}

.receiver-form {
  margin-top: 20px;
}

.receiver-form .el-form-item {
  margin-bottom: 20px;
}

.receiver-form .el-input,
.receiver-form .el-textarea {
  width: 100%;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

/* 兑换记录表格样式增强 */
.text-muted {
  color: #c0c4cc;
  font-style: italic;
}

.exchange-records-content .el-table {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 确认收货相关样式 */
.text-success {
  color: #67c23a;
  font-weight: 500;
}

.text-muted {
  color: #c0c4cc;
  font-style: italic;
}
</style>
