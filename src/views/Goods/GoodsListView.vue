<template>
  <div class="goods-management">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称或描述"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
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

    <!-- 商品列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="goodsList"
        stripe
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商品名称" width="210" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="160" />
        <el-table-column prop="pointsRequired" label="所需积分" width="90" />
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="190">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button size="small" type="info" @click="handleView(row)">查看</el-button>
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
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

    <!-- 商品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入商品名称" 
            autocomplete="off"
            :key="`name-${dialogVisible}`"
          />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
            autocomplete="off"
            :key="`description-${dialogVisible}`"
          />
        </el-form-item>
        <el-form-item label="所需积分" prop="pointsRequired">
          <el-input-number
            v-model="form.pointsRequired"
            :min="1"
            :max="10000"
            placeholder="输入积分"
            :key="`points-${dialogVisible}`"
          />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :max="9999"
            placeholder="输入库存数量"
            :key="`stock-${dialogVisible}`"
          />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select 
            v-model="form.category" 
            placeholder="请选择商品分类"
            :key="`category-${dialogVisible}`"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="images">
          <div class="image-upload-section">
            <el-upload
              v-model:file-list="fileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleRemoveImage"
              :before-upload="beforeUpload"
              :limit="5"
              :on-exceed="handleExceed"
              list-type="picture-card"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
              <div class="upload-text">上传图片</div>
            </el-upload>
            <div class="upload-tip">
              最多支持上传5张图片，支持JPG、PNG格式，单张图片不超过10MB
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情查看对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="700px"
    >
      <div v-if="currentGoods" class="goods-detail">
        <div class="detail-header">
          <h3>{{ currentGoods.name }}</h3>
          <el-tag :type="currentGoods.status === 1 ? 'success' : 'danger'" size="large">
            {{ currentGoods.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </div>
        
        <div class="detail-content">
          <div class="detail-section">
            <h4>基本信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品ID">{{ currentGoods.id }}</el-descriptions-item>
              <el-descriptions-item label="商品名称">{{ currentGoods.name }}</el-descriptions-item>
              <el-descriptions-item label="商品分类">{{ currentGoods.category }}</el-descriptions-item>
              <el-descriptions-item label="所需积分">{{ currentGoods.pointsRequired }} 积分</el-descriptions-item>
              <el-descriptions-item label="库存数量">{{ currentGoods.stock }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(currentGoods.createdTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(currentGoods.updatedTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="detail-section">
            <h4>商品描述</h4>
            <div class="description-content">
              {{ currentGoods.description || '暂无描述' }}
            </div>
          </div>
          
          <div class="detail-section" v-if="currentGoods.images && currentGoods.images.length > 0">
            <h4>商品图片</h4>
            <div class="image-gallery">
              <div 
                v-for="(image, index) in currentGoods.images" 
                :key="`image-${index}-${image}`" 
                class="image-item"
              >
                <div class="image-container">
                  <img 
                    :src="image" 
                    :alt="`商品图片${index + 1}`"
                    class="detail-image"
                    @error="handleImageError"
                  />
                  <div class="image-overlay">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="previewImage(image, index)"
                      class="preview-btn"
                    >
                      预览
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="imagePreviewVisible"
      title="图片预览"
      width="90%"
      center
      :close-on-click-modal="false"
    >
      <div class="image-preview-container">
        <img 
          :src="previewImageUrl" 
          :alt="`商品图片${previewImageIndex + 1}`"
          class="preview-image"
          @error="handlePreviewImageError"
        />
      </div>
      
      <template #footer>
        <div class="preview-footer">
          <el-button @click="prevImage" :disabled="previewImageIndex <= 0">上一张</el-button>
          <span class="image-counter">{{ previewImageIndex + 1 }} / {{ currentGoods?.images?.length || 0 }}</span>
          <el-button @click="nextImage" :disabled="previewImageIndex >= (currentGoods?.images?.length || 1) - 1">下一张</el-button>
        </div>
        <el-button @click="imagePreviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { goodsApi } from '@/api'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const imagePreviewVisible = ref(false)
const isEdit = ref(false)
const goodsList = ref([])
const categories = ref<string[]>([])
const currentGoods = ref<any>(null)
const previewImageUrl = ref('')
const previewImageIndex = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: null
})

// 分页信息
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null as number | null,
  name: '',
  description: '',
  pointsRequired: 1,
  stock: 10,
  category: '',
  images: [] as string[],
  status: 1
})

// 图片上传相关
const fileList = ref([])
const uploadUrl = '/api/v1/upload/image'
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('admin-token')}`
}

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  pointsRequired: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

const formRef = ref()

// 生命周期
onMounted(() => {
  loadGoodsList()
  loadCategories()
})

// 加载商品列表
const loadGoodsList = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const response = await goodsApi.getGoodsList(params)
    goodsList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await goodsApi.getGoodsCategories()
    categories.value = response.data || []
    // 如果没有分类，提供默认分类
    if (categories.value.length === 0) {
      categories.value = ['电子产品', '日用品', '纪念品', '书籍']
    }
    
    // 确保"其他"始终在分类列表中
    if (categories.value.indexOf('其他') === -1) {
      categories.value.push('其他')
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    // 如果加载失败，提供默认分类
    categories.value = ['电子产品', '日用品', '纪念品', '书籍', '其他']
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  loadGoodsList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    status: null
  })
  pagination.pageNo = 1
  loadGoodsList()
}

// 创建商品
const handleCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

// 编辑商品
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogVisible.value = true
  
  // 处理图片数据
  const images = row.images || (row.imageUrl ? [row.imageUrl] : [])
  Object.assign(form, {
    ...row,
    images: images
  })
  
  // 设置文件列表用于显示
  fileList.value = images.map((url: string, index: number) => ({
    uid: index,
    name: `image-${index}`,
    status: 'done',
    url: url
  }))
}

// 查看商品详情
const handleView = async (row: any) => {
  try {
    // 获取商品详细信息
    const response = await goodsApi.getGoodsDetail(row.id)
    currentGoods.value = response.data
    
    // 处理图片数据，确保images字段存在且格式正确
    if (currentGoods.value) {
      if (!currentGoods.value.images || currentGoods.value.images.length === 0) {
        // 如果没有images字段，尝试从imageUrl解析
        if (currentGoods.value.imageUrl) {
          try {
            // 尝试解析JSON格式的图片数据
            const images = JSON.parse(currentGoods.value.imageUrl)
            if (Array.isArray(images)) {
              currentGoods.value.images = images.filter(url => url && typeof url === 'string')
            } else {
              currentGoods.value.images = [currentGoods.value.imageUrl]
            }
          } catch {
            // 如果JSON解析失败，说明是单个图片URL
            currentGoods.value.images = [currentGoods.value.imageUrl]
          }
        } else {
          currentGoods.value.images = []
        }
      } else {
        // 确保images数组中的URL都是有效的字符串
        currentGoods.value.images = currentGoods.value.images.filter((url: string) => url && typeof url === 'string')
      }
    }
    
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 预览图片
const previewImage = (imageUrl: string, index: number) => {
  previewImageUrl.value = imageUrl
  previewImageIndex.value = index
  imagePreviewVisible.value = true
}

// 上一张图片
const prevImage = () => {
  if (previewImageIndex.value > 0 && currentGoods.value?.images) {
    previewImageIndex.value--
    previewImageUrl.value = currentGoods.value.images[previewImageIndex.value]
  }
}

// 下一张图片
const nextImage = () => {
  if (currentGoods.value?.images && previewImageIndex.value < currentGoods.value.images.length - 1) {
    previewImageIndex.value++
    previewImageUrl.value = currentGoods.value.images[previewImageIndex.value]
  }
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-goods.jpg'
}

// 处理预览图片加载错误
const handlePreviewImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-goods.jpg'
  ElMessage.warning('图片加载失败')
}

// 删除商品
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await goodsApi.deleteGoods(row.id)
    ElMessage.success('删除成功')
    loadGoodsList()
  } catch (error: any) {
    console.error('删除商品失败:', error)
    if (error !== 'cancel') {
      // 检查是否是业务错误（存在兑换记录）
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除失败')
      }
    }
  }
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await goodsApi.updateGoodsStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    loadGoodsList()
  } catch (error) {
    console.error('更新商品状态失败:', error)
    ElMessage.error('状态更新失败')
  }
}

// 图片上传处理函数
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    // 确保图片URL是完整的URL
    let imageUrl = response.data.url
    if (imageUrl && !imageUrl.startsWith('http')) {
      // 如果是相对路径，添加基础URL
      imageUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}${imageUrl}`
    }
    form.images.push(imageUrl)
    console.log('图片上传成功，URL:', imageUrl)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败')
}

