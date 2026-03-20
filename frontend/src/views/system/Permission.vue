<template>
  <div class="permission-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">权限管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增权限
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="权限名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入权限名称" 
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
        row-key="id"
        highlight-current-row
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="权限编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'menu' ? 'primary' : 'success'">
              {{ row.type === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.path || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
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
        <el-form-item label="权限名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入权限名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="请输入权限编码"
            maxlength="50"
            show-word-limit
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="'menu'">菜单</el-radio>
            <el-radio :label="'button'">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="permissionTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择父级"
            :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item label="路径" prop="path" v-if="form.type === 'menu'">
          <el-input 
            v-model="form.path" 
            placeholder="请输入路由路径"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.type === 'menu'">
          <el-input 
            v-model="form.icon" 
            placeholder="请输入图标类名"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getPermissionList, 
  createPermission, 
  updatePermission, 
  deletePermission, 
  getAllPermissions 
} from '@/api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentPermissionId = ref(null)
const permissionTree = ref([])

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
  type: 'menu',
  parentId: null,
  path: '',
  icon: '',
  sort: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

/**
 * 加载权限列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name?.trim() || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载权限树数据
 */
const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data || [])
  } catch (error) {
    console.error('加载权限树失败:', error)
    ElMessage.error('加载权限数据失败')
    permissionTree.value = []
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
 * 重置表单
 */
const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    type: 'menu',
    parentId: null,
    path: '',
    icon: '',
    sort: 0,
    status: 1
  })
}

/**
 * 新增权限
 */
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增权限'
  currentPermissionId.value = null
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑权限
 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  currentPermissionId.value = row.id
  resetForm()
  nextTick(() => {
    Object.assign(form, {
      name: row.name || '',
      code: row.code || '',
      type: row.type || 'menu',
      parentId: row.parentId || null,
      path: row.path || '',
      icon: row.icon || '',
      sort: row.sort ?? 0,
      status: row.status ?? 1
    })
  })
  dialogVisible.value = true
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
      type: form.type,
      parentId: form.parentId || 0,
      path: form.path?.trim() || '',
      icon: form.icon?.trim() || '',
      sort: form.sort,
      status: form.status
    }
    
    if (isEdit.value) {
      await updatePermission(currentPermissionId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await createPermission(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
    loadPermissions()
  } catch (error) {
    console.error('提交失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    ElMessage.error(errorMsg)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除权限
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限 "${row.name}" 吗？删除后不可恢复！`,
      '删除确认',
      { 
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    
    // 如果当前页只有一条数据且不是第一页，则返回上一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
    loadPermissions()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '删除失败'
    ElMessage.error(errorMsg)
  }
}

// 生命周期钩子
onMounted(() => {
  loadData()
  loadPermissions()
})
</script>

<style scoped>
.permission-manage {
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

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .permission-manage {
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
