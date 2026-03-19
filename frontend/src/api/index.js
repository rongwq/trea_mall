import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function assignUserRoles(id, roleIds) {
  return request.post(`/users/${id}/roles`, { roleIds })
}

export function getRoleList(params) {
  return request.get('/roles', { params })
}

export function getAllRoles() {
  return request.get('/roles/all')
}

export function getRoleById(id) {
  return request.get(`/roles/${id}`)
}

export function createRole(data) {
  return request.post('/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function assignRolePermissions(id, permissionIds) {
  return request.post(`/roles/${id}/permissions`, { permissionIds })
}

export function getPermissionList(params) {
  return request.get('/permissions', { params })
}

export function getAllPermissions() {
  return request.get('/permissions/all')
}

export function getPermissionTree() {
  return request.get('/permissions/tree')
}

export function getPermissionById(id) {
  return request.get(`/permissions/${id}`)
}

export function createPermission(data) {
  return request.post('/permissions', data)
}

export function updatePermission(id, data) {
  return request.put(`/permissions/${id}`, data)
}

export function deletePermission(id) {
  return request.delete(`/permissions/${id}`)
}
