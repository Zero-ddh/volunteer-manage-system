<template>
  <div class="skill-match">
    <div class="match-header">
      <h3>活动匹配分析</h3>
      <div class="match-score">
        <el-progress 
          :percentage="Math.round(overallMatchScore)" 
          :color="getScoreColor(overallMatchScore)"
          :stroke-width="8"
        />
        <span class="score-text">匹配度：{{ Math.round(overallMatchScore) }}%</span>
      </div>
    </div>

    <!-- 活动匹配分析结果 -->
    <div class="activity-match-results" v-if="matchDetails.length === 0">
      <div class="match-result-item">
        <span class="match-label">技能匹配度：</span>
        <span v-if="skillMatchKeywords.length > 0" class="match-keywords">
          （匹配命中词：{{ skillMatchKeywords.join('、') }}）
        </span>
        <span v-else-if="skillMatchScore === 100" class="match-no-requirements">
          （该活动无技能要求，默认匹配度：100%）
        </span>
        <span class="match-value">{{ Math.round(skillMatchScore) }}%</span>
      </div>
      <div class="match-result-item">
        <span class="match-label">历史活动类型匹配度：</span>
        <span v-if="categoryMatchKeywords.length > 0" class="match-keywords">
          （匹配命中词：{{ categoryMatchKeywords.join('、') }}）
        </span>
        <span v-else-if="categoryMatchScore === 100" class="match-no-history">
          （暂无历史活动记录，默认匹配度：100%）
        </span>
        <span class="match-value">{{ Math.round(categoryMatchScore) }}%</span>
      </div>
    </div>

    <div class="match-details" v-loading="loading">
      <div v-if="matchDetails.length === 0" style="display: none;">
        <!-- 屏蔽空状态显示 -->
      </div>
      
      <div v-else class="skill-requirements">
        <div v-for="requirement in matchDetails" :key="String(requirement.skillName)" class="requirement-item">
          <div class="requirement-header">
            <div class="skill-info">
              <h4>{{ requirement.skillName }}</h4>
              <span class="category">{{ requirement.skillCategoryName }}</span>
            </div>
            <div class="requirement-level">
              <el-tag :type="requirement.isRequired ? 'danger' : 'info'">
                {{ requirement.isRequired ? '必需' : '可选' }}
              </el-tag>
              <span class="level-text">{{ requirement.requiredLevelText }}</span>
            </div>
          </div>
          
          <div class="match-status">
            <div v-if="requirement.isMatched" class="matched">
              <el-icon class="success-icon"><Check /></el-icon>
              <div class="match-info">
                <span class="status-text">已匹配</span>
                <span class="user-level">{{ requirement.skillLevelText }}</span>
                <el-tag :type="getMatchTagType(Number(requirement.matchScore))" size="small">
                  {{ Math.round(Number(requirement.matchScore)) }}%
                </el-tag>
              </div>
            </div>
            
            <div v-else class="not-matched">
              <el-icon class="warning-icon"><Close /></el-icon>
              <div class="match-info">
                <span class="status-text">未匹配</span>
                <span class="missing-text">缺少此技能</span>
              </div>
            </div>
          </div>
          
          <div v-if="requirement.verified !== undefined" class="verification-status">
            <el-tag :type="requirement.verified ? 'success' : 'warning'" size="small">
              {{ requirement.verified ? '已验证' : '未验证' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="match-summary" v-if="recommendations.length > 0">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>建议</span>
          </div>
        </template>
        
        <div class="recommendations">
          <ul>
            <li v-for="recommendation in recommendations" :key="recommendation">
              {{ recommendation }}
            </li>
          </ul>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import { activityApi } from '@/api'

// Props
const props = defineProps<{
  activityId: number
  userId: number
}>()

// 响应式数据
const loading = ref(false)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const matchDetails = ref<any[]>([])
const overallMatchScore = ref(0)

// 新的匹配分数数据
const skillMatchScore = ref(0)
const categoryMatchScore = ref(0)
const totalMatchScore = ref(0)
const skillMatchKeywords = ref<string[]>([])
const categoryMatchKeywords = ref<string[]>([])

// 计算属性
const recommendations = computed(() => {
  const recs: string[] = []
  
  // 根据匹配度给出不同的建议
  if (overallMatchScore.value >= 50) {
    recs.push('您的技能与活动要求高度匹配，建议报名参加！')
  } else {
    recs.push('您的技能与活动要求匹配度不高，建议看看其他活动哦！')
  }
  
  return recs
})

// 方法
const loadMatchDetails = async () => {
  try {
    loading.value = true
    
    // 直接使用推荐算法接口获取完整的匹配数据
    await loadActivityMatchScore()
    
  } catch (error) {
    console.error('加载活动匹配详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载活动匹配分数
const loadActivityMatchScore = async () => {
  try {
    // 调用新的活动匹配详情接口
    const res = await activityApi.getActivityMatchDetails(props.activityId)
    
    // 从返回结果中提取匹配数据
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const activity = (res as any)?.data || res
    
    if (activity) {
      // 使用后端计算的实际分数
      skillMatchScore.value = activity.skillMatchScore || 0
      categoryMatchScore.value = activity.categoryMatchScore || 0
      totalMatchScore.value = activity.matchScore || 0
      overallMatchScore.value = activity.matchScore || 0
      
      // 获取匹配的关键词
      skillMatchKeywords.value = activity.skillMatchKeywords || []
      categoryMatchKeywords.value = activity.categoryMatchKeywords || []
    }
  } catch (error) {
    console.error('加载活动匹配分数失败:', error)
  }
}

// 监听 activityId 变化，重新加载数据
watch(() => props.activityId, (newId) => {
  if (newId) {
    loadMatchDetails()
  }
}, { immediate: false })

const getScoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  if (score >= 40) return '#f56c6c'
  return '#909399'
}

const getMatchTagType = (score: number) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 生命周期
onMounted(() => {
  loadMatchDetails()
})
</script>

<style scoped>
.skill-match {
  padding: 20px;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.match-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.match-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.match-score .el-progress {
  width: 120px;
}

.score-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.match-details {
  margin-bottom: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.skill-requirements {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.requirement-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.3s;
}

.requirement-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.requirement-header {
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

.requirement-level {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-text {
  font-size: 14px;
  color: #606266;
}

.match-status {
  margin-bottom: 8px;
}

.matched, .not-matched {
  display: flex;
  align-items: center;
  gap: 8px;
}

.success-icon {
  color: #67c23a;
  font-size: 16px;
}

.warning-icon {
  color: #f56c6c;
  font-size: 16px;
}

.match-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-text {
  font-size: 14px;
  font-weight: 500;
}

.user-level {
  font-size: 14px;
  color: #606266;
}

.missing-text {
  font-size: 14px;
  color: #f56c6c;
}

.verification-status {
  margin-top: 8px;
}

.match-summary {
  margin-top: 24px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.danger {
  color: #f56c6c;
}

.recommendations h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.recommendations ul {
  margin: 0;
  padding-left: 20px;
}

.recommendations li {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

/* 活动匹配分析结果样式 */
.activity-match-results {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  border: 1px solid #e9ecef;
}

.match-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
}

.match-result-item:last-child {
  border-bottom: none;
}


.match-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.match-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.match-keywords {
  font-size: 13px;
  color: #909399;
  margin-left: 4px;
}

.match-no-history {
  font-size: 13px;
  color: #909399;
  font-style: italic;
  margin-left: 4px;
}

.match-no-requirements {
  font-size: 13px;
  color: #909399;
  font-style: italic;
  margin-left: 4px;
}


@media (max-width: 768px) {
  .match-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .requirement-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .summary-stats {
    grid-template-columns: 1fr;
  }
  
  .activity-match-results {
    padding: 16px;
  }
}
</style>
