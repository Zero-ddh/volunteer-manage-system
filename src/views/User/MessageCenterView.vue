<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { messageApi, type Message } from '@/api/message'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const messages = ref<Message[]>([])
const total = ref(0)
const unreadCount = ref(0)
const messageDialogVisible = ref(false)
const currentMessage = ref<Message | null>(null)

// 分页参数
const pageNo = ref(1)
const pageSize = ref(10)

// 筛选参数
const filterType = ref<number | undefined>()
const filterRead = ref<number | undefined>()

// 消息类型映射
const messageTypeMap = {
  1: { text: '招募审核结果', type: 'primary' },
  2: { text: '活动通知', type: 'success' },
  3: { text: '系统通知', type: 'info' }
}

// 获取消息列表
async function fetchMessages() {
  loading.value = true
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...(filterType.value && { messageType: filterType.value }),
      ...(filterRead.value !== undefined && { isRead: filterRead.value })
    }
    
    const res = await messageApi.getList(params) as any
    if (res && res.records) {
      messages.value = res.records || []
      total.value = res.total || 0
    } else {
      messages.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
    messages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取未读消息数量
async function fetchUnreadCount() {
  try {
    const res = await messageApi.getUnreadCount() as any
    if (res !== undefined) {
      unreadCount.value = res
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// 标记消息为已读
async function markAsRead(messageId: number) {
  try {
    await messageApi.markAsRead(messageId)
    ElMessage.success('标记已读成功')
    fetchMessages()
    fetchUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 全部标记为已读
async function markAllAsRead() {
  try {
    await messageApi.markAllAsRead(filterType.value)
    ElMessage.success('全部标记为已读成功')
    fetchMessages()
    fetchUnreadCount()
  } catch (error) {
    console.error('全部标记为已读失败:', error)
    ElMessage.error('全部标记为已读失败')
  }
}

// 重置筛选
function resetFilter() {
  filterType.value = undefined
  filterRead.value = undefined
  pageNo.value = 1
  fetchMessages()
}

// 获取消息类型标签
function getMessageTypeTag(type: number) {
  return messageTypeMap[type as keyof typeof messageTypeMap]?.type || 'info'
}

// 获取消息类型文本
function getMessageTypeText(type: number) {
  return messageTypeMap[type as keyof typeof messageTypeMap]?.text || '未知'
}

// 显示消息详情弹窗
function showMessageDetail(message: Message) {
  currentMessage.value = message
  messageDialogVisible.value = true
  
  // 如果消息未读，自动标记为已读
  if (!message.isRead) {
    markAsRead(message.id)
  }
}

// 关闭消息详情弹窗
function closeMessageDialog() {
  messageDialogVisible.value = false
  currentMessage.value = null
}

// 分页处理
function handlePageChange(page: number) {
  pageNo.value = page
  fetchMessages()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  pageNo.value = 1
  fetchMessages()
}

onMounted(() => {
  fetchMessages()
  fetchUnreadCount()
})
</script>

<template>
  <div class="message-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">消息中心</h1>
      <div class="header-actions">
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          全部标记为已读
        </el-button>
      </div>
    </div>

    <!-- 消息统计 -->
    <div class="message-stats">
      <el-card>
        <div class="stats-content">
          <div class="stat-item">
            <div class="stat-number">{{ unreadCount }}</div>
            <div class="stat-label">未读消息</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ total }}</div>
            <div class="stat-label">总消息数</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 消息列表 -->
    <el-card>
      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-select v-model="filterType" clearable placeholder="消息类型" @change="fetchMessages" style="width: 150px; margin-right: 12px;">
          <el-option :value="1" label="招募审核结果" />
          <el-option :value="2" label="活动通知" />
          <el-option :value="3" label="系统通知" />
        </el-select>
        <el-select v-model="filterRead" clearable placeholder="阅读状态" @change="fetchMessages" style="width: 120px; margin-right: 12px;">
          <el-option :value="0" label="未读" />
          <el-option :value="1" label="已读" />
        </el-select>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <!-- 消息表格 -->
      <el-table :data="messages" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="message-title" :class="{ unread: !row.isRead }">
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300">
          <template #default="{ row }">
            <div class="message-content" @click="showMessageDetail(row)">
              {{ row.content }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="messageType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getMessageTypeTag(row.messageType)">
              {{ getMessageTypeText(row.messageType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'warning'">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdTime).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="!row.isRead" 
              type="primary" 
              size="small" 
              @click="markAsRead(row.id)"
            >
              标记已读
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </el-card>

    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="消息详情"
      width="600px"
      :before-close="closeMessageDialog"
    >
      <div v-if="currentMessage" class="message-detail">
        <div class="detail-item">
          <label>消息标题：</label>
          <span class="detail-value">{{ currentMessage.title }}</span>
        </div>
        
        <div class="detail-item">
          <label>消息类型：</label>
          <el-tag :type="getMessageTypeTag(currentMessage.messageType)">
            {{ getMessageTypeText(currentMessage.messageType) }}
          </el-tag>
        </div>
        
        <div class="detail-item">
          <label>阅读状态：</label>
          <el-tag :type="currentMessage.isRead ? 'success' : 'warning'">
            {{ currentMessage.isRead ? '已读' : '未读' }}
          </el-tag>
        </div>
        
        <div class="detail-item">
          <label>创建时间：</label>
          <span class="detail-value">{{ new Date(currentMessage.createdTime).toLocaleString('zh-CN') }}</span>
        </div>
        
        <div v-if="currentMessage.readTime" class="detail-item">
          <label>阅读时间：</label>
          <span class="detail-value">{{ new Date(currentMessage.readTime).toLocaleString('zh-CN') }}</span>
        </div>
        
        <!-- 屏蔽关联ID和关联类型字段 -->
        <!-- <div v-if="currentMessage.relatedId" class="detail-item">
          <label>关联ID：</label>
          <span class="detail-value">{{ currentMessage.relatedId }}</span>
        </div>
        
        <div v-if="currentMessage.relatedType" class="detail-item">
          <label>关联类型：</label>
          <span class="detail-value">{{ currentMessage.relatedType }}</span>
        </div> -->
        
        <div class="detail-item content-item">
          <label>消息内容：</label>
          <div class="message-content-detail">{{ currentMessage.content }}</div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeMessageDialog">关闭</el-button>
          <el-button 
            v-if="currentMessage && !currentMessage.isRead" 
            type="primary" 
            @click="markAsRead(currentMessage.id)"
          >
            标记已读
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.message-center {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.message-stats {
  margin-bottom: 16px;
}

.stats-content {
  display: flex;
  gap: 32px;
  padding: 16px 0;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 0;
}

.message-title {
  font-weight: 500;
}

.message-title.unread {
  font-weight: 600;
  color: #409eff;
}

.message-content {
  cursor: pointer;
  padding: 8px 0;
  line-height: 1.5;
  color: #606266;
  transition: color 0.2s ease;
}

.message-content:hover {
  color: #409eff;
}

/* 消息详情弹窗样式 */
.message-detail {
  padding: 16px 0;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  line-height: 1.6;
}

.detail-item label {
  font-weight: 600;
  color: #303133;
  min-width: 80px;
  margin-right: 12px;
}

.detail-value {
  color: #606266;
  flex: 1;
}

.content-item {
  flex-direction: column;
  align-items: stretch;
}

.content-item label {
  margin-bottom: 8px;
}

.message-content-detail {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #303133;
  min-height: 100px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table .el-table__row) {
  cursor: pointer;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>
