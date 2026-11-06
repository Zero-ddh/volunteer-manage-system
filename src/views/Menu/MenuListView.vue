<template>
  <div class="menu-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">菜单管理</h1>
      <p class="page-subtitle">
        管理系统菜单结构和权限配置 
        <span style="color: #f56c6c; font-weight: bold;">（注意：菜单的数据默认即可，不要随便编辑，否则系统容易出错！）</span>
      </p>
    </div>

    <!-- 菜单树 -->
    <div class="menu-tree">
      <el-table
        :data="menuList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="菜单名称" min-width="150">
          <template #default="{ row }">
            <span class="menu-name">{{ row.menuName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="菜单路径" min-width="200">
          <template #default="{ row }">
            <span class="menu-path">{{ row.path || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'primary' : 'success'" size="small">
              {{ row.menuType === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="80">
          <template #default="{ row }">
            <span class="sort-order">{{ row.sortOrder || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <!-- 屏蔽添加子菜单按钮 -->
            <!-- <el-button size="small" type="success" @click="handleAddChild(row)">添加子菜单</el-button> -->
            <!-- 屏蔽删除按钮 -->
            <!-- <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button> -->
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '添加菜单'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="treeSelectProps"
            placeholder="请选择父级菜单"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入菜单图标" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入菜单描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { menuApi } from '@/api/menu'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const menuList = ref<Array<Record<string, unknown>>>([])
const selectedMenus = ref<Array<Record<string, unknown>>>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentParent = ref<Record<string, unknown> | null>(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  status: null
})

// 表单数据
const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  path: '',
  icon: '',
  menuType: 1,
  sortOrder: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const rules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ]
}

// 树形选择器配置
const treeSelectProps = {
  children: 'children',
  label: 'menuName',
  value: 'id'
}

// 菜单树选项（用于父级菜单选择）
const menuTreeOptions = computed(() => {
  const buildTreeOptions = (menus: Array<Record<string, unknown>>, parentId = 0): Array<Record<string, unknown>> => {
    return menus
      .filter(menu => (menu as Record<string, unknown>).parentId === parentId)
      .map(menu => ({
        ...menu,
        children: buildTreeOptions(menus, (menu as Record<string, unknown>).id as number)
      }))
  }
  
  return [
    { id: 0, menuName: '根目录', children: buildTreeOptions(menuList.value) }
  ]
})

// 获取菜单列表
const getMenuList = async () => {
  loading.value = true
  try {
    const params = {
      status: searchForm.status
    }
    const response = await menuApi.list(params)
    menuList.value = buildMenuTree(response.data || [])
  } catch {
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 构建菜单树
const buildMenuTree = (menus: Array<Record<string, unknown>>) => {
  const menuMap = new Map()
  const rootMenus: Array<Record<string, unknown>> = []

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


// 编辑菜单
const handleEdit = (row: Record<string, unknown>) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}



// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      await menuApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await menuApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getMenuList()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}


// 表格选择变化
const handleSelectionChange = (selection: Array<Record<string, unknown>>) => {
  selectedMenus.value = selection
}

// 对话框关闭
const handleDialogClose = () => {
  currentParent.value = null
}

// 页面加载
onMounted(() => {
  getMenuList()
})
</script>

<style scoped>
.menu-management {
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

.menu-tree {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.menu-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.menu-path {
  font-size: 13px;
  color: #666;
  font-family: 'Courier New', monospace;
}

.sort-order {
  text-align: center;
  font-size: 12px;
  color: #666;
}
</style>