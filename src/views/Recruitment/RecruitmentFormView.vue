<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recruitmentApi } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const form = ref<any>({ 
  title: '', 
  description: '', 
  recruitCount: 1, 
  status: 1,
  startTime: '',
  endTime: '',
  requirements: '',
  contactPerson: '', // 联系人姓名
  contactInfo: '' // 联系方式
})
const saving = ref(false)
const isEdit = ref(false)

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入招募标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入招募描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  recruitCount: [
    { required: true, message: '请输入招募人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 1000, message: '招募人数必须在 1 到 1000 之间', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  requirements: [
    { required: true, message: '请输入招募要求', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '联系人姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

const formRef = ref()

async function submit() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  saving.value = true
  try {
    if (isEdit.value) {
      await recruitmentApi.update(Number(route.query.id), form.value)
      ElMessage.success('更新成功')
    } else {
      await recruitmentApi.create(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/recruitment')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function fetchDetail() {
  if (!route.query.id) return
  
  try {
    const res = await recruitmentApi.getDetail(Number(route.query.id))
    // 修复数据回填问题：提取data字段中的数据
    if (res && res.data) {
      form.value = { ...res.data }
    } else {
      // 兼容直接返回数据的情况
      form.value = { ...res }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

function goBack() {
  router.push('/recruitment')
}

onMounted(() => {
  if (route.query.id) {
    isEdit.value = true
    fetchDetail()
  }
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>{{ isEdit ? '编辑招募' : '创建招募' }}</h2>
      </div>
    </div>
    
    <el-card>
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="120px" 
        style="max-width: 800px;"
      >
        <el-form-item label="招募标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入招募标题" />
        </el-form-item>
        
        <el-form-item label="招募描述" prop="description">
          <el-input 
            type="textarea" 
            rows="4" 
            v-model="form.description" 
            placeholder="请详细描述招募内容、工作内容等"
          />
        </el-form-item>
        
        <el-form-item label="招募人数" prop="recruitCount">
          <el-input-number 
            v-model="form.recruitCount" 
            :min="1" 
            :max="1000"
            placeholder="招募人数"
          />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="招募要求" prop="requirements">
          <el-input 
            type="textarea" 
            rows="3" 
            v-model="form.requirements" 
            placeholder="请输入招募要求，如年龄、技能、经验等"
          />
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactPerson">
          <el-input 
            v-model="form.contactPerson" 
            placeholder="请输入联系人姓名"
          />
        </el-form-item>
        
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input 
            v-model="form.contactInfo" 
            placeholder="请输入联系方式，如电话、邮箱等"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px;">
            <el-option :value="1" label="草稿" />
            <el-option :value="2" label="发布中" />
            <el-option :value="3" label="已结束" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page { 
  padding: 16px; 
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>


