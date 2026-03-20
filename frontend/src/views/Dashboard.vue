<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
              <el-icon><UserFilled /></el-icon>
            </div>
          </template>
          <div class="card-body">
            <h2>{{ stats.userCount }}</h2>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>角色总数</span>
              <el-icon><Medal /></el-icon>
            </div>
          </template>
          <div class="card-body">
            <h2>{{ stats.roleCount }}</h2>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>权限总数</span>
              <el-icon><Key /></el-icon>
            </div>
          </template>
          <div class="card-body">
            <h2>{{ stats.permissionCount }}</h2>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card style="margin-top: 20px">
      <template #header>
        <span>系统信息</span>
      </template>
      <div class="system-info">
        <p><strong>系统名称：</strong>RBAC权限管理系统</p>
        <p><strong>技术栈：</strong>Spring Boot 3.3.0 + Vue 3 + Element Plus</p>
        <p><strong>当前用户：</strong>{{ userStore.username }}</p>
        <p><strong>用户角色：</strong>{{ userStore.roles.join(', ') }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { getUsers } from '../api/users'
import { getAllRoles } from '../api/roles'
import { getAllPermissions } from '../api/permissions'

const userStore = useUserStore()
const stats = ref({
  userCount: 0,
  roleCount: 0,
  permissionCount: 0
})

const loadStats = async () => {
  try {
    const [usersRes, rolesRes, permissionsRes] = await Promise.all([
      getUsers({ page: 0, size: 1 }),
      getAllRoles(),
      getAllPermissions()
    ])
    stats.value = {
      userCount: usersRes.data.totalElements,
      roleCount: rolesRes.data.length,
      permissionCount: permissionsRes.data.length
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-body {
  text-align: center;
}

.card-body h2 {
  margin: 0;
  font-size: 36px;
  color: #303133;
}

.system-info p {
  margin: 10px 0;
  color: #606266;
}
</style>
