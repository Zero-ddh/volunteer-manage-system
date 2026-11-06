<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    :label-width="labelWidth"
    :label-position="labelPosition"
    v-bind="$attrs"
  >
    <slot :form-data="formData" :form-ref="formRef"></slot>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  modelValue?: any
  rules?: FormRules
  labelWidth?: string
  labelPosition?: 'left' | 'right' | 'top'
}

interface Emits {
  (e: 'update:modelValue', value: any): void
  (e: 'submit', value: any): void
}

const props = withDefaults(defineProps<Props>(), {
  labelWidth: '100px',
  labelPosition: 'right'
})

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const formData = reactive(props.modelValue || {})

// 监听外部数据变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    Object.assign(formData, newValue)
  }
}, { deep: true })

// 监听内部数据变化
watch(formData, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

// 验证表单
const validate = async () => {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

// 重置表单
const resetFields = () => {
  formRef.value?.resetFields()
}

// 清除验证
const clearValidate = (props?: string | string[]) => {
  formRef.value?.clearValidate(props)
}

// 提交表单
const submit = async () => {
  const isValid = await validate()
  if (isValid) {
    emit('submit', formData)
  }
}

// 暴露方法
defineExpose({
  validate,
  resetFields,
  clearValidate,
  submit,
  formRef
})
</script>

<style scoped>
.el-form {
  width: 100%;
}
</style>
