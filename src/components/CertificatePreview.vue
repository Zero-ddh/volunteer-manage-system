<template>
  <div class="certificate-preview">
    <div class="preview-container">
      <!-- PDF预览 -->
      <div v-if="fileType === 'pdf'" class="pdf-preview">
        <iframe 
          :src="fileUrl" 
          width="100%" 
          height="600px"
          frameborder="0"
        ></iframe>
      </div>
      
      <!-- 图片预览 -->
      <div v-else-if="isImageType" class="image-preview">
        <el-image
          :src="fileUrl"
          :preview-src-list="[fileUrl]"
          fit="contain"
          style="width: 100%; max-height: 600px;"
        />
      </div>
      
      <!-- 不支持的文件类型 -->
      <div v-else class="unsupported-preview">
        <el-icon size="64" color="#909399"><Document /></el-icon>
        <p>不支持预览此文件类型</p>
        <p class="file-name">{{ originalName || '未知文件' }}</p>
      </div>
    </div>
    
    <!-- 文件信息 -->
    <div class="file-info">
      <div class="file-details">
        <el-icon><Document /></el-icon>
        <span class="file-name">{{ originalName || '未知文件' }}</span>
        <el-tag size="small" :type="getFileTypeTag(fileType)">
          {{ fileType?.toUpperCase() }}
        </el-tag>
      </div>
      
      <div class="file-actions">
        <el-button size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
        <el-button size="small" @click="openInNewTab">
          <el-icon><View /></el-icon>
          新窗口打开
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document, Download, View } from '@element-plus/icons-vue'

// Props
const props = defineProps<{
  fileUrl: string
  fileType: string
  originalName?: string
}>()

// 计算属性
const isImageType = computed(() => {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(props.fileType?.toLowerCase())
})

// 方法
const getFileTypeTag = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'pdf': 'danger',
    'jpg': 'success',
    'jpeg': 'success', 
    'png': 'success',
    'gif': 'success',
    'bmp': 'success',
    'webp': 'success'
  }
  return typeMap[type?.toLowerCase()] || 'info'
}

const downloadFile = () => {
  if (props.fileUrl) {
    const link = document.createElement('a')
    link.href = props.fileUrl
    link.download = props.originalName || 'certificate'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

const openInNewTab = () => {
  if (props.fileUrl) {
    window.open(props.fileUrl, '_blank')
  }
}
</script>

<style scoped>
.certificate-preview {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.preview-container {
  background: #f5f7fa;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pdf-preview {
  width: 100%;
  height: 100%;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.unsupported-preview {
  text-align: center;
  color: #909399;
  padding: 40px;
}

.unsupported-preview p {
  margin: 12px 0 0 0;
  font-size: 14px;
}

.file-name {
  font-weight: 500;
  color: #606266;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.file-details {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.file-details span {
  font-size: 14px;
  color: #606266;
}

.file-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .file-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .preview-container {
    min-height: 300px;
  }
}
</style>
