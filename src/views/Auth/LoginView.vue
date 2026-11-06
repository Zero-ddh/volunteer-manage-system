<template>
  <section class="auth-hero">
    <h1 class="platform-title">志愿者服务平台</h1>
    <a class="back-home" @click.prevent="$router.push('/')">← 返回首页</a>
    <div class="auth-container">
      <div class="card">
        <div class="card-header">
          <h1 class="title">用户登录</h1>
          <p class="subtitle">欢迎回到志愿者服务平台</p>
        </div>

        <div class="card-body">
          <div class="method-tabs">
            <button type="button" class="tab" :class="{ active: loginMethod==='username' }" @click="loginMethod='username'">用户名</button>
            <button type="button" class="tab" :class="{ active: loginMethod==='phone' }" @click="loginMethod='phone'">手机号</button>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
            <el-form-item prop="account">
              <el-input v-model="form.account" :placeholder="loginMethod==='phone' ? '请输入手机号' : '请输入用户名'" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
              >
                <template #suffix>
                  <el-icon class="password-icon" @click="togglePassword">
                    <View v-if="showPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="options">
              <label class="remember">
                <input type="checkbox" v-model="rememberMe" />
                <span>记住密码</span>
              </label>
              <a class="link" @click="showForgotDialog">忘记密码？</a>
            </div>

            <el-button class="primary-btn" type="primary" :loading="loading" @click="onSubmit" round>登录</el-button>
            <div class="register-link">还没有账号？<a @click.prevent="$router.push('/register')">立即注册</a></div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="forgotDialogVisible" title="忘记密码" width="400px">
      <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef" label-width="100px" autocomplete="off">
        <!-- 隐藏的假输入框，用于欺骗浏览器禁用自动填充 -->
        <input type="text" style="position:absolute;width:0;height:0;opacity:0;pointer-events:none" autocomplete="off" />
        <input type="password" style="position:absolute;width:0;height:0;opacity:0;pointer-events:none" autocomplete="off" />

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="forgotForm.phone"
            placeholder="请输入手机号"
            autocomplete="new-password"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="verifyCode">
          <el-input
            v-model="forgotForm.verifyCode"
            placeholder="请输入短信验证码"
            autocomplete="off"
            maxlength="6"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            autocomplete="new-password"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="forgotLoading" @click="handleForgotPassword">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { View, Hide } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const formRef = ref()
const loginMethod = ref<'username'|'phone'>('username')
const rememberMe = ref(false)
const showPassword = ref(false)
const form = ref({ account: '', password: '' })

// 忘记密码相关
const forgotDialogVisible = ref(false)
const forgotLoading = ref(false)
const forgotFormRef = ref()
const forgotForm = ref({
  phone: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: ''
})

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      validator: (_: any, val: string, cb: any) => {
        if (loginMethod.value === 'phone' && !/^1[3-9]\d{9}$/.test(val || '')) return cb(new Error('请输入正确的手机号'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
} as any

// 忘记密码表单验证规则
const forgotRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d+$/, message: '验证码只能是数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (_: any, val: string, cb: any) => {
        if (val !== forgotForm.value.newPassword) {
          cb(new Error('两次输入的密码不一致'))
        } else {
          cb()
        }
      },
      trigger: 'blur'
    }
  ]
} as any

watch(loginMethod, () => {
  form.value.account = ''
})

const onSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const payload = loginMethod.value === 'phone'
      ? { phone: form.value.account, password: form.value.password }
      : { username: form.value.account, password: form.value.password }
    await authStore.login(payload as any)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const showForgotDialog = () => {
  forgotDialogVisible.value = true
  // 重置表单
  forgotForm.value = {
    phone: '',
    verifyCode: '',
    newPassword: '',
    confirmPassword: ''
  }
  forgotFormRef.value?.clearValidate()
}

const handleForgotPassword = async () => {
  await forgotFormRef.value.validate()
  forgotLoading.value = true
  try {
    await authApi.forgotPassword({
      phone: forgotForm.value.phone,
      verifyCode: forgotForm.value.verifyCode,
      newPassword: forgotForm.value.newPassword
    })

    ElMessage.success('密码重置成功')
    forgotDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '密码重置失败')
  } finally {
    forgotLoading.value = false
  }
}
</script>

<style scoped>
.auth-hero {
  background: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%);
  min-height: 100vh; /* 背景铺满视口 */
  display: flex;
  flex-direction: column;
  align-items: center; /* 垂直居中卡片 */
  justify-content: center; /* 水平居中卡片 */
  padding: 0; /* 上下无额外留白 */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  position: relative;
}
.platform-title {
  position: absolute;
  top: 40px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 42px;
  font-weight: bold;
  color: #fff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 2px;
  margin: 0;
  z-index: 10;
}
.auth-container { display: flex; justify-content: center; width: 100%; }
.card { width: 100%; max-width: 420px; background: #fff; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; margin: 0 20px; }
.card-header { background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%); color: #fff; text-align: center; padding: 28px 24px 20px; }
.title { font-size: 24px; margin-bottom: 6px; }
.subtitle { font-size: 13px; opacity: .9; }
.card-body { padding: 24px; }
.back-home { position: absolute; left: 20px; top: 20px; color: #fff; text-decoration: none; font-size: 14px; cursor: pointer; font-weight: 500; }
.method-tabs { display: flex; border: 2px solid #e9ecef; border-radius: 8px; overflow: hidden; margin-bottom: 16px; }
.tab { flex: 1; padding: 8px 0; background: #f8f9fa; color: #666; border: none; cursor: pointer; font-size: 14px; }
.tab.active { background: #42a5f5; color: #fff; }
.options { display: flex; justify-content: space-between; align-items: center; margin: 6px 0 16px; }
.remember { display: flex; align-items: center; gap: 6px; color: #666; }
.link { color: #42a5f5; cursor: pointer; font-size: 14px; }
.primary-btn { width: 100%; margin-bottom: 12px; }
.register-link { text-align: center; color: #666; font-size: 14px; }
.register-link a { color: #42a5f5; cursor: pointer; }
.password-icon { cursor: pointer; color: #999; transition: color 0.3s; }
.password-icon:hover { color: #42a5f5; }

@media (max-width: 768px) {
  .platform-title {
    font-size: 32px;
    top: 30px;
    letter-spacing: 1px;
  }
  .back-home {
    left: 15px;
    top: 15px;
    font-size: 13px;
  }
}
</style>


