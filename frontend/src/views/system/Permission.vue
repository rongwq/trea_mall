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
          <el-input v-model="searchForm.name" placeholder="请输入权限名称" clearable maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" row-key="id" empty-text="暂无权限数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限名称" show-overflow-tooltip />
        <el-table-column prop="code" label="权限编码" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'menu' ? 'primary' : 'success'" size="small">{{ row.type === 'menu' ? '菜单' : '按钮' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false" @closed="handleDialogClosed">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="form.code" maxlength="100" show-word-limit placeholder="请输入权限编码" />
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
            :data="permissionTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择父级（顶级权限不选）"
          />
        </el-form-item>
        <el-form-item label="路径" prop="path" v-if="form.type === 'menu'">
          <el-input v-model="form.path" maxlength="200" show-word-limit placeholder="菜单访问路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.type === 'menu'">
          <el-input v-model="form.icon" maxlength="50" show-word-limit placeholder="Element Plus图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" placeholder="数字越小越靠前" />
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissionList, createPermission, updatePermission, deletePermission, getAllPermissions } from '@/api'

// 常量定义
const ROOT_PARENT_ID = 0
const MAX_NAME_LENGTH = 50
const MAX_CODE_LENGTH = 100
const MAX_PATH_LENGTH = 200

// 状态管理
const loading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(null)
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

// 分页
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
  parentId: ROOT_PARENT_ID,
  path: '',
  icon: '',
  sort: 0,
  status: 1
})

// 验证规则
const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 1, max: MAX_NAME_LENGTH, message: `权限名称长度在1到50个字符`, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 1, max: MAX_CODE_LENGTH, message: `权限编码长度在1到100个字符`, trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  path: [],
  sort: [
    { type: 'number', min: 0, message: '排序必须是非负整数', trigger: 'blur' }
  ]
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
  path: xssFilter(data.path),
  icon: xssFilter(data.icon)
})

// 加载权限列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载权限列表失败:', error)
    tableData.value = []
    pagination.total = 0
    ElMessage.error('加载权限列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载所有权限（用于树形选择
const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    permissionTree.value = buildTree(res.data || [])
  } catch (error) {
    console.error('加载权限列表失败:', error)
    permissionTree.value = []
  }
}

// 构建树状结构
const buildTree = (permissions, parentId = ROOT_PARENT_ID) => {
  if (!Array.isArray(permissions)) return []
  return permissions
    .filter(p => p && p.parentId === parentId)
    .map(p => ({
      ...p,
      children: buildTree(permissions, p.id)
    }))
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    type: 'menu',
    parentId: ROOT_PARENT_ID,
    path: '',
    icon: '',
    sort: 0,
    status: 1
  })
  if (formRef.value) {
    formRef.value.clearValidate()
    formRef.value.resetFields()
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  pagination.page = 1
  loadData()
}

// 对话框关闭回调
const handleDialogClosed = () => {
  resetForm()
  currentPermissionId.value = null
}

// 新增权限
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增权限'
  resetForm()
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  currentPermissionId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    code: row.code || '',
    type: row.type || 'menu',
    parentId: row.parentId ?? ROOT_PARENT_ID,
    path: row.path || '',
    icon: row.icon || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  })
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
      await updatePermission(currentPermissionId.value, filteredData)
      ElMessage.success('更新成功')
    } else {
      await createPermission(filteredData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
    loadPermissions()
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error(error.message || '操作失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 删除权限
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？该操作可能影响相关角色的权限配置！', '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    deleteLoading.value = row.id
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadPermissions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error(error.message || '删除失败，请重试')
    }
  } finally {
    deleteLoading.value = null
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
