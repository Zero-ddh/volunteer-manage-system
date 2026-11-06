<template>
  <div class="certificate-management">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h2>技能证书管理</h2>
          <p>管理您的技能证书，提升志愿者匹配度</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openAddCertificateDialog">
            <el-icon><Plus /></el-icon>
            添加证书
          </el-button>
        </div>
      </div>
    </div>

    <!-- 证书列表 -->
    <div class="certificate-list" v-loading="loading">
      <div v-if="certificates.length === 0" class="empty-state">
        <el-empty description="暂无证书信息">
          <el-button type="primary" @click="openAddCertificateDialog">
            添加证书
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="certificate-cards">
        <div v-for="certificate in certificates" :key="certificate.id" class="certificate-card">
          <CertificatePreview 
            :certificate="certificate" 
            @edit="editCertificate"
            @delete="deleteCertificate"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑证书对话框 -->
    <el-dialog 
      v-model="certificateDialogVisible" 
      :title="isEdit ? '编辑证书' : '添加证书'"
      width="600px"
    >
      <el-form :model="certificateForm" :rules="certificateRules" ref="certificateFormRef" label-width="100px">
        <el-form-item label="证书名称" prop="certificateName">
          <el-input v-model="certificateForm.certificateName" placeholder="请输入证书名称" />
        </el-form-item>
        
        <el-form-item label="颁发机构" prop="issuingAuthority">
          <el-input v-model="certificateForm.issuingAuthority" placeholder="请输入颁发机构" />
        </el-form-item>
        
        <el-form-item label="证书编号">
          <el-input v-model="certificateForm.certificateNumber" placeholder="请输入证书编号（可选）" />
        </el-form-item>
        
        <el-form-item label="颁发日期">
          <el-date-picker
            v-model="certificateForm.issueDate"
            type="date"
            placeholder="选择颁发日期"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="有效期至">
          <el-date-picker
            v-model="certificateForm.expiryDate"
            type="date"
            placeholder="选择有效期（可选）"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="关联技能" prop="skillId">
          <el-select v-model="certificateForm.skillId" placeholder="请选择关联技能" style="width: 100%">
            <el-option 
              v-for="skill in userSkills" 
              :key="skill.id" 
              :label="skill.skillName" 
              :value="skill.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="证书文件" prop="certificateFile">
          <div class="file-upload-section">
            <el-upload
              :http-request="uploadCertificateFile"
              :show-file-list="false"
              accept=".pdf"
              :before-upload="beforeUpload"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传证书文件
              </el-button>
            </el-upload>
            
            <div v-if="uploadedFile" class="uploaded-file">
              <el-icon><Document /></el-icon>
              <span>{{ uploadedFile.originalName }}</span>
              <el-button 
                type="danger"
                size="small" 
                text 
                @click="removeUploadedFile"
              >
                删除
              </el-button>
            </div>
            
            <div class="upload-tips">
              <el-text type="info" size="small">
                仅支持PDF格式，文件大小不超过10MB
              </el-text>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="certificateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCertificate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Document } from '@element-plus/icons-vue'
import { 
  apiGetUserCertificates, 
  apiAddCertificate,
  apiUpdateCertificate,
  apiDeleteCertificate,
  apiUploadCertificate,
  apiGetUserSkills
} from '@/api/user'
import CertificatePreview from '@/components/CertificatePreview.vue'
import { useAuthStore } from '@/stores/auth'

// 认证状态
const authStore = useAuthStore()

// 计算属性
const currentUserId = computed(() => authStore.userInfo?.id || 0)

interface Certificate {
  id: number
  userId: number
  skillId: number | null
  certificateName: string
  issuingAuthority: string
  certificateNumber: string
  issueDate: string | null
  expiryDate: string | null
  certificateFile: string
  originalFileName?: string
  fileType: string
  fileSize: number
}

interface UserSkill {
  id: number
  skillName: string
  skillLevel: string
  proficiency: number
}

interface UploadedFile {
  url: string
  fileType: string
  size: number
  originalName: string
}

