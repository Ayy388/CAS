import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { User } from '@/types'
import { api } from '@/lib/axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(JSON.parse(localStorage.getItem('cas:user') ?? 'null'))
  const token = ref<string | null>(localStorage.getItem('cas:token'))

  const isAuthenticated = computed(() => !!token.value)

  function persist() {
    if (token.value) {
      localStorage.setItem('cas:token', token.value)
    } else {
      localStorage.removeItem('cas:token')
    }
    if (user.value) {
      localStorage.setItem('cas:user', JSON.stringify(user.value))
    } else {
      localStorage.removeItem('cas:user')
    }
  }

  async function login(username: string, password: string) {
    const { data: res } = await api.post('/auth/login', { username, password })
    const { token: newToken, user: userData } = res.data
    token.value = newToken
    user.value = userData
    persist()
    return userData
  }

  function setUser(u: User) {
    user.value = u
    persist()
  }

  async function logout() {
    // Only call logout API if we have a token (prevents 401 loop)
    if (token.value) {
      try {
        await api.post('/auth/logout')
      } catch {
        // ignore network/API errors
      }
    }
    token.value = null
    user.value = null
    persist()
    try {
      const router = useRouter()
      router.push('/login')
    } catch {
      // ignore router errors (e.g. called outside component setup)
    }
  }

  return { user, token, isAuthenticated, login, setUser, logout }
})