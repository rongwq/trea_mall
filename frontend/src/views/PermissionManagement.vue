<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="openDialog()">新增权限</el-button>
        </div>
      </template>
      <el-table :data="permissions" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="method" label="方法" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button @click="openDialog(scope.row)" size="small">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="权限信息" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="权限名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="方法">
          <el-select v-model="form.method" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
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

const permissions = ref([])
const dialogVisible = ref(false)
const form = ref({})

const loadPermissions = async () => {
  const token = localStorage.getItem('token')
  const response = await axios.get('/api/permissions', {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (response.data.code === 200) {
    permissions.value = response.data.data
  }
}

const openDialog = (row = null) => {
  form.value = row ? { ...row } : {}
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const token = localStorage.getItem('token')
  const headers = { Authorization: `Bearer ${token}` }
  
  if (form.value.id) {
    await axios.put(`/api/permissions/${form.value.id}`, form.value, { headers })
  } else {
    await axios.post('/api/permissions', form.value, { headers })
  }
  
  dialogVisible.value = false
  loadPermissions()
  ElMessage.success('操作成功')
}

const handleDelete = async (id) => {
  const token = localStorage.getItem('token')
  await axios.delete(`/api/permissions/${id}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  loadPermissions()
  ElMessage.success('删除成功')
}

onMounted(() => {
  loadPermissions()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>