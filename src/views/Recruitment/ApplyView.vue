<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recruitmentApi } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const form = ref({ intro: '', skills: '', experience: '' })
const formRef = ref()

// 表单验证规则
const rules = {
  intro: [
    { required: true, message: '请输入自我介绍', trigger: 'blur' },
    { min: 10, max: 500, message: '自我介绍长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  skills: [
    { required: true, message: '请输入相关技能', trigger: 'blur' },
    { min: 2, max: 200, message: '相关技能长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  experience: [
    { required: true, message: '请输入相关经验', trigger: 'blur' },
    { min: 10, max: 500, message: '相关经验长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

async function submit() {
  if (!formRef.value) return
  
  try {
    // 验证表单
    await formRef.value.validate()
    
    submitting.value = true
    const id = Number(route.params.id)
    await recruitmentApi.apply(id, form.value)
    ElMessage.success('申请已提交')
    router.back()
  } catch (error) {
    if (error !== false) { // 验证失败时 error 为 false
      console.error('提交申请失败:', error)
      ElMessage.error('提交申请失败')
    }
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回上一级
      </el-button>
      <h2>申请招募</h2>
    </div>
    <el-form ref="formRef" label-width="100px" :model="form" :rules="rules">
      <el-form-item label="自我介绍" prop="intro">
        <el-input 
          type="textarea" 
          v-model="form.intro" 
          rows="4" 
          placeholder="请详细介绍一下自己，包括个人背景、兴趣爱好等"
        />
      </el-form-item>
      <el-form-item label="相关技能" prop="skills">
        <el-input 
          v-model="form.skills" 
          placeholder="示例：急救, 翻译, 摄影, 组织协调等" 
        />
      </el-form-item>
      <el-form-item label="相关经验" prop="experience">
        <el-input 
          type="textarea" 
          v-model="form.experience" 
          rows="3" 
          placeholder="请描述您的相关志愿服务经验，包括参与过的活动、担任的角色等" 
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">提交申请</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page { padding: 16px; max-width: 720px; }
.page-header { margin-bottom: 16px; }
.back-button { margin-bottom: 8px; }
</style>