interface UploadOptions {
  file: File
  onError?: (error: unknown) => void
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const certificates = ref<Certificate[]>([])
const userSkills = ref<UserSkill[]>([])
const certificateDialogVisible = ref(false)
const isEdit = ref(false)
const certificateFormRef = ref()

// 证书表单数据
const certificateForm = ref<Certificate>({
  id: 0,
  userId: currentUserId.value,
  skillId: null,
  certificateName: '',
  issuingAuthority: '',
  certificateNumber: '',
  issueDate: null,
  expiryDate: null,
  certificateFile: '',
  fileType: '',
  fileSize: 0
})

// 上传的文件信息
const uploadedFile = ref<UploadedFile | null>(null)

// 表单验证规则
const certificateRules = {
  certificateName: [
    { required: true, message: '请输入证书名称', trigger: 'blur' }
  ],
  issuingAuthority: [
    { required: true, message: '请输入颁发机构', trigger: 'blur' }
  ],
  skillId: [
    { required: true, message: '请选择关联技能', trigger: 'change' }
  ],
  certificateFile: [
    { required: true, message: '请上传证书文件', trigger: 'change' }
  ]
}

// 方法
const loadCertificates = async () => {
  try {
    loading.value = true
    const res = await apiGetUserCertificates(currentUserId.value) as unknown as Certificate[]
    // 过滤掉官方志愿服务平台的证书
    certificates.value = (res || []).filter(cert => 
      cert.issuingAuthority !== '官方志愿服务平台'
    )
  } catch (error) {
    console.error('加载证书列表失败:', error)
    ElMessage.error('加载证书列表失败')
  } finally {
    loading.value = false
  }
}

const loadUserSkills = async () => {
  try {
    const res = await apiGetUserSkills(currentUserId.value) as unknown as UserSkill[]
    userSkills.value = res || []
  } catch (error) {
    console.error('加载用户技能失败:', error)
    ElMessage.error('加载用户技能失败')
  }
}

const openAddCertificateDialog = () => {
  isEdit.value = false
  certificateForm.value = {
    id: 0,
    userId: currentUserId.value,
    skillId: null,
    certificateName: '',
    issuingAuthority: '',
    certificateNumber: '',
    issueDate: null,
    expiryDate: null,
    certificateFile: '',
    fileType: '',
    fileSize: 0
  }
  uploadedFile.value = null
  certificateDialogVisible.value = true
}

const editCertificate = (certificate: Certificate) => {
  isEdit.value = true
  certificateForm.value = { ...certificate }
  // 编辑时清空上传文件状态，用户需要重新上传文件才会更新
  uploadedFile.value = null
  certificateDialogVisible.value = true
}

const saveCertificate = async () => {
  try {
    await certificateFormRef.value.validate()
    saving.value = true
    
    const certificateData = { ...certificateForm.value }
    
    // 验证必填字段
    if (!certificateData.skillId) {
      ElMessage.error('请选择关联技能')
      return
    }
    
    // 添加模式下必须上传文件，编辑模式下如果没有重新上传则使用原有文件
    if (!isEdit.value && !certificateData.certificateFile) {
      ElMessage.error('请上传证书文件')
      return
    }
    
    if (isEdit.value && !certificateData.certificateFile && !uploadedFile.value) {
      ElMessage.error('请上传证书文件')
      return
    }
    
    // 如果有上传的文件，设置文件信息
    if (uploadedFile.value) {
      certificateData.certificateFile = uploadedFile.value.url
      certificateData.fileType = uploadedFile.value.fileType
      certificateData.fileSize = uploadedFile.value.size
      certificateData.originalFileName = uploadedFile.value.originalName
    }
    
    // 清理数据，只保留后端需要的字段
    const cleanCertificateData = {
      userId: certificateData.userId,
      skillId: certificateData.skillId,
      certificateName: certificateData.certificateName,
      issuingAuthority: certificateData.issuingAuthority,
      certificateNumber: certificateData.certificateNumber,
      issueDate: certificateData.issueDate,
      expiryDate: certificateData.expiryDate,
      certificateFile: certificateData.certificateFile,
      originalFileName: certificateData.originalFileName,
      fileType: certificateData.fileType,
      fileSize: certificateData.fileSize
    }
    
    // 调试：打印发送的数据
    console.log('发送证书数据:', cleanCertificateData)
    
    if (isEdit.value) {
      // 编辑模式：更新现有证书
      await apiUpdateCertificate(certificateData.id, cleanCertificateData)
      ElMessage.success('证书更新成功')
    } else {
      // 添加模式：创建新证书
      await apiAddCertificate(cleanCertificateData)
      ElMessage.success('证书保存成功')
    }
    
    certificateDialogVisible.value = false
    await loadCertificates()
  } catch (error) {
    console.error('保存证书失败:', error)
    ElMessage.error('保存证书失败: ' + ((error as Error).message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const deleteCertificate = async (certificateId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个证书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await apiDeleteCertificate(certificateId)
    ElMessage.success('证书删除成功')
    await loadCertificates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除证书失败:', error)
      ElMessage.error('删除证书失败')
    }
  }
}

const uploadCertificateFile = async (options: UploadOptions) => {
  try {
    const res = await apiUploadCertificate(options.file) as unknown as UploadedFile
    uploadedFile.value = res
    certificateForm.value.certificateFile = res.url
    certificateForm.value.fileType = res.fileType
    certificateForm.value.fileSize = res.size
    // 保存原始文件名
    certificateForm.value.originalFileName = res.originalName
    ElMessage.success('证书文件上传成功')
  } catch (error) {
    console.error('证书文件上传失败:', error)
    ElMessage.error('证书文件上传失败')
    options.onError?.(error)
  }
}

const beforeUpload = (file: File) => {
  const allowedTypes = ['application/pdf']
  const isValidType = allowedTypes.includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 10 // 10MB
  
  if (!isValidType) {
    ElMessage.error('仅支持PDF格式的证书文件')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

const removeUploadedFile = () => {
  uploadedFile.value = null
  certificateForm.value.certificateFile = ''
  certificateForm.value.fileType = ''
  certificateForm.value.fileSize = 0
  certificateForm.value.originalFileName = ''
}

// 生命周期
onMounted(async () => {
  // 确保用户信息已加载
  if (!authStore.userInfo) {
    await authStore.getUserInfo()
  }
  
  // 确保有有效的用户ID
  if (currentUserId.value > 0) {
    await Promise.all([
      loadCertificates(),
      loadUserSkills()
    ])
  }
})
</script>

<style scoped>
.certificate-management {
  padding: 20px;
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

.certificate-list {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.certificate-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

/* 证书卡片样式 - 移除绝对定位，按钮现在集成在CertificatePreview组件中 */

.file-upload-section {
  width: 100%;
}

.uploaded-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.uploaded-file span {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.upload-tips {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    align-self: flex-end;
  }
  
  .certificate-cards {
    grid-template-columns: 1fr;
  }
  
  .certificate-actions {
    position: static;
    opacity: 1;
    margin-top: 12px;
    justify-content: flex-end;
  }
}
</style>
