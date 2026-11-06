<template>
  <div class="checkin-page">
    <div class="checkin-header">
      <h1>✅ 活动签到</h1>
      <p>欢迎参加志愿者活动</p>
    </div>
    
    <div class="activity-info">
      <div v-if="activityLoading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载活动信息...</span>
      </div>
      <div v-else>
        <h2>{{ activity.title }}</h2>
        <div class="info-item">
          <span class="label">活动时间：</span>
          <span>{{ activity.startTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">活动地点：</span>
          <span>{{ activity.location }}</span>
        </div>
        <div class="info-item">
          <span class="label">活动状态：</span>
          <el-tag :type="getStatusType(activity.status)">
            {{ getStatusText(activity.status) }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <div class="checkin-form">
      <h3>个人信息</h3>
      <div v-if="userInfoLoading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载个人信息...</span>
      </div>
      <el-form v-else :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="姓名" readonly />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" readonly />
        </el-form-item>
        <el-form-item label="身份证" prop="idCard">
          <el-input v-model="form.idCard" placeholder="身份证号" readonly />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            placeholder="可选：填写备注信息"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <div class="location-info">
        <h4>位置信息<span v-if="isDevelopmentMode" class="dev-mode-tip">（当前是开发模式模拟定位）</span></h4>
        <p v-if="currentLocation" class="location-text">
          📍 {{ currentLocation }}
        </p>
        <p v-else class="location-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在获取位置信息...
        </p>
        <el-button size="small" @click="getCurrentLocation" :loading="locationLoading">
          刷新位置
        </el-button>
      </div>
      
      <div class="checkin-actions">
        <el-button 
          type="success" 
          size="large" 
          @click="confirmCheckin"
          :loading="checkinLoading"
          :disabled="!canCheckin"
        >
          确认签到
        </el-button>
        
        <el-button 
          size="large" 
          @click="goBack"
        >
          返回
        </el-button>
      </div>
    </div>
    
    <!-- 签到成功弹窗 -->
    <el-dialog
      v-model="successDialogVisible"
      title="签到成功"
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div class="success-content">
        <el-icon class="success-icon"><Check /></el-icon>
        <h3>签到成功！</h3>
        <p>感谢您参与志愿者活动</p>
        <div class="success-info">
          <p>签到时间：{{ checkinTime }}</p>
          <p>签到地点：{{ checkinLocation }}</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="closeSuccessDialog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Check } from '@element-plus/icons-vue'
import { checkinApi, activityApi, authApi } from '@/api'

const route = useRoute()
const router = useRouter()

// 表单数据
const form = ref({
  name: '',
  phone: '',
  idCard: '',
  remark: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '身份证号不能为空', trigger: 'blur' }
  ]
}

// 活动信息
const activity = ref({
  id: 0,
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  status: 0
})

// 加载状态
const activityLoading = ref(false)
const userInfoLoading = ref(false)

// 位置信息
const currentLocation = ref('')
const locationLoading = ref(false)
const locationError = ref('')

// 开发模式开关（课程设计项目设为true）
const isDevelopmentMode = ref(true)

// 签到状态
const checkinLoading = ref(false)
const successDialogVisible = ref(false)
const checkinTime = ref('')
const checkinLocation = ref('')

// 表单引用
const formRef = ref()

// 计算属性
const canCheckin = computed(() => {
  return form.value.name && form.value.phone && form.value.idCard
})

// 解析二维码内容并获取活动详情
const parseQRCode = async () => {
  const qrCode = route.params.qrCode as string
  if (qrCode && qrCode.includes('activity_')) {
    const parts = qrCode.split('_')
    if (parts.length >= 2) {
      const activityId = Number(parts[1])
      await loadActivityDetail(activityId)
    }
  }
}

// 获取用户信息
const loadUserInfo = async () => {
  userInfoLoading.value = true
  try {
    // 优先尝试获取签到专用信息
    try {
      const response = await checkinApi.getCurrentUserCheckinInfo() as unknown as {
        name: string
        phone: string
        idCard: string
      }
      if (response && response.name) {
        form.value.name = response.name
        form.value.phone = response.phone || ''
        form.value.idCard = response.idCard || ''
        return
      }
    } catch {
      console.log('签到专用API失败，尝试通用用户信息API')
    }
    
    // 如果签到专用API失败，使用通用用户信息API
    const response = await authApi.getUserInfo() as unknown as {
      realName: string
      phone: string
      idCard: string
    }
    if (response) {
      form.value.name = response.realName || ''
      form.value.phone = response.phone || ''
      form.value.idCard = response.idCard || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.warning('获取用户信息失败，请手动填写')
  } finally {
    userInfoLoading.value = false
  }
}

// 获取活动详情
const loadActivityDetail = async (activityId: number) => {
  activityLoading.value = true
  try {
    const response = await activityApi.getActivityDetail(activityId) as unknown as {
      id: number
      title: string
      location: string
      startTime: string
      endTime: string
      status: number
    }
    if (response) {
      activity.value = {
        id: response.id,
        title: response.title,
        location: response.location,
        startTime: response.startTime,
        endTime: response.endTime,
        status: response.status
      }
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
    // 如果获取失败，使用默认值
    activity.value = {
      id: activityId,
      title: '活动详情加载失败',
      location: '未知地点',
      startTime: '未知时间',
      endTime: '未知时间',
      status: 0
    }
  } finally {
    activityLoading.value = false
  }
}

// 完全模拟的定位函数（用于课程设计演示）
const getMockLocationForDemo = () => {
  const demoLocations = [
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院正门广场',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院教学楼A座门前',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院图书馆大厅',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院体育馆入口',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院学生活动中心',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院食堂门前',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院宿舍区广场',
    '广东省佛山市禅城区江湾一路18号佛山科学技术学院实验楼门前'
  ]
  
  const randomIndex = Math.floor(Math.random() * demoLocations.length)
  const randomLocation = demoLocations[randomIndex]
  console.log('模拟定位选择:', randomLocation)
  return randomLocation
}

// IP定位备用方案
const getLocationByIP = async () => {
  try {
    // 使用免费的IP定位服务
    const response = await fetch('https://ipapi.co/json/')
    const data = await response.json()
    if (data.city && data.region) {
      return `${data.city}, ${data.region}`
    }
  } catch (error) {
    console.error('IP定位失败:', error)
  }
  return null
}

// GPS定位函数
const getCurrentLocationGPS = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持定位功能'))
      return
    }
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        const location = `纬度: ${latitude.toFixed(6)}, 经度: ${longitude.toFixed(6)}`
        resolve(location)
      },
      (error) => {
        console.error('GPS定位失败:', error)
        reject(error)
      },
      {
        enableHighAccuracy: false,
        timeout: 15000,  // 减少GPS超时时间到15秒
        maximumAge: 300000
      }
    )
  })
}

