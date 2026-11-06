<template>
  <div class="menu-page">
    <div style="margin-bottom:12px;">
      <el-button type="primary" @click="openCreate">新增菜单</el-button>
    </div>
    <el-table :data="list" row-key="id" default-expand-all :tree-props="{children:'children'}" v-loading="loading">
      <el-table-column prop="menuName" label="菜单名称" />
      <el-table-column prop="menuCode" label="菜单编码" />
      <el-table-column prop="menuType" label="类型" width="100" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="component" label="组件" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该菜单吗？" @confirm="onRemove(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级菜单">
          <el-input v-model.number="form.parentId" placeholder="0为根" />
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="菜单编码">
          <el-input v-model="form.menuCode" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model.number="form.menuType" placeholder="选择类型">
            <el-option :value="1" label="目录" />
            <el-option :value="2" label="菜单" />
            <el-option :value="3" label="按钮" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="form.sortOrder" type="number" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { menuApi } from '@/api/menu'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref<any>({ id: 0, parentId: 0, menuName: '', menuCode: '', menuType: 2, path: '', component: '', sortOrder: 0, status: 1 })

const load = async () => {
  loading.value = true
  try {
    const data = await menuApi.list()
    list.value = buildTree(data)
  } finally { loading.value = false }
}

const openCreate = () => { dialogTitle.value = '新增菜单'; form.value = { id: 0, parentId: 0, menuName: '', menuCode: '', menuType: 2, path: '', component: '', sortOrder: 0, status: 1 }; dialogVisible.value = true }
const openEdit = (row: any) => { dialogTitle.value = '编辑菜单'; form.value = { ...row }; dialogVisible.value = true }
const onSave = async () => {
  if (!form.value.id) await menuApi.create(form.value)
  else await menuApi.update(form.value)
  ElMessage.success('保存成功'); dialogVisible.value = false; await load()
}
const onRemove = async (id: number) => { await menuApi.remove(id); ElMessage.success('删除成功'); await load() }

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
.menu-page { padding: 12px; }
</style>


