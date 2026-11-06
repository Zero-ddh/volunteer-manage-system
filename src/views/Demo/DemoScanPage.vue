<template>
  <div class="demo-scan-page">
    <div class="demo-header">
      <h1>📱 扫码签到演示</h1>
      <p>模拟手机扫码后的签到页面</p>
    </div>

    <div class="demo-content">
      <div class="qr-preview">
        <img v-if="qrCodeImage" :src="qrCodeImage" alt="活动二维码" class="qr-image" />
        <div v-else class="qr-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>正在生成二维码...</p>
        </div>
        <p class="qr-tip">扫描上方二维码</p>
      </div>

      <div class="demo-actions">
        <el-button
          type="primary"
          size="large"
          @click="simulateScan"
          :disabled="!qrCodeImage"
        >
          📱 模拟扫码
        </el-button>

        <el-button
          size="large"
          @click="downloadQRCode"
          :disabled="!qrCodeImage"
        >
          📥 下载二维码
        </el-button>
      </div>

      <div class="demo-info">
        <h3>演示说明</h3>
        <ul>
          <li>点击"模拟扫码"按钮体验扫码签到流程</li>
          <li>系统将跳转到签到页面</li>
          <li>填写个人信息完成签到</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { checkinApi } from '@/api'

const route = useRoute()
const router = useRouter()

const qrCodeImage = ref<string>('')
const qrCodeUrl = ref<string>('')

// 获取活动ID
const activityId = Number(route.params.id) || 6

// 生成二维码
const generateQRCode = async () => {
  try {
    const response = await checkinApi.getActivityQRCode(activityId) as unknown as {
      qrCode: string;
      qrImage: string;
      expireTime: number
    }

    if (response) {
      qrCodeImage.value = response.qrImage
      qrCodeUrl.value = response.qrCode
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败')
  }
}

// 模拟扫码
const simulateScan = () => {
  if (!qrCodeUrl.value) {
    ElMessage.warning('二维码尚未生成')
    return
  }

  // 解析二维码URL，提取活动ID
  const urlParts = qrCodeUrl.value.split('/')
  const qrCode = urlParts[urlParts.length - 1]

  // 跳转到签到页面
  router.push(`/checkin/${qrCode}`)
}

// 下载二维码
const downloadQRCode = () => {
  if (!qrCodeImage.value) return

  const link = document.createElement('a')
  link.href = qrCodeImage.value
  link.download = `活动签到二维码_${activityId}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  ElMessage.success('二维码已下载')
}

onMounted(() => {
  generateQRCode()
})
</script>

<style scoped>
.demo-scan-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  padding: 40px 20px;
  color: white;
  text-align: center;
}

.demo-header {
  margin-bottom: 40px;
}

.demo-header h1 {
  font-size: 2.5rem;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.demo-header p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0;
}

.demo-content {
  max-width: 500px;
  margin: 0 auto;
}

.qr-preview {
  background: white;
  padding: 30px;
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.qr-image {
  width: 200px;
  height: 200px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 15px;
}

.qr-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.qr-loading .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.qr-tip {
  color: #606266;
  font-size: 16px;
  margin: 0;
  font-weight: 500;
}

.demo-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 40px;
}

.demo-actions .el-button {
  padding: 15px 30px;
  font-size: 16px;
  border-radius: 8px;
}

.demo-info {
  background: rgba(255, 255, 255, 0.1);
  padding: 25px;
  border-radius: 12px;
  text-align: left;
  backdrop-filter: blur(10px);
}

.demo-info h3 {
  margin: 0 0 15px 0;
  font-size: 1.3rem;
}

.demo-info ul {
  margin: 0;
  padding-left: 20px;
}

.demo-info li {
  margin-bottom: 8px;
  line-height: 1.5;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .demo-scan-page {
    padding: 20px 15px;
  }

  .demo-header h1 {
    font-size: 2rem;
  }

  .demo-header p {
    font-size: 1rem;
  }

  .qr-preview {
    padding: 20px;
  }

  .qr-image {
    width: 150px;
    height: 150px;
  }

  .demo-actions .el-button {
    padding: 12px 20px;
    font-size: 14px;
  }
}
</style>