// 获取当前位置 - 重构后的主函数（支持模拟模式）
const getCurrentLocation = async () => {
  locationLoading.value = true
  locationError.value = ''
  
  if (isDevelopmentMode.value) {
    // 开发模式：直接使用模拟定位
    console.log('演示模式：使用模拟定位')
    setTimeout(() => {
      const mockLocation = getMockLocationForDemo()
      currentLocation.value = mockLocation || '广东省佛山市禅城区江湾一路18号佛山科学技术学院正门广场'
      locationLoading.value = false
      ElMessage.success('模拟定位成功（演示模式）')
      console.log('演示模式定位成功:', mockLocation)
    }, 1000) // 模拟1秒加载时间
    return
  }
  
  // 生产模式：正常定位逻辑
  try {
    // 先尝试GPS定位
    const gpsLocation = await getCurrentLocationGPS()
    currentLocation.value = gpsLocation as string
    console.log('GPS定位成功:', gpsLocation)
  } catch (error: unknown) {
    console.log('GPS定位失败，尝试IP定位')
    try {
      // GPS失败后尝试IP定位
      const ipLocation = await getLocationByIP()
      if (ipLocation) {
        currentLocation.value = ipLocation
        console.log('IP定位成功:', ipLocation)
      } else {
        throw new Error('IP定位也失败')
      }
    } catch (ipError) {
      console.error('所有定位方式都失败:', ipError)
      
      // 根据GPS错误类型提供不同的提示
      let errorMessage = '定位失败，请手动输入位置'
      if (error && typeof error === 'object' && 'code' in error) {
        const geolocationError = error as GeolocationPositionError
        switch(geolocationError.code) {
          case geolocationError.PERMISSION_DENIED:
            errorMessage = '定位权限被拒绝，请手动输入位置'
            break
          case geolocationError.POSITION_UNAVAILABLE:
            errorMessage = '定位服务不可用，请手动输入位置'
            break
          case geolocationError.TIMEOUT:
            errorMessage = '定位超时，请手动输入位置'
            break
        }
      }
      
      currentLocation.value = errorMessage
      locationError.value = errorMessage
    }
  } finally {
    locationLoading.value = false
  }
}

