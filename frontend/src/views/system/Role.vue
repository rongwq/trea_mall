<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" placeholder="请输入角色名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe empty-text="暂无角色数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignPermission(row)">分配权限</el-button>
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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" maxlength="50" show-word-limit placeholder="请输入角色编码（字母、数字、下划线）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" maxlength="200" show-word-limit placeholder="请输入描述" />
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

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="500px">
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :check-strictly="false"
      />
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
import { getRoleList, createRole, updateRole, deleteRole, getAllPermissions, assignRolePermissions, getRoleById } from '@/api'

const getInitialForm = () => ({
  name: '',
  code: '',
  description: '',
  status: 1
})

const loading = ref(false)
const submitLoading = ref(false)
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

const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive(getInitialForm())

const validateCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入角色编码'))
  } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('角色编码只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data)
  } catch (error) {
    console.error('加载权限列表失败:', error)
    permissionTree.value = []
  }
}

const buildTree = (permissions, parentId = 0) => {
  if (!permissions || !Array.isArray(permissions)) {
    return []
  }
  return permissions
    .filter(p => p.parentId === parentId)
    .map(p => ({
      ...p,
      children: buildTree(permissions, p.id)
    }))
    .filter(p => p.children.length > 0 || p.parentId !== undefined)
}

const resetForm = () => {
  Object.assign(form, getInitialForm())
}

const handleDialogClosed = () => {
  formRef.value?.clearValidate()
  resetForm()
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  currentRoleId.value = row.id
  Object.assign(form, {
    name: row.name,
    code: row.code,
    description: row.description || '',
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
      await updateRole(currentRoleId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？删除后不可恢复。', '提示', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  try {
    const res = await getRoleById(row.id)
    const permissionIds = res.data.permissions ? res.data.permissions.map(p => p.id) : []
    permissionDialogVisible.value = true
    await nextTick()
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(permissionIds)
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败')
  }
}

const handleAssignPermissionSubmit = async () => {
  if (!treeRef.value) {
    ElMessage.warning('权限树未加载完成')
    return
  }
  
  try {
    permissionSubmitLoading.value = true
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]
    await assignRolePermissions(currentRoleId.value, permissionIds)
    ElMessage.success('分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
  } finally {
    permissionSubmitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadPermissions()
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

.search-form {
  margin-bottom: 20px;
}
</style>
