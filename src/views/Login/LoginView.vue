<template>
<div class="page page--web">
    <div class="login-container">
      <div class="login-header">
        <h1 class="system-title">志愿者活动服务平台</h1>
        <div class="login-header-content">
          <h2 class="login-title">管理后台</h2>
          <div class="admin-badge">管理员登录</div>
        </div>
      </div>

      <form class="login-form" @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input v-model="username" type="text" class="form-input" placeholder="请输入管理员用户名" required />
          <div class="error-message" v-show="usernameError">{{ usernameError }}</div>
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <div class="password-input-wrapper">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请输入密码"
              required
            />
            <span class="password-toggle" @click="togglePassword">
              <svg v-if="!showPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
              <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
            </span>
          </div>
          <div class="error-message" v-show="passwordError">{{ passwordError }}</div>
        </div>

        <div class="remember-me">
          <input id="rememberMe" type="checkbox" v-model="remember" />
          <label for="rememberMe">记住密码</label>
        </div>

        <button class="login-button" type="submit" :disabled="loading">
          <span v-if="!loading">登录管理后台</span>
          <span v-else>登录中...</span>
        </button>

        <div class="loading" v-show="loading">
          <span class="spinner"></span>
          <span>登录中...</span>
        </div>

        <div class="back-to-front">
          返回 <a href="http://localhost:3000/" target="_self">用户前台</a>
        </div>
      </form>
    </div>
  </div>

</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAdminAuthStore()

const username = ref('')
const password = ref('')
const remember = ref(false)
const loading = ref(false)
const usernameError = ref('')
const passwordError = ref('')
const showPassword = ref(false)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

function validate(): boolean {
  usernameError.value = ''
  passwordError.value = ''

  let ok = true
  if (!username.value || username.value.trim().length < 3) {
    usernameError.value = '用户名长度至少3个字符'
    ok = false
  }
  if (!password.value || password.value.length < 6) {
    passwordError.value = '密码长度至少6个字符'
    ok = false
  }
  return ok
}

async function onSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    // 调用真实的后端登录接口
    const loginData = {
      username: username.value,
      password: password.value
    }

    await authStore.login(loginData)

    // 处理记住密码
    if (remember.value) {
      localStorage.setItem('admin-remember', JSON.stringify({ u: username.value }))
    } else {
      localStorage.removeItem('admin-remember')
    }

    ElMessage.success('登录成功')

    // 延迟跳转，确保状态更新完成
    setTimeout(() => {
      router.push('/')
    }, 100)
  } catch (error: unknown) {
    console.error('登录失败:', error)
    const errorMessage = error instanceof Error ? error.message : '登录失败，请检查用户名和密码'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1e3a8a;
}

.page--web {
  /* 占满整个视口宽度以适配Web端 */
  width: 100%;
}

.login-container {
  background: #fff;
  border-radius: 15px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  overflow: hidden;
  width: 100%;
  max-width: 450px;
  margin: 20px;
}

.login-header {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  color: #fff;
  padding: 30px 30px 25px;
  text-align: center;
}

.system-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 20px 0;
  color: #fff;
  line-height: 1.2;
}

.login-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.login-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}

.admin-badge {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 12px;
  margin-top: 10px;
}

.login-form {
  padding: 30px 30px;
}

.form-group {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.form-label {
  display: inline-block;
  font-weight: bold;
  margin: 0;
  color: #1d52f1;
  min-width: 55px;
  flex-shrink: 0;
  font-size: 14px;
}
.password-input-wrapper {
  position: relative;
  flex: 1;
}
.form-input {
  width: 100%;
  padding: 8px 45px 8px 10px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}
.form-input:focus { outline: none; border-color: #42a5f5; }
.password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}
.password-toggle:hover { color: #42a5f5; }
.eye-icon {
  width: 20px;
  height: 20px;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  margin-left: 65px;
}

.remember-me { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.remember-me input[type="checkbox"] { width: 16px; height: 16px; }

.login-button {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}
.login-button:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(66, 165, 245, 0.4); }
.login-button:disabled { background: #6c757d; cursor: not-allowed; transform: none; box-shadow: none; }

.loading { text-align: center; margin-top: 10px; }
.spinner {
  width: 20px; height: 20px; border: 2px solid #f3f3f3; border-top: 2px solid #42a5f5;
  border-radius: 50%; display: inline-block; margin-right: 10px; animation: spin 1s linear infinite;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.back-to-front { text-align: center; color: #666; font-size: 14px; }
.back-to-front a { color: #42a5f5; text-decoration: none; font-weight: bold; }
.back-to-front a:hover { text-decoration: underline; }

@media (max-width: 480px) {
  .login-container { margin: 10px; max-width: none; }
  .login-header { padding: 30px 20px 20px; }
  .login-form { padding: 30px 20px; }
}
</style>


