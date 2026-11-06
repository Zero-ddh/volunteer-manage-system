<template>
  <div class="profile-page">
    <h2>个人信息</h2>
    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入身份证号" />
      </el-form-item>
      <el-form-item label="所在地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入所在地址" />
      </el-form-item>
      <el-form-item label="个人简介">
        <el-input v-model="form.introduction" type="textarea" :rows="4" placeholder="请简单介绍一下自己" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSave" :loading="saving">保存</el-button>
      </el-form-item>
    </el-form>

    <!-- 修改密码模块 -->
    <h3>修改密码</h3>
    <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onChangePassword" :loading="changingPassword">修改密码</el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
authStore.restore?.()

const formRef = ref()
const passwordFormRef = ref()
interface UserProfile {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  idCard: string
  address: string
  introduction: string
  avatar: string
}

const form = ref<UserProfile>({ 
  id: 0, 
  username: '', 
  realName: '', 
  email: '', 
  phone: '', 
  idCard: '',
  address: '',
  introduction: '',
  avatar: '' 
})
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const saving = ref(false)
const changingPassword = ref(false)

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { 
      validator: (_: unknown, v: string, cb: (error?: Error) => void) => {
        if (!v) return cb()
        // 身份证号格式验证：不超过18位数字或字母
        if (v.length > 18) {
          return cb(new Error('身份证号不能超过18位'))
        }
        if (!/^[0-9A-Za-z]+$/.test(v)) {
          return cb(new Error('身份证号只能包含数字和字母'))
        }
        cb()
      },
      trigger: 'blur'
    }
  ],
  address: [{ required: true, message: '请输入所在地址', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_: unknown, v: string, cb: (error?: Error) => void) => {
        if (v !== passwordForm.value.newPassword) {
          return cb(new Error('两次输入的密码不一致'))
        }
        cb()
      },
      trigger: 'blur'
    }
  ]
}

const loadProfile = async () => {
  try {
    const response = await authApi.getUserInfo()
    console.log('API响应:', response)
    if (response) {
      console.log('用户数据:', response)
      const userData = response as unknown as UserProfile
      // 确保所有必要字段都有默认值
      form.value = {
        id: userData.id || 0,
        username: userData.username || '',
        realName: userData.realName || '',
        email: userData.email || '',
        phone: userData.phone || '',
        idCard: userData.idCard || '',
        address: userData.address || '',
        introduction: userData.introduction || '',
        avatar: userData.avatar || ''
      }
      console.log('表单数据已更新:', form.value)
    } else {
      console.log('API响应数据为空')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

onMounted(async () => {
  await loadProfile()
})

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await authApi.updateUserInfo(form.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const onChangePassword = async () => {
  await passwordFormRef.value.validate()
  changingPassword.value = true
  try {
    await authApi.changePassword({
      userId: form.value.id,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    passwordFormRef.value.resetFields()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '密码修改失败'
    ElMessage.error(errorMessage)
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
.profile-page { max-width: 720px; margin: 20px auto; }
.profile-page h3 { margin-top: 30px; margin-bottom: 20px; color: #333; }
</style>


