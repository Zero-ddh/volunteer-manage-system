<template>
  <div class="volunteer-filter">
    <div class="filter-header">
      <h3>志愿者筛选</h3>
      <div class="filter-actions">
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="primary" @click="applyFilter">应用筛选</el-button>
      </div>
    </div>

    <div class="filter-content">
      <!-- 技能筛选 -->
      <div class="filter-section">
        <h4>技能筛选</h4>
        <div class="filter-row">
          <div class="filter-item">
            <label>技能分类：</label>
            <el-cascader
              v-model="filterForm.skillCategoryId"
              :options="skillCategories"
              :props="cascaderProps"
              placeholder="选择技能分类"
              clearable
              style="width: 200px"
            />
          </div>
          
          <div class="filter-item">
            <label>技能名称：</label>
            <el-input 
              v-model="filterForm.skillName" 
              placeholder="输入技能名称"
              style="width: 200px"
            />
          </div>
          
          <div class="filter-item">
            <label>最低等级：</label>
            <el-select v-model="filterForm.minLevel" placeholder="选择等级" style="width: 120px">
              <el-option label="初级" :value="1" />
              <el-option label="中级" :value="2" />
              <el-option label="高级" :value="3" />
              <el-option label="专家级" :value="4" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 匹配度筛选 -->
      <div class="filter-section">
        <h4>匹配度筛选</h4>
        <div class="filter-row">
          <div class="filter-item">
            <label>最低匹配度：</label>
            <el-slider
              v-model="filterForm.minMatchScore"
              :min="0"
              :max="100"
              :step="10"
              show-stops
              show-input
              style="width: 200px"
            />
          </div>
          
          <div class="filter-item">
            <label>验证状态：</label>
            <el-select v-model="filterForm.verified" placeholder="选择状态" style="width: 120px">
              <el-option label="全部" :value="null" />
              <el-option label="已验证" :value="1" />
              <el-option label="未验证" :value="0" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 其他筛选 -->
      <div class="filter-section">
        <h4>其他筛选</h4>
        <div class="filter-row">
          <div class="filter-item">
            <label>服务时长：</label>
            <el-input-number 
              v-model="filterForm.minServiceHours" 
              :min="0"
              placeholder="最低时长"
              style="width: 120px"
            />
            <span class="unit">小时</span>
          </div>
          
          <div class="filter-item">
            <label>参与活动：</label>
            <el-input-number 
              v-model="filterForm.minActivityCount" 
              :min="0"
              placeholder="最少次数"
              style="width: 120px"
            />
            <span class="unit">次</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选结果 -->
    <div class="filter-results" v-loading="loading">
      <div class="results-header">
        <h4>筛选结果 ({{ filteredVolunteers.length }} 人)</h4>
        <div class="results-actions">
          <el-button @click="exportResults">导出结果</el-button>
          <el-button type="primary" @click="sendNotification">发送通知</el-button>
        </div>
      </div>
      
      <div v-if="filteredVolunteers.length === 0" class="empty-results">
        <el-empty description="暂无符合条件的志愿者" />
      </div>
      
      <div v-else class="volunteer-list">
        <div v-for="volunteer in filteredVolunteers" :key="volunteer.userId" class="volunteer-card">
          <div class="volunteer-header">
            <div class="volunteer-info">
              <img :src="volunteer.avatar || '/default-avatar.png'" class="avatar" />
              <div class="info">
                <h5>{{ volunteer.realName }}</h5>
                <span class="username">@{{ volunteer.username }}</span>
              </div>
            </div>
            <div class="match-score">
              <el-progress 
                :percentage="Math.round(volunteer.matchScore)" 
                :color="getScoreColor(volunteer.matchScore)"
                :stroke-width="6"
              />
              <span class="score-text">{{ Math.round(volunteer.matchScore) }}%</span>
            </div>
          </div>
          
          <div class="volunteer-skills">
            <div class="skill-item">
              <span class="skill-name">{{ volunteer.skillName }}</span>
              <el-tag :type="getLevelTagType(volunteer.skillLevel)" size="small">
                {{ volunteer.skillLevelText }}
              </el-tag>
              <el-tag :type="volunteer.verified ? 'success' : 'warning'" size="small">
                {{ volunteer.verified ? '已验证' : '未验证' }}
              </el-tag>
            </div>
          </div>
          
          <div class="volunteer-stats">
            <div class="stat-item">
              <span class="stat-label">服务时长：</span>
              <span class="stat-value">{{ volunteer.totalServiceHours || 0 }}h</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">参与活动：</span>
              <span class="stat-value">{{ volunteer.activityCount || 0 }}次</span>
            </div>
          </div>
          
          <div class="volunteer-actions">
            <el-button size="small" @click="viewProfile(volunteer.userId)">查看详情</el-button>
            <el-button size="small" type="primary" @click="selectVolunteer(volunteer)">选择</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  apiGetVolunteersBySkill,
  apiGetSkillCategoryTree
} from '@/api/user'

