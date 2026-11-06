<template>
  <div class="star-volunteers">
    <div class="page-header">
      <h2>星级志愿者</h2>
      <p>表彰优秀志愿者，弘扬志愿服务精神</p>
    </div>

    <!-- 星级志愿者列表 -->
    <div class="star-volunteers-section">
      <div class="section-header">
        <h3>⭐ 星级志愿者</h3>

        <!-- 搜索区域 -->
        <div class="search-area">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入姓名搜索"
            clearable
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select
            v-model="searchForm.starLevel"
            placeholder="选择星级"
            clearable
            style="width: 150px; margin-right: 10px;"
            @change="handleSearch"
          >
            <el-option label="全部星级" value="" />
            <el-option label="五星" :value="5" />
            <el-option label="四星" :value="4" />
            <el-option label="三星" :value="3" />
            <el-option label="二星" :value="2" />
            <el-option label="一星" :value="1" />
          </el-select>

          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>

          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <el-table :data="filteredVolunteers" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号">
          <template #default="{ row }">
            <span class="phone-number">{{ maskPhone(row.phone) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPoints" label="积分">
          <template #default="{ row }">
            <span class="points-value">{{ row.totalPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalServiceHours" label="服务时长">
          <template #default="{ row }">
            <span class="service-hours">{{ row.totalServiceHours }}h</span>
          </template>
        </el-table-column>
        <el-table-column prop="activityCount" label="活动次数">
          <template #default="{ row }">
            <span class="activity-count">{{ row.activityCount }}次</span>
          </template>
        </el-table-column>
        <el-table-column label="星级">
          <template #default="{ row }">
            <div class="star-level">
              <div class="stars">
                <span v-for="i in row.stars" :key="i" class="star">★</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { starVolunteersApi } from '@/api/starVolunteers'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

// 类型定义
interface StarVolunteer {
  id: number
  realName: string
  phone: string
  level: string
  totalServiceHours: number
  totalPoints: number
  activityCount: number
  stars: number
  starText: string
}

// 搜索表单
interface SearchForm {
  name: string
  starLevel: number | string
}

// 响应式数据
const loading = ref(false)
const starVolunteers = ref<StarVolunteer[]>([])
const searchForm = ref<SearchForm>({
  name: '',
  starLevel: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

// 手机号脱敏处理
function maskPhone(phone: string): string {
  if (!phone) return ''
  if (phone.length !== 11) return phone

  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 搜索处理
function handleSearch() {
  currentPage.value = 1
  updateFilteredData()
}

// 重置搜索
function resetSearch() {
  searchForm.value = {
    name: '',
    starLevel: ''
  }
  currentPage.value = 1
  updateFilteredData()
}

// 分页大小改变
function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  updateFilteredData()
}

// 当前页改变
function handleCurrentChange(val: number) {
  currentPage.value = val
  updateFilteredData()
}

// 更新过滤后的数据
function updateFilteredData() {
  fetchStarVolunteers()
}

// 直接使用后端返回的数据
const filteredVolunteers = computed(() => {
  return starVolunteers.value
})

// 获取星级志愿者列表
async function fetchStarVolunteers() {
  loading.value = true
  try {
    const params = {
      name: searchForm.value.name || undefined,
      starLevel: searchForm.value.starLevel || undefined,
      page: currentPage.value,
      size: pageSize.value
    }

    const res = await starVolunteersApi.getStarVolunteers(params) as any
    if (res && res.list && Array.isArray(res.list)) {
      // 使用后端返回的数据
      starVolunteers.value = res.list
      totalCount.value = res.total || 0

      console.log('获取到星级志愿者数据:', res.list.length, '条，总数:', res.total)
    } else {
      console.log('未获取到星级志愿者数据')
      starVolunteers.value = []
      totalCount.value = 0
    }
  } catch (error) {
    console.error('获取星级志愿者列表失败:', error)
    ElMessage.error('获取星级志愿者列表失败')
    starVolunteers.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchStarVolunteers()
})
</script>

<style scoped>
.star-volunteers {
  padding: 20px;
  width: 100%;
  margin: 0 auto;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #2c5aa0 0%, #4a7bc8 100%);
  border-radius: 12px;
  color: white;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: bold;
}

.page-header p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.star-volunteers-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.search-area {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.points-value {
  font-weight: bold;
  color: #e74c3c;
}

.service-hours {
  font-weight: bold;
  color: #2c5aa0;
}

.activity-count {
  color: #666;
  font-size: 14px;
}

.phone-number {
  color: #666;
  font-size: 14px;
}

.star-level {
  display: flex;
  justify-content: center;
  align-items: center;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  color: #ffd700;
  font-size: 16px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 表格样式优化 */
:deep(.el-table) {
  width: 100% !important;
}

:deep(.el-table__header-wrapper) {
  width: 100% !important;
}

:deep(.el-table__body-wrapper) {
  width: 100% !important;
}

:deep(.el-table th) {
  text-align: center;
  font-weight: bold;
}

:deep(.el-table td) {
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .star-volunteers {
    padding: 10px;
  }

  .page-header {
    padding: 20px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .star-volunteers-section {
    padding: 20px;
  }
}
</style>