// 确认签到
const confirmCheckin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (!canCheckin.value) {
      ElMessage.warning('请完善所有必填信息')
      return
    }
    
    await ElMessageBox.confirm('确认提交签到信息吗？', '确认签到', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    checkinLoading.value = true
    
    const checkinData = {
      name: form.value.name,
      phone: form.value.phone,
      idCard: form.value.idCard,
      location: currentLocation.value || '未获取位置信息',
      remark: form.value.remark || '扫码签到'
    }
    
    // 调用签到API
    const response = await checkinApi.checkin(activity.value.id, checkinData)
    
    if (response) {
      checkinTime.value = new Date().toLocaleString('zh-CN')
      checkinLocation.value = currentLocation.value
      successDialogVisible.value = true
    }
    
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '签到失败'
    ElMessage.error(errorMessage)
  } finally {
    checkinLoading.value = false
  }
}

// 关闭成功弹窗
const closeSuccessDialog = () => {
  successDialogVisible.value = false
  goBack()
}

// 返回
const goBack = () => {
  router.go(-1)
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap: Record<number, string> = {
    1: 'info',    // 未开始
    2: 'warning', // 报名中
    3: 'success', // 进行中
    4: 'danger'   // 已结束
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '未开始',
    2: '报名中',
    3: '进行中',
    4: '已结束'
  }
  return statusMap[status] || '未知'
}

onMounted(() => {
  parseQRCode()
  loadUserInfo()
  getCurrentLocation()
})
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 10px;
  max-width: 400px;
  margin: 0 auto;
}

.checkin-header {
  text-align: center;
  margin-bottom: 15px;
  color: #2c3e50;
}

.checkin-header h1 {
  font-size: 1.5rem;
  margin: 0 0 6px 0;
  font-weight: 600;
}

.checkin-header p {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin: 0;
}

.activity-info {
  background: white;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #666;
}

.loading-container .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.activity-info h2 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-item .label {
  font-weight: 600;
  color: #34495e;
  min-width: 80px;
}

.checkin-form {
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.checkin-form h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.location-info {
  margin: 15px 0;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.location-info h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.location-text {
  color: #28a745;
  font-weight: 500;
  margin: 10px 0;
}

.location-loading {
  color: #6c757d;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0;
}

.checkin-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.checkin-actions .el-button {
  flex: 1;
  padding: 10px;
  font-size: 13px;
  border-radius: 6px;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 15px;
}

.success-content h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.success-content p {
  margin: 0 0 20px 0;
  color: #7f8c8d;
}

.success-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  text-align: left;
}

.success-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #495057;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .checkin-page {
    padding: 15px;
  }
  
  .checkin-header h1 {
    font-size: 1.8rem;
  }
  
  .activity-info,
  .checkin-form {
    padding: 20px;
  }
  
  .checkin-actions {
    flex-direction: column;
  }
  
  .checkin-actions .el-button {
    padding: 12px;
    font-size: 14px;
  }
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 15px;
}

.success-content h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.success-content p {
  margin: 0 0 20px 0;
  color: #7f8c8d;
}

.success-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  text-align: left;
}

.success-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #495057;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .checkin-page {
    padding: 8px;
    max-width: 100%;
  }
  
  .checkin-header {
    margin-bottom: 12px;
  }
  
  .checkin-header h1 {
    font-size: 1.3rem;
  }
  
  .checkin-header p {
    font-size: 0.8rem;
  }
  
  .activity-info,
  .checkin-form {
    padding: 10px;
    margin-bottom: 12px;
  }
  
  .activity-info h2,
  .checkin-form h3 {
    font-size: 1rem;
    margin-bottom: 8px;
  }
  
  .info-item {
    font-size: 12px;
    margin-bottom: 6px;
  }
  
  .location-info {
    padding: 8px;
    margin: 12px 0;
  }
  
  .checkin-actions {
    flex-direction: column;
    gap: 8px;
    margin-top: 15px;
  }
  
  .checkin-actions .el-button {
    padding: 10px;
    font-size: 13px;
  }
}

/* 开发模式提示样式 */
.dev-mode-tip {
  color: #909399;
  font-size: 12px;
  font-style: italic;
  margin-left: 8px;
}
</style>
