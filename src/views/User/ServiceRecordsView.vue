<template>
  <div class="service-records-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h2>服务记录</h2>
          <p>记录您的志愿服务过程，分享服务心得与感悟</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openAddRecordDialog">
            <el-icon><Plus /></el-icon>
            添加服务记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">⏰</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalHours }}</div>
              <div class="stat-label">累计服务时长(小时)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">🎯</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.serviceCount }}</div>
              <div class="stat-label">服务次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">📍</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.locationCount }}</div>
              <div class="stat-label">服务地点</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模拟平台功能区域 -->
    <el-row :gutter="16" class="action-cards">
      
      <el-col :span="12">
        <el-card class="action-card">
          <div class="action-content">
            <div class="action-icon">📜</div>
            <div class="action-info">
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                <h3 style="margin: 0;">生成官方认证证书</h3>
                <el-tooltip 
                  content="证书等级规则&#10;根据服务时长自动确定证书等级:&#10;10-49小时:普通志愿者&#10;50-99小时:铜牌志愿者&#10;100-199小时:银牌志愿者&#10;200小时以上:金牌志愿者"
                  placement="top"
                  effect="dark"
                >
                  <el-icon style="color: #909399; cursor: help; font-size: 16px;">
                    <QuestionFilled />
                  </el-icon>
                </el-tooltip>
              </div>
              <p>基于服务时长生成官方认证志愿服务证书</p>
              <el-button 
                type="primary" 
                @click="generateOfficialCertificate" 
                :loading="generatingOfficial"
                size="small"
              >
                <el-icon><Medal /></el-icon>
                生成官方认证证书
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="action-card">
          <div class="action-content">
            <div class="action-icon">🔄</div>
            <div class="action-info">
              <h3>同步官方志愿平台</h3>
              <p>模拟同步服务时长到官方志愿平台</p>
              <el-button type="warning" @click="syncToPlatform" :loading="syncing" size="small">
                <el-icon><Upload /></el-icon>
                同步数据
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col> 
    </el-row>

    <!-- 屏蔽官方平台对接区域 -->
    <!-- <el-row :gutter="16" class="official-platform-section">
      <el-col :span="24">
        <el-card class="platform-card">
          <template #header>
            <div class="platform-header">
              <span>官方平台对接</span>
              <el-tag type="success" size="small">100%成功率</el-tag>
            </div>
          </template>
          
          <div class="platform-actions">
            <el-button 
              type="primary" 
              :loading="generatingOfficial"
              @click="generateOfficialCertificate"
              :disabled="stats.totalHours < 10"
              size="large"
            >
              <el-icon><Medal /></el-icon>
              生成官方认证证书
            </el-button>
            
            <el-button 
              type="success" 
              :loading="verifying"
              @click="showVerifyDialog"
              size="large"
            >
              <el-icon><Check /></el-icon>
              验证证书有效性
            </el-button>
            
            <el-button 
              type="info" 
              @click="viewPersonalProfile"
              size="large"
            >
              <el-icon><User /></el-icon>
              查看个人档案
            </el-button>
          </div>
          
          <div v-if="stats.totalHours < 10" class="tip-info">
            <el-alert 
              title="提示" 
              type="info" 
              :closable="false"
              show-icon
            >
              服务时长需达到10小时以上才能生成官方认证证书，当前时长：{{ stats.totalHours }}小时
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row> -->

    <!-- 屏蔽证书验证对话框 -->
    <!-- <el-dialog v-model="verifyDialogVisible" title="证书验证" width="500px">
      <el-form :model="verifyForm" label-width="100px">
        <el-form-item label="证书编号">
          <el-input 
            v-model="verifyForm.certificateNumber" 
            placeholder="请输入证书编号"
            clearable
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="verifyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="verifyCertificate">验证</el-button>
      </template>
    </el-dialog> -->

    <el-tabs v-model="active" @tab-change="handleTabChange">
      <el-tab-pane label="服务记录" name="records">
        <el-table :data="records" v-loading="loading" @row-click="viewRecordDetail">
          <el-table-column prop="activityTitle" label="活动" min-width="150" />
          <el-table-column prop="serviceDate" label="服务日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.serviceDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="服务地点" width="300" />
          <el-table-column label="典型案例" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isTypicalCase" type="success" size="small">
                <el-icon><Star /></el-icon>
                典型案例
              </el-tag>
              <span v-else style="color: #c0c4cc;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click.stop="editRecord(row)">编辑</el-button>
              <el-button size="small" type="primary" @click.stop="viewRecordDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
          style="margin-top: 20px; text-align: right;"
        />
      </el-tab-pane>
      
      <el-tab-pane label="服务足迹" name="footprints">
        <div class="footprint-map">
          <div id="map-container" style="height: 400px; width: 100%; border-radius: 8px; overflow: hidden;"></div>
        </div>
        <!-- 服务地点统计模块已屏蔽 -->
        <!-- <div class="footprint-list">
          <h3>服务地点统计</h3>
          <el-table :data="locationStats" v-loading="loading">
            <el-table-column prop="location" label="地点" />
            <el-table-column prop="serviceCount" label="服务次数" width="120" />
            <el-table-column prop="totalHours" label="总时长" width="120" />
          </el-table>
        </div> -->
      </el-tab-pane>
      
      <el-tab-pane label="我的志愿服务证书" name="certificates">
        <div class="certificate-section">
          <div class="certificate-header">
            <h3>我的志愿服务证书</h3>
            <p>查看和管理您的志愿服务证书</p>
          </div>
          
          <el-table :data="certificates" v-loading="certificateLoading" style="margin-top: 20px;">
            <el-table-column prop="certificateName" label="证书名称" min-width="150" />
            <el-table-column prop="certificateNumber" label="证书编号" min-width="200" />
            <el-table-column prop="issuingAuthority" label="颁发机构" min-width="150" />
            <el-table-column prop="issueDate" label="颁发日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.issueDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="expiryDate" label="有效期至" width="120">
              <template #default="{ row }">
                {{ row.expiryDate ? formatDate(row.expiryDate) : '永久有效' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getCertificateStatusType(row.status)">
                  {{ getCertificateStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createdTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <div style="display: flex; gap: 8px; white-space: nowrap;">
                  <!-- 屏蔽下载PDF按钮 -->
                  <!-- <el-button 
                    type="primary" 
                    size="small" 
                    @click="downloadCertificate(row)"
                    :disabled="row.status !== 2"
                  >
                    <el-icon><Download /></el-icon>
                    下载PDF
                  </el-button> -->
                  <el-button 
                    type="info" 
                    size="small" 
                    @click="viewCertificateDetail(row)"
                  >
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="certificatePageNo"
            v-model:page-size="certificatePageSize"
            :page-sizes="[5, 10, 20]"
            :total="certificateTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadCertificates"
            @current-change="loadCertificates"
            style="margin-top: 20px; text-align: right;"
          />
        </div>
      </el-tab-pane>
      
      <!-- 屏蔽积分记录标签页，因为已有专门的积分记录页面 -->
      <!-- <el-tab-pane label="积分记录" name="points">
        <el-table :data="points" v-loading="loading">
          <el-table-column prop="createdTime" label="时间" width="180" />
          <el-table-column prop="points" label="积分" width="120" />
          <el-table-column prop="source" label="来源/说明" min-width="200" />
          <el-table-column prop="balance" label="余额" width="120" />
        </el-table>
      </el-tab-pane> -->
    </el-tabs>

    <!-- 添加/编辑服务记录对话框 -->
    <el-dialog 
      v-model="recordDialogVisible" 
      :title="isEdit ? '编辑服务记录' : '添加服务记录'"
      width="800px"
    >
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动" prop="activityId">
              <el-select v-model="recordForm.activityId" placeholder="选择活动" style="width: 100%">
                <el-option 
                  v-for="activity in activities" 
                  :key="activity.id" 
                  :label="activity.title" 
                  :value="activity.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务日期" prop="serviceDate">
              <el-date-picker 
                v-model="recordForm.serviceDate" 
                type="date" 
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="服务质量" prop="serviceQuality">
              <el-rate v-model="recordForm.serviceQuality" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="服务地点" prop="location">
          <el-input 
            v-model="recordForm.location" 
            placeholder="请输入服务地点，建议包含省市区县信息（如：广东省广州市白云区xxx街道）" 
            type="textarea"
            :rows="2"
            @blur="handleAddressChange(recordForm.location)"
            @input="debounceAddressInput"
          />
          <div style="margin-top: 10px; display: flex; justify-content: flex-end;">
            <el-button @click="getCurrentLocation" :loading="locationLoading" size="default">
              <el-icon><Location /></el-icon>
              GPS定位
            </el-button>
          </div>
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            建议输入完整地址（省市区县街道），系统将自动获取经纬度用于服务足迹展示
          </div>
          <div v-if="recordForm.latitude && recordForm.longitude" style="margin-top: 5px; color: #67c23a; font-size: 12px;">
            ✓ 已获取位置：{{ recordForm.latitude.toFixed(6) }}, {{ recordForm.longitude.toFixed(6) }}
          </div>
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input 
            v-model="recordForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请描述服务内容和过程"
          />
        </el-form-item>
        
        <el-form-item label="服务心得" prop="experience">
          <el-input 
            v-model="recordForm.experience" 
            type="textarea" 
            :rows="4"
            placeholder="请分享您的服务心得和感悟"
          />
        </el-form-item>
        
        <el-form-item label="服务照片">
          <el-upload
            v-model:file-list="photoFileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handlePhotoSuccess"
            :on-error="handlePhotoError"
            :before-upload="beforePhotoUpload"
            list-type="picture-card"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <!-- 典型案例相关字段 -->
        <el-divider content-position="left">
          <span style="color: #409EFF; font-weight: bold;">典型案例设置</span>
        </el-divider>
        
        <el-form-item label="设为典型案例">
          <el-switch 
            v-model="recordForm.isTypicalCase" 
            active-text="是" 
            inactive-text="否"
            @change="handleTypicalCaseChange"
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            开启后将作为典型案例展示和分享
          </span>
        </el-form-item>

        <div v-if="recordForm.isTypicalCase" class="typical-case-fields">
          <el-form-item label="案例标题" prop="caseTitle">
            <el-input 
              v-model="recordForm.caseTitle" 
              placeholder="请输入案例标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="案例类型" prop="caseType">
            <el-select v-model="recordForm.caseType" placeholder="选择案例类型" style="width: 100%">
              <el-option label="感人故事" :value="1" />
              <el-option label="技能应用" :value="2" />
              <el-option label="团队协作" :value="3" />
              <el-option label="成长感悟" :value="4" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="案例内容" prop="caseContent">
            <el-input 
              v-model="recordForm.caseContent" 
              type="textarea" 
              :rows="5"
              placeholder="请详细描述这个典型案例的经过、感受和意义"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="案例标签">
            <el-input 
              v-model="recordForm.caseTags" 
              placeholder="请输入标签，用逗号分隔，如：环保,感动,团队"
              maxlength="500"
              show-word-limit
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px;">
              建议标签：环保、教育、敬老、医疗、团队、感动、技能、成长等
            </div>
          </el-form-item>
        </div>
      </el-form>
      
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="saving">
          {{ isEdit ? '更新' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 服务记录详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="服务记录详情"
      width="900px"
    >
      <div v-if="currentRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动名称">{{ currentRecord.activityTitle }}</el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ formatDate(currentRecord.serviceDate) }}</el-descriptions-item>
          <el-descriptions-item label="服务地点">{{ currentRecord.location }}</el-descriptions-item>
          <el-descriptions-item label="服务质量">
            <el-rate v-model="currentRecord.serviceQuality" disabled show-score />
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h3>服务描述</h3>
          <p>{{ currentRecord.description }}</p>
        </div>
        
        <div class="detail-section" v-if="currentRecord.experience">
          <h3>服务心得</h3>
          <p>{{ currentRecord.experience }}</p>
        </div>
        
        <div class="detail-section" v-if="currentRecord.photos">
          <h3>服务照片</h3>
          <div class="photo-gallery">
            <el-image
              v-for="(photo, index) in getPhotoList(currentRecord.photos)"
              :key="index"
              :src="photo"
              :preview-src-list="getPhotoList(currentRecord.photos)"
              fit="cover"
              style="width: 120px; height: 120px; margin-right: 10px; margin-bottom: 10px;"
            />
          </div>
        </div>

        <!-- 典型案例信息 -->
        <div class="detail-section" v-if="currentRecord.isTypicalCase">
          <h3>典型案例信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="案例标题">{{ currentRecord.caseTitle }}</el-descriptions-item>
            <el-descriptions-item label="案例类型">
              <el-tag :type="getCaseTypeTagType(currentRecord.caseType || 0)">
                {{ getCaseTypeName(currentRecord.caseType || 0) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="案例标签" :span="2">
              <el-tag 
                v-for="tag in getCaseTags(currentRecord.caseTags || '')" 
                :key="tag" 
                style="margin-right: 5px;"
              >
                {{ tag }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <div style="margin-top: 15px;">
            <h4>案例内容</h4>
            <p style="line-height: 1.6; color: #606266;">{{ currentRecord.caseContent }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, Timer, Document, Upload, Location, QuestionFilled } from '@element-plus/icons-vue'
// 移除serviceApi导入，因为不再需要积分记录相关功能
// import { serviceApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import type { UploadFile } from 'element-plus'

// 类型定义
interface ServiceRecord {
  id: number
  activityId: number
  activityTitle?: string
  serviceDate: string
  serviceHours: number
  serviceQuality: number
  location: string
  latitude?: number
  longitude?: number
  description: string
  experience?: string
  photos?: string
  points?: number  // 添加积分字段
  isTypicalCase: number
  caseTitle?: string
  caseContent?: string
  caseType?: number
  caseTags?: string
  caseViewCount?: number
  caseLikeCount?: number
  isFeaturedCase: number
}

// 移除积分记录接口定义，因为已有专门的积分记录页面
// interface PointsRecord {
//   createdTime: string
//   points: number
//   source: string
//   balance: number
// }

interface LocationStat {
  location: string
  serviceCount: number
  totalHours: number
}

interface Certificate {
  id: number
  certificateName: string
  certificateNumber: string
  issuingAuthority: string
  issueDate: string
  expiryDate?: string
  status: number
  auditRemark?: string
  createdTime: string
}

interface Activity {
  id: number
  title: string
}

interface ServiceStats {
  totalHours: number
  serviceCount: number
  locationCount: number
}

// 为了兼容现有的响应结构，定义更具体的响应类型
interface AutoCalculateResponse {
  data?: {
    message?: string
  }
}

interface CertificateResponse {
  data?: {
    success?: boolean
    certificateNumber?: string
    message?: string
  }
}

interface SyncResponse {
  data?: {
    message?: string
  }
}

interface VerifyResponse {
  data?: {
    valid?: boolean
    message?: string
    verificationUrl?: string
  }
}

interface ProfileResponse {
  data?: {
    totalServiceHours?: number
    certificateLevel?: string
    platformStatus?: string
    lastSyncTime?: string
  }
}

// 响应式数据
const loading = ref(false)
const active = ref('records')
const records = ref<ServiceRecord[]>([])
// 移除积分记录相关变量，因为已有专门的积分记录页面
// const points = ref<PointsRecord[]>([])
// const totalPoints = ref(0)
const stats = ref<ServiceStats>({
  totalHours: 0,
  serviceCount: 0,
  locationCount: 0
})
const locationStats = ref<LocationStat[]>([])

// 分页数据
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 证书相关数据
const certificates = ref<Certificate[]>([])
const certificateLoading = ref(false)
const certificatePageNo = ref(1)
const certificatePageSize = ref(10)
const certificateTotal = ref(0)

// 对话框相关
const recordDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

// 模拟平台功能状态
const calculating = ref(false)
const generating = ref(false)
const syncing = ref(false)

// 官方平台功能状态
const generatingOfficial = ref(false)
const verifying = ref(false)
const verifyDialogVisible = ref(false)

// 证书验证表单
const verifyForm = ref({
  certificateNumber: ''
})

// 位置相关状态
const locationLoading = ref(false)

// 表单数据类型
interface RecordFormData {
  id: number
  activityId: number | null
  serviceDate: string | null
  serviceHours: number
  serviceQuality: number
  location: string
  latitude: number | null
  longitude: number | null
  description: string
  experience: string
  weather: string
  participantsCount: number
  isTypicalCase: boolean
  caseTitle: string
  caseContent: string
  caseType: number | null
  caseTags: string
  caseViewCount: number
  caseLikeCount: number
  isFeaturedCase: boolean
}

// 表单数据
const recordForm = ref<RecordFormData>({
  id: 0,
  activityId: null,
  serviceDate: null,
  serviceHours: 0,
  serviceQuality: 5,
  location: '',
  latitude: null,
  longitude: null,
  description: '',
  experience: '',
  weather: '',
  participantsCount: 0,
  isTypicalCase: false,
  caseTitle: '',
  caseContent: '',
  caseType: null,
  caseTags: '',
  caseViewCount: 0,
  caseLikeCount: 0,
  isFeaturedCase: false
})

const recordFormRef = ref()

// 活动列表
const activities = ref<Activity[]>([])

// 当前记录
const currentRecord = ref<ServiceRecord | null>(null)

// 照片上传
const photoFileList = ref<UploadFile[]>([])
const uploadUrl = computed(() => '/api/v1/upload/image')
const uploadHeaders = computed(() => {
  const authStore = useAuthStore()
  return {
    'Authorization': `Bearer ${authStore.token}`
  }
})

// 表单验证规则
const recordRules = {
  activityId: [{ required: true, message: '请选择活动', trigger: 'change' }],
  serviceDate: [{ required: true, message: '请选择服务日期', trigger: 'change' }],
  // 移除serviceHours的必填验证，因为现在是自动计算
  location: [
    { required: true, message: '请输入服务地点', trigger: 'blur' },
    { 
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (!value || value.trim().length < 3) {
          callback(new Error('请输入有效的服务地点'))
        } else {
          // 移除严格的省市区校验，只做基本长度校验
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  // 典型案例验证规则
  caseTitle: [
    { 
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (recordForm.value.isTypicalCase && !value) {
          callback(new Error('请输入案例标题'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  caseType: [
    { 
      validator: (_rule: unknown, value: number, callback: (error?: Error) => void) => {
        if (recordForm.value.isTypicalCase && !value) {
          callback(new Error('请选择案例类型'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  caseContent: [
    { 
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (recordForm.value.isTypicalCase && !value) {
          callback(new Error('请输入案例内容'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 加载数据
const load = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadServiceRecords(),
      loadServiceStats(),
      loadLocationStats()
      // 移除：loadPointsRecords()
    ])
  } finally {
    loading.value = false
  }
}

// 加载服务记录
const loadServiceRecords = async () => {
  try {
    const response = await request.get('/v1/service-record/user/list', {
      params: {
        pageNo: pageNo.value,
        pageSize: pageSize.value
      }
    })
    // 后端返回的是 Result<Page<ServiceRecord>> 结构
    // 检查响应数据结构
    const responseData = response as { records?: ServiceRecord[]; total?: number; data?: { records?: ServiceRecord[]; total?: number } }
    if (responseData.records) {
      // 直接访问 records 属性
      records.value = responseData.records || []
      total.value = responseData.total || 0
    } else if (responseData.data && responseData.data.records) {
      // 通过 data 属性访问
      records.value = responseData.data.records || []
      total.value = responseData.data.total || 0
    } else {
      records.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载服务记录失败:', error)
    ElMessage.error('加载服务记录失败')
  }
}

// 加载服务统计
const loadServiceStats = async () => {
  try {
    const response = await request.get('/v1/service-record/user/stats')
    // 检查响应数据结构
    const responseData = response as { 
      totalHours?: number; 
      serviceCount?: number; 
      locationCount?: number;
      data?: { 
        totalHours?: number; 
        serviceCount?: number; 
        locationCount?: number;
      } 
    }
    if (responseData.data) {
      stats.value = {
        totalHours: responseData.data.totalHours || 0,
        serviceCount: responseData.data.serviceCount || 0,
        locationCount: responseData.data.locationCount || 0
      }
    } else if (responseData.totalHours !== undefined) {
      stats.value = {
        totalHours: responseData.totalHours || 0,
        serviceCount: responseData.serviceCount || 0,
        locationCount: responseData.locationCount || 0
      }
    } else {
      stats.value = { totalHours: 0, serviceCount: 0, locationCount: 0 }
    }
  } catch (error) {
    console.error('加载服务统计失败:', error)
  }
}

// 加载地点统计
const loadLocationStats = async () => {
  try {
    const response = await request.get('/v1/service-record/user/location-stats')
    // 检查响应数据结构
    if (response.data) {
      locationStats.value = response.data
    } else if (Array.isArray(response)) {
      locationStats.value = response
    } else {
      locationStats.value = []
    }
  } catch (error) {
    console.error('加载地点统计失败:', error)
  }
}

// 移除积分记录加载函数，因为已有专门的积分记录页面
// const loadPointsRecords = async () => {
//   try {
//     const pointsData = await serviceApi.getPointsRecords()
//     points.value = Array.isArray(pointsData) ? pointsData : []
//     totalPoints.value = points.value.length ? points.value[points.value.length - 1]?.balance || 0 : 0
//   } catch (error) {
//     console.error('加载积分记录失败:', error)
//   }
// }

// 移除积分计算函数，因为已屏蔽积分卡片
// const calculateTotalPoints = () => {
//   if (!records.value || records.value.length === 0) {
//     return 0
//   }
//   
//   // 计算所有服务记录的积分总和
//   return records.value.reduce((total, record) => {
//     return total + (record.points || 0)
//   }, 0)
// }

// 加载活动列表
const loadActivities = async () => {
  try {
    const response = await request.get('/v1/user/activities')
    // 检查响应数据结构
    if (response.data) {
      activities.value = response.data
    } else if (Array.isArray(response)) {
      activities.value = response
    } else {
      activities.value = []
    }
  } catch (error: unknown) {
    console.error('加载用户参与活动失败:', error)
    const errorMessage = error instanceof Error ? error.message : '未知错误'
    ElMessage.error('加载用户参与活动失败: ' + errorMessage)
  }
}

// 打开添加记录对话框
const openAddRecordDialog = async () => {
  // 检查用户登录状态
  const authStore = useAuthStore()
  if (!authStore.isLoggedIn) {
    ElMessage.error('请先登录')
    return
  }
  
  isEdit.value = false
  recordForm.value = {
    id: 0,
    activityId: null,
    serviceDate: null,
    serviceHours: 0,
    serviceQuality: 5,
    location: '',
    latitude: null,  // 重置经纬度
    longitude: null,
    description: '',
    experience: '',
    weather: '',
    participantsCount: 0,
    // 典型案例相关字段
    isTypicalCase: false,
    caseTitle: '',
    caseContent: '',
    caseType: null,
    caseTags: '',
    caseViewCount: 0,
    caseLikeCount: 0,
    isFeaturedCase: false
  }
  photoFileList.value = []
  await loadActivities()
  recordDialogVisible.value = true
}

// 编辑记录
const editRecord = async (record: ServiceRecord) => {
  isEdit.value = true
  recordForm.value = {
    id: record.id,
    activityId: record.activityId,
    serviceDate: record.serviceDate,
    serviceHours: record.serviceHours,
    serviceQuality: record.serviceQuality,
    location: record.location,
    latitude: record.latitude || null,
    longitude: record.longitude || null,
    description: record.description,
    experience: record.experience || '',
    weather: '',
    participantsCount: 0,
    isTypicalCase: record.isTypicalCase === 1,
    caseTitle: record.caseTitle || '',
    caseContent: record.caseContent || '',
    caseType: record.caseType || null,
    caseTags: record.caseTags || '',
    caseViewCount: record.caseViewCount || 0,
    caseLikeCount: record.caseLikeCount || 0,
    isFeaturedCase: record.isFeaturedCase === 1
  }
  // 处理图片显示 - 使用getPhotoList处理URL转换
  const photoUrls = getPhotoList(record.photos || '')
  photoFileList.value = photoUrls.map((url: string) => ({
    url,
    name: url.split('/').pop() || 'photo'
  }))
  await loadActivities()
  recordDialogVisible.value = true
}

// 查看记录详情
const viewRecordDetail = (record: ServiceRecord) => {
  currentRecord.value = record
  detailDialogVisible.value = true
}

// 保存记录
const saveRecord = async () => {
  try {
    await recordFormRef.value.validate()
    saving.value = true
    
    const recordData = { ...recordForm.value }
    
    // 处理日期格式 - 只保留日期部分
    if (recordData.serviceDate) {
      const date = new Date(recordData.serviceDate)
      recordData.serviceDate = date.toISOString().split('T')[0] || null // 格式化为 YYYY-MM-DD
    }
    
    // 处理布尔值转换为整数
    const recordDataForApi = {
      ...recordData,
      isTypicalCase: recordData.isTypicalCase ? 1 : 0,
      isFeaturedCase: recordData.isFeaturedCase ? 1 : 0
    }
    
    // 处理照片URL - 只使用有效的服务器URL，过滤掉blob URL
    if (photoFileList.value.length > 0) {
      const photoUrls = photoFileList.value
        .map(file => file.url || (file as unknown as { response?: { data?: { url?: string } } }).response?.data?.url)
        .filter(url => url && !url.startsWith('blob:')) // 过滤掉blob URL
        .map(url => {
          // 如果是完整URL，转换为相对URL用于存储
          if (url && url.startsWith('http://localhost:8080/api/uploads/')) {
            return url.replace('http://localhost:8080/api', '')
          }
          return url
        })
      ;(recordDataForApi as unknown as { photos?: string }).photos = JSON.stringify(photoUrls)
    }
    
    // 处理典型案例数据
    if (recordDataForApi.isTypicalCase) {
      // 保持典型案例相关字段
    } else {
      // 如果不是典型案例，清空典型案例相关字段
      recordDataForApi.caseTitle = ''
      recordDataForApi.caseContent = ''
      recordDataForApi.caseType = null
      recordDataForApi.caseTags = ''
    }
    
    // 确保经纬度数据被保存
    console.log('保存的服务记录数据:', recordDataForApi)
    
    if (isEdit.value) {
      await request.put(`/v1/service-record/${recordDataForApi.id}`, recordDataForApi)
      ElMessage.success('服务记录更新成功')
    } else {
      await request.post('/v1/service-record', recordDataForApi)
      ElMessage.success('服务记录创建成功')
    }
    
    recordDialogVisible.value = false
    await load()
  } catch (error) {
    console.error('保存服务记录失败:', error)
    ElMessage.error('保存服务记录失败')
  } finally {
    saving.value = false
  }
}

// 典型案例开关变化处理
const handleTypicalCaseChange = (value: boolean) => {
  if (!value) {
    // 如果关闭典型案例，清空相关字段
    recordForm.value.caseTitle = ''
    recordForm.value.caseContent = ''
    recordForm.value.caseType = null
    recordForm.value.caseTags = ''
  }
}

// 照片上传成功
const handlePhotoSuccess = (response: { code: number; data: { url: string } }, file: UploadFile, fileList: UploadFile[]) => {
  ElMessage.success('照片上传成功')
  // 确保使用服务器返回的URL而不是blob URL
  if (response && response.data && response.data.url) {
    // 将相对URL转换为完整URL用于显示缩略图
    let imageUrl = response.data.url
    if (imageUrl.startsWith('/uploads/')) {
      imageUrl = `http://localhost:8080/api${imageUrl}`
    }
    
    // 更新文件列表中的URL为完整的显示URL
    const targetFile = fileList.find(f => f.uid === file.uid)
    if (targetFile) {
      targetFile.url = imageUrl
      console.log('照片上传成功，URL已更新:', imageUrl)
    }
  }
}

// 照片上传失败
const handlePhotoError = (error: unknown) => {
  console.error('照片上传失败:', error)
  ElMessage.error('照片上传失败')
}

// 照片上传前验证
const beforePhotoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  
  return true
}

// 获取照片列表 - 处理相对URL转换为完整URL
const getPhotoList = (photos: string) => {
  if (!photos) return []
  try {
    const photoUrls = JSON.parse(photos)
    // 将相对URL转换为完整URL，过滤掉blob URL
    return photoUrls
      .filter((url: string) => {
        if (url.startsWith('blob:')) {
          console.warn('检测到无效的blob URL，已过滤:', url)
          return false
        }
        return true
      })
      .map((url: string) => {
        // 如果是相对URL，转换为完整URL
        if (url.startsWith('/uploads/')) {
          return `http://localhost:8080/api${url}`
        }
        return url
      })
  } catch {
    return []
  }
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

// 格式化日期时间（包含时分秒）
const formatDateTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取当前位置
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      ElMessage.error('您的浏览器不支持定位功能')
      reject(new Error('Geolocation not supported'))
      return
    }
    
    locationLoading.value = true
    
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords
        
        // 保存经纬度到表单
        recordForm.value.latitude = latitude
        recordForm.value.longitude = longitude
        
        // 使用高德地图逆地理编码获取地址
        try {
          const address = await getAddressFromCoordinates(latitude, longitude)
          recordForm.value.location = address
          ElMessage.success('位置获取成功')
        } catch {
          // 如果逆地理编码失败，使用坐标作为地址
          recordForm.value.location = `纬度: ${latitude.toFixed(6)}, 经度: ${longitude.toFixed(6)}`
          ElMessage.success('位置获取成功（坐标模式）')
        }
        
        locationLoading.value = false
        resolve(position)
      },
      (error) => {
        locationLoading.value = false
        switch (error.code) {
          case error.PERMISSION_DENIED:
            ElMessage.error('用户拒绝了定位请求，请在浏览器设置中允许定位权限')
            break
          case error.POSITION_UNAVAILABLE:
            ElMessage.error('位置信息不可用，请检查网络连接或GPS设置')
            break
          case error.TIMEOUT:
            ElMessage.error('定位请求超时，请尝试在室外或网络较好的环境下重试')
            break
          default:
            ElMessage.error('获取位置失败，请检查定位权限和网络连接')
        }
        reject(error)
      },
      {
        enableHighAccuracy: true,
        timeout: 30000, // 增加到30秒
        maximumAge: 300000 // 5分钟缓存
      }
    )
  })
}

// 使用高德地图逆地理编码获取地址（经纬度→地址）
const getAddressFromCoordinates = async (latitude: number, longitude: number) => {
  const AMAP_API_KEY = 'f784e5e5c539396eefcdd8a1039cd87f'
  
  try {
    const response = await fetch(
      `https://restapi.amap.com/v3/geocode/regeo?location=${longitude},${latitude}&key=${AMAP_API_KEY}&extensions=base`
    )
    const data = await response.json()
    
    if (data.status === '1' && data.regeocode) {
      return data.regeocode.formatted_address || `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
    } else {
      throw new Error('逆地理编码失败')
    }
  } catch (error) {
    console.error('逆地理编码失败:', error)
    throw error
  }
}

// 使用高德地图地理编码获取经纬度（地址→经纬度）
const getCoordinatesFromAddress = async (address: string) => {
  const AMAP_API_KEY = 'f784e5e5c539396eefcdd8a1039cd87f'
  
  try {
    const response = await fetch(
      `https://restapi.amap.com/v3/geocode/geo?address=${encodeURIComponent(address)}&key=${AMAP_API_KEY}`
    )
    const data = await response.json()
    
    if (data.status === '1' && data.geocodes && data.geocodes.length > 0) {
      const location = data.geocodes[0].location.split(',')
      return {
        longitude: parseFloat(location[0]),
        latitude: parseFloat(location[1]),
        formattedAddress: data.geocodes[0].formatted_address,
        level: data.geocodes[0].level
      }
    } else {
      throw new Error('地址解析失败')
    }
  } catch (error) {
    console.error('地理编码失败:', error)
    throw error
  }
}

// 验证地址格式 - 改为引导提示而非严格校验
const validateAddress = (address: string) => {
  if (!address || address.trim().length < 3) {
    return { valid: false, message: '请输入有效的地址' }
  }
  
  // 检查是否包含基本的地理信息，但不强制要求特定格式
  const hasBasicLocation = address.includes('省') || address.includes('市') || address.includes('区') || 
                          address.includes('县') || address.includes('自治区') || address.includes('州')
  
  if (!hasBasicLocation) {
    return { 
      valid: true, // 改为valid: true，不阻止提交
      message: '建议输入包含省市区县的完整地址，以便更好地展示服务足迹' 
    }
  }
  
  return { valid: true, message: '地址格式正确' }
}

// 处理地址输入变化
const handleAddressChange = async (address: string) => {
  if (!address || address.trim().length < 3) {
    return
  }
  
  const validation = validateAddress(address)
  // 无论校验结果如何，都尝试获取坐标，只显示提示信息
  if (!validation.valid) {
    ElMessage.info(validation.message) // 改为info级别，不阻止操作
  }
  
  try {
    const result = await getCoordinatesFromAddress(address)
    recordForm.value.longitude = result.longitude
    recordForm.value.latitude = result.latitude
    
    // 如果返回的格式化地址与输入不同，更新输入框
    if (result.formattedAddress !== address) {
      recordForm.value.location = result.formattedAddress
    }
    
    ElMessage.success('地址解析成功，已获取经纬度')
  } catch (error) {
    console.error('地址解析失败:', error)
    ElMessage.warning('地址解析失败，请检查地址格式或手动获取位置')
  }
}

// 防抖处理地址输入
let addressInputTimer: number | null = null

// 声明高德地图API类型
declare global {
  interface Window {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    AMap: any
    initAmapMap: () => void
  }
}

const debounceAddressInput = (address: string) => {
  if (addressInputTimer) {
    clearTimeout(addressInputTimer)
  }
  
  addressInputTimer = setTimeout(() => {
    if (address && address.trim().length > 5) {
      handleAddressChange(address)
    }
  }, 1000) // 1秒后执行
}

// 初始化地图 - 彻底简化版本
const initMap = async () => {
  const mapContainer = document.getElementById('map-container')
  if (!mapContainer) return
  
  // 清除之前的地图说明，避免重复
  const existingInfo = mapContainer.parentNode?.querySelector('.map-info')
  if (existingInfo) {
    existingInfo.remove()
  }
  
  try {
    // 获取用户服务足迹数据
    const response = await request.get('/v1/service-record/user/footprints')
    console.log('地图API响应:', response)
    
    // 根据实际API响应结构解析数据
    let footprints: ServiceRecord[] = []
    if (response.data && Array.isArray(response.data)) {
      footprints = response.data
    } else if (response.data && response.data.records) {
      footprints = response.data.records
    } else if (Array.isArray(response)) {
      footprints = response
    }
    
    console.log('解析后的足迹数据:', footprints)
    
    if (footprints.length === 0) {
      mapContainer.innerHTML = '<div style="text-align: center; padding: 50px; color: #999; background: #f5f5f5; border-radius: 8px;"><div style="font-size: 16px; margin-bottom: 10px;">🗺️</div><div>暂无服务足迹数据</div><div style="font-size: 12px; margin-top: 5px;">请先添加服务记录</div></div>'
      return
    }
    
    // 过滤出有经纬度数据的记录
    const validFootprints = footprints.filter((item: ServiceRecord) => item.latitude && item.longitude)
    console.log('有效足迹数据:', validFootprints)
    
    if (validFootprints.length === 0) {
      mapContainer.innerHTML = '<div style="text-align: center; padding: 50px; color: #999; background: #f5f5f5; border-radius: 8px;"><div style="font-size: 16px; margin-bottom: 10px;">📍</div><div>暂无位置数据</div><div style="font-size: 12px; margin-top: 5px;">请先获取服务地点位置</div></div>'
      return
    }
    
    // 计算地图中心点
    const centerLat = validFootprints.reduce((sum: number, item: ServiceRecord) => sum + (item.latitude || 0), 0) / validFootprints.length
    const centerLng = validFootprints.reduce((sum: number, item: ServiceRecord) => sum + (item.longitude || 0), 0) / validFootprints.length
    
    console.log('地图中心点:', centerLat, centerLng)
    
    // 使用高德地图JavaScript API实现交互式地图
    const AMAP_API_KEY = 'f784e5e5c539396eefcdd8a1039cd87f'
    
    // 动态加载高德地图API
    const loadAmapScript = () => {
      return new Promise((resolve, reject) => {
        if (window.AMap) {
          resolve(window.AMap)
          return
        }
        
        const script = document.createElement('script')
        script.src = `https://webapi.amap.com/maps?v=1.4.15&key=${AMAP_API_KEY}&callback=initAmapMap`
        script.async = true
        script.onerror = reject
        
        // 设置全局回调函数
        window.initAmapMap = () => {
          resolve(window.AMap)
        }
        
        document.head.appendChild(script)
      })
    }
    
    // 初始化交互式地图
    const initInteractiveMap = async () => {
      try {
        await loadAmapScript()
        
        // 创建地图实例
        const map = new window.AMap.Map('map-container', {
          zoom: 10,
          center: [centerLng, centerLat],
          mapStyle: 'amap://styles/normal'
        })
        
        // 添加标记点
        validFootprints.forEach((item: ServiceRecord, index: number) => {
          const marker = new window.AMap.Marker({
            position: [item.longitude!, item.latitude!],
            title: `${item.activityTitle} - ${item.location}`,
            label: {
              content: `${index + 1}`,
              direction: 'center',
              style: {
                color: '#fff',
                fontSize: '12px',
                fontWeight: 'bold'
              }
            },
            icon: new window.AMap.Icon({
              size: new window.AMap.Size(25, 34),
              image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
              imageSize: new window.AMap.Size(25, 34)
            })
          })
          
          map.add(marker)
          
          // 添加信息窗口
          const infoWindow = new window.AMap.InfoWindow({
            content: `
              <div style="padding: 10px;">
                <h4 style="margin: 0 0 5px 0;">${item.activityTitle}</h4>
                <p style="margin: 0 0 5px 0; color: #666;">${item.location}</p>
                <p style="margin: 0; font-size: 12px; color: #999;">
                  服务日期: ${item.serviceDate}
                </p>
              </div>
            `,
            offset: new window.AMap.Pixel(0, -30)
          })
          
          marker.on('click', () => {
            infoWindow.open(map, marker.getPosition())
          })
        })
        
        // 自适应显示所有标记点
        map.setFitView()
        
        console.log('交互式地图初始化成功')
        
      } catch (error) {
        console.error('交互式地图初始化失败:', error)
        // 降级到静态地图
        mapContainer.innerHTML = `
          <img src="https://restapi.amap.com/v3/staticmap?location=${centerLng},${centerLat}&zoom=10&size=800*400&key=${AMAP_API_KEY}" 
               style="width: 100%; height: 100%; object-fit: cover;" 
               alt="服务足迹地图" />
        `
      }
    }
    
    // 清空容器并初始化交互式地图
    mapContainer.innerHTML = '<div id="map-container" style="height: 400px; width: 100%;"></div>'
    initInteractiveMap()
    
    // 添加地图说明
    const mapInfo = document.createElement('div')
    mapInfo.className = 'map-info'
    mapInfo.style.cssText = 'margin-top: 10px; padding: 10px; background: #f0f9ff; border-radius: 4px; font-size: 12px; color: #0369a1;'
    mapInfo.innerHTML = `📍 共显示 ${validFootprints.length} 个服务地点，中心位置：${centerLat.toFixed(6)}, ${centerLng.toFixed(6)}`
    mapContainer.parentNode?.appendChild(mapInfo)
    
  } catch (error) {
    console.error('加载地图失败:', error)
    mapContainer.innerHTML = '<div style="text-align: center; padding: 50px; color: #f56c6c; background: #fef2f2; border-radius: 8px;"><div style="font-size: 16px; margin-bottom: 10px;">❌</div><div>地图加载失败</div><div style="font-size: 12px; margin-top: 5px;">请稍后重试</div></div>'
  }
}

// 监听标签页切换
const handleTabChange = (tabName: string) => {
  if (tabName === 'footprints') {
    // 延迟初始化地图，确保DOM已渲染
    setTimeout(initMap, 100)
  } else if (tabName === 'certificates') {
    // 每次切换到证书标签页时重新加载证书数据
    loadCertificates()
  }
}



// ========== 模拟平台功能方法 ==========

// 自动计算时长
const autoCalculateHours = async () => {
  calculating.value = true
  try {
    const response = await request.post<AutoCalculateResponse>('/v1/platform/auto-calculate')
    ElMessage.success((response.data as { message?: string })?.message || '服务时长自动计算完成')
    await load()
  } catch (error: unknown) {
    console.error('自动计算失败:', error)
    const errorMessage = error instanceof Error ? error.message : '自动计算失败'
    ElMessage.error(errorMessage)
  } finally {
    calculating.value = false
  }
}

// 生成证书
const generateCertificate = async () => {
  generating.value = true
  try {
    const response = await request.post<CertificateResponse>('/v1/platform/generate-certificate')
    if ((response.data as { success?: boolean; certificateNumber?: string; message?: string })?.success) {
      ElMessage.success(`证书生成成功！证书编号：${(response.data as { certificateNumber?: string }).certificateNumber || 'N/A'}`)
      await load()
    } else {
      ElMessage.error((response.data as { message?: string })?.message || '证书生成失败')
    }
  } catch (error: unknown) {
    console.error('证书生成失败:', error)
    const errorMessage = error instanceof Error ? error.message : '证书生成失败'
    ElMessage.error(errorMessage)
  } finally {
    generating.value = false
  }
}

// 同步到平台
const syncToPlatform = async () => {
  syncing.value = true
  try {
    const response = await request.post<SyncResponse>('/v1/platform/sync')
    ElMessage.success((response.data as { message?: string })?.message || '数据已同步到官方平台')
  } catch (error: unknown) {
    console.error('同步失败:', error)
    const errorMessage = error instanceof Error ? error.message : '同步失败'
    ElMessage.error(errorMessage)
  } finally {
    syncing.value = false
  }
}

// 生成官方认证证书
const generateOfficialCertificate = async () => {
  // 前端校验：服务时长不足10小时时提示
  if (stats.value.totalHours < 10) {
    ElMessage.warning('服务时长不足10小时，暂无法生成官方认证证书')
    return
  }
  
  generatingOfficial.value = true
  try {
    const response = await request.post<CertificateResponse>('/v1/platform/generate-official-certificate')
    console.log('完整响应对象:', response) // 调试日志
    console.log('响应数据类型:', typeof response) // 调试日志
    console.log('响应数据:', response.data) // 调试日志
    console.log('响应数据是否存在:', !!response.data) // 调试日志
    
    // 修复：后端返回的数据结构可能是直接的数据对象或嵌套结构
    let responseData: any = null
    
    // 安全地获取响应数据
    if (response && typeof response === 'object') {
      if (response.data && typeof response.data === 'object') {
        responseData = response.data
      } else {
        // 如果 response.data 不存在，说明后端直接返回了数据对象
        responseData = response
      }
    }
    
    console.log('最终响应数据:', responseData) // 调试日志
    
    // 修复：处理不同的响应数据结构
    if (responseData && responseData.success) {
      // 情况1：后端直接返回了数据对象 { success: true, totalHours: 280.9, ... }
      const data = responseData
      console.log('解析后的data对象:', data)
      console.log('data.totalHours:', data.totalHours)
      console.log('data.certificateLevel:', data.certificateLevel)
      console.log('data.userName:', data.userName)
      
      ElMessage.success(`官方认证证书生成成功！证书编号：${data.certificateNumber}`)
      
      // 生成并下载PDF证书
      await generateAndDownloadPDF(data)
      
      // 显示证书详情和下载入口
      ElMessageBox.confirm(
        `证书生成成功！\n\n证书等级：${data.certificateLevel}\n证书编号：${data.certificateNumber}\n验证链接：${data.verificationUrl}\n\nPDF证书已自动下载到您的设备`,
        '证书生成成功',
        { 
          type: 'success',
          confirmButtonText: '重新下载PDF',
          cancelButtonText: '关闭',
          showCancelButton: true
        }
      ).then(() => {
        // 用户点击重新下载
        generateAndDownloadPDF(data)
      }).catch(() => {
        // 用户点击关闭
      })
      
      await load()
      await loadCertificates() // 刷新证书列表
    } else if (responseData && responseData.data && responseData.data.success) {
      // 情况2：后端返回了嵌套结构 { data: { success: true, totalHours: 280.9, ... } }
      const data = responseData.data
      console.log('解析后的data对象:', data)
      console.log('data.totalHours:', data.totalHours)
      console.log('data.certificateLevel:', data.certificateLevel)
      console.log('data.userName:', data.userName)
      
      ElMessage.success(`官方认证证书生成成功！证书编号：${data.certificateNumber}`)
      
      // 生成并下载PDF证书
      await generateAndDownloadPDF(data)
      
      // 显示证书详情和下载入口
      ElMessageBox.confirm(
        `证书生成成功！\n\n证书等级：${data.certificateLevel}\n证书编号：${data.certificateNumber}\n验证链接：${data.verificationUrl}\n\nPDF证书已自动下载到您的设备`,
        '证书生成成功',
        { 
          type: 'success',
          confirmButtonText: '重新下载PDF',
          cancelButtonText: '关闭',
          showCancelButton: true
        }
      ).then(() => {
        // 用户点击重新下载
        generateAndDownloadPDF(data)
      }).catch(() => {
        // 用户点击关闭
      })
      
      await load()
      await loadCertificates() // 刷新证书列表
    } else {
      console.log('证书生成失败，响应数据:', responseData) // 调试日志
      const errorMessage = responseData?.data?.message || responseData?.message || '证书生成失败'
      ElMessage.error(errorMessage)
    }
  } catch (error: unknown) {
    console.error('生成官方认证证书失败:', error)
    const errorMessage = error instanceof Error ? error.message : '证书生成失败'
    ElMessage.error(errorMessage)
  } finally {
    generatingOfficial.value = false
  }
}

// 生成并下载PDF证书
const generateAndDownloadPDF = async (certificateData: { certificateNumber?: string; certificateLevel?: string; userName?: string; totalHours?: number; issueDate?: string; verificationUrl?: string }) => {
  try {
    // 添加调试日志
    console.log('PDF生成数据:', certificateData)
    console.log('总服务时长:', certificateData.totalHours)
    console.log('证书等级:', certificateData.certificateLevel)
    console.log('用户名:', certificateData.userName)
    
    // 动态导入PDF生成库
    const html2canvas = (await import('html2canvas')).default
    const jsPDF = (await import('jspdf')).default
    
    // 创建证书HTML元素
    const certificateElement = createCertificateElement(certificateData)
    document.body.appendChild(certificateElement)
    
    // 将HTML转换为Canvas
    const canvas = await html2canvas(certificateElement, {
      scale: 2, // 提高清晰度
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff'
    })
    
    // 创建PDF
    const pdf = new jsPDF('landscape', 'mm', 'a4')
    const imgData = canvas.toDataURL('image/png')
    
    // 计算图片在PDF中的位置和大小
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = canvas.width
    const imgHeight = canvas.height
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight)
    const imgX = (pdfWidth - imgWidth * ratio) / 2
    const imgY = (pdfHeight - imgHeight * ratio) / 2
    
    // 添加图片到PDF
    pdf.addImage(imgData, 'PNG', imgX, imgY, imgWidth * ratio, imgHeight * ratio)
    
    // 下载PDF
    pdf.save(`志愿服务证书_${certificateData.certificateNumber}.pdf`)
    
    // 清理DOM元素
    document.body.removeChild(certificateElement)
    
    ElMessage.success('PDF证书下载成功！')
  } catch (error) {
    console.error('PDF生成失败:', error)
    ElMessage.error('PDF证书生成失败，请重试')
  }
}

// 生成PDF内容（简化版HTML转PDF）
const generatePDFContent = (data: { certificateNumber?: string; certificateLevel?: string; userName?: string; totalHours?: number; issueDate?: string; verificationUrl?: string }) => {
  const htmlContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>志愿服务证书</title>
      <style>
        body {
          font-family: 'Microsoft YaHei', Arial, sans-serif;
          margin: 0;
          padding: 40px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .certificate {
          background: white;
          border-radius: 20px;
          padding: 60px;
          box-shadow: 0 20px 40px rgba(0,0,0,0.1);
          text-align: center;
          max-width: 800px;
          width: 100%;
          position: relative;
        }
        .certificate::before {
          content: '';
          position: absolute;
          top: 20px;
          left: 20px;
          right: 20px;
          bottom: 20px;
          border: 3px solid #667eea;
          border-radius: 15px;
          pointer-events: none;
        }
        .header {
          margin-bottom: 40px;
        }
        .title {
          font-size: 36px;
          font-weight: bold;
          color: #2c3e50;
          margin-bottom: 10px;
          text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
        }
        .subtitle {
          font-size: 18px;
          color: #7f8c8d;
          margin-bottom: 30px;
        }
        .content {
          margin: 40px 0;
        }
        .recipient {
          font-size: 28px;
          font-weight: bold;
          color: #e74c3c;
          margin: 20px 0;
        }
        .description {
          font-size: 20px;
          line-height: 1.8;
          color: #34495e;
          margin: 30px 0;
        }
        .details {
          margin: 40px 0;
          padding: 30px;
          background: #f8f9fa;
          border-radius: 15px;
          border-left: 5px solid #667eea;
        }
        .detail-item {
          display: flex;
          justify-content: space-between;
          margin: 15px 0;
          font-size: 16px;
        }
        .detail-label {
          font-weight: bold;
          color: #2c3e50;
        }
        .detail-value {
          color: #7f8c8d;
        }
        .footer {
          margin-top: 50px;
          padding-top: 30px;
          border-top: 2px solid #ecf0f1;
        }
        .signature {
          display: flex;
          justify-content: space-between;
          margin-top: 40px;
        }
        .signature-item {
          text-align: center;
        }
        .signature-line {
          border-bottom: 2px solid #2c3e50;
          width: 150px;
          margin: 0 auto 10px;
        }
        .verification {
          margin-top: 30px;
          padding: 20px;
          background: #e8f5e8;
          border-radius: 10px;
          border: 1px solid #4caf50;
        }
        .verification-url {
          color: #2e7d32;
          font-size: 14px;
          word-break: break-all;
        }
      </style>
    </head>
    <body>
      <div class="certificate">
        <div class="header">
          <div class="title">志愿服务证书</div>
          <div class="subtitle">Volunteer Service Certificate</div>
        </div>
        
        <div class="content">
          <div class="description">
            兹证明
          </div>
          <div class="recipient">${data.userName || '志愿者'}</div>
          <div class="description">
            在志愿服务活动中表现优异，累计服务时长达到 <strong>${data.totalHours || 0}</strong> 小时，
            特授予 <strong>${data.certificateLevel || '志愿者'}</strong> 荣誉称号。
          </div>
        </div>
        
        <div class="details">
          <div class="detail-item">
            <span class="detail-label">证书编号：</span>
            <span class="detail-value">${data.certificateNumber || 'N/A'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">颁发日期：</span>
            <span class="detail-value">${data.issueDate || new Date().toLocaleDateString()}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">证书等级：</span>
            <span class="detail-value">${data.certificateLevel || '志愿者'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">服务时长：</span>
            <span class="detail-value">${data.totalHours || 0} 小时</span>
          </div>
        </div>
        
        <div class="footer">
          <div class="signature">
            <div class="signature-item">
              <div class="signature-line"></div>
              <div>志愿服务平台</div>
              <div style="font-size: 14px; color: #7f8c8d;">Volunteer Service Platform</div>
            </div>
            <div class="signature-item">
              <div class="signature-line"></div>
              <div>${data.issueDate || new Date().toLocaleDateString()}</div>
              <div style="font-size: 14px; color: #7f8c8d;">Date</div>
            </div>
          </div>
          
          <div class="verification">
            <div style="font-weight: bold; color: #2e7d32; margin-bottom: 10px;">证书验证</div>
            <div class="verification-url">验证链接：${data.verificationUrl || 'https://volunteer.official.gov.cn/verify'}</div>
          </div>
        </div>
      </div>
    </body>
    </html>
  `
  
  return htmlContent
}

// 创建证书DOM元素
const createCertificateElement = (data: { certificateNumber?: string; certificateLevel?: string; userName?: string; totalHours?: number; issueDate?: string; verificationUrl?: string }) => {
  const element = document.createElement('div')
  element.style.cssText = `
    position: fixed;
    top: -9999px;
    left: -9999px;
    width: 800px;
    height: 600px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: 'Microsoft YaHei', Arial, sans-serif;
    color: white;
    text-align: center;
    padding: 40px;
    box-sizing: border-box;
  `
  
  element.innerHTML = `
    <div style="background: white; border-radius: 20px; padding: 60px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); max-width: 100%; width: 100%; position: relative;">
      <div style="position: absolute; top: 20px; left: 20px; right: 20px; bottom: 20px; border: 3px solid #667eea; border-radius: 15px; pointer-events: none;"></div>
      
      <div style="margin-bottom: 30px;">
        <h1 style="color: #2c3e50; font-size: 36px; margin: 0 0 10px 0; font-weight: bold;">志愿服务证书</h1>
        <div style="width: 100px; height: 3px; background: #667eea; margin: 0 auto;"></div>
      </div>
      
      <div style="margin-bottom: 40px;">
        <p style="color: #7f8c8d; font-size: 18px; margin: 0;">Certificate of Volunteer Service</p>
      </div>
      
      <div style="margin-bottom: 30px;">
        <p style="color: #2c3e50; font-size: 24px; margin: 0; font-weight: bold;">${data.userName}</p>
        <p style="color: #7f8c8d; font-size: 16px; margin: 10px 0 0 0;">在志愿服务活动中表现优异</p>
      </div>
      
      <div style="margin-bottom: 30px;">
        <p style="color: #2c3e50; font-size: 18px; margin: 0;">累计服务时长：<span style="font-weight: bold; color: #667eea;">${data.totalHours}</span> 小时</p>
        <p style="color: #2c3e50; font-size: 18px; margin: 10px 0 0 0;">证书等级：<span style="font-weight: bold; color: #667eea;">${data.certificateLevel}</span></p>
      </div>
      
      <div style="margin-bottom: 30px;">
        <p style="color: #2c3e50; font-size: 16px; margin: 0;">证书编号：${data.certificateNumber || 'N/A'}</p>
        <p style="color: #7f8c8d; font-size: 14px; margin: 10px 0 0 0;">颁发日期：${data.issueDate || new Date().toLocaleDateString()}</p>
      </div>
      
      <div style="margin-top: 40px;">
        <p style="color: #7f8c8d; font-size: 14px; margin: 0;">官方志愿服务平台</p>
        <p style="color: #7f8c8d; font-size: 12px; margin: 5px 0 0 0;">Official Volunteer Service Platform</p>
      </div>
    </div>
  `
  
  return element
}

// 显示证书验证对话框
const showVerifyDialog = () => {
  verifyForm.value.certificateNumber = ''
  verifyDialogVisible.value = true
}

// 验证证书
const verifyCertificate = async () => {
  if (!verifyForm.value.certificateNumber) {
    ElMessage.warning('请输入证书编号')
    return
  }
  
  verifying.value = true
  try {
    const response = await request.post<VerifyResponse>('/v1/platform/verify-official-certificate', {
      certificateNumber: verifyForm.value.certificateNumber
    })
    
    const data = response.data as { valid?: boolean; message?: string; verificationUrl?: string }
    
    if (data.valid) {
      ElMessage.success('证书验证成功！')
      ElMessageBox.alert(
        `证书编号：${verifyForm.value.certificateNumber}\n验证结果：有效\n官方验证链接：${data.verificationUrl}`,
        '验证成功',
        { type: 'success' }
      )
    } else {
      ElMessage.error('证书验证失败')
    }
  } catch (error: unknown) {
    console.error('证书验证失败:', error)
    ElMessage.error('证书验证失败')
  } finally {
    verifying.value = false
    verifyDialogVisible.value = false
  }
}

// 查看个人档案
const viewPersonalProfile = async () => {
  try {
    const response = await request.get<ProfileResponse>('/v1/platform/personal-profile')
    const data = response.data as { 
      totalServiceHours?: number; 
      certificateLevel?: string; 
      platformStatus?: string;
      lastSyncTime?: string;
    }
    
    ElMessageBox.alert(
      `总服务时长：${data.totalServiceHours}小时\n证书等级：${data.certificateLevel}\n平台状态：${data.platformStatus}\n最后同步时间：${data.lastSyncTime}`,
      '个人档案信息',
      { type: 'info' }
    )
  } catch (error: unknown) {
    console.error('获取个人档案失败:', error)
    ElMessage.error('获取个人档案失败')
  }
}

// 典型案例相关辅助方法
const getCaseTypeName = (type: number) => {
  const typeMap: { [key: number]: string } = {
    1: '感人故事',
    2: '技能应用',
    3: '团队协作',
    4: '成长感悟'
  }
  return typeMap[type] || '未知类型'
}

const getCaseTypeTagType = (type: number) => {
  const typeMap: { [key: number]: string } = {
    1: 'danger',
    2: 'success',
    3: 'warning',
    4: 'primary'
  }
  return typeMap[type] || 'info'
}

const getCaseTags = (tags: string) => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
}

// 证书相关方法
const loadCertificates = async () => {
  certificateLoading.value = true
  try {
    const response = await request.get('/v1/platform/user-certificates', {
      params: {
        pageNo: certificatePageNo.value,
        pageSize: certificatePageSize.value,
        issuingAuthority: '官方志愿服务平台' // 只查询官方志愿服务平台的证书
      }
    })
    
    const responseData = response as { records?: Certificate[]; total?: number; data?: { records?: Certificate[]; total?: number } }
    if (responseData.records) {
      certificates.value = responseData.records || []
      certificateTotal.value = responseData.total || 0
    } else if (responseData.data && responseData.data.records) {
      certificates.value = responseData.data.records || []
      certificateTotal.value = responseData.data.total || 0
    } else {
      certificates.value = []
      certificateTotal.value = 0
    }
  } catch (error) {
    console.error('加载证书列表失败:', error)
    ElMessage.error('加载证书列表失败')
  } finally {
    certificateLoading.value = false
  }
}

const getUserServiceInfo = async () => {
  try {
    console.log('开始获取用户服务信息...')
    
    // 获取用户个人档案信息，包含总服务时长和证书等级
    const response = await request.get('/v1/platform/personal-profile')
    console.log('个人档案API响应:', response)
    console.log('个人档案API响应数据:', response.data)
    
    // 修复：后端返回的是 { code, message, data: { totalServiceHours, ... } } 结构
    const responseData = response.data as { 
      code?: number; 
      message?: string; 
      data?: { 
        totalServiceHours?: number; 
        certificateLevel?: string; 
        userId?: number;
        platformStatus?: string;
        lastSyncTime?: string;
        totalRecords?: number;
        starLevel?: number;
      } 
    }
    
    const profileData = responseData.data || {}
    
    console.log('解析后的profileData:', profileData)
    console.log('totalServiceHours:', profileData.totalServiceHours)
    console.log('certificateLevel:', profileData.certificateLevel)
    
    // 获取用户基本信息
    const authStore = useAuthStore()
    const userName = authStore.userInfo?.realName || '志愿者'
    console.log('从authStore获取的用户名:', userName)
    
    const result = {
      userName,
      totalHours: profileData.totalServiceHours || 0,
      certificateLevel: profileData.certificateLevel || '志愿者',
      userId: profileData.userId
    }
    
    console.log('getUserServiceInfo返回结果:', result)
    return result
  } catch (error) {
    console.error('获取用户服务信息失败:', error)
    // 返回默认值
    const defaultResult = {
      userName: '志愿者',
      totalHours: 0,
      certificateLevel: '志愿者',
      userId: null
    }
    console.log('使用默认值:', defaultResult)
    return defaultResult
  }
}

const downloadCertificate = async (certificate: Certificate) => {
  try {
    console.log('开始下载证书PDF，证书信息:', certificate)
    
    // 获取用户真实信息和服务时长
    const userInfo = await getUserServiceInfo()
    console.log('getUserServiceInfo返回的用户信息:', userInfo)
    
    // 构建证书数据用于PDF生成
    const certificateData = {
      certificateNumber: certificate.certificateNumber,
      certificateLevel: getCertificateLevelFromName(certificate.certificateName),
      userName: userInfo.userName || '志愿者',
      totalHours: userInfo.totalHours || 0,
      issueDate: certificate.issueDate,
      verificationUrl: `https://volunteer.official.gov.cn/verify/${certificate.certificateNumber}`
    }
    
    console.log('证书列表下载PDF数据:', certificateData)
    console.log('totalHours值:', certificateData.totalHours)
    console.log('certificateLevel值:', certificateData.certificateLevel)
    console.log('userName值:', certificateData.userName)
    
    await generateAndDownloadPDF(certificateData)
    ElMessage.success('证书下载成功！')
  } catch (error) {
    console.error('下载证书失败:', error)
    ElMessage.error('下载证书失败')
  }
}

const viewCertificateDetail = (certificate: Certificate) => {
  ElMessageBox.alert(
    `证书名称：${certificate.certificateName}\n证书编号：${certificate.certificateNumber}\n颁发机构：${certificate.issuingAuthority}\n颁发日期：${certificate.issueDate}\n有效期至：${certificate.expiryDate || '永久有效'}\n状态：${getCertificateStatusText(certificate.status)}`,
    '证书详情',
    { type: 'info' }
  )
}

const getCertificateStatusText = (status: number) => {
  switch (status) {
    case 1: return '待审核'
    case 2: return '已通过'
    case 3: return '已拒绝'
    default: return '未知状态'
  }
}

const getCertificateStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getCertificateLevelFromName = (certificateName: string) => {
  if (certificateName.includes('金牌')) return '金牌志愿者'
  if (certificateName.includes('银牌')) return '银牌志愿者'
  if (certificateName.includes('铜牌')) return '铜牌志愿者'
  return '志愿者'
}

onMounted(() => {
  load()
  loadCertificates()
})
</script>

<style scoped>
.service-records-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  flex-shrink: 0;
}

.stats-cards {
  margin-bottom: 24px;
}

.action-cards {
  margin-bottom: 24px;
}

.official-platform-section {
  margin-bottom: 24px;
}

.platform-card {
  border: 2px solid #e6f7ff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

.platform-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.platform-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin: 20px 0;
}

.platform-actions .el-button {
  min-width: 180px;
}

.tip-info {
  margin-top: 16px;
}

.certificate-section {
  padding: 20px 0;
}

.certificate-header {
  text-align: center;
  margin-bottom: 30px;
}

.certificate-header h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 24px;
}

.certificate-header p {
  color: #7f8c8d;
  font-size: 14px;
}

.action-card {
  height: 140px;
}

.action-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 16px;
}

.action-icon {
  font-size: 32px;
  margin-right: 16px;
  opacity: 0.8;
}

.action-info {
  flex: 1;
}

.action-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.action-info p {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.footprint-map {
  margin-bottom: 24px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.footprint-list h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.record-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.detail-section p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.photo-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 典型案例字段样式 */
.typical-case-fields {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  margin-top: 10px;
}

.typical-case-fields .el-form-item {
  margin-bottom: 18px;
}

.typical-case-fields .el-form-item:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .service-records-page {
    padding: 0 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    align-self: flex-end;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
  
  .action-cards .el-col {
    margin-bottom: 16px;
  }
  
  .action-content {
    flex-direction: column;
    text-align: center;
  }
  
  .action-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
}
</style>


