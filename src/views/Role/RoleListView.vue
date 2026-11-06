<template>
  <div class="role-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <p class="page-subtitle">管理系统角色和权限分配</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-group">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索角色名称或描述..."
          style="width: 300px"
          clearable
        />
        <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 120px" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="btn-group">
        <el-button type="success" @click="handleAdd">添加角色</el-button>
        <!-- 屏蔽批量编辑按钮 -->
        <!-- <el-button type="warning" @click="handleBatchEdit" :disabled="selectedRoles.length === 0">
          批量编辑
        </el-button> -->
        <!-- 屏蔽批量删除按钮 -->
        <!-- <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRoles.length === 0">
          批量删除
        </el-button> -->
        <!-- 屏蔽导出数据按钮 -->
        <!-- <el-button @click="handleExport">导出数据</el-button> -->
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="role-list">
      <el-table
        :data="roleList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="角色信息" min-width="200">
          <template #default="{ row }">
            <div class="role-info">
              <div class="role-icon">{{ getRoleIcon(row.roleCode) }}</div>
              <div class="role-details">
                <div class="role-name">{{ row.roleName }}</div>
                <div class="role-desc">{{ row.description || '暂无描述' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 屏蔽权限字段 -->
        <!-- <el-table-column label="权限" min-width="200">
          <template #default="{ row }">
            <div class="permissions-list">
              <el-tag
                v-for="permission in getRolePermissions(row.id)"
                :key="permission"
                size="small"
                class="permission-tag"
              >
                {{ permission }}
              </el-tag>
            </div>
          </template>
        </el-table-column> -->
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" @click="handlePermissionAssign(row)">权限</el-button>
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

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加角色'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
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

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="700px"
      @close="handlePermissionDialogClose"
    >
      <el-form label-width="100px">
        <el-form-item label="角色">
          <span>{{ currentRole?.roleName }} ({{ currentRole?.roleCode }})</span>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            check-strictly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 角色详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="角色详情"
      width="600px"
    >
      <div v-if="currentRole" class="role-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="角色名称">{{ currentRole.roleName }}</el-descriptions-item>
          <el-descriptions-item label="角色编码">{{ currentRole.roleCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRole.status === 1 ? 'success' : 'danger'">
              {{ currentRole.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ currentRole.createdTime }}
          </el-descriptions-item>
          <el-descriptions-item label="角色描述" :span="2">
            {{ currentRole.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 屏蔽权限列表 -->
        <!-- <div class="permissions-section">
          <h4>权限列表</h4>
          <div class="permissions-list">
            <el-tag
              v-for="permission in getRolePermissions((currentRole as Record<string, unknown>).id as number)"
              :key="permission"
              size="small"
              class="permission-tag"
            >
              {{ permission }}
            </el-tag>
          </div>
        </div> -->
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/role'
import { menuApi } from '@/api/menu'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const roleList = ref<Array<Record<string, unknown>>>([])
const selectedRoles = ref<Array<Record<string, unknown>>>([])
const menuTree = ref<Array<Record<string, unknown>>>([])
const menuTreeRef = ref()
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref<Record<string, unknown> | null>(null)
const selectedMenuIds = ref([])
const allRolesCache = ref<Array<Record<string, unknown>>>([]) // 缓存所有角色数据

// 搜索表单
const searchForm = reactive({
  keyword: '',
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
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'menuName'
}

// 获取角色图标
const getRoleIcon = (roleCode: string) => {
  const iconMap: { [key: string]: string } = {
    'admin': '👑',
    'organizer': '📋',
    'user-admin': '👥',
    'analyst': '📊',
    'maintainer': '🔧'
  }
  return iconMap[roleCode] || '🔐'
}

// 获取角色权限 - 已屏蔽，不再使用
// const getRolePermissions = (roleId: number) => {
//   // 这里应该从后端获取，暂时返回模拟数据
//   const permissions: { [key: number]: string[] } = {
//     1: ['全部权限'],
//     2: ['活动创建', '活动编辑', '活动发布', '活动取消', '报名审核', '签到管理', '活动总结发布'],
//     3: ['用户管理', '志愿者管理', '数据查看'],
//     4: ['数据统计', '报表查看', '数据导出'],
//     5: [' Logan设置', '日志查看', '备份管理']
//   }
//   return permissions[roleId] || []
// }

// 获取角色列表
const getRoleList = async () => {
  loading.value = true
  try {
    // 如果缓存为空，先获取所有角色数据
    if (allRolesCache.value.length === 0) {
      const response = await roleApi.list()
      allRolesCache.value = response?.data || []
    }
    
    // 使用缓存数据进行前端搜索和过滤
    let filteredRoles = [...allRolesCache.value]
    
    // 前端搜索过滤
    if (searchForm.keyword) {
      filteredRoles = filteredRoles.filter((role: Record<string, unknown>) => 
        (role.roleName as string)?.toLowerCase().includes(searchForm.keyword.toLowerCase()) ||
        (role.description as string)?.toLowerCase().includes(searchForm.keyword.toLowerCase()) ||
        (role.roleCode as string)?.toLowerCase().includes(searchForm.keyword.toLowerCase())
      )
    }
    
    // 状态过滤
    if (searchForm.status !== null && searchForm.status !== undefined) {
      filteredRoles = filteredRoles.filter((role: Record<string, unknown>) => role.status === searchForm.status)
    }
    
    // 前端分页
    const startIndex = (pagination.pageNo - 1) * pagination.pageSize
    const endIndex = startIndex + pagination.pageSize
    const paginatedRoles = filteredRoles.slice(startIndex, endIndex)
    
    roleList.value = paginatedRoles
    pagination.total = filteredRoles.length
  } catch {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新角色缓存
const refreshRoleCache = async () => {
  try {
    const response = await roleApi.list()
    allRolesCache.value = response?.data || []
  } catch {
    ElMessage.error('刷新角色数据失败')
  }
}

// 获取菜单树
const getMenuTree = async () => {
  try {
    const response = await menuApi.list()
    menuTree.value = buildMenuTree(response.data || [])
  } catch {
    ElMessage.error('获取菜单列表失败')
  }
}

// 构建菜单树
const buildMenuTree = (menus: Array<{id: number; parentId: number; [key: string]: unknown}>) => {
  const menuMap = new Map()
  const rootMenus: Array<{id: number; parentId: number; children: unknown[]; [key: string]: unknown}> = []

  // 创建菜单映射
  menus.forEach(menu => {
    menuMap.set(menu.id, { ...menu, children: [] })
  })

  // 构建树形结构
  menus.forEach(menu => {
    const menuItem = menuMap.get(menu.id)
    if (menu.parentId === 0) {
      rootMenus.push(menuItem)
    } else {
      const parent = menuMap.get(menu.parentId)
      if (parent) {
        parent.children.push(menuItem)
      }
    }
  })

  return rootMenus
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: null
  })
  pagination.pageNo = 1
  getRoleList()
}

// 添加角色
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row: Record<string, unknown>) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 查看角色详情
const handleView = (row: Record<string, unknown>) => {
  currentRole.value = row
  detailDialogVisible.value = true
}

// 删除角色
const handleDelete = async (row: Record<string, unknown>) => {
  try {
    await ElMessageBox.confirm(
      `确认删除角色 "${row.roleName}" 吗？删除后无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await roleApi.remove((row as Record<string, unknown>).id as number)
    ElMessage.success('删除成功')
    await refreshRoleCache()
    getRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}


// 权限分配
const handlePermissionAssign = async (row: Record<string, unknown>) => {
  currentRole.value = row
  // 先清空之前的数据，避免显示历史数据
  selectedMenuIds.value = []
  try {
    // 获取角色已有的菜单权限
    const response = await roleApi.getRoleMenus((row as Record<string, unknown>).id as number)
    selectedMenuIds.value = response.data || []
    
    // 打开对话框
    permissionDialogVisible.value = true
    
    // 等待对话框完全渲染后，使用setCheckedKeys方法设置选中状态
    await nextTick()
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(selectedMenuIds.value)
    }
  } catch {
    selectedMenuIds.value = []
    permissionDialogVisible.value = true
    // 即使获取失败也要清空选中状态
    await nextTick()
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys([])
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await roleApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await roleApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await refreshRoleCache()
    getRoleList()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 权限分配提交
const handlePermissionSubmit = async () => {
  submitting.value = true
  try {
    // 使用getCheckedKeys方法获取当前选中的菜单ID
    const checkedKeys = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys() : []
    await roleApi.bindMenus((currentRole.value as Record<string, unknown>).id as number, checkedKeys)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch {
    ElMessage.error('权限分配失败')
  } finally {
    submitting.value = false
  }
}


// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNo = 1
  getRoleList()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.pageNo = page
  getRoleList()
}

// 表格选择变化
const handleSelectionChange = (selection: Array<Record<string, unknown>>) => {
  selectedRoles.value = selection
}

// 对话框关闭
const handleDialogClose = () => {
  // 重置表单验证
}

// 权限分配对话框关闭
const handlePermissionDialogClose = () => {
  // 清空当前角色和选中的菜单ID，确保下次打开时重新获取数据
  currentRole.value = null
  selectedMenuIds.value = []
}

// 页面加载
onMounted(() => {
  getRoleList()
  getMenuTree()
})
</script>

<style scoped>
.role-management {
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

.role-list {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.role-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.role-icon {
  width: 40px;
  height: 40px;
  background: #667eea;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.role-details {
  flex: 1;
}

.role-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.role-desc {
  font-size: 14px;
  color: #666;
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.permission-tag {
  margin: 2px;
}

.role-detail {
  padding: 20px 0;
}

.permissions-section {
  margin-top: 20px;
}

.permissions-section h4 {
  margin-bottom: 10px;
  color: #333;
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

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style>