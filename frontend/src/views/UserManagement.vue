<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openDialog()">新增用户</el-button>
        </div>
      </template>
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button @click="openDialog(scope.row)" size="small">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="用户信息" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const users = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadUsers = async () => {
  const token = localStorage.getItem('token')
  const response = await axios.get('/api/users', {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (response.data.code === 200) {
    users.value = response.data.data
  }
}

const openDialog = (row = null) => {
  form.value = row ? { ...row } : { enabled: true }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const token = localStorage.getItem('token')
  const headers = { Authorization: `Bearer ${token}` }
  
  if (form.value.id) {
    await axios.put(`/api/users/${form.value.id}`, form.value, { headers })
  } else {
    await axios.post('/api/users', form.value, { headers })
  }
  
  dialogVisible.value = false
  loadUsers()
  ElMessage.success('操作成功')
}

const handleDelete = async (id) => {
  const token = localStorage.getItem('token')
  await axios.delete(`/api/users/${id}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  loadUsers()
  ElMessage.success('删除成功')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>