<template>
  <div class="activity-summary">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑活动总结' : '发布活动总结' }}</h1>
      <p class="page-subtitle">{{ isEdit ? '修改活动总结内容' : '为活动发布总结和照片' }}</p>
    </div>

    <!-- 活动总结表单 -->
    <el-card>
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        class="summary-form-content"
      >
        <el-form-item label="选择活动" prop="activityId">
          <el-select 
            v-model="form.activityId" 
            placeholder="请选择要总结的活动" 
            style="width: 100%"
            filterable
            :loading="activityLoading"
            @change="handleActivityChange"
          >
            <el-option 
              v-for="activity in activityList" 
              :key="activity.id" 
              :label="activity.title" 
              :value="activity.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="活动信息" v-if="selectedActivity">
          <div class="activity-info">
            <div class="info-item">
              <span class="label">活动标题：</span>
              <span class="value">{{ selectedActivity.title }}</span>
            </div>
            <div class="info-item">
              <span class="label">活动时间：</span>
              <span class="value">{{ formatDateTime(selectedActivity.startTime) }} - {{ formatDateTime(selectedActivity.endTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">活动地点：</span>
              <span class="value">{{ selectedActivity.location }}</span>
            </div>
            <div class="info-item">
              <span class="label">参与人数：</span>
              <span class="value">{{ selectedActivity.currentParticipants }}/{{ selectedActivity.maxParticipants }}人</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="总结内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="8"
            placeholder="请详细描述活动过程、成果、感受等..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动照片" prop="photos">
          <el-upload
            v-model:file-list="fileList"
            action="/api/v1/upload/image"
            :headers="uploadHeaders"
            :before-upload="beforePhotoUpload"
            :on-success="handlePhotoSuccess"
            :on-remove="handlePhotoRemove"
            :on-error="handlePhotoError"
            multiple
            list-type="picture-card"
            :limit="10"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg/png格式，单张图片不超过5MB，最多上传10张
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="saveDraft" :loading="saving">
              保存草稿
            </el-button>
            <el-button type="success" @click="publishSummary" :loading="publishing">
              发布总结
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动总结列表 -->
    <el-card class="summary-list" v-if="!isEdit">
      <template #header>
        <div class="card-header">
          <span>历史活动总结</span>
          <el-button type="primary" @click="loadSummaryList">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="summaryList" v-loading="summaryLoading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="活动信息" min-width="200">
          <template #default="{ row }">
            <div class="activity-info">
              <div class="activity-title">{{ getActivityTitle(row.activityId) }}</div>
              <div class="activity-time">{{ formatDateTime(row.publishTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="总结内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="照片数量" width="100">
          <template #default="{ row }">
            {{ getPhotoCount(row.photos) }}张
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="editSummary(row.id)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteSummary(row.id)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="summaryPageNo"
          v-model:page-size="summaryPageSize"
          :page-sizes="[5, 10, 20]"
          :total="summaryTotal"
          layout="total, sizes, prev, pager, next"
          @size-change="loadSummaryList"
          @current-change="loadSummaryList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { activitySummaryApi, activityApi } from '@/api'

const route = useRoute()
const router = useRouter()

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 响应式数据
const formRef = ref()
const saving = ref(false)
const publishing = ref(false)
const activityLoading = ref(false)
const activityList = ref<any[]>([])
const selectedActivity = ref<any>(null)
const fileList = ref<any[]>([])
const summaryList = ref<any[]>([])
const summaryLoading = ref(false)
const summaryPageNo = ref(1)
const summaryPageSize = ref(5)
const summaryTotal = ref(0)

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  activityId: undefined as number | undefined,
  organizerId: 1, // 当前登录的组织者ID
  content: '',
  photos: ''
})

// 表单验证规则
const rules = {
  activityId: [
    { required: true, message: '请选择要总结的活动', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入总结内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '总结内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

// 上传配置
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
}

// 上传前验证
const beforePhotoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 照片上传成功
const handlePhotoSuccess = (response: any, file: any) => {
  if (response.code === 200) {
    const photos = JSON.parse(form.photos || '[]')
    photos.push(response.data.url)
    form.photos = JSON.stringify(photos)
    ElMessage.success('照片上传成功')
  } else {
    ElMessage.error('照片上传失败')
  }
}

// 照片移除
const handlePhotoRemove = (file: any) => {
  const photos = JSON.parse(form.photos || '[]')
  const index = photos.findIndex((url: string) => url === file.response?.data?.url)
  if (index > -1) {
    photos.splice(index, 1)
    form.photos = JSON.stringify(photos)
  }
}

// 照片上传失败
const handlePhotoError = () => {
  ElMessage.error('照片上传失败')
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 获取活动标题
const getActivityTitle = (activityId: number) => {
  const activity = activityList.value.find(a => a.id === activityId)
  return activity?.title || `活动${activityId}`
}

// 获取照片数量
const getPhotoCount = (photos: string) => {
  if (!photos) return 0
  try {
    return JSON.parse(photos).length
  } catch {
    return 0
  }
}

// 加载活动列表
const loadActivityList = async () => {
  try {
    activityLoading.value = true
    const res = await activityApi.getActivityList({ 
      pageNo: 1, 
      pageSize: 1000,
      status: 4 // 只显示已结束的活动
    })
    activityList.value = res.data?.records || res.data?.list || []
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  } finally {
    activityLoading.value = false
  }
}

// 活动选择变化
const handleActivityChange = (activityId: number) => {
  selectedActivity.value = activityList.value.find(a => a.id === activityId)
}

// 加载总结列表
const loadSummaryList = async () => {
  try {
    summaryLoading.value = true
    const res = await activitySummaryApi.getSummaryList({
      pageNo: summaryPageNo.value,
      pageSize: summaryPageSize.value,
      organizerId: form.organizerId
    })
    summaryList.value = res.data?.records || res.data?.list || []
    summaryTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载总结列表失败')
  } finally {
    summaryLoading.value = false
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    const data = { ...form }
    if (isEdit.value) {
      await activitySummaryApi.updateSummary(Number(route.params.id), data)
      ElMessage.success('草稿保存成功')
    } else {
      await activitySummaryApi.createSummary(data)
      ElMessage.success('草稿保存成功')
    }
    
    goBack()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 发布总结
const publishSummary = async () => {
  try {
    await formRef.value?.validate()
    
    await ElMessageBox.confirm('确认发布此活动总结吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    publishing.value = true
    
    const data = { ...form }
    if (isEdit.value) {
      await activitySummaryApi.updateSummary(Number(route.params.id), data)
      ElMessage.success('活动总结发布成功')
    } else {
      await activitySummaryApi.createSummary(data)
      ElMessage.success('活动总结发布成功')
    }
    
    goBack()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  } finally {
    publishing.value = false
  }
}

// 编辑总结
const editSummary = (id: number) => {
  router.push(`/activity-summaries/${id}/edit`)
}

// 删除总结
const deleteSummary = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此活动总结吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await activitySummaryApi.deleteSummary(id)
    ElMessage.success('删除成功')
    await loadSummaryList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 返回列表
const goBack = () => {
  router.push('/activity-summaries')
}

// 加载总结详情（编辑模式）
const loadSummaryDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await activitySummaryApi.getSummaryDetail(Number(route.params.id))
    if (res.data) {
      Object.assign(form, res.data)
      
      // 处理照片列表
      if (res.data.photos) {
        try {
          const photos = JSON.parse(res.data.photos)
          fileList.value = photos.map((url: string, index: number) => ({
            name: `photo_${index + 1}.jpg`,
            url: url,
            response: { data: { url } }
          }))
        } catch (error) {
          console.error('解析照片列表失败:', error)
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载总结详情失败')
    goBack()
  }
}

// 页面加载
onMounted(() => {
  loadActivityList()
  if (isEdit.value) {
    loadSummaryDetail()
  } else {
    loadSummaryList()
  }
})
</script>

<style scoped>
.activity-summary {
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

.summary-form-content {
  padding: 20px 0;
}

.activity-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.6;
}

.info-item {
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: bold;
  color: #333;
  margin-right: 8px;
}

.value {
  color: #666;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.summary-list {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-info {
  line-height: 1.4;
}

.activity-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>
