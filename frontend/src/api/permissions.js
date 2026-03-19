import request from './request'

export const getPermissions = (params) => {
  return request.get('/permissions', { params })
}

export const getAllPermissions = () => {
  return request.get('/permissions/all')
}

export const getPermissionById = (id) => {
  return request.get(`/permissions/${id}`)
}

export const createPermission = (data) => {
  return request.post('/permissions', data)
}

export const updatePermission = (id, data) => {
  return request.put(`/permissions/${id}`, data)
}

export const deletePermission = (id) => {
  return request.delete(`/permissions/${id}`)
}
