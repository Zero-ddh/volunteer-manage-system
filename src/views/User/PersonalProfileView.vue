<template>
  <div class="personal-profile">
    <div class="profile-header">
      <h2>个人档案</h2>
      <el-button type="primary" @click="exportProfile">
        <el-icon><Download /></el-icon>
        导出个人档案
      </el-button>
    </div>
    
    <div class="profile-content">
      <!-- 基本信息 -->
      <el-card class="profile-card">
        <template #header>
          <span>基本信息</span>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">姓名：</span>
            <span class="value">{{ userInfo.realName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">总服务时长：</span>
            <span class="value">{{ profileData.totalServiceHours || 0 }}小时</span>
          </div>
          <div class="info-item">
            <span class="label">志愿者等级：</span>
            <el-tag :type="getLevelType(profileData.certificateLevel)">
              {{ profileData.certificateLevel || '普通志愿者' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">平台状态：</span>
            <el-tag :type="profileData.platformStatus === '已连接' ? 'success' : 'warning'">
              {{ profileData.platformStatus || '未连接' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">最后同步时间：</span>
            <span class="value">{{ formatDateTime(profileData.lastSyncTime) }}</span>
          </div>
        </div>
      </el-card>
      
      <!-- 服务统计 -->
      <el-card class="profile-card">
        <template #header>
          <span>服务统计</span>
        </template>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-value">{{ profileData.totalServiceHours || 0 }}</div>
              <div class="stat-label">总服务时长(小时)</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-value">{{ profileData.totalRecords || 0 }}</div>
              <div class="stat-label">服务次数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-value">{{ certificates.length }}</div>
              <div class="stat-label">获得证书</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 证书列表 -->
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>志愿服务证书</span>
            <el-button type="primary" size="small" @click="generateOfficialCertificate" :disabled="profileData.totalServiceHours < 10">
              <el-icon><Medal /></el-icon>
              生成官方认证证书
            </el-button>
          </div>
        </template>
        
        <div v-if="certificates.length === 0" class="empty-state">
          <el-empty description="暂无证书" />
        </div>
        
        <el-table v-else :data="certificates" style="width: 100%">
          <el-table-column prop="certificateName" label="证书名称" />
          <el-table-column prop="certificateNumber" label="证书编号" />
          <el-table-column prop="issuingAuthority" label="颁发机构" />
          <el-table-column prop="issueDate" label="颁发日期">
            <template #default="{ row }">
              {{ formatDate(row.issueDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button size="small" @click="downloadCertificate(row)" v-if="row.certificateFile">
                下载
              </el-button>
              <el-button size="small" type="success" @click="verifyCertificate(row.certificateNumber)">
                验证
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 服务记录摘要 -->
      <el-card class="profile-card">
        <template #header>
          <span>最近服务记录</span>
        </template>
        <el-table :data="recentRecords" style="width: 100%">
          <el-table-column prop="activityTitle" label="活动名称" />
          <el-table-column prop="serviceDate" label="服务日期">
            <template #default="{ row }">
              {{ formatDate(row.serviceDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="serviceHours" label="服务时长(小时)" />
          <el-table-column prop="location" label="服务地点" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Medal } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const userInfo = ref<any>({})
const profileData = ref<any>({})
const certificates = ref<any[]>([])
const recentRecords = ref<any[]>([])

// 接口类型定义
interface ProfileData {
  totalServiceHours?: number
  certificateLevel?: string
  platformStatus?: string
  lastSyncTime?: string
  totalRecords?: number
}

interface Certificate {
  id: number
  certificateName: string
  certificateNumber: string
  issuingAuthority: string
  issueDate: string
  status: number
  certificateFile?: string
}

interface ServiceRecord {
  id: number
  activityTitle: string
  serviceDate: string
  serviceHours: number
  location: string
}

// 页面加载
onMounted(async () => {
  await loadProfileData()
  await loadCertificates()
  await loadRecentRecords()
})

// 加载个人档案数据
const loadProfileData = async () => {
  try {
    const response = await request.get<ProfileData>('/v1/platform/personal-profile')
    profileData.value = response.data || {}
  } catch (error) {
    console.error('获取个人档案失败:', error)
    ElMessage.error('获取个人档案失败')
  }
}

// 加载证书列表
const loadCertificates = async () => {
  try {
    const response = await request.get<Certificate[]>('/v1/skills/certificates')
    certificates.value = response.data || []
  } catch (error) {
    console.error('获取证书列表失败:', error)
    ElMessage.error('获取证书列表失败')
  }
}

// 加载最近服务记录
const loadRecentRecords = async () => {
  try {
    const response = await request.get<ServiceRecord[]>('/v1/service-records/recent')
    recentRecords.value = response.data || []
  } catch (error) {
    console.error('获取服务记录失败:', error)
    ElMessage.error('获取服务记录失败')
  }
}

// 生成官方认证证书
const generateOfficialCertificate = async () => {
  try {
    const response = await request.post('/v1/platform/generate-official-certificate')
    // 修复：后端返回的数据结构是 { code, message, data: { success, ... } }
    const responseData = response.data as { code?: number; message?: string; data?: { success?: boolean; certificateNumber?: string; message?: string; certificateLevel?: string; verificationUrl?: string } }
    
    if (responseData.data?.success) {
      const data = responseData.data
      ElMessage.success(`官方认证证书生成成功！证书编号：${data.certificateNumber}`)
      
      ElMessageBox.alert(
        `证书等级：${data.certificateLevel}\n证书编号：${data.certificateNumber}\n验证链接：${data.verificationUrl}`,
        '证书生成成功',
        { type: 'success' }
      )
      
      await loadCertificates()
    } else {
      ElMessage.error(responseData.data?.message || '证书生成失败')
    }
  } catch (error: unknown) {
    console.error('生成官方认证证书失败:', error)
    const errorMessage = error instanceof Error ? error.message : '证书生成失败'
    ElMessage.error(errorMessage)
  }
}

// 验证证书
const verifyCertificate = async (certificateNumber: string) => {
  try {
    const response = await request.post('/v1/platform/verify-official-certificate', {
      certificateNumber
    })
    
    const data = response.data as { valid?: boolean; message?: string; verificationUrl?: string }
    
    if (data.valid) {
      ElMessage.success('证书验证成功！')
      ElMessageBox.alert(
        `证书编号：${certificateNumber}\n验证结果：有效\n官方验证链接：${data.verificationUrl}`,
        '验证成功',
        { type: 'success' }
      )
    } else {
      ElMessage.error('证书验证失败')
    }
  } catch (error: unknown) {
    console.error('证书验证失败:', error)
    ElMessage.error('证书验证失败')
  }
}

// 下载证书
const downloadCertificate = (certificate: Certificate) => {
  if (!certificate.certificateFile) {
    ElMessage.warning('证书文件不存在')
    return
  }
  
  try {
    const link = document.createElement('a')
    link.href = certificate.certificateFile
    link.download = certificate.certificateName || 'certificate'
    link.target = '_blank'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('开始下载证书文件')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请检查文件是否存在')
  }
}

// 导出个人档案
const exportProfile = () => {
  const profileText = `
个人志愿服务档案

基本信息：
姓名：${userInfo.value.realName || '未设置'}
总服务时长：${profileData.value.totalServiceHours || 0}小时
志愿者等级：${profileData.value.certificateLevel || '普通志愿者'}
平台状态：${profileData.value.platformStatus || '未连接'}
最后同步时间：${formatDateTime(profileData.value.lastSyncTime)}

服务统计：
总服务时长：${profileData.value.totalServiceHours || 0}小时
服务次数：${profileData.value.totalRecords || 0}次
获得证书：${certificates.value.length}个

证书列表：
${certificates.value.map(cert => 
  `- ${cert.certificateName} (${cert.certificateNumber})`
).join('\n')}

最近服务记录：
${recentRecords.value.map(record => 
  `- ${record.activityTitle} (${record.serviceDate}) - ${record.serviceHours}小时`
).join('\n')}

导出时间：${new Date().toLocaleString()}
  `.trim()
  
  const blob = new Blob([profileText], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `个人志愿服务档案_${new Date().toISOString().split('T')[0]}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('个人档案导出成功')
}

// 辅助方法
const getLevelType = (level: string) => {
  if (level?.includes('金牌')) return 'danger'
  if (level?.includes('银牌')) return 'warning'
  if (level?.includes('铜牌')) return 'success'
  return 'info'
}

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '待审核'
    case 2: return '已通过'
    case 3: return '已拒绝'
    default: return '未知'
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}
</script>

<style scoped>
.personal-profile {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.profile-header h2 {
  margin: 0;
  color: #303133;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-item .label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
  min-width: 100px;
}

.info-item .value {
  color: #303133;
}

.stat-item {
  text-align: center;
  padding: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.empty-state {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .personal-profile {
    padding: 16px;
  }
  
  .profile-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>
