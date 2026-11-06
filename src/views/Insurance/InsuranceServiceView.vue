<template>
  <div class="insurance-service-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>保险服务</h1>
        <p>为您的志愿服务提供全方位安全保障</p>
        <div class="header-actions">
          <el-button 
            v-if="!authStore.isLoggedIn" 
            type="primary" 
            @click="$router.push('/login')"
          >
            登录购买保险
          </el-button>
          <el-button 
            v-else 
            type="primary" 
            @click="$router.push('/user/safety/insurance')"
          >
            我的保险
          </el-button>
        </div>
      </div>
    </div>

    <!-- 保险产品展示 -->
    <div class="products-section">
      <div class="section-header">
        <h2>保险产品</h2>
        <p>选择适合您的保险产品，保障志愿服务安全</p>
      </div>
      
      <div class="products-grid" v-loading="loading">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-header">
            <h3>{{ product.name }}</h3>
            <div class="price-tag">
              <span class="currency">¥</span>
              <span class="amount">{{ product.price }}</span>
            </div>
          </div>
          
          <div class="product-content">
            <p class="description">{{ product.description }}</p>
            
            <div class="coverage-details">
              <div class="coverage-item">
                <el-icon><Money /></el-icon>
                <span class="label">保障金额</span>
                <span class="value">¥{{ product.coverageAmount }}</span>
              </div>
              <div class="coverage-item">
                <el-icon><Calendar /></el-icon>
                <span class="label">保障期限</span>
                <span class="value">{{ product.coveragePeriod }}天</span>
              </div>
            </div>
            
            <div class="features">
              <div class="feature-item" v-for="feature in getProductFeatures(product)" :key="feature">
                <el-icon><Check /></el-icon>
                <span>{{ feature }}</span>
              </div>
            </div>
          </div>
          
          <div class="product-footer">
            <el-button 
              type="primary" 
              size="large"
              @click="handlePurchase"
              :loading="purchasing"
            >
              {{ authStore.isLoggedIn ? '立即购买' : '登录购买' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务特色 -->
    <div class="features-section">
      <div class="section-header">
        <h2>服务特色</h2>
        <p>专业的保险服务，让您安心参与志愿服务</p>
      </div>
      
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <h3>全面保障</h3>
          <p>覆盖志愿服务过程中的各种风险，提供全方位保障</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Star /></el-icon>
          </div>
          <h3>快速理赔</h3>
          <p>简化理赔流程，快速处理理赔申请，让您无后顾之忧</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <h3>专业服务</h3>
          <p>专业的保险顾问团队，为您提供专业的保险咨询服务</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Phone /></el-icon>
          </div>
          <h3 class="no-wrap">24小时服务</h3>
          <p>全天候客服支持，随时为您解答保险相关问题</p>
        </div>
      </div>
    </div>

    <!-- 购买流程 -->
    <div class="process-section">
      <div class="section-header">
        <h2>购买流程</h2>
        <p>简单三步，轻松购买保险</p>
      </div>
      
      <div class="process-steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3>选择产品</h3>
            <p>浏览保险产品，选择适合您的保障方案</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3>确认购买</h3>
            <p>确认产品信息，选择支付方式完成购买</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3 class="no-wrap">立即生效</h3>
            <p>支付成功后保险立即生效，开始享受保障</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { 
  Money, 
  Calendar, 
  Check, 
  Lock, 
  Star, 
  Tools, 
  Phone 
} from '@element-plus/icons-vue'
import { getInsuranceProducts } from '@/api/insurance'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const purchasing = ref(false)
const products = ref<Array<{
  id: number;
  name: string;
  price: number;
  description: string;
  coverageAmount: number;
  coveragePeriod: number;
}>>([])

// 加载保险产品
const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getInsuranceProducts()
    products.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载保险产品失败:', error)
    ElMessage.error('加载保险产品失败')
  } finally {
    loading.value = false
  }
}

// 获取产品特色
const getProductFeatures = (product: {
  id: number;
  name: string;
  price: number;
  description: string;
  coverageAmount: number;
  coveragePeriod: number;
}) => {
  const features = []
  if (product.coverageAmount >= 500000) {
    features.push('高额保障')
  }
  if (product.coveragePeriod >= 365) {
    features.push('全年保障')
  }
  features.push('快速理赔', '专业服务')
  return features
}

// 处理购买
const handlePurchase = () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到用户中心的保险页面
  router.push('/user/safety/insurance')
}

onMounted(loadProducts)
</script>

<style scoped>
.insurance-service-page {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.header-content h1 {
  font-size: 48px;
  margin: 0 0 16px 0;
  font-weight: 700;
}

.header-content p {
  font-size: 20px;
  margin: 0 0 32px 0;
  opacity: 0.9;
}

.header-actions {
  margin-top: 24px;
}

.products-section,
.features-section,
.process-section {
  padding: 80px 0;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-header h2 {
  font-size: 36px;
  margin: 0 0 16px 0;
  color: #333;
}

.section-header p {
  font-size: 18px;
  color: #666;
  margin: 0;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.product-header h3 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.price-tag {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 18px;
  color: #f56c6c;
}

.amount {
  font-size: 36px;
  font-weight: 700;
  color: #f56c6c;
}

.description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
}

.coverage-details {
  margin-bottom: 24px;
}

.coverage-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.coverage-item .label {
  color: #666;
  font-size: 14px;
}

.coverage-item .value {
  color: #333;
  font-weight: 600;
  margin-left: auto;
}

.features {
  margin-bottom: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.feature-item .el-icon {
  color: #67c23a;
}

.product-footer {
  text-align: center;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.feature-card {
  text-align: center;
  padding: 32px 24px;
}

.feature-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.feature-icon .el-icon {
  font-size: 32px;
  color: white;
}

.feature-card h3 {
  font-size: 20px;
  margin: 0 0 16px 0;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.step {
  text-align: center;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  margin: 0 auto 24px;
}

.step-content h3 {
  font-size: 20px;
  margin: 0 0 16px 0;
  color: #333;
}

.step-content p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.no-wrap {
  white-space: nowrap;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .process-steps {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .header-content h1 {
    font-size: 36px;
  }
  
  .header-content p {
    font-size: 16px;
  }
  
  .section-header h2 {
    font-size: 28px;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .process-steps {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
