import request from './request'

export const getUsers = (params) => {
  return request.get('/users', { params })
}

export const getUserById = (id) => {
  return request.get(`/users/${id}`)
}

export const createUser = (data) => {
  return request.post('/users', data)
}

export const updateUser = (id, data) => {
  return request.put(`/users/${id}`, data)
}

export const deleteUser = (id) => {
  return request.delete(`/users/${id}`)
}

export const assignRoles = (id, roleIds) => {
  return request.post(`/users/${id}/roles`, { roleIds })
}

export const changePassword = (id, data) => {
  return request.post(`/users/${id}/change-password`, data)
}
