<template>
  <div class="insurance-product-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>保险产品管理</h2>
      <p>管理保险产品的增删改查、上架下架等操作</p>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="产品名称">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入产品名称或描述" 
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增产品
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 产品列表 -->
    <el-card class="table-card">
      <el-table :data="productList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <!-- 屏蔽产品描述字段 -->
        <!-- <el-table-column prop="description" label="产品描述" min-width="200" show-overflow-tooltip /> -->
        <el-table-column prop="price" label="价格(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="coverageAmount" label="保障金额(元)" width="140" align="right">
          <template #default="{ row }">
            <span class="coverage-text">¥{{ row.coverageAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="coveragePeriod" label="保障期限(天)" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="operation-buttons">
              <div class="button-row">
                <el-button size="small" @click="handleView(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button size="small" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
              <div class="button-row">
                <el-button 
                  size="small" 
                  :type="row.status === 1 ? 'warning' : 'success'"
                  @click="handleToggleStatus(row)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑产品对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="productFormRef" 
        :model="productForm" 
        :rules="productRules" 
        label-width="120px"
      >
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品描述" prop="description">
          <el-input 
            v-model="productForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入产品描述"
          />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number 
            v-model="productForm.price" 
            :min="0" 
            :precision="2"
            placeholder="请输入价格"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="保障金额(元)" prop="coverageAmount">
          <el-input-number 
            v-model="productForm.coverageAmount" 
            :min="0" 
            :precision="2"
            placeholder="请输入保障金额"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="保障期限(天)" prop="coveragePeriod">
          <el-input-number 
            v-model="productForm.coveragePeriod" 
            :min="1" 
            placeholder="请输入保障期限"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="!isCreate">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看产品详情对话框 -->
    <el-dialog 
      v-model="viewDialogVisible" 
      title="产品详情" 
      width="500px"
    >
      <div v-if="viewProduct" class="product-detail">
        <div class="detail-item">
          <label>产品名称：</label>
          <span>{{ viewProduct.name }}</span>
        </div>
        <div class="detail-item">
          <label>产品描述：</label>
          <div class="description-content">{{ viewProduct.description }}</div>
        </div>
        <div class="detail-item">
          <label>价格：</label>
          <span class="price-text">¥{{ viewProduct.price }}</span>
        </div>
        <div class="detail-item">
          <label>保障金额：</label>
          <span class="coverage-text">¥{{ viewProduct.coverageAmount }}</span>
        </div>
        <div class="detail-item">
          <label>保障期限：</label>
          <span>{{ viewProduct.coveragePeriod }} 天</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="viewProduct.status === 1 ? 'success' : 'danger'">
            {{ viewProduct.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ formatDateTime(viewProduct.createdTime) }}</span>
        </div>
        <div class="detail-item" v-if="viewProduct.updatedTime">
          <label>更新时间：</label>
          <span>{{ formatDateTime(viewProduct.updatedTime) }}</span>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Edit, Switch, Delete, View } from '@element-plus/icons-vue'
import { insuranceApi } from '@/api/index'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isCreate = ref(true)
const productFormRef = ref<FormInstance>()
const viewDialogVisible = ref(false)
const viewProduct = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null as number | null
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 产品列表
const productList = ref([])

// 产品表单
const productForm = reactive({
  id: null as number | null,
  name: '',
  description: '',
  price: 0,
  coverageAmount: 0,
  coveragePeriod: 0,
  status: 1
})

// 表单验证规则
const productRules: FormRules = {
  name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '产品名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入产品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '产品描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  coverageAmount: [
    { required: true, message: '请输入保障金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '保障金额必须大于0', trigger: 'blur' }
  ],
  coveragePeriod: [
    { required: true, message: '请输入保障期限', trigger: 'blur' },
    { type: 'number', min: 1, message: '保障期限必须大于0', trigger: 'blur' }
  ]
}

// 获取产品列表
const getProductList = async () => {
  try {
    loading.value = true
    const params = {
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined
    }
    
    const response = await insuranceApi.getProductList(params)
    if (response.code === 200) {
      productList.value = response.data.records
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取产品列表失败')
    }
  } catch (error) {
    console.error('获取产品列表失败:', error)
    ElMessage.error('获取产品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getProductList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.pageNo = 1
  getProductList()
}

// 新增产品
const handleCreate = () => {
  isCreate.value = true
  dialogTitle.value = '新增保险产品'
  resetProductForm()
  dialogVisible.value = true
}

// 查看产品
const handleView = (row: any) => {
  viewProduct.value = { ...row }
  viewDialogVisible.value = true
}

// 编辑产品
const handleEdit = (row: any) => {
  isCreate.value = false
  dialogTitle.value = '编辑保险产品'
  Object.assign(productForm, row)
  dialogVisible.value = true
}

// 上架/下架产品
const handleToggleStatus = async (row: any) => {
  const action = row.status === 1 ? '下架' : '上架'
  const newStatus = row.status === 1 ? 0 : 1
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}该产品吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await insuranceApi.updateProductStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      getProductList()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}产品失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除产品
const handleDelete = async (row: any) => {
  try {
    // 先检查是否存在保险订单记录
    const hasOrders = await checkProductOrders(row.id)
    if (hasOrders) {
      ElMessage.warning('该产品存在保险订单记录，无法删除！')
      return
    }
    
    await ElMessageBox.confirm(
      '确定要删除该产品吗？删除后不可恢复！',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await insuranceApi.deleteProduct(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getProductList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除产品失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 检查产品是否存在保险订单记录
const checkProductOrders = async (productId: number): Promise<boolean> => {
  try {
    const response = await insuranceApi.getOrderList({ 
      pageNo: 1, 
      pageSize: 1, 
      productId: productId 
    })
    return response.code === 200 && response.data.total > 0
  } catch (error) {
    console.error('检查产品订单失败:', error)
    return false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    submitLoading.value = true
    
    let response
    if (isCreate.value) {
      response = await insuranceApi.createProduct(productForm)
    } else {
      response = await insuranceApi.updateProduct(productForm.id!, productForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(isCreate.value ? '创建成功' : '更新成功')
      dialogVisible.value = false
      getProductList()
    } else {
      ElMessage.error(response.message || (isCreate.value ? '创建失败' : '更新失败'))
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error(isCreate.value ? '创建失败' : '更新失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetProductForm = () => {
  productForm.id = null
  productForm.name = ''
  productForm.description = ''
  productForm.price = 0
  productForm.coverageAmount = 0
  productForm.coveragePeriod = 0
  productForm.status = 1
}

// 关闭对话框
const handleDialogClose = () => {
  productFormRef.value?.resetFields()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.pageNo = 1
  getProductList()
}

const handleCurrentChange = (val: number) => {
  pagination.pageNo = val
  getProductList()
}

// 页面加载时获取数据
onMounted(() => {
  getProductList()
})
</script>

<style scoped>
.insurance-product-list {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.price-text {
  color: #E6A23C;
  font-weight: 600;
}

.coverage-text {
  color: #67C23A;
  font-weight: 600;
}

.dialog-footer {
  text-align: right;
}

.operation-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.button-row {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

.product-detail {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  margin-right: 12px;
}

.detail-item span {
  color: #303133;
  flex: 1;
}

.description-content {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  flex: 1;
}
</style>
