<template>
  <div class="certificate-preview">
    <div class="certificate-header">
      <h4>{{ certificate.certificateName }}</h4>
      <div class="header-right">
        <div class="certificate-actions">
          <el-button size="small" @click="$emit('edit', certificate)">编辑</el-button>
          <el-button size="small" type="danger" @click="$emit('delete', certificate.id)">删除</el-button>
        </div>
        <el-tag :type="getStatusType(certificate.status)">
          {{ getStatusText(certificate.status) }}
        </el-tag>
      </div>
    </div>
    
    <div class="certificate-info">
      <div class="info-row">
        <div class="info-item">
          <span class="label">颁发机构：</span>
          <span>{{ certificate.issuingAuthority || '未知' }}</span>
        </div>
        
        <div class="info-item">
          <span class="label">证书编号：</span>
          <span>{{ certificate.certificateNumber || '未知' }}</span>
        </div>
      </div>
      
      <div class="info-row">
        <div class="info-item">
          <span class="label">颁发日期：</span>
          <span>{{ certificate.issueDate || '未知' }}</span>
        </div>
        
        <div class="info-item">
          <span class="label">有效期至：</span>
          <span>{{ certificate.expiryDate || '长期有效' }}</span>
        </div>
      </div>
    </div>
    
    <div class="certificate-file">
      <div class="file-info">
        <el-icon><Document /></el-icon>
        <span>{{ getFileName(certificate) }}</span>
        <el-tag size="small">{{ certificate.fileType?.toUpperCase() }}</el-tag>
      </div>
      
      <div class="file-actions">
        <el-button size="small" @click="previewFile">预览</el-button>
        <el-button size="small" @click="downloadFile">下载</el-button>
      </div>
    </div>
    
    <div v-if="certificate.auditRemark" class="audit-remark">
      <span class="label">审核备注：</span>
      <p>{{ certificate.auditRemark }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

// Props
const props = defineProps<{
  certificate: any
}>()

// 方法
const getStatusText = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: '待审核',
    2: '已通过',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: { [key: number]: string } = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || ''
}

const getFileName = (certificate: any) => {
  // 优先使用原始文件名
  if (certificate.originalFileName) {
    return certificate.originalFileName
  }
  // 如果没有原始文件名，则从URL中提取
  if (certificate.certificateFile) {
    return certificate.certificateFile.split('/').pop() || '未知文件'
  }
  return '未知文件'
}

const previewFile = () => {
  if (props.certificate.certificateFile) {
    window.open(props.certificate.certificateFile, '_blank')
  }
}

const downloadFile = () => {
  if (props.certificate.certificateFile) {
    const link = document.createElement('a')
    link.href = props.certificate.certificateFile
    // 使用原始文件名作为下载文件名
    link.download = getFileName(props.certificate)
    link.click()
  }
}
</script>

<style scoped>
.certificate-preview {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.certificate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.certificate-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.certificate-actions {
  display: flex;
  gap: 8px;
}

.certificate-info {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  flex: 1;
}

.info-item .label {
  min-width: 80px;
  font-size: 14px;
  color: #606266;
}

.info-item span:last-child {
  font-size: 14px;
  color: #303133;
}

.certificate-file {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-info span {
  font-size: 14px;
  color: #606266;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.audit-remark {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.audit-remark .label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.audit-remark p {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .certificate-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-right {
    width: 100%;
    justify-content: space-between;
  }
  
  .info-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .certificate-file {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