// Props
const props = defineProps<{
  activityId: number
}>()

// Emits
const emit = defineEmits<{
  volunteerSelected: [volunteer: any]
}>()

// 响应式数据
const loading = ref(false)
const skillCategories = ref<any[]>([])
const filteredVolunteers = ref<any[]>([])

// 筛选表单
const filterForm = ref({
  skillCategoryId: null,
  skillName: '',
  minLevel: null,
  minMatchScore: 60,
  verified: null,
  minServiceHours: null,
  minActivityCount: null
})

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  emitPath: false
}

// 方法
const loadSkillCategories = async () => {
  try {
    const res = await apiGetSkillCategoryTree()
    skillCategories.value = res || []
  } catch (error) {
    console.error('加载技能分类失败:', error)
    ElMessage.error('加载技能分类失败')
  }
}

const applyFilter = async () => {
  try {
    loading.value = true
    const res = await apiGetVolunteersBySkill(
      props.activityId, 
      filterForm.value.skillCategoryId, 
      filterForm.value.skillName
    )
    
    // 应用其他筛选条件
    let volunteers = res || []
    
    if (filterForm.value.minLevel) {
      volunteers = volunteers.filter(v => v.skillLevel >= filterForm.value.minLevel)
    }
    
    if (filterForm.value.minMatchScore) {
      volunteers = volunteers.filter(v => v.matchScore >= filterForm.value.minMatchScore)
    }
    
    if (filterForm.value.verified !== null) {
      volunteers = volunteers.filter(v => v.verified === filterForm.value.verified)
    }
    
    if (filterForm.value.minServiceHours) {
      volunteers = volunteers.filter(v => (v.totalServiceHours || 0) >= filterForm.value.minServiceHours)
    }
    
    if (filterForm.value.minActivityCount) {
      volunteers = volunteers.filter(v => (v.activityCount || 0) >= filterForm.value.minActivityCount)
    }
    
    filteredVolunteers.value = volunteers
    ElMessage.success(`找到 ${volunteers.length} 名符合条件的志愿者`)
    
  } catch (error) {
    console.error('筛选志愿者失败:', error)
    ElMessage.error('筛选志愿者失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.value = {
    skillCategoryId: null,
    skillName: '',
    minLevel: null,
    minMatchScore: 60,
    verified: null,
    minServiceHours: null,
    minActivityCount: null
  }
  filteredVolunteers.value = []
}

const exportResults = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中...')
}

const sendNotification = () => {
  // TODO: 实现发送通知功能
  ElMessage.info('发送通知功能开发中...')
}

const viewProfile = (userId: number) => {
  // TODO: 实现查看志愿者详情
  ElMessage.info('查看详情功能开发中...')
}

const selectVolunteer = (volunteer: any) => {
  emit('volunteerSelected', volunteer)
  ElMessage.success(`已选择志愿者：${volunteer.realName}`)
}

const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  if (score >= 40) return '#f56c6c'
  return '#909399'
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
onMounted(() => {
  loadSkillCategories()
})
</script>

<style scoped>
.volunteer-filter {
  padding: 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.filter-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.filter-content {
  margin-bottom: 24px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}

.unit {
  font-size: 14px;
  color: #909399;
}

.filter-results {
  margin-top: 24px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.results-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.results-actions {
  display: flex;
  gap: 12px;
}

.empty-results {
  text-align: center;
  padding: 40px 0;
}

.volunteer-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.volunteer-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.3s;
}

.volunteer-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.volunteer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.volunteer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.info h5 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.username {
  font-size: 12px;
  color: #909399;
}

.match-score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.match-score .el-progress {
  width: 80px;
}

.score-text {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.volunteer-skills {
  margin-bottom: 12px;
}

.skill-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.skill-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.volunteer-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
}

.volunteer-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .volunteer-list {
    grid-template-columns: 1fr;
  }
  
  .volunteer-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .volunteer-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
