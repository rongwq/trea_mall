import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const hasRole = (role) => roles.value.includes(role)
  const hasPermission = (permission) => permissions.value.includes(permission)

  const setAuth = (authData) => {
    token.value = authData.token
    username.value = authData.username
    roles.value = authData.roles || []
    permissions.value = authData.permissions || []
    
    localStorage.setItem('token', authData.token)
    localStorage.setItem('username', authData.username)
    localStorage.setItem('roles', JSON.stringify(authData.roles || []))
    localStorage.setItem('permissions', JSON.stringify(authData.permissions || []))
  }

  const clearAuth = () => {
    token.value = ''
    username.value = ''
    roles.value = []
    permissions.value = []
    
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  const login = async (credentials) => {
    const response = await loginApi(credentials)
    setAuth(response.data)
    return response
  }

  const fetchCurrentUser = async () => {
    try {
      const response = await getCurrentUser()
      const userData = response.data
      roles.value = userData.roles || []
      permissions.value = userData.permissions || []
      localStorage.setItem('roles', JSON.stringify(userData.roles || []))
      localStorage.setItem('permissions', JSON.stringify(userData.permissions || []))
    } catch (error) {
      console.error('Failed to fetch current user:', error)
    }
  }

  const logout = () => {
    clearAuth()
  }

  return {
    token,
    username,
    roles,
    permissions,
    isLoggedIn,
    hasRole,
    hasPermission,
    login,
    logout,
    fetchCurrentUser,
    setAuth,
    clearAuth
  }
})
