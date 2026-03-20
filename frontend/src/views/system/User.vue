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
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable maxlength="20" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" empty-text="暂无用户数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" show-overflow-tooltip />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles || []" :key="role.id" size="small" style="margin-right: 4px; margin-bottom: 4px;">{{ role.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" :loading="deleteLoading === row.id">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @closed="handleDialogClosed">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" maxlength="20" show-word-limit placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password maxlength="20" show-word-limit placeholder="请输入密码（6-20位）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="50" show-word-limit placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
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

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px" :close-on-click-modal="false">
      <div style="max-height: 300px; overflow-y: auto;">
        <el-checkbox-group v-model="selectedRoleIds">
          <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" style="display: block; margin: 8px 0;">{{ role.name }}</el-checkbox>
        </el-checkbox-group>
        <el-empty v-if="allRoles.length === 0" description="暂无角色数据" />
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoleSubmit" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, getAllRoles, assignUserRoles } from '@/api'

// 常量定义
const MAX_USERNAME_LENGTH = 20
const MAX_PASSWORD_LENGTH = 20
const MAX_EMAIL_LENGTH = 50
const MAX_PHONE_LENGTH = 11

// 状态管理
const loading = ref(false)
const deleteLoading = ref(null)
const submitLoading = ref(false)
const assignLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentUserId = ref(null)
const allRoles = ref([])
const selectedRoleIds = ref([])

// 搜索表单
const searchForm = reactive({
  username: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  status: 1
})

// XSS过滤
const xssFilter = (str) => {
  if (!str) return ''
  return str.replace(/[<>&"'`]/g, (match) => {
    const escape = {
      '<': '&lt;',
      '>': '&gt;',
      '&': '&amp;',
      '"': '&quot;',
      "'": '&#x27;',
      '`': '&#x60;'
    }
    return escape[match]
  })
}

// 过滤表单数据
const filterFormData = (data) => ({
  ...data,
  username: xssFilter(data.username),
  email: xssFilter(data.email),
  phone: xssFilter(data.phone)
})

// 验证规则
const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: MAX_USERNAME_LENGTH, message: `用户名长度在1到${MAX_USERNAME_LENGTH}个字符`, trigger: 'blur' }
  ],
  password: isEdit.value ? [] : [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: MAX_PASSWORD_LENGTH, message: `密码长度在6到${MAX_PASSWORD_LENGTH}个字符`, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    { max: MAX_EMAIL_LENGTH, message: `邮箱长度不能超过${MAX_EMAIL_LENGTH}个字符`, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}))

// 重置表单
const resetForm = () => {
  Object.assign(form, { username: '', password: '', email: '', phone: '', status: 1 })
  if (formRef.value) {
    formRef.value.clearValidate()
    formRef.value.resetFields()
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  pagination.page = 1
  loadData()
}

// 对话框关闭回调
const handleDialogClosed = () => {
  resetForm()
  currentUserId.value = null
}

// 加载用户列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    tableData.value = []
    pagination.total = 0
    ElMessage.error('加载用户列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载所有角色
const loadAllRoles = async () => {
  try {
    const res = await getAllRoles()
    allRoles.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    allRoles.value = []
  }
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  currentUserId.value = row.id
  Object.assign(form, {
    username: row.username || '',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status ?? 1
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    const filteredData = filterFormData(form)
    if (isEdit.value) {
      await updateUser(currentUserId.value, filteredData)
      ElMessage.success('更新成功')
    } else {
      await createUser(filteredData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error(error.message || '操作失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？该操作不可恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    deleteLoading.value = row.id
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.message || '删除失败，请重试')
    }
  } finally {
    deleteLoading.value = null
  }
}

// 分配角色
const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  selectedRoleIds.value = row.roles ? row.roles.map(r => r.id).filter(id => id != null) : []
  await loadAllRoles()
  roleDialogVisible.value = true
}

// 提交分配角色
const handleAssignRoleSubmit = async () => {
  assignLoading.value = true
  try {
    await assignUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('分配角色失败:', error)
    ElMessage.error(error.message || '分配失败，请重试')
  } finally {
    assignLoading.value = false
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
