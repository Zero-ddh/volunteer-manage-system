<template>
  <div class="activity-skill-requirements">
    <div class="requirements-header">
      <h3>技能要求设置</h3>
      <el-button type="primary" size="small" @click="addRequirement">
        <el-icon><Plus /></el-icon>
        添加技能要求
      </el-button>
    </div>

    <div class="requirements-list" v-loading="loading">
      <div v-if="requirements.length === 0" class="empty-state">
        <el-empty description="暂无技能要求">
          <el-button type="primary" @click="addRequirement">添加技能要求</el-button>
        </el-empty>
      </div>
      
      <div v-else class="requirement-cards">
        <div v-for="(requirement, index) in requirements" :key="index" class="requirement-card">
          <div class="card-header">
            <div class="skill-info">
              <h4>{{ requirement.skillName }}</h4>
              <span class="category">{{ requirement.skillCategoryName }}</span>
            </div>
            <div class="card-actions">
              <el-button size="small" @click="editRequirement(index)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRequirement(index)">删除</el-button>
            </div>
          </div>
          
          <div class="requirement-details">
            <div class="detail-item">
              <span class="label">要求等级：</span>
              <el-tag :type="getLevelTagType(requirement.requiredLevel)">
                {{ requirement.requiredLevelText }}
              </el-tag>
            </div>
            
            <div class="detail-item">
              <span class="label">是否必需：</span>
              <el-tag :type="requirement.isRequired ? 'danger' : 'info'">
                {{ requirement.isRequiredText }}
              </el-tag>
            </div>
            
            <div v-if="requirement.description" class="detail-item">
              <span class="label">描述：</span>
              <span>{{ requirement.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑技能要求对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑技能要求' : '添加技能要求'"
      width="600px"
    >
      <el-form :model="requirementForm" :rules="requirementRules" ref="requirementFormRef" label-width="100px">
        <el-form-item label="技能分类" prop="skillCategoryId">
          <el-cascader
            v-model="requirementForm.skillCategoryId"
            :options="skillCategories"
            :props="cascaderProps"
            placeholder="请选择技能分类"
            clearable
            style="width: 100%"
            @change="onCategoryChange"
          />
        </el-form-item>
        
        <el-form-item label="技能名称" prop="skillName">
          <el-input v-model="requirementForm.skillName" placeholder="请输入技能名称" />
        </el-form-item>
        
        <el-form-item label="要求等级" prop="requiredLevel">
          <el-select v-model="requirementForm.requiredLevel" placeholder="请选择要求等级" style="width: 100%">
            <el-option label="初级" :value="1" />
            <el-option label="中级" :value="2" />
            <el-option label="高级" :value="3" />
            <el-option label="专家级" :value="4" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="是否必需" prop="isRequired">
          <el-radio-group v-model="requirementForm.isRequired">
            <el-radio :value="1">必需</el-radio>
            <el-radio :value="0">可选</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="技能描述">
          <el-input 
            v-model="requirementForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入技能要求描述（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRequirement" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 保存按钮 -->
    <div v-if="requirements.length > 0" class="save-section">
      <el-button type="primary" @click="saveAllRequirements" :loading="saving">
        保存技能要求
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  apiGetActivitySkillRequirements, 
  apiSetActivitySkillRequirements,
  apiGetSkillCategoryTree
} from '@/api/user'

// Props
const props = defineProps<{
  activityId: number
}>()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const requirements = ref<any[]>([])
const skillCategories = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingIndex = ref(-1)
const requirementFormRef = ref()

// 技能要求表单数据
const requirementForm = ref({
  skillCategoryId: null,
  skillName: '',
  requiredLevel: 1,
  isRequired: 1,
  description: ''
})

// 表单验证规则
const requirementRules = {
  skillCategoryId: [
    { required: true, message: '请选择技能分类', trigger: 'change' }
  ],
  skillName: [
    { required: true, message: '请输入技能名称', trigger: 'blur' }
  ],
  requiredLevel: [
    { required: true, message: '请选择要求等级', trigger: 'change' }
  ],
  isRequired: [
    { required: true, message: '请选择是否必需', trigger: 'change' }
  ]
}

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  emitPath: false
}

