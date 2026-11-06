<template>
  <section class="auth-hero">
    <div class="auth-container">
      <div class="card">
        <div class="card-header">
          <h1 class="title">用户注册</h1>
          <p class="subtitle">加入我们，开启志愿服务之旅</p>
        </div>

        <div class="card-body">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" autocomplete="off">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="username">
                  <el-input v-model="form.username" placeholder="请输入用户名（3-20个字符）" autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入真实姓名" autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱地址（可选）" autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号码" autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item prop="idCard">
                  <el-input v-model="form.idCard" placeholder="请输入身份证号" autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="age">
                  <el-input v-model.number="form.age" type="number" placeholder="请输入年龄" autocomplete="off" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio label="male">男</el-radio>
                    <el-radio label="female">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="24">
                <el-form-item prop="address">
                  <el-input v-model="form.address" placeholder="请输入所在地址xx省xx市xx区" autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="password">
                  <el-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）" autocomplete="new-password" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="confirmPassword">
                  <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="new-password" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item prop="introduction">
              <el-input v-model="form.introduction" type="textarea" :rows="4" placeholder="请简单介绍一下自己（500字以内）" />
            </el-form-item>

            <el-form-item prop="agree">
              <el-checkbox v-model="form.agree">我已阅读并同意《用户服务协议》和《隐私政策》</el-checkbox>
            </el-form-item>

            <el-button class="primary-btn" type="primary" :loading="loading" @click="onSubmit" round>立即注册</el-button>
            <div class="login-link">已有账号？<a @click.prevent="$router.push('/login')">立即登录</a></div>
          </el-form>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  idCard: '',
  gender: '',
  age: undefined as unknown as number,
  address: '',
  introduction: '',
  agree: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20个字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{
    validator: (_: unknown, v: string, cb: (error?: Error) => void) => {
      if (!v) return cb();
      if (!/^1[3-9]\d{9}$/.test(v)) return cb(new Error('手机号格式不正确'));
      cb()
    },
    trigger: 'blur'
  }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  age: [{ type: 'number', required: true, message: '请输入年龄', trigger: 'blur' }],
  address: [{ required: true, message: '请输入所在地址', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{
    validator: (_: unknown, v: string, cb: (error?: Error) => void) => {
      if (v !== form.value.password) return cb(new Error('两次输入的密码不一致'));
      cb()
    },
    trigger: 'blur'
  }],
  introduction: [{ max: 500, message: '最多500字', trigger: 'blur' }],
  agree: [{
    type: 'boolean',
    validator: (_: unknown, v: boolean, cb: (error?: Error) => void) => {
      if (!v) return cb(new Error('请同意用户协议和隐私政策'));
      cb()
    },
    trigger: 'change'
  }]
}

const onSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    // 将gender字符串转换为数字：male->1, female->2, 其他->0
    const genderMap: { [key: string]: number } = {
      'male': 1,
      'female': 2
    }

      await authApi.register({
        username: form.value.username,
        password: form.value.password,
        realName: form.value.realName,
        email: form.value.email,
        phone: form.value.phone,
        idCard: form.value.idCard,
        gender: genderMap[form.value.gender] || 0,
        age: form.value.age,
        address: form.value.address,
        introduction: form.value.introduction
      })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e: unknown) {
    const error = e as { message?: string }
    ElMessage.error(error?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-hero { background: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 0; margin-left: calc(50% - 50vw); margin-right: calc(50% - 50vw); }
.auth-container { display: flex; justify-content: center; }
.card { width: 100%; max-width: 1200px; background: #fff; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; margin: 0 20px; }
.card-header { background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%); color: #fff; text-align: center; padding: 28px 24px 20px; }
.title { font-size: 24px; margin-bottom: 6px; }
.subtitle { font-size: 13px; opacity: .9; }
.card-body { padding: 24px; }
.primary-btn { width: 100%; margin: 12px 0; }
.login-link { text-align: center; color: #666; font-size: 14px; }
.login-link a { color: #42a5f5; cursor: pointer; }
</style>


