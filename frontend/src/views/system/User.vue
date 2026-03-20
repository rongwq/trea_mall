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
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag
              v-for="(role, index) in displayedRoles(row.roles)"
              :key="role.id"
              size="small"
              style="margin-right: 5px; margin-bottom: 2px"
            >
              {{ role.name }}
            </el-tag>
            <el-tag v-if="row.roles && row.roles.length > 3" size="small" type="info">
              +{{ row.roles.length - 3 }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === USER_STATUS.ENABLED ? 'success' : 'danger'" size="small">
              {{ row.status === USER_STATUS.ENABLED ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
      </el-table>

      <div class="table-footer">
        <el-button
          type="danger"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          批量删除({{ selectedRows.length }})
        </el-button>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleBeforeClose"
    >
      <el-form :model="form" :rules="dynamicRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="USER_STATUS.ENABLED">启用</el-radio>
            <el-radio :value="USER_STATUS.DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-checkbox-group v-model="selectedRoleIds" v-if="allRoles.length > 0">
        <el-checkbox
          v-for="role in allRoles"
          :key="role.id"
          :value="role.id"
          style="display: block; margin-bottom: 10px"
        >
          {{ role.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-else description="暂无角色，请先创建角色" />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoleSubmit" :loading="roleSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { debounce } from 'lodash-es'
import { getUserList, createUser, updateUser, deleteUser, getAllRoles, assignUserRoles } from '@/api'

// 用户状态常量
const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1
}

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const roleSubmitLoading = ref(false)

// 表格数据
const tableData = ref([])
const selectedRows = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentUserId = ref(null)

// 角色数据
const allRoles = ref([])
const selectedRoleIds = ref([])

// 表单原始数据（用于判断是否有修改）
const originalForm = ref({})

// 搜索表单
const searchForm = reactive({
  username: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 用户表单
const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  status: USER_STATUS.ENABLED
})

// 基础验证规则
const baseRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: '只能包含字母、数字、下划线和中文',
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

// 动态验证规则（根据新增/编辑切换）
const dynamicRules = computed(() => {
  const rules = { ...baseRules }
  if (!isEdit.value) {
    // 新增时密码必填
    rules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*]{6,20}$/,
        message: '密码必须包含字母和数字',
        trigger: 'blur'
      }
    ]
  } else {
    // 编辑时密码可选，但如果填写则需符合规则
    rules.password = [
      {
        validator: (rule, value, callback) => {
          if (!value) {
            callback()
            return
          }
          if (value.length < 6 || value.length > 20) {
            callback(new Error('长度在 6 到 20 个字符'))
            return
          }
          if (!/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*]{6,20}$/.test(value)) {
            callback(new Error('密码必须包含字母和数字'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }
  return rules
})

/**
 * 加载用户列表数据
 */
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
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载所有角色
 */
const loadAllRoles = async () => {
  try {
    const res = await getAllRoles()
    allRoles.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

/**
 * 防抖搜索
 */
const handleSearch = debounce(() => {
  pagination.page = 1
  loadData()
}, 300)

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.username = ''
  pagination.page = 1
  loadData()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

/**
 * 页码变化
 */
const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

/**
 * 表格选择变化
 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

/**
 * 获取显示的角色（最多显示3个）
 */
const displayedRoles = (roles) => {
  if (!roles || !Array.isArray(roles)) return []
  return roles.slice(0, 3)
}

/**
 * 打开新增用户弹窗
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  currentUserId.value = null
  resetForm()
  dialogVisible.value = true
}

/**
 * 打开编辑用户弹窗
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  currentUserId.value = row.id
  resetForm()
  nextTick(() => {
    Object.assign(form, {
      username: row.username,
      password: '',
      email: row.email || '',
      phone: row.phone || '',
      status: row.status
    })
    // 保存原始数据用于判断是否有修改
    originalForm.value = JSON.parse(JSON.stringify(form))
  })
  dialogVisible.value = true
}

/**
 * 重置表单
 */
const resetForm = () => {
  form.username = ''
  form.password = ''
  form.email = ''
  form.phone = ''
  form.status = USER_STATUS.ENABLED
  originalForm.value = {}
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

/**
 * 检查表单是否有修改
 */
const isFormDirty = () => {
  return JSON.stringify(form) !== JSON.stringify(originalForm.value)
}

/**
 * 弹窗关闭前确认
 */
const handleBeforeClose = (done) => {
  if (isFormDirty()) {
    ElMessageBox.confirm('有未保存的修改，确定要关闭吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(() => {
      done()
    }).catch(() => {})
  } else {
    done()
  }
}

/**
 * 取消按钮
 */
const handleCancel = () => {
  if (isFormDirty()) {
    ElMessageBox.confirm('有未保存的修改，确定要关闭吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(() => {
      dialogVisible.value = false
    }).catch(() => {})
  } else {
    dialogVisible.value = false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 编辑时过滤掉空密码
      const submitData = { ...form }
      if (!submitData.password) {
        delete submitData.password
      }
      await updateUser(currentUserId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除用户
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    loading.value = true
    await deleteUser(row.id)
    ElMessage.success('删除成功')

    // 如果当前页只有一条数据且不是第一页，回到上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  } finally {
    loading.value = false
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`,
      '提示',
      { type: 'warning' }
    )
    loading.value = true
    // 串行删除
    for (const row of selectedRows.value) {
      await deleteUser(row.id)
    }
    ElMessage.success('批量删除成功')

    // 检查是否需要回到上一页
    const remainingCount = tableData.value.length - selectedRows.value.length
    if (remainingCount === 0 && pagination.page > 1) {
      pagination.page--
    }
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  } finally {
    loading.value = false
  }
}

/**
 * 打开分配角色弹窗
 */
const handleAssignRole = (row) => {
  currentUserId.value = row.id
  selectedRoleIds.value = row.roles ? row.roles.map(r => r.id) : []
  roleDialogVisible.value = true
}

/**
 * 提交角色分配
 */
const handleAssignRoleSubmit = async () => {
  roleSubmitLoading.value = true
  try {
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

// 组件挂载时加载数据
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

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .user-manage {
    padding: 10px;
  }

  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }

  .table-footer {
    flex-direction: column;
    gap: 10px;
  }

  .el-pagination {
    justify-content: center;
  }
}
</style>
