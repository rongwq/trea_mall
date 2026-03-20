<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="请输入用户名" 
            clearable 
            @keyup.enter="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="handleResetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <template v-if="row.roles && row.roles.length > 0">
              <el-tag v-for="role in row.roles" :key="role.id" class="role-tag">{{ role.name }}</el-tag>
            </template>
            <span v-else class="empty-text">暂无角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :loading="deleteLoading[row.id]"
            >删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户数据" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="roleDialogVisible" 
      title="分配角色" 
      width="400px"
      :close-on-click-modal="false"
    >
      <el-checkbox-group v-model="selectedRoleIds" class="role-checkbox-group">
        <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">
          {{ role.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="allRoles.length === 0" description="暂无可分配角色" />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoleSubmit" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, getAllRoles, assignUserRoles } from '@/api'

defineOptions({
  name: 'UserManage'
})

const loading = ref(false)
const submitLoading = ref(false)
const assignLoading = ref(false)
const deleteLoading = reactive({})
const tableData = ref([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentUserId = ref(null)
const allRoles = ref([])
const selectedRoleIds = ref([])

const searchForm = reactive({
  username: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const initialForm = {
  username: '',
  password: '',
  email: '',
  phone: '',
  status: 1
}

const form = reactive({ ...initialForm })

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadAllRoles = async () => {
  try {
    const res = await getAllRoles()
    allRoles.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    allRoles.value = []
  }
}

const resetForm = () => {
  Object.assign(form, initialForm)
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  currentUserId.value = row.id
  Object.assign(form, {
    username: row.username,
    email: row.email || '',
    phone: row.phone || '',
    status: row.status
  })
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(currentUserId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    deleteLoading[row.id] = true
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除失败:', error)
    }
  } finally {
    deleteLoading[row.id] = false
  }
}

const handleAssignRole = (row) => {
  currentUserId.value = row.id
  selectedRoleIds.value = row.roles ? row.roles.map(r => r.id) : []
  roleDialogVisible.value = true
}

const handleAssignRoleSubmit = async () => {
  assignLoading.value = true
  try {
    await assignUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('分配角色失败')
    console.error('分配角色失败:', error)
  } finally {
    assignLoading.value = false
  }
}

const handleResetSearch = () => {
  searchForm.username = ''
  pagination.page = 1
  loadData()
}

onMounted(() => {
  loadData()
  loadAllRoles()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.role-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.empty-text {
  color: #909399;
  font-size: 14px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-checkbox-group .el-checkbox {
  margin-right: 0;
}

@media (max-width: 768px) {
  .user-manage {
    padding: 10px;
  }
  
  .search-form {
    display: flex;
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .pagination-wrapper {
    overflow-x: auto;
  }
  
  .el-table {
    font-size: 12px;
  }
}
</style>
