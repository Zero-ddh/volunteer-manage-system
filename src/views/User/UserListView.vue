<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">管理后台用户基本信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-group">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户名、姓名或邮箱..."
          style="width: 300px"
          clearable
          autocomplete="off"
          :autofill="false"
        />
        <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 120px" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="btn-group">
        <el-button type="success" @click="handleAdd">添加用户</el-button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <el-table
        :data="userList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="用户信息" width="400">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-avatar">{{ row.realName?.charAt(0) || row.username?.charAt(0) || 'U' }}</div>
              <div class="user-details">
                <div class="user-name">{{ row.username }}</div>
                <div class="user-email">{{ row.realName }} · {{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="180">
          <template #default="{ row }">
            <el-tag v-if="row.roleIds?.length > 0" type="primary">
              {{ getRoleName(row.roleIds[0]) }}
            </el-tag>
            <span v-else class="text-gray">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" @click="handleRoleAssign(row)">角色</el-button>
              <el-button size="small" type="info" @click="handleResetPassword(row)">重置密码</el-button>
              <el-button 
                size="small" 
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            autocomplete="off"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名" 
            autocomplete="off"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱" 
            autocomplete="off"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号" 
            autocomplete="off"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            autocomplete="new-password"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>{{ currentUser?.username }} ({{ currentUser?.realName }})</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox
              v-for="role in roleList"
              :key="role.id"
              :label="role.id"
            >
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="用户">
          <span>{{ currentUser?.username }} ({{ currentUser?.realName }})</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="resetPasswordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
            autocomplete="new-password"
            :autofill="false"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="resetPasswordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
            autocomplete="new-password"
            :autofill="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitting">
          重置密码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUserApi } from '@/api/user'
import { roleApi } from '@/api/role'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const userList = ref<Array<Record<string, unknown>>>([])
const selectedUsers = ref<Array<Record<string, unknown>>>([])
const statistics = ref<Record<string, unknown>>({})
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref<Record<string, unknown> | null>(null)
const selectedRoleIds = ref<number[]>([])
const roleList = ref<Array<Record<string, unknown>>>([])
const resetPasswordFormRef = ref<{ validate: () => Promise<boolean> } | null>(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  status: null
})

// 分页
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  status: 1
})

// 重置密码表单数据
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 重置密码验证规则
const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== resetPasswordForm.confirmPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取角色名称
const getRoleName = (roleId: number) => {
  const role = roleList.value.find(r => (r as Record<string, unknown>).id === roleId)
  return role ? (role as Record<string, unknown>).roleName as string : '未知角色'
}

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await adminUserApi.list(params)
    userList.value = response.data?.records || []
    pagination.total = response.data?.total || 0
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const getStatistics = async () => {
  try {
    const response = await adminUserApi.statistics()
    statistics.value = response.data || {}
  } catch {
    console.error('获取统计信息失败:')
  }
}

// 获取角色列表
const getRoleList = async () => {
  try {
    const response = await roleApi.list()
    roleList.value = response.data || []
  } catch {
    ElMessage.error('获取角色列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    role: '',
    status: null
  })
  pagination.pageNo = 1
  getUserList()
}

// 添加用户
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    password: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: Record<string, unknown>) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row: Record<string, unknown>) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户 "${row.username}" 吗？删除后无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminUserApi.delete((row as Record<string, unknown>).id as number)
    ElMessage.success('删除成功')
    getUserList()
    getStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}


// 切换状态
const handleToggleStatus = async (row: Record<string, unknown>) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await adminUserApi.updateStatus((row as Record<string, unknown>).id as number, newStatus)
    ElMessage.success('状态更新成功')
    getUserList()
    getStatistics()
  } catch {
    ElMessage.error('状态更新失败')
  }
}

// 角色分配
const handleRoleAssign = (row: Record<string, unknown>) => {
  currentUser.value = row
  selectedRoleIds.value = [...((row as Record<string, unknown>).roleIds as number[] || [])]
  roleDialogVisible.value = true
}

// 重置密码
const handleResetPassword = (row: Record<string, unknown>) => {
  currentUser.value = row
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminUserApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await adminUserApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getUserList()
    getStatistics()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 角色分配提交
const handleRoleSubmit = async () => {
  submitting.value = true
  try {
    await adminUserApi.assignRoles((currentUser.value as Record<string, unknown>).id as number, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    getUserList()
  } catch {
    ElMessage.error('角色分配失败')
  } finally {
    submitting.value = false
  }
}

// 重置密码提交
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value || !currentUser.value) return
  
  try {
    await resetPasswordFormRef.value?.validate()
    submitting.value = true
    
    await adminUserApi.resetPassword((currentUser.value as Record<string, unknown>).id as number, resetPasswordForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
    resetPasswordForm.newPassword = ''
    resetPasswordForm.confirmPassword = ''
  } catch (error) {
    if (error !== false) { // 验证失败时error为false
      ElMessage.error('密码重置失败')
    }
  } finally {
    submitting.value = false
  }
}


// 表格选择变化
const handleSelectionChange = (selection: Array<Record<string, unknown>>) => {
  selectedUsers.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNo = 1
  getUserList()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.pageNo = page
  getUserList()
}

// 对话框关闭
const handleDialogClose = () => {
  // 重置表单验证
}

// 页面加载
onMounted(() => {
  getUserList()
  getStatistics()
  getRoleList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  background: #f0f9ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.action-bar {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-group {
  display: flex;
  gap: 10px;
}

.user-list {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.user-email {
  font-size: 14px;
  color: #666;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.text-gray {
  color: #999;
}

.operation-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
}

.operation-buttons .el-button {
  margin: 0;
  padding: 4px 8px;
  font-size: 12px;
  min-width: auto;
}
</style>