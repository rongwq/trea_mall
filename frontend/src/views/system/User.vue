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
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe empty-text="暂无用户数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" style="margin-right: 5px">{{ role.name }}</el-tag>
            <span v-if="!row.roles || row.roles.length === 0" style="color: #909399">暂无角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @closed="handleDialogClosed">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" maxlength="50" show-word-limit placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password maxlength="20" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="100" show-word-limit placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" show-word-limit placeholder="请输入手机号" />
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

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px" @closed="handleRoleDialogClosed">
      <el-empty v-if="allRoles.length === 0" description="暂无可分配角色" />
      <el-checkbox-group v-else v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id">{{ role.name }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoleSubmit" :loading="roleSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, getAllRoles, assignUserRoles } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const roleSubmitLoading = ref(false)
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

const getInitialForm = () => ({
  username: '',
  password: '',
  email: '',
  phone: '',
  status: 1
})

const form = reactive(getInitialForm())

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: isEdit.value ? [] : [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}))

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    tableData.value = []
    pagination.total = 0
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
  Object.assign(form, getInitialForm())
}

const handleDialogClosed = () => {
  formRef.value?.clearValidate()
  resetForm()
}

const handleRoleDialogClosed = () => {
  selectedRoleIds.value = []
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.username = ''
  pagination.page = 1
  loadData()
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
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    
    submitLoading.value = true
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
    console.error('保存用户失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？删除后不可恢复。', '提示', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  selectedRoleIds.value = row.roles ? row.roles.map(r => r.id) : []
  await loadAllRoles()
  roleDialogVisible.value = true
}

const handleAssignRoleSubmit = async () => {
  try {
    roleSubmitLoading.value = true
    await assignUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('分配角色失败:', error)
  } finally {
    roleSubmitLoading.value = false
  }
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
</style>
