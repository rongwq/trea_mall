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
          <el-input v-model="searchForm.name" placeholder="请输入角色名称" clearable maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" show-overflow-tooltip />
        <el-table-column prop="code" label="角色编码" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="status">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignPermission(row)">分配权限</el-button>
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
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" maxlength="50" show-word-limit placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" maxlength="200" show-word-limit placeholder="请输入描述" />
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

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="500px" :close-on-click-modal="false" @closed="handlePermissionDialogClosed">
      <div v-loading="permissionLoading">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="{ label: 'name', children: 'children' }"
          show-checkbox
          node-key="id"
          default-expand-all
          :check-strictly="false"
          empty-text="暂无权限数据"
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
import { getRoleList, createRole, updateRole, deleteRole, getAllPermissions, assignRolePermissions, getRoleById } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(null)
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

const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  name: '',
  code: '',
  description: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 1, max: 50, message: '角色名称长度在1到50个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 1, max: 50, message: '角色编码长度在1到50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过200个字符', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载角色列表失败:', error)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data || [])
  } catch (error) {
    console.error('加载权限列表失败:', error)
    permissionTree.value = []
  }
}

const buildTree = (permissions, parentId = 0) => {
  if (!Array.isArray(permissions)) return []
  return permissions
    .filter(p => p && p.parentId === parentId)
    .map(p => ({
      ...p,
      children: buildTree(permissions, p.id)
    }))
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
    name: row.name || '', 
    code: row.code || '', 
    description: row.description || '', 
    status: row.status ?? 1 
  })
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, { name: '', code: '', description: '', status: 1 })
  if (formRef.value) {
    formRef.value.clearValidate()
    formRef.value.resetFields()
  }
}

const handleDialogClosed = () => {
  resetForm()
  currentRoleId.value = null
}

const handlePermissionDialogClosed = () => {
  if (treeRef.value) {
    treeRef.value.setCheckedKeys([])
  }
  currentRoleId.value = null
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const filteredData = filterFormData(form)
    if (isEdit.value) {
      await updateRole(currentRoleId.value, filteredData)
      ElMessage.success('更新成功')
    } else {
      await createRole(filteredData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error(error.message || '操作失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？该操作不可恢复！', '提示', { 
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    deleteLoading.value = row.id
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error(error.message || '删除失败，请重试')
    }
  } finally {
    deleteLoading.value = null
  }
}

const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  permissionLoading.value = true
  permissionDialogVisible.value = true
  
  try {
    // 确保权限树已加载
    if (permissionTree.value.length === 0) {
      await loadPermissions()
    }
    
    const res = await getRoleById(row.id)
    const permissionIds = res.data?.permissions ? res.data.permissions.map(p => p.id).filter(id => id != null) : []
    
    await nextTick()
    if (treeRef.value) {
      treeRef.value.setCheckedKeys([])
      treeRef.value.setCheckedKeys(permissionIds)
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败，请重试')
  } finally {
    permissionLoading.value = false
  }
}

const handleAssignPermissionSubmit = async () => {
  if (!treeRef.value) return
  
  permissionSubmitLoading.value = true
  try {
    // 获取半选中和全选中的节点
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const allPermissionIds = [...new Set([...checkedKeys, ...halfCheckedKeys])].filter(id => id != null)
    
    await assignRolePermissions(currentRoleId.value, allPermissionIds)
    ElMessage.success('分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error(error.message || '分配失败，请重试')
  } finally {
    permissionSubmitLoading.value = false
  }
}

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
  name: xssFilter(data.name),
  code: xssFilter(data.code),
  description: xssFilter(data.description)
})

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
