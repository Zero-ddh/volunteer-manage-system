<template>
  <div class="activity-form">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑活动' : '发布活动' }}</h1>
      <p class="page-subtitle">{{ isEdit ? '修改活动信息' : '创建新的志愿活动' }}</p>
    </div>

    <!-- 活动表单 -->
    <el-card>
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        class="activity-form-content"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动标题" prop="title">
              <el-input 
                v-model="form.title" 
                placeholder="请输入活动标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动分类" prop="categoryId">
              <el-select 
                v-model="form.categoryId" 
                placeholder="请选择分类" 
                style="width: 100%"
                :loading="categoryLoading"
                filterable
              >
                <el-option 
                  v-for="category in categoryList" 
                  :key="category.id" 
                  :label="category.categoryName" 
                  :value="category.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动组织者" prop="organizerId">
              <el-select 
                v-model="form.organizerId" 
                placeholder="请选择组织者" 
                style="width: 100%"
                filterable
                :loading="organizerLoading"
              >
                <el-option 
                  v-for="organizer in organizerList" 
                  :key="organizer.id" 
                  :label="organizer.realName || organizer.username" 
                  :value="organizer.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大参与人数" prop="maxParticipants">
              <el-input-number 
                v-model="form.maxParticipants" 
                :min="1" 
                :max="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="请详细描述活动内容、目的和要求"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动地点" prop="location">
          <el-input 
            v-model="form.location" 
            placeholder="请输入详细的活动地点，包括省市区街道门牌号"
            maxlength="200"
          />
        </el-form-item>

        <!-- 封面图片字段已屏蔽 -->
        <!-- <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            :headers="uploadHeaders"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：400x300px，支持jpg、png格式，大小不超过10MB</div>
        </el-form-item> -->

        <el-form-item label="技能要求" prop="requirements">
          <el-input 
            v-model="form.requirements" 
            type="textarea" 
            :rows="3"
            placeholder="请输入技能要求（如无技能要求请留空），如需技能要求请使用逗号分隔，如：技能1,技能2,技能3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动参与条件" prop="participationConditions">
          <el-input 
            v-model="form.participationConditions" 
            type="textarea" 
            :rows="3"
            placeholder="请输入活动参与条件，如：必须年满18周岁、身体健康、具备基本沟通能力等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactInfo">
          <el-input 
            v-model="form.contactInfo" 
            placeholder="请输入联系方式（电话、微信等）"
            maxlength="200"
          />
        </el-form-item>

        <el-form-item label="紧急联系人" prop="emergencyContactName">
          <el-input 
            v-model="form.emergencyContactName" 
            placeholder="请输入紧急联系人姓名"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="紧急联系电话" prop="emergencyContactPhone">
          <el-input 
            v-model="form.emergencyContactPhone" 
            placeholder="请输入紧急联系人电话"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="活动状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 300px">
            <el-option label="草稿" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="进行中" :value="3" />
            <el-option label="已结束" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="saveDraft" :loading="saving">
              保存草稿
            </el-button>
            <el-button type="success" @click="saveActivity" :loading="publishing">
              保存
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi, userApi, activityCategoryApi } from '@/api'

