<template>
  <div class="roles-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button
            v-if="userStore.hasPermission('role:create')"
            type="primary"
            @click="handleCreate"
          >
            新增角色
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索角色名称/编码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="权限">
          <template #default="{ row }">
            <el-tag
              v-for="perm in row.permissionCodes?.slice(0, 3)"
              :key="perm"
              size="small"
              type="warning"
              style="margin-right: 5px"
            >
              {{ perm }}
            </el-tag>
            <el-tag v-if="row.permissionCodes?.length > 3" size="small" type="info">
              +{{ row.permissionCodes.length - 3 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="userStore.hasPermission('role:update')"
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="userStore.hasPermission('role:update')"
              type="warning"
              size="small"
              @click="handleAssignPermissions(row)"
            >
              分配权限
            </el-button>
            <el-button
              v-if="userStore.hasPermission('role:delete')"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" placeholder="如：ADMIN" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="如：管理员" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- Assign Permissions Dialog -->
    <el-dialog
      v-model="permissionsDialogVisible"
      title="分配权限"
      width="500px"
    >
      <el-checkbox-group v-model="selectedPermissions">
        <el-checkbox
          v-for="perm in allPermissions"
          :key="perm.id"
          :label="perm.id"
        >
          {{ perm.name }} ({{ perm.code }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="permissionsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPermissions" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getRoles, createRole, updateRole, deleteRole, assignPermissions } from '../api/roles'
import { getAllPermissions } from '../api/permissions'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const allPermissions = ref([])
const dialogVisible = ref(false)
const permissionsDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  code: '',
  name: '',
  description: ''
})

const formRules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const selectedPermissions = ref([])
const currentRole = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoles({
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword
    })
    tableData.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    allPermissions.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  loadData()
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handlePageChange = (val) => {
  pagination.page = val
  loadData()
}

const handleCreate = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, {
    code: '',
    name: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    description: row.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(currentId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleAssignPermissions = (row) => {
  currentRole.value = row
  selectedPermissions.value = row.permissionIds ? [...row.permissionIds] : []
  permissionsDialogVisible.value = true
}

const handleSubmitPermissions = async () => {
  submitLoading.value = true
  try {
    await assignPermissions(currentRole.value.id, selectedPermissions.value)
    ElMessage.success('分配权限成功')
    permissionsDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadPermissions()
})
</script>

<style scoped>
.roles-page {
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

.el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.el-checkbox {
  margin-right: 0;
}
</style>
