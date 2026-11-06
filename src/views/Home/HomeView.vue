<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎使用志愿者活动管理后台</h1>
      <p class="welcome-subtitle">志愿者活动服务平台管理系统</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 系统首页组件
</script>

<style scoped>
.home-container {
  padding: 40px;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.welcome-section {
  text-align: center;
  background: #fff;
  padding: 60px 40px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  width: 100%;
}

.welcome-title {
  font-size: 48px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 20px;
  line-height: 1.2;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 20px;
  color: #7f8c8d;
  margin: 0;
  font-weight: 300;
}

@media (max-width: 768px) {
  .home-container {
    padding: 20px;
  }
  
  .welcome-section {
    padding: 40px 20px;
  }
  
  .welcome-title {
    font-size: 36px;
  }
  
  .welcome-subtitle {
    font-size: 18px;
  }
}
</style>