// 计算属性
const currentActivityId = computed(() => props.activityId)

// 方法
const loadRequirements = async () => {
  try {
    loading.value = true
    const res = await apiGetActivitySkillRequirements(currentActivityId.value)
    requirements.value = res || []
  } catch (error) {
    console.error('加载技能要求失败:', error)
    ElMessage.error('加载技能要求失败')
  } finally {
    loading.value = false
  }
}

const loadSkillCategories = async () => {
  try {
    const res = await apiGetSkillCategoryTree()
    skillCategories.value = res || []
  } catch (error) {
    console.error('加载技能分类失败:', error)
    ElMessage.error('加载技能分类失败')
  }
}

const addRequirement = () => {
  isEdit.value = false
  editingIndex.value = -1
  requirementForm.value = {
    skillCategoryId: null,
    skillName: '',
    requiredLevel: 1,
    isRequired: 1,
    description: ''
  }
  dialogVisible.value = true
}

const editRequirement = (index: number) => {
  isEdit.value = true
  editingIndex.value = index
  requirementForm.value = { ...requirements.value[index] }
  dialogVisible.value = true
}

const removeRequirement = (index: number) => {
  requirements.value.splice(index, 1)
}

const saveRequirement = async () => {
  try {
    await requirementFormRef.value.validate()
    
    const requirementData = { ...requirementForm.value }
    
    // 设置技能分类名称
    if (requirementData.skillCategoryId) {
      const category = findCategoryById(skillCategories.value, requirementData.skillCategoryId)
      if (category) {
        requirementData.skillCategoryName = category.categoryName
      }
    }
    
    // 设置等级文本
    requirementData.requiredLevelText = getLevelText(requirementData.requiredLevel)
    
    // 设置是否必需文本
    requirementData.isRequiredText = requirementData.isRequired ? '必需' : '可选'
    
    if (isEdit.value) {
      requirements.value[editingIndex.value] = requirementData
    } else {
      requirements.value.push(requirementData)
    }
    
    dialogVisible.value = false
    ElMessage.success('技能要求保存成功')
    
  } catch (error) {
    console.error('保存技能要求失败:', error)
    ElMessage.error('保存技能要求失败')
  }
}

const saveAllRequirements = async () => {
  try {
    saving.value = true
    await apiSetActivitySkillRequirements(currentActivityId.value, requirements.value)
    ElMessage.success('技能要求保存成功')
  } catch (error) {
    console.error('保存技能要求失败:', error)
    ElMessage.error('保存技能要求失败')
  } finally {
    saving.value = false
  }
}

const onCategoryChange = (value: any) => {
  if (value) {
    const category = findCategoryById(skillCategories.value, value)
    if (category) {
      requirementForm.value.skillName = category.categoryName
    }
  }
}

const findCategoryById = (categories: any[], id: number): any => {
  for (const category of categories) {
    if (category.id === id) {
      return category
    }
    if (category.children) {
      const found = findCategoryById(category.children, id)
      if (found) return found
    }
  }
  return null
}

const getLevelText = (level: number) => {
  const levelMap: { [key: number]: string } = {
    1: '初级',
    2: '中级',
    3: '高级',
    4: '专家级'
  }
  return levelMap[level] || '初级'
}

const getLevelTagType = (level: number) => {
  const typeMap: { [key: number]: string } = {
    1: '',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return typeMap[level] || ''
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadRequirements(),
    loadSkillCategories()
  ])
})
</script>

<style scoped>
.activity-skill-requirements {
  padding: 20px;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.requirements-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.requirements-list {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.requirement-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.requirement-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.3s;
}

.requirement-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.skill-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.category {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.requirement-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}

.save-section {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .requirement-cards {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
