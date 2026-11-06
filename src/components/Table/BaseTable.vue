<template>
  <div class="base-table">
    <!-- 表格工具栏 -->
    <div class="table-toolbar" v-if="$slots.toolbar">
      <slot name="toolbar"></slot>
    </div>
    
    <!-- 表格主体 -->
    <el-table
      ref="tableRef"
      :data="data"
      :loading="loading"
      :height="height"
      :max-height="maxHeight"
      :stripe="stripe"
      :border="border"
      :size="size"
      :row-key="rowKey"
      :default-sort="defaultSort"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      v-bind="$attrs"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
      />
      
      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="80"
        align="center"
      />
      
      <!-- 数据列 -->
      <slot></slot>
      
      <!-- 操作列 -->
      <el-table-column
        v-if="$slots.actions"
        label="操作"
        :width="actionWidth"
        align="center"
        fixed="right"
      >
        <template #default="{ row, $index }">
          <slot name="actions" :row="row" :index="$index"></slot>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页器 -->
    <div class="table-pagination" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
  
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TableInstance, TableColumnCtx } from 'element-plus'

interface Props {
  data: any[]
  loading?: boolean
  height?: string | number
  maxHeight?: string | number
  stripe?: boolean
  border?: boolean
  size?: 'large' | 'default' | 'small'
  rowKey?: string | ((row: any) => string)
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  showSelection?: boolean
  showIndex?: boolean
  showPagination?: boolean
  total?: number
  pageSize?: number
  currentPage?: number
  pageSizes?: number[]
  paginationLayout?: string
  actionWidth?: string | number
}

interface Emits {
  (e: 'selection-change', selection: any[]): void
  (e: 'sort-change', sortInfo: any): void
  (e: 'row-click', row: any, column: TableColumnCtx<any>, event: Event): void
  (e: 'size-change', size: number): void
  (e: 'current-change', page: number): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  stripe: true,
  border: true,
  size: 'default',
  showSelection: false,
  showIndex: false,
  showPagination: true,
  total: 0,
  pageSize: 10,
  currentPage: 1,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  actionWidth: 150
})

const emit = defineEmits<Emits>()

const tableRef = ref<TableInstance>()

const currentPage = ref(props.currentPage)
const pageSize = ref(props.pageSize)

const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

const handleSortChange = (sortInfo: any) => {
  emit('sort-change', sortInfo)
}

const handleRowClick = (row: any, column: TableColumnCtx<any>, event: Event) => {
  emit('row-click', row, column, event)
}

const handleSizeChange = (size: number) => {
  emit('size-change', size)
}

const handleCurrentChange = (page: number) => {
  emit('current-change', page)
}

defineExpose({
  tableRef
})
</script>

<style scoped>
.base-table {
  width: 100%;
}

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .table-pagination {
    justify-content: center;
  }
  
  .table-pagination :deep(.el-pagination) {
    justify-content: center;
  }
}
</style>


