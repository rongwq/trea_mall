import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import UserManagement from '../views/UserManagement.vue'
import RoleManagement from '../views/RoleManagement.vue'
import PermissionManagement from '../views/PermissionManagement.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/users',
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: RoleManagement
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: PermissionManagement
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router