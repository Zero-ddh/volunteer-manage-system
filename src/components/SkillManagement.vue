<template>
  <div class="skill-management">
    <div class="skill-header">
      <h3>技能管理</h3>
      <el-button type="primary" size="small" @click="openAddSkillDialog">
        <el-icon><Plus /></el-icon>
        新增技能
      </el-button>
    </div>

    <!-- 技能列表 -->
    <div class="skill-list" v-loading="loading">
      <div v-if="skills.length === 0" class="empty-state">
        <el-empty description="暂无技能信息" />
      </div>
      <div v-else class="skill-cards">
        <div v-for="skill in skills" :key="skill.id" class="skill-card">
          <div class="skill-card-header">
            <div class="skill-info">
              <h4 class="skill-name">{{ skill.skillName }}</h4>
              <span class="skill-category">{{ skill.skillCategoryName }}</span>
            </div>
            <div class="skill-actions">
              <el-button size="small" @click="editSkill(skill)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteSkill(skill.id)">删除</el-button>
            </div>
          </div>
          
          <div class="skill-details">
            <div class="skill-level">
              <span class="label">技能等级：</span>
              <el-tag :type="getLevelTagType(skill.skillLevel)">
                {{ getLevelText(skill.skillLevel) }}
              </el-tag>
            </div>
            
            <div class="skill-status">
              <span class="label">验证状态：</span>
              <el-tag :type="skill.verified ? 'success' : 'warning'">
                {{ skill.verified ? '已验证' : '未验证' }}
              </el-tag>
            </div>
            
            <div v-if="skill.certificateName" class="certificate-info">
              <span class="label">证书：</span>
              <el-tag :type="getCertificateStatusType(skill.certificateStatus)">
                {{ skill.certificateName }}
              </el-tag>
              <span class="certificate-status">{{ getCertificateStatusText(skill.certificateStatus) }}</span>
              <el-button 
                type="primary" 
                size="small" 
                text 
                @click="goToCertificateManagement"
              >
                管理证书
              </el-button>
            </div>
            
            <div v-if="skill.description" class="skill-description">
              <span class="label">描述：</span>
              <span>{{ skill.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑技能对话框 -->
    <el-dialog 
      v-model="skillDialogVisible" 
      :title="isEdit ? '编辑技能' : '新增技能'"
      width="600px"
    >
      <el-form :model="skillForm" :rules="skillRules" ref="skillFormRef" label-width="100px">
        <el-form-item label="技能名称" prop="skillName">
          <el-input v-model="skillForm.skillName" placeholder="请输入技能名称" />
        </el-form-item>
        
        <el-form-item label="技能分类" prop="skillCategoryId">
          <el-cascader
            v-model="skillForm.skillCategoryId"
            :options="skillCategories"
            :props="cascaderProps"
            placeholder="请选择技能分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="技能等级" prop="skillLevel">
          <el-select v-model="skillForm.skillLevel" placeholder="请选择技能等级" style="width: 100%">
            <el-option label="初级" :value="1" />
            <el-option label="中级" :value="2" />
            <el-option label="高级" :value="3" />
            <el-option label="专家级" :value="4" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="技能描述">
          <el-input 
            v-model="skillForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入技能描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="skillDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSkill" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  apiGetSkillCategoryTree, 
  apiGetUserSkills, 
  apiAddUserSkill, 
  apiUpdateUserSkill, 
  apiDeleteUserSkill
} from '@/api/user'
import { useRouter } from 'vue-router'

// Props
const props = defineProps<{
  userId: number
}>()

// Router
const router = useRouter()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const skills = ref<any[]>([])
const skillCategories = ref<any[]>([])
const skillDialogVisible = ref(false)
const isEdit = ref(false)
const skillFormRef = ref()

// 技能表单数据
const skillForm = ref({
  id: 0,
  userId: props.userId,
  skillName: '',
  skillCategoryId: null,
  skillLevel: 1,
  description: ''
})

// 表单验证规则
const skillRules = {
  skillName: [
    { required: true, message: '请输入技能名称', trigger: 'blur' }
  ],
  skillCategoryId: [
    { required: true, message: '请选择技能分类', trigger: 'change' }
  ],
  skillLevel: [
    { required: true, message: '请选择技能等级', trigger: 'change' }
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
const currentUserId = computed(() => props.userId)

// 方法
const loadSkills = async () => {
  try {
    loading.value = true
    const res = await apiGetUserSkills(currentUserId.value)
    skills.value = res || []
  } catch (error) {
    console.error('加载技能列表失败:', error)
    ElMessage.error('加载技能列表失败')
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

const openAddSkillDialog = () => {
  isEdit.value = false
  skillForm.value = {
    id: 0,
    userId: currentUserId.value,
    skillName: '',
    skillCategoryId: null,
    skillLevel: 1,
    description: ''
  }
  skillDialogVisible.value = true
}

const editSkill = (skill: any) => {
  isEdit.value = true
  skillForm.value = { ...skill }
  skillDialogVisible.value = true
}

const saveSkill = async () => {
  try {
    await skillFormRef.value.validate()
    saving.value = true
    
    const skillData = { ...skillForm.value }
    
    if (isEdit.value) {
      await apiUpdateUserSkill(skillData.id, skillData)
      ElMessage.success('技能更新成功')
    } else {
      await apiAddUserSkill(skillData)
      ElMessage.success('技能添加成功')
    }
    
    skillDialogVisible.value = false
    await loadSkills()
  } catch (error) {
    console.error('保存技能失败:', error)
    ElMessage.error('保存技能失败')
  } finally {
    saving.value = false
  }
}

const deleteSkill = async (skillId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个技能吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await apiDeleteUserSkill(skillId)
    ElMessage.success('技能删除成功')
    await loadSkills()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除技能失败:', error)
      ElMessage.error('删除技能失败')
    }
  }
}

const goToCertificateManagement = () => {
  router.push('/user/certificates')
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

const getCertificateStatusText = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: '待审核',
    2: '已通过',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const getCertificateStatusType = (status: number) => {
  const typeMap: { [key: number]: string } = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || ''
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadSkills(),
    loadSkillCategories()
  ])
})
</script>

<style scoped>
.skill-management {
  padding: 20px;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.skill-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.skill-list {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.skill-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.skill-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.3s;
}

.skill-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.skill-card-header {
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

.skill-category {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.skill-actions {
  display: flex;
  gap: 8px;
}

.skill-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skill-details > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}

.skill-description {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.skill-description .label {
  min-width: 60px;
}

.certificate-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.certificate-info .label {
  min-width: 60px;
}

.certificate-status {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .skill-cards {
    grid-template-columns: 1fr;
  }
  
  .skill-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .skill-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