const handleRemoveImage = (_file: any) => {
  const index = form.images.findIndex((url: string) => url === _file.url)
  if (index > -1) {
    form.images.splice(index, 1)
  }
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 准备提交的数据，排除不需要的字段（时间字段、deleted等）
    const submitData: any = {
      name: form.name,
      description: form.description,
      pointsRequired: form.pointsRequired,
      stock: form.stock,
      category: form.category,
      images: form.images,
      status: form.status
    }
    
    // 如果是编辑，添加id
    if (isEdit.value) {
      submitData.id = form.id
    }
    
    if (isEdit.value) {
      await goodsApi.updateGoods(form.id!, submitData)
      ElMessage.success('更新成功')
    } else {
      await goodsApi.createGoods(submitData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadGoodsList()
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 重置表单
const resetForm = () => {
  // 强制清空表单数据
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    pointsRequired: 1,
    stock: 10,
    category: '',
    images: [],
    status: 1
  })
  
  // 清空文件列表
  fileList.value = []
  
  // 重置表单验证状态
  formRef.value?.resetFields()
  
  // 强制清空所有输入框的值（防止浏览器缓存）
  setTimeout(() => {
    if (formRef.value) {
      const inputs = formRef.value.$el.querySelectorAll('input, textarea')
      inputs.forEach((input: HTMLInputElement | HTMLTextAreaElement) => {
        input.value = ''
        // 触发input事件确保Vue响应式更新
        input.dispatchEvent(new Event('input', { bubbles: true }))
      })
    }
  }, 50)
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNo = 1
  loadGoodsList()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNo = page
  loadGoodsList()
}
</script>

<style scoped>
.goods-management {
  padding: 20px;
}

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

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 图片上传样式 */
.image-upload-section {
  width: 100%;
}

.upload-text {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  margin: 0 8px 8px 0;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
}

.operation-buttons .el-button {
  margin: 0;
  padding: 4px 8px;
  font-size: 12px;
  min-width: auto;
}

/* 商品详情样式 */
.goods-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
    
    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .detail-content {
    .detail-section {
      margin-bottom: 24px;
      
      h4 {
        margin: 0 0 12px 0;
        font-size: 16px;
        font-weight: 600;
        color: #606266;
        border-left: 4px solid #409eff;
        padding-left: 8px;
      }
      
      .description-content {
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;
        color: #606266;
        line-height: 1.6;
        min-height: 60px;
      }
      
      .image-gallery {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 12px;
        
        .image-item {
          border-radius: 6px;
          overflow: hidden;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          transition: transform 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }
          
          .image-container {
            position: relative;
            width: 100%;
            height: 120px;
            
            .detail-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
            }
            
            .image-overlay {
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: rgba(0, 0, 0, 0.5);
              display: flex;
              align-items: center;
              justify-content: center;
              opacity: 0;
              transition: opacity 0.3s ease;
              
              .preview-btn {
                background: rgba(255, 255, 255, 0.9);
                color: #409eff;
                border: none;
                
                &:hover {
                  background: #409eff;
                  color: white;
                }
              }
            }
            
            &:hover .image-overlay {
              opacity: 1;
            }
          }
        }
      }
    }
  }
}

/* 图片预览对话框样式 */
.image-preview-container {
  text-align: center;
  padding: 20px;
  height: 60vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  margin: 10px 0;
  
  .preview-image {
    max-width: calc(100% - 40px);
    max-height: calc(100% - 40px);
    width: auto;
    height: auto;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    display: block;
  }
}

.preview-footer {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  
  .image-counter {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }
}
</style>
