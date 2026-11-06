<template>
  <div class="volunteer-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">志愿者管理</h1>
      <p class="page-subtitle">管理志愿者用户信息和活动记录</p>
    </div>

    <!-- 统计卡片 - 已屏蔽 -->
    <!-- <div class="stats-cards">
      <div class="stats-card">
        <div class="stats-icon">👥</div>
        <div class="stats-content">
          <div class="stats-value">{{ statistics.totalVolunteers || 0 }}</div>
          <div class="stats-label">总志愿者数</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">✅</div>
        <div class="stats-content">
          <div class="stats-value">{{ statistics.activeVolunteers || 0 }}</div>
          <div class="stats-label">正常状态</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">❌</div>
        <div class="stats-content">
          <div class="stats-value">{{ statistics.inactiveVolunteers || 0 }}</div>
          <div class="stats-label">禁用状态</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">⏰</div>
        <div class="stats-content">
          <div class="stats-value">{{ statistics.totalServiceHours || 0 }}</div>
          <div class="stats-label">总服务时长(小时)</div>
        </div>
      </div>
    </div> -->

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-group">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索志愿者姓名或用户名..."
          style="width: 300px"
          clearable
        />
        <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 120px" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="searchForm.gender" placeholder="全部性别" style="width: 120px" clearable>
          <el-option label="男" :value="1" />
          <el-option label="女" :value="2" />
        </el-select>
        <el-select v-model="searchForm.starLevel" placeholder="全部星级" style="width: 120px" clearable>
          <el-option label="无星" :value="0" />
          <el-option label="一星" :value="1" />
          <el-option label="二星" :value="2" />
          <el-option label="三星" :value="3" />
          <el-option label="四星" :value="4" />
          <el-option label="五星" :value="5" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="btn-group">
        <!-- 已屏蔽的按钮 -->
        <!-- <el-button type="success" @click="handleAdd">添加志愿者</el-button>
        <el-button type="warning" @click="handleBatchAudit">批量审核</el-button>
        <el-button type="danger" @click="handleBatchDisable">批量禁用</el-button>
        <el-button @click="handleExport">导出数据</el-button> -->
      </div>
    </div>

    <!-- 志愿者列表 -->
    <div class="volunteer-list">
      <el-table
        :data="volunteerList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="姓名" width="120" align="center">
          <template #default="{ row }">
            <div class="volunteer-name-cell">
              <div class="volunteer-avatar">{{ row.realName?.charAt(0) || 'U' }}</div>
              <span>{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户名" width="150" align="center">
          <template #default="{ row }">
            <span>{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column label="星级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStarLevelType(row.starLevel)">
              {{ getStarLevelText(row.starLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="服务时长" width="120" align="center">
          <template #default="{ row }">
            <div class="stats-info">
              <div class="stats-value">{{ row.totalServiceHours }}</div>
              <div class="stats-unit">小时</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参与活动" width="120" align="center">
          <template #default="{ row }">
            <div class="stats-info">
              <div class="stats-value">{{ row.activityCount }}</div>
              <div class="stats-unit">次</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="总积分" width="120" align="center">
          <template #default="{ row }">
            <div class="stats-info">
              <div class="stats-value">{{ row.totalPoints }}</div>
              <div class="stats-unit">分</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="info" size="small" @click="handleRating(row)">评级</el-button>
              <el-button 
                :type="row.status === 1 ? 'danger' : 'success'" 
                size="small" 
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 志愿者详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="志愿者详情"
      width="1000px"
      :before-close="handleCloseDetail"
    >
      <div v-if="volunteerDetail" class="volunteer-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>用户名：</label>
              <span>{{ volunteerDetail.username }}</span>
            </div>
            <div class="detail-item">
              <label>真实姓名：</label>
              <span>{{ volunteerDetail.realName }}</span>
            </div>
            <div class="detail-item">
              <label>手机号：</label>
              <span>{{ volunteerDetail.phone }}</span>
            </div>
            <div class="detail-item">
              <label>邮箱：</label>
              <span>{{ volunteerDetail.email }}</span>
            </div>
            <div class="detail-item">
              <label>身份证号：</label>
              <span>{{ volunteerDetail.idCard || '未填写' }}</span>
            </div>
            <div class="detail-item">
              <label>性别：</label>
              <span>{{ volunteerDetail.genderText }}</span>
            </div>
            <div class="detail-item">
              <label>年龄：</label>
              <span>{{ volunteerDetail.age }}</span>
            </div>
            <div class="detail-item">
              <label>地址：</label>
              <span>{{ volunteerDetail.address }}</span>
            </div>
            <div class="detail-item">
              <label>状态：</label>
              <el-tag :type="volunteerDetail.status === 1 ? 'success' : 'danger'">
                {{ volunteerDetail.statusText }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>服务统计</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>星级：</label>
              <el-tag :type="getStarLevelType(volunteerDetail.starLevel)">
                {{ getStarLevelText(volunteerDetail.starLevel) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>总服务时长：</label>
              <span>{{ volunteerDetail.totalServiceHours }} 小时</span>
            </div>
            <div class="detail-item">
              <label>参与活动次数：</label>
              <span>{{ volunteerDetail.activityCount }} 次</span>
            </div>
            <div class="detail-item">
              <label>总积分：</label>
              <span>{{ volunteerDetail.totalPoints }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>个人简介</h3>
          <p class="introduction">{{ volunteerDetail.introduction || '暂无简介' }}</p>
        </div>

        <!-- 用户技能信息 -->
        <div v-if="volunteerDetail.userSkills && volunteerDetail.userSkills.length > 0" class="detail-section">
          <h3>用户技能信息</h3>
          <el-table :data="volunteerDetail.userSkills" size="small" border>
            <el-table-column prop="skillName" label="技能名称" width="120" />
            <el-table-column prop="skillCategoryName" label="技能分类" width="100" />
            <el-table-column prop="skillLevelText" label="技能等级" width="80">
              <template #default="{ row }">
                <el-tag :type="getSkillLevelType(row.skillLevel)" size="small">
                  {{ row.skillLevelText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="verifiedText" label="验证状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.verified === 1 ? 'success' : 'warning'" size="small">
                  {{ row.verifiedText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="certificateName" label="关联证书" />
            <el-table-column prop="description" label="技能描述" />
          </el-table>
        </div>

        <!-- 用户证书信息 -->
        <div v-if="filteredUserCertificates && filteredUserCertificates.length > 0" class="detail-section">
          <h3>用户证书信息</h3>
          <el-table :data="filteredUserCertificates" size="small" border>
            <el-table-column prop="certificateName" label="证书名称" width="150" />
            <el-table-column prop="issuingAuthority" label="颁发机构" width="120" />
            <el-table-column prop="certificateNumber" label="证书编号" width="120" />
            <el-table-column prop="issueDate" label="颁发日期" width="100" />
            <el-table-column prop="expiryDate" label="有效期至" width="100" />
            <el-table-column prop="statusText" label="审核状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getCertificateStatusType(row.status)" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  v-if="row.certificateFile" 
                  type="primary" 
                  size="small" 
                  @click="downloadCertificate(row)"
                >
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 紧急联系人信息 -->
        <div class="detail-section">
          <h3>紧急联系人信息</h3>
          <div v-if="emergencyContactsLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="emergencyContacts.length === 0" class="empty-state">
            <el-empty description="暂无紧急联系人信息" />
          </div>
          <el-table v-else :data="emergencyContacts" size="small" border>
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="phone" label="联系电话" width="140" />
            <el-table-column prop="relation" label="关系" width="100" />
            <el-table-column prop="isPrimaryText" label="主联系人" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isPrimary === 1 ? 'danger' : 'info'" size="small">
                  {{ row.isPrimaryText }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑志愿者对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑志愿者"
      width="600px"
      :before-close="handleCloseEdit"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" placeholder="请选择性别">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="editForm.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="个人简介" prop="introduction">
          <el-input
            v-model="editForm.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseEdit">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit" :loading="editLoading">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 评级对话框 -->
    <el-dialog
      v-model="ratingDialogVisible"
      title="志愿者评级"
      width="500px"
      :before-close="handleCloseRating"
    >
      <div v-if="ratingForm" class="rating-dialog">
        <div class="volunteer-info">
          <div class="volunteer-avatar">{{ ratingForm.realName?.charAt(0) || 'U' }}</div>
          <div class="volunteer-details">
            <h4>{{ ratingForm.realName }}</h4>
            <p>用户名：{{ ratingForm.username }}</p>
            <p>当前星级：{{ getStarLevelText(ratingForm.starLevel) }}</p>
          </div>
        </div>
        
        <el-form
          ref="ratingFormRef"
          :model="ratingForm"
          :rules="ratingRules"
          label-width="100px"
        >
          <el-form-item label="新星级" prop="starLevel">
            <div class="star-rating-container">
              <el-rate
                v-model="ratingForm.starLevel"
                :max="5"
                show-text
                :texts="['一星', '二星', '三星', '四星', '五星']"
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              />
              <div class="current-rating-text">
                当前选择：{{ ratingForm.starLevel === 0 ? '无星' : getStarLevelText(ratingForm.starLevel) }}
              </div>
            </div>
          </el-form-item>
          <el-form-item label="评级说明" prop="ratingRemark">
            <el-input
              v-model="ratingForm.ratingRemark"
              type="textarea"
              :rows="3"
              placeholder="请输入评级说明（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseRating">取消</el-button>
          <el-button type="primary" @click="handleSaveRating" :loading="ratingLoading">保存评级</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { volunteerApi } from '@/api/volunteer'

// 响应式数据
const loading = ref(false)
const volunteerList = ref<any[]>([])
const selectedVolunteers = ref<any[]>([])
const statistics = ref<any>({})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null,
  gender: null,
  starLevel: null
})

// 分页信息
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const volunteerDetail = ref<any>(null)

// 过滤用户证书信息，剔除官方志愿服务平台的证书
const filteredUserCertificates = computed(() => {
  if (!volunteerDetail.value || !volunteerDetail.value.userCertificates) {
    return []
  }
  return volunteerDetail.value.userCertificates.filter((cert: any) => 
    cert.issuingAuthority !== '官方志愿服务平台'
  )
})

// 紧急联系人相关数据
const emergencyContacts = ref<any[]>([])
const emergencyContactsLoading = ref(false)

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref()
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  gender: 0,
  age: null,
  address: '',
  status: 1,
  introduction: ''
})

// 评级对话框
const ratingDialogVisible = ref(false)
const ratingFormRef = ref()
const ratingLoading = ref(false)
const ratingForm = reactive({
  id: null,
  realName: '',
  username: '',
  starLevel: 0,
  ratingRemark: ''
})

// 编辑表单验证规则
const editRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

// 评级表单验证规则
const ratingRules = {
  starLevel: [
    { required: true, message: '请选择星级', trigger: 'change' }
  ]
}

// 获取星级类型
const getStarLevelType = (starLevel: number) => {
  switch (starLevel) {
    case 5: return 'danger'
    case 4: return 'warning'
    case 3: return 'success'
    case 2: return 'primary'
    case 1: return 'info'
    default: return 'info'
  }
}

// 获取星级文本
const getStarLevelText = (starLevel: number) => {
  switch (starLevel) {
    case 5: return '五星'
    case 4: return '四星'
    case 3: return '三星'
    case 2: return '二星'
    case 1: return '一星'
    default: return '无星'
  }
}

// 加载志愿者列表
const loadVolunteerList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize
    }
    const response = await volunteerApi.list(params)
    volunteerList.value = response.data.records
    pagination.total = response.data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载志愿者列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const response = await volunteerApi.statistics()
    statistics.value = response.data
  } catch (error: any) {
    console.error('加载统计信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  loadVolunteerList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  searchForm.gender = null
  searchForm.starLevel = null
  pagination.pageNo = 1
  loadVolunteerList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNo = 1
  loadVolunteerList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.pageNo = page
  loadVolunteerList()
}

// 选择改变
const handleSelectionChange = (selection: any[]) => {
  selectedVolunteers.value = selection
}

// 查看详情
const handleView = async (row: any) => {
  try {
    const response = await volunteerApi.detail(row.id)
    volunteerDetail.value = response.data
    detailDialogVisible.value = true
    
    // 加载紧急联系人信息
    await loadEmergencyContacts(row.id)
  } catch (error: any) {
    ElMessage.error(error.message || '获取志愿者详情失败')
  }
}

// 加载紧急联系人信息
const loadEmergencyContacts = async (userId: number) => {
  try {
    emergencyContactsLoading.value = true
    const response = await volunteerApi.getEmergencyContacts(userId)
    emergencyContacts.value = response.data || []
  } catch (error: any) {
    console.error('获取紧急联系人失败:', error)
    emergencyContacts.value = []
  } finally {
    emergencyContactsLoading.value = false
  }
}

// 编辑志愿者
const handleEdit = async (row: any) => {
  try {
    // 获取志愿者详情数据
    const response = await volunteerApi.detail(row.id)
    const volunteerDetail = response.data
    
    Object.assign(editForm, {
      id: volunteerDetail.id,
      realName: volunteerDetail.realName,
      phone: volunteerDetail.phone,
      email: volunteerDetail.email,
      idCard: volunteerDetail.idCard || '',
      gender: volunteerDetail.gender || 0,
      age: volunteerDetail.age,
      address: volunteerDetail.address,
      status: volunteerDetail.status,
      introduction: volunteerDetail.introduction || ''
    })
    editDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取志愿者详情失败')
  }
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确认${action}志愿者"${row.realName}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await volunteerApi.updateStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadVolunteerList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}失败`)
    }
  }
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate()
  editLoading.value = true
  
  try {
    await volunteerApi.update(editForm)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadVolunteerList()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    editLoading.value = false
  }
}

// 关闭详情对话框
const handleCloseDetail = (done?: () => void) => {
  detailDialogVisible.value = false
  volunteerDetail.value = null
  emergencyContacts.value = []
  emergencyContactsLoading.value = false
  if (done) {
    done()
  }
}

// 获取技能等级类型
const getSkillLevelType = (level: number) => {
  switch (level) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

// 获取证书状态类型
const getCertificateStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 下载证书
const downloadCertificate = (certificate: any) => {
  if (!certificate.certificateFile) {
    ElMessage.warning('证书文件不存在')
    return
  }
  
  // 构建证书文件URL - 修复URL路径问题
  let fileUrl = certificate.certificateFile
  if (!fileUrl.startsWith('http') && !fileUrl.startsWith('/')) {
    fileUrl = `/api/uploads/certificates/${fileUrl}`
  }
  
  try {
    // 创建下载链接
    const link = document.createElement('a')
    link.href = fileUrl
    link.download = certificate.certificateName || 'certificate'
    link.target = '_blank'
    
    // 添加到页面并触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    
    ElMessage.success('开始下载证书文件')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请检查文件是否存在')
  }
}

// 关闭编辑对话框
const handleCloseEdit = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
}

// 评级志愿者
const handleRating = async (row: any) => {
  try {
    // 获取志愿者详情数据
    const response = await volunteerApi.detail(row.id)
    const volunteerDetail = response.data
    
    Object.assign(ratingForm, {
      id: volunteerDetail.id,
      realName: volunteerDetail.realName,
      username: volunteerDetail.username,
      starLevel: volunteerDetail.starLevel || 0,
      ratingRemark: ''
    })
    ratingDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取志愿者详情失败')
  }
}

// 保存评级
const handleSaveRating = async () => {
  if (!ratingFormRef.value) return
  
  await ratingFormRef.value.validate()
  ratingLoading.value = true
  
  try {
    await volunteerApi.updateRating(ratingForm)
    ElMessage.success('评级保存成功')
    ratingDialogVisible.value = false
    loadVolunteerList()
  } catch (error: any) {
    ElMessage.error(error.message || '评级保存失败')
  } finally {
    ratingLoading.value = false
  }
}

// 关闭评级对话框
const handleCloseRating = () => {
  ratingDialogVisible.value = false
  ratingFormRef.value?.resetFields()
}

// 添加志愿者
const handleAdd = () => {
  ElMessage.info('添加志愿者功能将在后续版本中实现')
}

// 批量审核
const handleBatchAudit = () => {
  if (selectedVolunteers.value.length === 0) {
    ElMessage.warning('请先选择要审核的志愿者')
    return
  }
  ElMessage.info('批量审核功能将在后续版本中实现')
}

// 批量禁用
const handleBatchDisable = async () => {
  if (selectedVolunteers.value.length === 0) {
    ElMessage.warning('请先选择要禁用的志愿者')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认禁用选中的 ${selectedVolunteers.value.length} 个志愿者吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const userIds = selectedVolunteers.value.map((item: any) => item.id)
    await volunteerApi.batchOperate({
      operation: 'disable',
      userIds: userIds
    })
    
    ElMessage.success('批量禁用成功')
    loadVolunteerList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量禁用失败')
    }
  }
}

// 导出数据
const handleExport = () => {
  ElMessage.info('导出数据功能将在后续版本中实现')
}

// 组件挂载时加载数据
onMounted(() => {
  loadVolunteerList()
  loadStatistics()
})
</script>

<style scoped>
.volunteer-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stats-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stats-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 10px;
}

.stats-content {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stats-label {
  color: #666;
  font-size: 14px;
}

.action-bar {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-group {
  display: flex;
  gap: 10px;
}

.volunteer-list {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.volunteer-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.volunteer-avatar {
  width: 32px;
  height: 32px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.stats-info {
  text-align: center;
}

.stats-value {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.stats-unit {
  color: #666;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.volunteer-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item label {
  font-weight: bold;
  color: #666;
  margin-right: 10px;
  min-width: 80px;
}

.introduction {
  color: #666;
  line-height: 1.6;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 5px;
}

/* 表格样式优化 */
.detail-section .el-table {
  margin-top: 8px;
}

.detail-section .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.dialog-footer {
  text-align: right;
}

/* 紧急联系人相关样式 */
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
}

.loading-container .el-icon {
  margin-right: 8px;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .btn-group {
    justify-content: center;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

/* 评级对话框样式 */
.rating-dialog {
  padding: 20px 0;
}

.volunteer-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.volunteer-info .volunteer-avatar {
  width: 60px;
  height: 60px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.volunteer-details h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.volunteer-details p {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.rating-dialog .el-rate {
  margin-top: 8px;
}

.rating-dialog .el-form-item__label {
  font-weight: bold;
  color: #333;
}

.star-rating-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.current-rating-text {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}
</style>
