<template>
  <AppLayout />
</template>

<script setup lang="ts">
import AppLayout from '@/components/Layout/AppLayout.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
}

#app {
  height: 100%;
}

/* 全局样式重置 */
a {
  text-decoration: none;
  color: inherit;
}

ul, ol {
  list-style: none;
}

/* Element Plus 样式覆盖 */
.el-button {
  border-radius: 6px;
}

.el-input__wrapper {
  border-radius: 6px;
}

.el-card {
  border-radius: 8px;
}

/* 响应式工具类 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.mb-16 {
  margin-bottom: 16px;
}

.mb-20 {
  margin-bottom: 20px;
}

.mb-24 {
  margin-bottom: 24px;
}

.mt-16 {
  margin-top: 16px;
}

.mt-20 {
  margin-top: 20px;
}

.mt-24 {
  margin-top: 24px;
}
</style>
