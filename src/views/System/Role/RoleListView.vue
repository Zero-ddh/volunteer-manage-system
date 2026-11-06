<template>
  <div class="role-page">
    <div style="margin-bottom:12px;">
      <el-button type="primary" @click="openCreate">新增角色</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="openBind(row)">分配菜单</el-button>
          <el-popconfirm title="确定删除该角色吗？" @confirm="onRemove(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindVisible" title="分配菜单">
      <el-tree
        :data="menuTree"
        show-checkbox
        node-key="id"
        ref="treeRef"
        :props="{ label: 'menuName', children: 'children' }" />
      <template #footer>
        <el-button @click="bindVisible=false">取消</el-button>
        <el-button type="primary" @click="onBindSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { roleApi } from '@/api/role'
import { menuApi } from '@/api/menu'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref<any>({ id: 0, roleName: '', roleCode: '', status: 1 })

const bindVisible = ref(false)
const currentRoleId = ref<number>(0)
const menuTree = ref<any[]>([])
const treeRef = ref()

const load = async () => {
  loading.value = true
  try {
    list.value = await roleApi.list()
  } finally { loading.value = false }
}

const openCreate = () => { dialogTitle.value = '新增角色'; form.value = { id: 0, roleName: '', roleCode: '', status: 1 }; dialogVisible.value = true }
const openEdit = (row: any) => { dialogTitle.value = '编辑角色'; form.value = { ...row }; dialogVisible.value = true }
const onSave = async () => {
  if (!form.value.id) await roleApi.create(form.value)
  else await roleApi.update(form.value)
  ElMessage.success('保存成功'); dialogVisible.value = false; await load()
}
const onRemove = async (id: number) => { await roleApi.remove(id); ElMessage.success('删除成功'); await load() }

const openBind = async (row: any) => {
  currentRoleId.value = row.id
  const menus = await menuApi.list()
  menuTree.value = buildTree(menus)
  bindVisible.value = true
}
const onBindSave = async () => {
  const checked = treeRef.value!.getCheckedKeys()
  await roleApi.bindMenus(currentRoleId.value, checked)
  ElMessage.success('绑定成功')
  bindVisible.value = false
}

const buildTree = (list: any[]) => {
  const map: any = {}
  list.forEach(i => { i.children = []; map[i.id] = i })
  const roots: any[] = []
  list.forEach(i => {
    if (!i.parentId || i.parentId === 0) roots.push(i)
    else map[i.parentId]?.children.push(i)
  })
  return roots
}

onMounted(load)
</script>

<style scoped>
.role-page { padding: 12px; }
</style>


