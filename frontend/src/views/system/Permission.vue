<template>
  <div class="permission-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="handleAdd">新增权限</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="权限名称">
          <el-input v-model="searchForm.name" placeholder="请输入权限名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe row-key="id" empty-text="暂无权限数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名称" />
        <el-table-column prop="code" label="权限编码" />
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            <el-tag :type="row.type === 'menu' ? 'primary' : 'success'">{{ row.type === 'menu' ? '菜单' : '按钮' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="form.code" maxlength="50" show-word-limit placeholder="请输入权限编码（字母、数字、下划线、冒号）" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="menu">菜单</el-radio>
            <el-radio value="button">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父级" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="availableParentTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择父级（不选则为顶级权限）"
          />
        </el-form-item>
        <el-form-item label="路径" prop="path" v-if="form.type === 'menu'">
          <el-input v-model="form.path" maxlength="200" show-word-limit placeholder="请输入路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.type === 'menu'">
          <el-input v-model="form.icon" maxlength="100" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissionList, createPermission, updatePermission, deletePermission, getAllPermissions } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentPermissionId = ref(null)
const permissionTree = ref([])

const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getInitialForm = () => ({
  name: '',
  code: '',
  type: 'menu',
  parentId: null,
  path: '',
  icon: '',
  sort: 0,
  status: 1
})

const form = reactive(getInitialForm())

const validateCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入权限编码'))
  } else if (!/^[a-zA-Z0-9_:]+$/.test(value)) {
    callback(new Error('权限编码只能包含字母、数字、下划线和冒号'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const availableParentTree = computed(() => {
  if (!isEdit.value || !currentPermissionId.value) {
    return permissionTree.value
  }
  const filterTree = (nodes) => {
    if (!nodes || !Array.isArray(nodes)) return []
    return nodes
      .filter(node => node.id !== currentPermissionId.value)
      .map(node => ({
        ...node,
        children: filterTree(node.children)
      }))
  }
  return filterTree(permissionTree.value)
})

const buildTree = (permissions, parentId = null) => {
  if (!permissions || !Array.isArray(permissions)) {
    return []
  }
  return permissions
    .filter(p => p.parentId === parentId)
    .map(p => ({
      ...p,
      children: buildTree(permissions, p.id)
    }))
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('加载权限列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data)
  } catch (error) {
    console.error('加载权限树失败:', error)
    permissionTree.value = []
  }
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
  dialogTitle.value = '新增权限'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  currentPermissionId.value = row.id
  Object.assign(form, {
    name: row.name,
    code: row.code,
    type: row.type,
    parentId: row.parentId,
    path: row.path || '',
    icon: row.icon || '',
    sort: row.sort,
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
      await updatePermission(currentPermissionId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
    loadPermissions()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？删除后不可恢复。', '提示', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page--
    }
    loadData()
    loadPermissions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

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

.search-form {
  margin-bottom: 20px;
}
</style>
