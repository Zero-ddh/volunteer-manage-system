<template>
  <div class="skill-management-page">
    <div class="page-header">
      <h2>技能管理</h2>
      <p>管理您的技能信息，提升志愿者匹配度</p>
    </div>
    
    <!-- 使用现有的技能管理组件 -->
    <SkillManagement :userId="currentUserId" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import SkillManagement from '@/components/SkillManagement.vue'

const authStore = useAuthStore()

// 确保用户信息已加载
onMounted(async () => {
  if (!authStore.userInfo) {
    await authStore.getUserInfo()
  }
})

const currentUserId = computed(() => authStore.userInfo?.id || 0)
</script>

<style scoped>
.skill-management-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>
