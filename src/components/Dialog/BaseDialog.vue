<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :top="top"
    :modal="modal"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :destroy-on-close="destroyOnClose"
    :center="center"
    :align-center="alignCenter"
    :draggable="draggable"
    v-bind="$attrs"
  >
    <!-- 对话框内容 -->
    <slot :visible="visible"></slot>
    
    <!-- 对话框底部 -->
    <template #footer v-if="$slots.footer || showDefaultFooter">
      <slot name="footer" :visible="visible">
        <div class="dialog-footer" v-if="showDefaultFooter">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleConfirm" :loading="confirmLoading">
            {{ confirmText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  modelValue: boolean
  title?: string
  width?: string | number
  top?: string
  modal?: boolean
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
  showClose?: boolean
  destroyOnClose?: boolean
  center?: boolean
  alignCenter?: boolean
  draggable?: boolean
  showDefaultFooter?: boolean
  confirmText?: string
  confirmLoading?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  width: '50%',
  top: '15vh',
  modal: true,
  closeOnClickModal: true,
  closeOnPressEscape: true,
  showClose: true,
  destroyOnClose: false,
  center: false,
  alignCenter: false,
  draggable: false,
  showDefaultFooter: true,
  confirmText: '确定',
  confirmLoading: false
})

const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)

// 监听外部变化
watch(() => props.modelValue, (newValue) => {
  visible.value = newValue
})

// 监听内部变化
watch(visible, (newValue) => {
  emit('update:modelValue', newValue)
})

// 处理关闭前事件
const handleBeforeClose = (done: () => void) => {
  emit('close')
  done()
}

// 处理确认
const handleConfirm = () => {
  emit('confirm')
}

// 处理取消
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

// 打开对话框
const open = () => {
  visible.value = true
}

// 关闭对话框
const close = () => {
  visible.value = false
}

// 暴露方法
defineExpose({
  open,
  close
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
}
</style>
