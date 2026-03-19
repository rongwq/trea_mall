<template>
  <div class="dashboard">
    <el-card>
      <h2>欢迎使用 Mall Admin 管理系统</h2>
      <p>当前用户：{{ userStore.userInfo.username }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
}
</style>
