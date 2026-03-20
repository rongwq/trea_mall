<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">角色管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入角色名称" 
            clearable
            @keyup.enter="handleSearch"
            maxlength="50"
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
        border
        highlight-current-row
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.name" placement="top" :disabled="row.name.length <= 20">
              <span>{{ row.name }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="角色编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === ROLE_STATUS.ENABLED ? 'success' : 'danger'">
              {{ row.status === ROLE_STATUS.ENABLED ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button size="small" type="warning" @click="handleAssignPermission(row)">
              <el-icon><Key /></el-icon>分配权限
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="80px"
        status-icon
      >
        <el-form-item label="角色名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入角色名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="请输入角色编码，如：admin"
            maxlength="50"
            show-word-limit
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入角色描述"
            maxlength="200"
            show-word-limit
            resize="none"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="ROLE_STATUS.ENABLED">启用</el-radio>
            <el-radio :label="ROLE_STATUS.DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog 
      v-model="permissionDialogVisible" 
      title="分配权限" 
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-loading="permissionLoading" class="permission-tree-container">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="{ label: 'name', children: 'children' }"
          show-checkbox
          node-key="id"
          default-expand-all
          :check-strictly="false"
          :default-checked-keys="defaultCheckedKeys"
        />
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissionSubmit" :loading="permissionSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { 
  getRoleList, 
  createRole, 
  updateRole, 
  deleteRole, 
  getAllPermissions, 
  assignRolePermissions, 
  getRoleById 
} from '@/api'

// 角色状态常量
const ROLE_STATUS = {
  ENABLED: 1,
  DISABLED: 0
}

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const permissionLoading = ref(false)
const permissionSubmitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const currentRoleId = ref(null)
const permissionTree = ref([])
const defaultCheckedKeys = ref([])

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  name: '',
  code: '',
  description: '',
  status: ROLE_STATUS.ENABLED
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_\-]+$/, message: '只能包含中文、字母、数字、下划线和横线', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述最多 200 个字符', trigger: 'blur' }
  ]
}

/**
 * 加载角色列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name?.trim() || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 加载权限树数据
 */
const loadPermissions = async () => {
  permissionLoading.value = true
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data || [])
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('加载权限数据失败')
  } finally {
    permissionLoading.value = false
  }
}

/**
 * 构建权限树
 * @param {Array} permissions - 权限列表
 * @param {number} parentId - 父节点ID
 * @returns {Array} 树形结构
 */
const buildTree = (permissions, parentId = 0) => {
  if (!Array.isArray(permissions)) return []
  return permissions
    .filter(p => p.parentId === parentId)
    .map(p => ({
      ...p,
      children: buildTree(permissions, p.id)
    }))
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.name = ''
  pagination.page = 1
  pagination.size = 10
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
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

/**
 * 新增角色
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  currentRoleId.value = null
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑角色
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  currentRoleId.value = row.id
  resetForm()
  // 使用解构赋值避免引用问题
  nextTick(() => {
    Object.assign(form, {
      name: row.name || '',
      code: row.code || '',
      description: row.description || '',
      status: row.status ?? ROLE_STATUS.ENABLED
    })
  })
  dialogVisible.value = true
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    description: '',
    status: ROLE_STATUS.ENABLED
  })
  defaultCheckedKeys.value = []
}

/**
 * 弹窗关闭回调
 */
const handleDialogClosed = () => {
  if (formRef.value) {
    formRef.value.resetFields()
    formRef.value.clearValidate()
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    const submitData = {
      name: form.name.trim(),
      code: form.code.trim(),
      description: form.description?.trim() || '',
      status: form.status
    }
    
    if (isEdit.value) {
      await updateRole(currentRoleId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createRole(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMsg)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除角色
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.name}" 吗？删除后不可恢复！`,
      '删除确认',
      { 
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    
    // 如果当前页只有一条数据且不是第一页，则返回上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '删除失败'
    ElMessage.error(errorMsg)
  }
}

/**
 * 分配权限
 */
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  permissionLoading.value = true
  
  try {
    // 并行加载权限树和角色权限
    const [permissionsRes, roleRes] = await Promise.all([
      getAllPermissions(),
      getRoleById(row.id)
    ])
    
    permissionTree.value = buildTree(permissionsRes.data || [])
    const permissionIds = roleRes.data?.permissions?.map(p => p.id) || []
    
    // 使用 nextTick 确保树组件渲染完成后再设置选中状态
    nextTick(() => {
      if (treeRef.value) {
        treeRef.value.setCheckedKeys(permissionIds)
      }
    })
  } catch (error) {
    console.error('加载权限数据失败:', error)
    ElMessage.error('加载权限数据失败')
    permissionDialogVisible.value = false
  } finally {
    permissionLoading.value = false
  }
}

/**
 * 提交权限分配
 */
const handleAssignPermissionSubmit = async () => {
  if (!treeRef.value) return
  
  permissionSubmitLoading.value = true
  try {
    // 获取全选和半选的节点
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]
    
    await assignRolePermissions(currentRoleId.value, permissionIds)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('权限分配失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '权限分配失败'
    ElMessage.error(errorMsg)
  } finally {
    permissionSubmitLoading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .role-manage {
    padding: 10px;
  }
  
  .search-form {
    :deep(.el-form-item) {
      margin-bottom: 10px;
    }
  }
  
  .pagination {
    justify-content: center;
  }
}
</style>
