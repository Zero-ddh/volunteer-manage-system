<template>
  <div class="community-member-management">
    <div class="page-header">
      <h2>社群成员管理</h2>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="社群">
          <el-select
            v-model="searchForm.communityId"
            placeholder="请选择社群"
            clearable
            style="width: 200px"
            @change="handleCommunityChange"
          >
            <el-option
              v-for="community in communityList"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 150px"
          >
            <el-option label="普通成员" :value="1" />
            <el-option label="管理员" :value="2" />
            <el-option label="群主" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="已退出" :value="3" />
            <el-option label="全部状态" :value="null" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入用户名或手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成员列表 -->
    <div class="table-section">
      <el-table :data="memberList" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号码" width="130" />
        <el-table-column prop="communityName" label="所属社群" width="300" />
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="160" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewProfile(row)">查看资料</el-button>
            <el-dropdown @command="(command: string) => handleRoleChange(row, command)">
              <el-button size="small">
                角色管理<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    command="1" 
                    :disabled="row.role === 1"
                  >
                    设为普通成员
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="2" 
                    :disabled="row.role === 2"
                  >
                    设为管理员
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="3" 
                    :disabled="row.role === 3"
                  >
                    设为群主
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleRemove(row)"
              :disabled="row.role === 3"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 成员资料对话框 -->
    <el-dialog v-model="profileDialogVisible" title="成员资料" width="600px">
      <div v-if="currentMember" class="member-profile">
        <div class="profile-header">
          <div class="avatar">
            <el-avatar :size="80" :src="currentMember.avatar">
              {{ currentMember.username?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="profile-info">
            <h3>{{ currentMember.username }}</h3>
            <p>{{ currentMember.phone }}</p>
            <div class="profile-tags">
              <el-tag :type="getRoleTagType(currentMember.role)">
                {{ getRoleText(currentMember.role) }}
              </el-tag>
              <el-tag :type="getStatusTagType(currentMember.status)">
                {{ getStatusText(currentMember.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="profile-details">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="所属社群">{{ currentMember.communityName }}</el-descriptions-item>
            <el-descriptions-item label="加入时间">{{ currentMember.joinTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ArrowDown } from '@element-plus/icons-vue'

// 类型定义
interface Community {
  id: number
  name: string
}

interface CommunityMember {
  id: number
  communityId: number
  userId: number
  username: string
  phone: string
  communityName: string
  role: number
  status: number
  joinTime: string
  lastActiveTime?: string
  avatar?: string
  points?: number
}

const loading = ref(false)
const profileDialogVisible = ref(false)
const memberList = ref<CommunityMember[]>([])
const communityList = ref<Community[]>([])
const currentMember = ref<CommunityMember | null>(null)

const searchForm = ref({
  communityId: null,
  role: null,
  status: null,
  keyword: ''
})

const pagination = ref({
  pageNo: 1,
  pageSize: 20,
  total: 0
})

// 获取角色标签类型
const getRoleTagType = (role: number) => {
  const typeMap: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[role] || 'info'
}

// 获取角色文本
const getRoleText = (role: number) => {
  const textMap: Record<number, string> = {
    1: '普通成员',
    2: '管理员',
    3: '群主'
  }
  return textMap[role] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    1: '正常',
    2: '禁言',
    3: '已退出'
  }
  return textMap[status] || '未知'
}

// 加载社群列表
const loadCommunities = async () => {
  try {
    const response = await fetch('/api/admin/community/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ pageNo: 1, pageSize: 1000 })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      communityList.value = data.data.records || []
    }
  } catch (error) {
    console.error('加载社群列表失败:', error)
  }
}

// 加载成员数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
            const response = await fetch('/api/admin/community-members', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify(params)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      memberList.value = data.data.records || []
      pagination.value.total = data.data.total || 0
    } else {
      ElMessage.error(data.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 社群改变
const handleCommunityChange = () => {
  pagination.value.pageNo = 1
  loadData()
}

// 查看成员资料
const handleViewProfile = (row: CommunityMember) => {
  currentMember.value = row
  profileDialogVisible.value = true
}

// 角色变更
const handleRoleChange = async (row: CommunityMember, newRole: string) => {
  const roleNum = parseInt(newRole)
  const action = getRoleText(roleNum)
  
  try {
    await ElMessageBox.confirm(`确定要将成员"${row.username}"设为${action}吗？`, '确认操作', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community-members/${row.id}/role`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      },
      body: JSON.stringify({ role: roleNum })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(`设为${action}成功`)
      loadData()
    } else {
      ElMessage.error(data.message || `设为${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`设为${action}失败`)
    }
  }
}

// 移除成员
const handleRemove = async (row: CommunityMember) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员"${row.username}"吗？移除后该成员将退出社群！`, '确认移除', {
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/community-members/${row.id}/remove`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('移除成功')
      loadData()
    } else {
      ElMessage.error(data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.pageNo = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = { communityId: null, role: null, status: null, keyword: '' }
  pagination.value.pageNo = 1
  loadData()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.pageNo = 1
  loadData()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.pageNo = page
  loadData()
}

onMounted(() => {
  loadCommunities()
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.member-profile {
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.profile-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.profile-info p {
  margin: 0 0 15px 0;
  color: #909399;
}

.profile-tags {
  display: flex;
  gap: 10px;
}

.profile-details {
  margin-top: 20px;
}
</style>


