<template>
  <div v-if="activity" class="activity-detail">
    <!-- 顶部信息 -->
    <el-card class="top-card mb-16">
      <!-- 返回列表按钮 - 位于卡片右上角 -->
      <el-button @click="goBack" icon="ArrowLeft" type="primary" plain size="small" class="back-button">
        返回列表
      </el-button>
      
      <div class="activity-header">
        <!-- 活动封面图片已屏蔽 -->
        <!-- <div class="cover-container">
          <img v-if="activity.coverImage" class="cover" :src="activity.coverImage" alt="活动封面" />
          <div v-else class="cover-placeholder">
            <div class="placeholder-icon">📷</div>
            <div class="placeholder-text">暂无图片</div>
          </div>
        </div> -->
        <div class="info">
          <h1 class="title">{{ activity.title }}</h1>
          <div class="meta">
            <span>📅 {{ activity.startTime }} ~ {{ activity.endTime }}</span>
            <span>📍 {{ activity.location }}</span>
            <span>👥 需要{{ activity.maxParticipants || 0 }}人</span>
          </div>
          <div class="status" :class="statusClass(activity.status)">{{ statusText(activity.status) }}</div>
          <div class="tags">
            <span class="tag" v-if="activity.categoryName">{{ activity.categoryName }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体两栏布局 -->
    <div class="detail-grid">
      <!-- 左侧：详情内容 -->
      <el-card class="left">
        <div class="section">
          <div class="section-title">活动介绍</div>
          <div class="section-content">{{ activity.description }}</div>
        </div>
        <div class="section">
          <div class="section-title">技能要求</div>
          <div class="section-content">{{ activity.requirements || '无' }}</div>
        </div>

        <div class="section">
          <div class="section-title">参与活动条件</div>
          <div class="section-content">{{ activity.participationConditions || '无' }}</div>
        </div>
        
        <!-- 活动匹配分析 -->
        <div v-if="isLoggedIn" class="section">
          <div class="section-title">AI智能匹配分析</div>
          <div class="section-content">
            <SkillMatch :activityId="activity.id" :userId="currentUserId" />
          </div>
        </div>
      </el-card>

      <!-- 右侧：报名信息等 -->
      <el-card class="right">
        <div class="progress">
          <div class="progress-bar"><div class="progress-fill" :style="{ width: progressPercent(activity) + '%' }"></div></div>
          <div class="progress-text">已报名 {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants || 0 }} 人</div>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" @click="register" :loading="registerLoading">
            立即报名
          </el-button>
          <el-button size="large" @click="share">分享活动</el-button>
        </div>
        
        <!-- 紧急联络区域 -->
        <div class="emergency-contact-section">
          <div class="section-title">紧急联络</div>
          <div class="contact-info">
            <div v-if="activity.contactInfo" class="contact-item">
              <span class="label">活动联系人：</span>
              <span class="value">{{ activity.contactInfo }}</span>
            </div>
            <div v-if="activity.emergencyContactName" class="contact-item">
              <span class="label">紧急联系人：</span>
              <span class="value">{{ activity.emergencyContactName }}</span>
            </div>
            <div v-if="activity.emergencyContactPhone" class="contact-item">
              <span class="label">紧急联系电话：</span>
              <span class="value">
                <a :href="`tel:${activity.emergencyContactPhone}`" class="phone-link">
                  {{ activity.emergencyContactPhone }}
                </a>
              </span>
            </div>
          </div>
          <div class="emergency-actions">
            <el-button 
              v-if="activity.contactInfo" 
              type="primary" 
              @click="callContact"
              icon="Phone"
              size="small"
            >
              联系组织方
            </el-button>
            <el-button 
              v-if="activity.emergencyContactPhone" 
              type="danger" 
              @click="callEmergency"
              icon="Warning"
              size="small"
            >
              紧急联系
            </el-button>
          </div>
        </div>
        
        <!-- 签到二维码区域 -->
        <div v-if="activityQRCode && !hasCheckedIn" class="qr-section">
          <div class="qr-header">
            <h3>活动签到二维码</h3>
          </div>
          <div class="qr-content">
            <!-- 二维码图片 -->
            <div class="qr-image-container">
              <img :src="activityQRCode.qrImage" alt="活动签到二维码" class="qr-image" />
            </div>
            
            <!-- 二维码信息 -->
            <div class="qr-info">
              <p class="qr-tip">请扫描上方二维码完成签到</p>
              <p class="qr-expire" v-if="qrExpireTime">
                二维码有效期至：{{ formatTime(qrExpireTime) }}
              </p>
            </div>
            
            <!-- 操作按钮 -->
            <div class="qr-actions">
              <el-button type="primary" @click="goToDemo">
                <el-icon><Camera /></el-icon>
                演示扫码
              </el-button>
              <el-button @click="downloadQRCode">
                <el-icon><Download /></el-icon>
                下载二维码
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 签到区域 - 已屏蔽开始签到入口 -->
        <!-- <div v-if="canCheckin" class="checkin-section">
          <div class="checkin-header">
            <h3>活动签到</h3>
            <el-button 
              type="success" 
              size="large"
              @click="startCheckin"
              :loading="checkinLoading"
            >
              开始签到
            </el-button>
          </div>
          <div class="checkin-info">
            <p>请确保您已到达活动现场，并准备好进行签到</p>
            <p class="checkin-tip">每天可以签到一次，明天可以再次签到</p>
          </div>
        </div> -->
        
        <!-- 已签到状态 -->
        <div v-if="hasCheckedIn" class="checked-in-section">
          <div class="checkin-success">
            <el-icon class="success-icon"><Check /></el-icon>
            <h3>今日签到成功</h3>
            <p>您今天已成功签到此活动</p>
            <div class="checkin-details">
              <span>签到时间：{{ checkinTime }}</span>
              <span>签到地点：{{ checkinLocation }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 签到弹窗 -->
    <el-dialog
      v-model="checkinDialogVisible"
      title="活动签到"
      width="600px"
      :before-close="handleCheckinClose"
    >
      <div class="checkin-content">
        <!-- GPS定位验证 -->
        <div class="location-verification">
          <div class="location-header">
            <el-icon><Location /></el-icon>
            <span>位置验证</span>
          </div>
          <div class="location-info">
            <p>当前位置：{{ currentLocation || '正在获取...' }}</p>
            <el-button size="small" @click="getCurrentLocation" :loading="locationLoading">
              刷新位置
            </el-button>
          </div>
          <div v-if="locationError" class="location-error">
            <el-icon><Warning /></el-icon>
            <span>{{ locationError }}</span>
          </div>
        </div>
        
        <!-- 二维码显示 -->
        <div class="qr-display">
          <div class="qr-header">
            <el-icon><Camera /></el-icon>
            <span>活动签到二维码</span>
          </div>
          <div class="qr-image-container">
            <img v-if="activityQRCode?.qrImage" :src="activityQRCode.qrImage" alt="活动签到二维码" class="qr-image" />
            <div v-else class="qr-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>正在生成二维码...</p>
            </div>
          </div>
          <div class="qr-info">
            <p class="qr-tip">请扫描上方二维码完成签到</p>
            <p class="qr-expire" v-if="qrExpireTime">
              二维码有效期至：{{ formatTime(qrExpireTime) }}
            </p>
          </div>
          <div class="qr-actions">
            <el-button @click="downloadQRCode" :disabled="!activityQRCode?.qrImage">
              <el-icon><Download /></el-icon>
              下载二维码
            </el-button>
          </div>
        </div>
        
        <!-- 签到确认 -->
        <div class="checkin-confirm">
          <el-button 
            type="success" 
            size="large"
            @click="confirmCheckin" 
            :loading="checkinLoading"
            :disabled="!canConfirmCheckin"
          >
            确认签到
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 报名确认弹窗 -->
    <el-dialog
      v-model="showConfirmDialog"
      title="确认报名"
      width="500px"
      :before-close="handleClose"
    >
      <div class="confirm-content">
        <div class="activity-info">
          <h3>{{ activity.title }}</h3>
          <div class="info-item">
            <span class="label">活动时间：</span>
            <span>{{ activity.startTime }} ~ {{ activity.endTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">活动地点：</span>
            <span>{{ activity.location }}</span>
          </div>
          <div class="info-item">
            <span class="label">技能要求：</span>
            <span>{{ activity.requirements || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">参与活动条件：</span>
            <span>{{ activity.participationConditions || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系方式：</span>
            <span>{{ activity.contactInfo }}</span>
          </div>
        </div>
        
        <div class="user-info" v-if="authStore.userInfo">
          <h4>您的信息</h4>
          <div class="info-item">
            <span class="label">姓名：</span>
            <span>{{ authStore.userInfo.realName || authStore.userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号：</span>
            <span>{{ authStore.userInfo.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱：</span>
            <span>{{ authStore.userInfo.email }}</span>
          </div>
        </div>
        
        <div class="confirm-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>确认报名此活动吗？报名后请按时参加活动，管理员将进行审核。</span>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showConfirmDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmRegister" :loading="registerLoading">
            确认报名
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, Location, Camera, Check, Warning, Loading, Download } from '@element-plus/icons-vue'
import SkillMatch from '@/components/SkillMatch.vue'
import { checkinApi, activityApi } from '@/api'

// 定义报名表数据类型
interface UserSkill {
  skillName?: string
  skillCategoryName?: string
  skillLevelText?: string
  verifiedText?: string
  certificateName?: string
}

interface RegistrationFormData {
  activityTitle?: string
  activityStartTime?: string
  activityEndTime?: string
  activityLocation?: string
  activityDescription?: string
  realName?: string
  username?: string
  phone?: string
  email?: string
  idCard?: string
  registrationTime?: string
  statusText?: string
  userSkills?: UserSkill[]
}

import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const activityStore = useActivityStore()
const authStore = useAuthStore()
const activity = computed(() => activityStore.currentActivity)

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)
const currentUserId = computed(() => authStore.userInfo?.id || 0)

// 弹窗状态
const showConfirmDialog = ref(false)
const registerLoading = ref(false)

// 签到相关状态
const checkinDialogVisible = ref(false)
const checkinLoading = ref(false)
const currentLocation = ref('')
const locationLoading = ref(false)
const locationError = ref('')
const hasCheckedIn = ref(false)
const checkinTime = ref('')
const checkinLocation = ref('')

// 二维码相关状态
const activityQRCode = ref<{ qrCode: string; qrImage: string; expireTime: number } | null>(null)
const qrExpireTime = ref<number | null>(null)

// 检查是否可以签到 - 已屏蔽
// const canCheckin = computed(() => {
//   if (!activity.value || !authStore.isLoggedIn) return false
//   
//   // 检查活动状态是否为进行中
//   if (activity.value.status !== 3) return false
//   
//   // 检查今天是否已签到
//   if (hasCheckedIn.value) return false
//   
//   // 这里可以添加更多检查逻辑，比如用户是否已报名等
//   return true
// })

// 检查是否可以确认签到
const canConfirmCheckin = computed(() => {
  return currentLocation.value && activityQRCode.value?.qrCode
})

onMounted(async () => {
  const id = Number(route.params.id)
  if (Number.isFinite(id)) {
    await activityStore.getActivityDetail(id)
    // 获取用户信息用于弹窗显示
    if (authStore.isLoggedIn && !authStore.userInfo) {
      await authStore.getUserInfo()
    }
    // 检查用户是否已签到
    await checkUserCheckinStatus(id)
    // 获取活动二维码
    await getActivityQRCode(id)
  }
})

onUnmounted(() => {
  // 清理工作
})

// 检查用户签到状态
const checkUserCheckinStatus = async (activityId: number) => {
  try {
    const userId = authStore.userInfo?.id
    if (!userId) return
    
    // 使用新的API检查今天是否已签到
    const response = await checkinApi.checkTodayCheckinStatus(activityId)
    
    if (response.data) {
      hasCheckedIn.value = response.data.hasCheckedInToday
      if (response.data.hasCheckedInToday) {
        checkinTime.value = response.data.checkinTime
        checkinLocation.value = response.data.checkinLocation
      }
    }
  } catch (error) {
    console.error('检查签到状态失败:', error)
  }
}

// 返回列表
const goBack = () => {
  router.push('/activities')
}

const register = async () => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    ElMessage.warning('您好,请先登录后再操作！')
    router.push('/login')
    return
  }

  // 检查报名人数是否已满
  if (activity.value.maxParticipants && activity.value.maxParticipants > 0) {
    if (activity.value.currentParticipants && activity.value.currentParticipants >= activity.value.maxParticipants) {
      ElMessage.warning('报名人数已满，暂无法报名！')
      return
    }
  }

  // 显示确认弹窗
  showConfirmDialog.value = true
}

// 确认报名
const confirmRegister = async () => {
  registerLoading.value = true
  try {
    const id = Number(route.params.id)
    const response = await request.post(`/v1/activities/${id}/register`) as {
      success: boolean;
      message: string;
      status: number;
      registrationId?: number;
    }
    
    console.log('报名响应:', response)
    
    // 根据返回的状态显示不同消息
    // 后端返回的是扁平结构：{success, message, status, registrationId}
    if (response.success) {
      ElMessage.success(response.message)
      showConfirmDialog.value = false
      
      // 刷新活动详情以更新报名人数
      await activityStore.getActivityDetail(id)
      
      // 生成电子报名表
      if (response.registrationId) {
        console.log('开始生成报名表，registrationId:', response.registrationId)
        await generateRegistrationForm(response.registrationId)
      } else {
        console.log('报名成功但没有registrationId')
      }
    } else {
      ElMessage.warning(response.message)
      showConfirmDialog.value = false
    }
  } catch (error: unknown) {
    console.error('报名失败:', error)
    const errorMessage = error instanceof Error ? error.message : '报名失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    registerLoading.value = false
  }
}

// 生成电子报名表
const generateRegistrationForm = async (registrationId: number) => {
  try {
    console.log('开始请求报名表数据，registrationId:', registrationId)
    const registrationData = await activityApi.getRegistrationFormData(registrationId)
    console.log('报名表数据:', registrationData)
    
    // 生成并下载PDF
    await generateAndDownloadPDF(registrationData)
    
    // 显示成功消息并支持重新下载
    ElMessageBox.confirm(
      '报名表已自动下载到您的设备！\n\n如需重新下载，请点击下方按钮。',
      '报名表生成成功',
      { 
        type: 'success',
        confirmButtonText: '重新下载PDF',
        cancelButtonText: '关闭',
        showCancelButton: true
      }
    ).then(() => {
      // 用户点击重新下载
      generateRegistrationForm(registrationId)
    }).catch(() => {
      // 用户点击关闭
    })
  } catch (error) {
    console.error('生成报名表失败:', error)
    ElMessage.error('生成报名表失败，请稍后重试')
  }
}

// 生成并下载PDF报名表
const generateAndDownloadPDF = async (data: RegistrationFormData) => {
  try {
    // 动态导入PDF生成库
    const html2canvas = (await import('html2canvas')).default
    const jsPDF = (await import('jspdf')).default
    
    // 创建报名表HTML元素
    const formElement = createRegistrationFormElement(data)
    document.body.appendChild(formElement)
    
    // 将HTML转换为Canvas
    const canvas = await html2canvas(formElement, {
      scale: 2, // 提高清晰度
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      width: formElement.offsetWidth,
      height: formElement.offsetHeight
    })
    
    // 创建PDF
    const pdf = new jsPDF('portrait', 'mm', 'a4')
    const imgData = canvas.toDataURL('image/png', 1.0)
    
    // 计算图片在PDF中的位置和大小
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = canvas.width * 0.264583 // 转换为mm
    const imgHeight = canvas.height * 0.264583
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight)
    
    // 添加图片到PDF
    pdf.addImage(imgData, 'PNG', 0, 0, imgWidth * ratio, imgHeight * ratio)
    
    // 下载PDF
    const fileName = `报名表_${data.activityTitle || '活动'}_${new Date().toLocaleDateString()}.pdf`
    pdf.save(fileName)
    
    // 清理DOM元素
    document.body.removeChild(formElement)
    
    ElMessage.success('PDF报名表下载成功！')
  } catch (error) {
    console.error('PDF生成失败:', error)
    throw error
  }
}

// 创建报名表HTML元素
const createRegistrationFormElement = (data: RegistrationFormData) => {
  const element = document.createElement('div')
  element.style.cssText = `
    position: fixed;
    top: -9999px;
    left: -9999px;
    width: 210mm;
    padding: 20mm;
    background: white;
    font-family: 'Microsoft YaHei', Arial, sans-serif;
    color: #333;
  `
  
  element.innerHTML = `
    <style>
      .form-container {
        width: 100%;
        background: white;
      }
      .header {
        text-align: center;
        margin-bottom: 30px;
        padding-bottom: 20px;
        border-bottom: 3px solid #2c5aa0;
      }
      .title {
        font-size: 24px;
        font-weight: bold;
        color: #2c5aa0;
        margin: 0 0 10px 0;
      }
      .section {
        margin-bottom: 25px;
      }
      .section-title {
        font-size: 16px;
        font-weight: bold;
        color: #2c5aa0;
        margin-bottom: 10px;
        padding: 8px;
        background-color: #f5f7fa;
        border-left: 4px solid #2c5aa0;
      }
      .info-row {
        display: flex;
        margin-bottom: 10px;
        border-bottom: 1px solid #eee;
        padding: 10px 0;
      }
      .label {
        width: 120px;
        font-weight: bold;
        color: #555;
      }
      .value {
        flex: 1;
        color: #333;
      }
      .table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 10px;
      }
      .table th,
      .table td {
        padding: 8px;
        border: 1px solid #ddd;
        text-align: left;
      }
      .table th {
        background-color: #f5f7fa;
        font-weight: bold;
      }
      .footer {
        margin-top: 30px;
        padding-top: 15px;
        border-top: 1px solid #ddd;
        text-align: center;
        font-size: 12px;
        color: #999;
      }
    </style>
    <div class="form-container">
      <!-- 标题 -->
      <div class="header">
        <div class="title">志愿者活动电子报名表</div>
      </div>
      
      <!-- 活动基本信息 -->
      <div class="section">
        <div class="section-title">活动基本信息</div>
        <div class="info-row">
          <div class="label">活动名称：</div>
          <div class="value">${data.activityTitle || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">活动时间：</div>
          <div class="value">${data.activityStartTime || ''} - ${data.activityEndTime || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">活动地点：</div>
          <div class="value">${data.activityLocation || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">活动描述：</div>
          <div class="value">${data.activityDescription || ''}</div>
        </div>
      </div>
      
      <!-- 个人信息 -->
      <div class="section">
        <div class="section-title">个人信息</div>
        <div class="info-row">
          <div class="label">姓名：</div>
          <div class="value">${data.realName || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">用户名：</div>
          <div class="value">${data.username || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">手机号：</div>
          <div class="value">${data.phone || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">邮箱：</div>
          <div class="value">${data.email || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">身份证号：</div>
          <div class="value">${data.idCard || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">报名时间：</div>
          <div class="value">${data.registrationTime || ''}</div>
        </div>
        <div class="info-row">
          <div class="label">报名状态：</div>
          <div class="value">${data.statusText || ''}</div>
        </div>
      </div>
      
      ${data.userSkills && data.userSkills.length > 0 ? `
      <!-- 个人技能 -->
      <div class="section">
        <div class="section-title">个人技能</div>
        <table class="table">
          <thead>
            <tr>
              <th>技能名称</th>
              <th>技能分类</th>
              <th>技能等级</th>
              <th>是否认证</th>
              <th>相关证书</th>
            </tr>
          </thead>
          <tbody>
            ${data.userSkills?.map((skill: UserSkill) => `
              <tr>
                <td>${skill.skillName || ''}</td>
                <td>${skill.skillCategoryName || ''}</td>
                <td>${skill.skillLevelText || ''}</td>
                <td>${skill.verifiedText || ''}</td>
                <td>${skill.certificateName || '-'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
      ` : ''}
      
      <!-- 页脚 -->
      <div class="footer">
        <p>此报名表由志愿者服务活动平台自动生成</p>
        <p>生成时间：${new Date().toLocaleString('zh-CN')}</p>
      </div>
    </div>
  `
  
  return element
}

// 关闭弹窗
const handleClose = () => {
  showConfirmDialog.value = false
}

// 开始签到 - 已屏蔽
// const startCheckin = async () => {
//   if (!canCheckin.value) {
//     ElMessage.warning('您暂不能进行签到')
//     return
//   }
//   
//   checkinDialogVisible.value = true
//   await getCurrentLocation()
// }

// 获取当前位置
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      locationError.value = '您的浏览器不支持定位功能'
      reject(new Error('Geolocation not supported'))
      return
    }
    
    locationLoading.value = true
    locationError.value = ''
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        currentLocation.value = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
        locationLoading.value = false
        resolve(position)
      },
      (error) => {
        locationLoading.value = false
        switch (error.code) {
          case error.PERMISSION_DENIED:
            locationError.value = '用户拒绝了定位请求'
            break
          case error.POSITION_UNAVAILABLE:
            locationError.value = '位置信息不可用'
            break
          case error.TIMEOUT:
            locationError.value = '定位请求超时'
            break
          default:
            locationError.value = '获取位置失败，请检查定位权限'
        }
        reject(error)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000
      }
    )
  })
}

// 关闭签到弹窗
const handleCheckinClose = () => {
  checkinDialogVisible.value = false
}

// 确认签到
const confirmCheckin = async () => {
  try {
    checkinLoading.value = true
    
    const checkinData = {
      location: currentLocation.value,
      latitude: 0, // 实际应该从GPS获取
      longitude: 0, // 实际应该从GPS获取
      qrCode: activityQRCode.value?.qrCode || ''
    }
    
    const response = await checkinApi.checkin(Number(route.params.id), checkinData)
    if (response.data) {
      ElMessage.success('签到成功！')
      checkinDialogVisible.value = false
      
      // 重新检查签到状态
      await checkUserCheckinStatus(Number(route.params.id))
    }
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '签到失败'
    ElMessage.error(errorMessage)
  } finally {
    checkinLoading.value = false
  }
}

// 获取活动二维码
const getActivityQRCode = async (activityId: number) => {
  try {
    const response = await checkinApi.getActivityQRCode(activityId) as unknown as { qrCode: string; qrImage: string; expireTime: number }
    console.log('二维码API响应:', response) // 调试日志
    if (response) {
      activityQRCode.value = response
      qrExpireTime.value = response.expireTime
    }
  } catch (error) {
    console.error('获取二维码失败:', error)
    // 不显示错误，二维码是可选的
  }
}

// 下载二维码
const downloadQRCode = () => {
  if (!activityQRCode.value?.qrImage) return
  
  const link = document.createElement('a')
  link.href = activityQRCode.value.qrImage
  link.download = `活动签到二维码_${activity.value?.title || 'unknown'}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('二维码已下载')
}

// 格式化时间
const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 联系组织方功能
const callContact = () => {
  ElMessage.info('请联系上方的活动联系人，可拨打直接电话联系！')
}

// 紧急联系功能
const callEmergency = () => {
  ElMessage.info('紧急情况请联系上方的紧急联系人！')
}

// 跳转到演示页面
const goToDemo = () => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    ElMessage.warning('您好,请先登录后再操作！')
    router.push('/login')
    return
  }
  
  const activityId = Number(route.params.id)
  router.push(`/demo/${activityId}`)
}

// 分享活动 - 显示提示信息
const share = () => {
  ElMessage.info('分享功能暂未开发，敬请期待！')
}

const progressPercent = (item: Record<string, unknown>) => {
  const a = Number(item.currentParticipants || 0)
  const b = Number(item.maxParticipants || 0)
  if (!b) return 0
  return Math.min(100, Math.round((a / b) * 100))
}

const statusText = (s: number) => {
  const statusMap: Record<number, string> = { 
    1: '草稿',
    2: '即将开始', 
    3: '进行中',
    4: '已结束',
    5: '已取消'
  }
  return statusMap[s] || '进行中'
}

const statusClass = (s: number) => {
  const classMap: Record<number, string> = {
    1: 'status-draft',
    2: 'status-upcoming',
    3: 'status-ongoing', 
    4: 'status-ended',
    5: 'status-cancelled'
  }
  return classMap[s] || 'status-ongoing'
}
</script>

<style scoped>
/* 页面头部操作区 */
.page-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.top-card { 
  border-radius: 10px; 
  position: relative; /* 添加相对定位 */
}
.activity-header { display: flex; gap: 16px; }
.cover-container { width: 260px; height: 180px; }
.cover { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
.cover-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}
.placeholder-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.6;
}
.placeholder-text {
  font-size: 14px;
  font-weight: 500;
}

/* 返回列表按钮 - 位于top-card右上角 */
.back-button {
  position: absolute;
  right: 20px;
  top: 20px;
  z-index: 10;
}

.title { margin: 0 0 8px 0; font-size: 24px; }
.meta { color: #666; display: flex; gap: 16px; margin-bottom: 8px; font-size: 14px; }
.status { padding: 5px 12px; border-radius: 16px; display: inline-block; font-size: 12px; font-weight: 700; margin-bottom: 8px; }
.status-ongoing { background: #d4edda; color: #155724; }
.status-upcoming { background: #fff3cd; color: #856404; }
.status-ended { background: #f8d7da; color: #721c24; }
.tags { display: flex; gap: 8px; }
.tag { padding: 4px 8px; background: #e9ecef; color: #666; border-radius: 12px; font-size: 12px; }

.detail-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.left, .right { border-radius: 10px; }

.section { margin-bottom: 16px; }
.section-title { font-size: 18px; font-weight: 700; color: #333; padding-bottom: 8px; border-bottom: 2px solid #2c5aa0; margin-bottom: 8px; }
.section-content { color: #666; line-height: 1.8; }

.progress { margin-bottom: 16px; }
.progress-bar { width: 100%; height: 8px; background: #e9ecef; border-radius: 4px; overflow: hidden; margin-bottom: 8px; }
.progress-fill { height: 100%; background: #2c5aa0; }
.progress-text { color: #666; font-size: 14px; text-align: center; }
.actions { display: flex; flex-direction: column; gap: 12px; }

/* 确认弹窗样式 */
.confirm-content {
  padding: 16px 0;
}

.activity-info h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 18px;
}

.user-info {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.user-info h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item .label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
}

.confirm-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
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

/* 签到相关样式 */
.qr-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.qr-header {
  text-align: center;
  margin-bottom: 20px;
}

.qr-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.qr-content {
  text-align: center;
}

.qr-image-container {
  margin: 20px 0;
}

.qr-image {
  width: 200px;
  height: 200px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.qr-info {
  margin: 15px 0;
}

.qr-tip {
  font-size: 16px;
  color: #606266;
  margin: 10px 0;
}

.qr-expire {
  font-size: 14px;
  color: #909399;
  margin: 5px 0;
}

.qr-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.checkin-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.checkin-header h3 {
  margin: 0;
  color: #28a745;
  font-size: 16px;
  font-weight: 600;
}

.checkin-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.checkin-info p {
  margin: 8px 0;
  color: #666;
}

.checkin-tip {
  color: #409EFF !important;
  font-size: 14px;
  font-weight: 500;
}

.checked-in-section {
  margin-top: 20px;
  padding: 16px;
  background: #d4edda;
  border-radius: 8px;
  border: 1px solid #c3e6cb;
}

.checkin-success {
  text-align: center;
}

.success-icon {
  font-size: 32px;
  color: #28a745;
  margin-bottom: 8px;
}

.checkin-success h3 {
  margin: 0 0 8px 0;
  color: #155724;
  font-size: 16px;
}

.checkin-success p {
  margin: 0 0 12px 0;
  color: #155724;
  font-size: 14px;
}

.checkin-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #155724;
}

/* 签到弹窗样式 */
.checkin-content {
  padding: 20px 0;
}

.location-verification,
.qr-display {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.location-header,
.qr-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
}

.location-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.location-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.location-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc3545;
  font-size: 14px;
}

.qr-image-container {
  text-align: center;
  margin-bottom: 16px;
}

.qr-image {
  width: 200px;
  height: 200px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  margin-bottom: 8px;
}

.qr-info {
  text-align: center;
  margin-bottom: 16px;
}

.qr-tip {
  font-size: 16px;
  color: #606266;
  margin: 10px 0;
}

.qr-expire {
  font-size: 14px;
  color: #909399;
  margin: 5px 0;
}

.qr-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.qr-placeholder p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

/* 紧急联络区域样式 */
.emergency-contact-section {
  margin-top: 20px;
  padding: 16px;
  background: #fff5f5;
  border-radius: 8px;
  border: 1px solid #fed7d7;
}

.emergency-contact-section .section-title {
  font-size: 16px;
  font-weight: 600;
  color: #e53e3e;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.emergency-contact-section .section-title::before {
  content: "🚨";
  font-size: 18px;
}

.contact-info {
  margin-bottom: 16px;
}

.contact-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.contact-item .label {
  font-weight: 600;
  color: #666;
  min-width: 100px;
}

.contact-item .value {
  color: #333;
}

.phone-link {
  color: #e53e3e;
  text-decoration: none;
  font-weight: 600;
}

.phone-link:hover {
  text-decoration: underline;
}

.emergency-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.checkin-confirm {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
}

@media (max-width: 768px) {
  .activity-header { flex-direction: column; }
  .cover-container { width: 100%; height: 200px; }
  .detail-grid { grid-template-columns: 1fr; }
  
  .checkin-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .location-info {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .checkin-details {
    text-align: left;
  }
}
</style>



