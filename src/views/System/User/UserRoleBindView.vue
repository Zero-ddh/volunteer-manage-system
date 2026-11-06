<template>
  <div class="user-role-bind">
    <el-form inline>
      <el-form-item label="用户ID">
        <el-input v-model.number="userId" placeholder="输入用户ID" style="width:200px;" />
      </el-form-item>
      <el-button type="primary" @click="load">加载角色</el-button>
    </el-form>

    <div v-if="userId">
      <el-checkbox-group v-model="checked" style="margin-top:12px;">
        <el-checkbox v-for="r in roles" :key="r.id" :label="r.id">{{ r.roleName }} ({{ r.roleCode }})</el-checkbox>
      </el-checkbox-group>
      <div style="margin-top:12px;">
        <el-button type="primary" @click="onSave">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { roleApi } from '@/api/role'
import { adminUserApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const userId = ref<number | null>(null)
const roles = ref<any[]>([])
const checked = ref<number[]>([])

const load = async () => {
  roles.value = await roleApi.list()
  if (userId.value) {
    checked.value = await adminUserApi.getUserRoles(userId.value)
  }
}

const onSave = async () => {
  if (!userId.value) return
  await adminUserApi.setUserRoles(userId.value, checked.value)
  ElMessage.success('保存成功')
}
</script>

<style scoped>
.user-role-bind { padding: 12px; }
</style>


