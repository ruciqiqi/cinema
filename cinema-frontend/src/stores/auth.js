import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('cinema_token') || '')
  const username = ref(localStorage.getItem('cinema_username') || '')
  const role = ref(localStorage.getItem('cinema_role') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'admin')

  function setSession(res) {
    token.value = res.token
    username.value = res.username
    role.value = res.role
    localStorage.setItem('cinema_token', res.token)
    localStorage.setItem('cinema_username', res.username)
    localStorage.setItem('cinema_role', res.role)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('cinema_token')
    localStorage.removeItem('cinema_username')
    localStorage.removeItem('cinema_role')
  }

  async function login(u, p) {
    const res = await api.post('/auth/login', { username: u, password: p })
    if (res.data.success) setSession(res.data)
    return res.data
  }

  async function register(u, p, ph) {
    const res = await api.post('/auth/register', { username: u, password: p, phone: ph })
    if (res.data.success) setSession(res.data)
    return res.data
  }

  return { token, username, role, isLoggedIn, isAdmin, setSession, logout, login, register }
})