const route = useRoute()
const router = useRouter()

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 响应式数据
const formRef = ref()
const saving = ref(false)
const publishing = ref(false)
const organizerLoading = ref(false)
const organizerList = ref<Array<{id: number, realName?: string, username: string}>>([])
const categoryLoading = ref(false)
const categoryList = ref<Array<{id: number, categoryName: string}>>([])

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  categoryId: undefined as number | undefined,
  organizerId: 1, // 默认组织者ID
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 20,
  currentParticipants: 0,
  status: 1, // 默认草稿状态
  coverImage: '',
  requirements: '',
  participationConditions: '',
  contactInfo: '',
  emergencyContactName: '',
  emergencyContactPhone: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择活动分类', trigger: 'change' }
  ],
  organizerId: [
    { required: true, message: '请选择活动组织者', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择活动状态', trigger: 'change' }
  ],
  requirements: [
    { min: 0, max: 500, message: '技能要求长度不能超过 500 个字符', trigger: 'blur' }
  ],
  participationConditions: [
    { required: true, message: '请输入活动参与条件', trigger: 'blur' },
    { min: 5, max: 500, message: '参与条件长度在 5 到 500 个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { min: 5, max: 200, message: '联系方式长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  emergencyContactName: [
    { required: true, message: '请输入紧急联系人姓名', trigger: 'blur' },
    { min: 2, max: 100, message: '紧急联系人姓名长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  emergencyContactPhone: [
    { required: true, message: '请输入紧急联系人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入正确的电话号码格式', trigger: 'blur' }
  ]
}

// 上传配置
const uploadUrl = '/api/v1/upload/image'
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
}

// 上传前验证
const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 封面上传成功
const handleCoverSuccess = (response: {code: number, data: {url: string}}) => {
  if (response.code === 200) {
    // 确保图片URL是完整的URL
    let imageUrl = response.data.url
    if (imageUrl && !imageUrl.startsWith('http')) {
      // 如果是相对路径，添加基础URL
      imageUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}${imageUrl}`
    }
    form.coverImage = imageUrl
    console.log('图片上传成功，URL:', imageUrl)
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('封面上传失败')
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    const data = { ...form, status: 1 } // 强制设为草稿状态
    if (isEdit.value) {
      await activityApi.updateActivity(Number(route.params.id), data)
      ElMessage.success('草稿保存成功')
    } else {
      await activityApi.createActivity(data)
      ElMessage.success('草稿保存成功')
    }
    
    goBack()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存活动
const saveActivity = async () => {
  try {
    await formRef.value?.validate()
    
    publishing.value = true
    
    // 保存时保持当前状态，不强制修改状态
    const data = { ...form }
    
    if (isEdit.value) {
      await activityApi.updateActivity(Number(route.params.id), data)
      ElMessage.success('活动保存成功')
    } else {
      await activityApi.createActivity(data)
      ElMessage.success('活动保存成功')
    }
    
    goBack()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    publishing.value = false
  }
}

// 加载组织者列表
const loadOrganizerList = async () => {
  try {
    organizerLoading.value = true
    const res = await userApi.getUserList({ 
      pageNo: 1, 
      pageSize: 100,
      role: 'organizer' // 假设后端支持按角色筛选
    })
    organizerList.value = res.data?.records || res.data?.list || []
  } catch {
    ElMessage.error('加载组织者列表失败')
    // 如果API不支持角色筛选，则加载所有用户作为备选
    try {
      const res = await userApi.getUserList({ pageNo: 1, pageSize: 100 })
      organizerList.value = res.data?.records || res.data?.list || []
    } catch {
      console.error('加载用户列表失败')
    }
  } finally {
    organizerLoading.value = false
  }
}

// 加载活动分类列表
const loadCategoryList = async () => {
  try {
    categoryLoading.value = true
    const res = await activityCategoryApi.getEnabledCategories() as unknown as {code: number, data: Array<{id: number, categoryName: string}>}
    if (res.code === 200) {
      categoryList.value = res.data || []
    } else {
      ElMessage.error('加载活动分类失败')
    }
  } catch {
    console.error('加载活动分类失败')
    ElMessage.error('加载活动分类失败')
  } finally {
    categoryLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/activities/list')
}

// 加载活动详情（编辑模式）
const loadActivityDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await activityApi.getActivityDetail(Number(route.params.id))
    if (res.data) {
      Object.assign(form, res.data)
      // 处理封面图片URL
      if (form.coverImage && !form.coverImage.startsWith('http')) {
        form.coverImage = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}${form.coverImage}`
      }
    }
  } catch {
    ElMessage.error('加载活动详情失败')
    goBack()
  }
}

// 页面加载
onMounted(() => {
  loadOrganizerList()
  loadCategoryList()
  if (isEdit.value) {
    loadActivityDetail()
  }
})
</script>

<style scoped>
.activity-form {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
}

.activity-form-content {
  max-width: 800px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 200px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .activity-form-content {
    max-width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cover-uploader {
    width: 100%;
    height: 200px;
  }
}
</style>