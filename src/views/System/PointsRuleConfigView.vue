<template>
  <div class="points-rule-config">
    <div class="page-header">
      <h2>积分规则配置</h2>
      <p>配置用户获得积分的规则和分数</p>
    </div>

    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>积分规则列表</span>
          <div class="header-buttons">
            <el-button type="success" @click="generatePointsData" :loading="generating">
              手动生成积分数据
            </el-button>
            <el-button type="primary" @click="refreshRules">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="rulesList" v-loading="loading" stripe>
        <el-table-column prop="ruleName" label="规则名称" width="150" />
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag type="primary">服务时长</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsPerUnit" label="每单位积分" width="120" />
        <el-table-column prop="unitName" label="单位" width="80" />
        <el-table-column prop="maxPointsPerDay" label="每日上限" width="100" />
        <el-table-column prop="description" label="规则描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editRule(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑规则对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑积分规则" width="600px">
      <el-form :model="currentRule" :rules="rules" ref="ruleForm" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="currentRule.ruleName" disabled />
        </el-form-item>
        <el-form-item label="每单位积分" prop="pointsPerUnit">
          <el-input-number
            v-model="currentRule.pointsPerUnit"
            :min="0"
            :max="1000"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="单位名称" prop="unitName">
          <el-input v-model="currentRule.unitName" />
        </el-form-item>
        <el-form-item label="每日上限" prop="maxPointsPerDay">
          <el-input-number
            v-model="currentRule.maxPointsPerDay"
            :min="0"
            :max="10000"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="规则描述" prop="description">
          <el-input
            v-model="currentRule.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api'

const loading = ref(false)
const saving = ref(false)
const generating = ref(false)
const rulesList = ref<any[]>([])
const editDialogVisible = ref(false)
const currentRule = ref<any>({})
const ruleForm = ref()

const rules = {
  pointsPerUnit: [
    { required: true, message: '请输入每单位积分', trigger: 'blur' }
  ],
  unitName: [
    { required: true, message: '请输入单位名称', trigger: 'blur' }
  ]
}

// 获取积分规则列表
const fetchRules = async () => {
  loading.value = true
  try {
    const res = await systemApi.getPointsRules()
    rulesList.value = res?.data || []
  } catch (error) {
    ElMessage.error('获取积分规则失败')
  } finally {
    loading.value = false
  }
}

// 编辑规则
const editRule = (rule: any) => {
  currentRule.value = { ...rule }
  editDialogVisible.value = true
}

// 保存规则
const saveRule = async () => {
  try {
    await ruleForm.value.validate()
    saving.value = true
    
    const res = await systemApi.updatePointsRule(currentRule.value.id, currentRule.value)
    if (res?.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      fetchRules()
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 切换状态
const toggleStatus = async (rule: any) => {
  try {
    const res = await systemApi.togglePointsRuleStatus(rule.id, rule.status)
    if (res?.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error('状态更新失败')
      rule.status = rule.status === 1 ? 0 : 1
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    rule.status = rule.status === 1 ? 0 : 1
  }
}

// 刷新规则
const refreshRules = () => {
  fetchRules()
}

// 手动生成积分数据
const generatePointsData = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将根据当前的服务记录重新计算所有用户的积分数据，是否继续？',
      '确认生成积分数据',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    generating.value = true
    const res = await systemApi.generatePointsData()
    
    if (res?.code === 200 && res?.data?.success) {
      ElMessage.success(res.data.message)
    } else {
      ElMessage.error(res?.data?.message || '生成积分数据失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('生成积分数据失败:', error)
      ElMessage.error('生成积分数据失败')
    }
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  fetchRules()
})
</script>

<style scoped>
.points-rule-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 新增：按钮组样式 */
.header-buttons {
  display: flex;
  gap: 12px;
}
</style>
