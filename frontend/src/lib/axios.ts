import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10_000,
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// Track logout-in-progress to prevent recursive 401 loops
let isLoggingOut = false

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isLoggingOut) {
      // Skip redirect for auth endpoints (login/logout) to avoid infinite loops
      const url = error.config?.url || ''
      if (url.includes('/auth/login') || url.includes('/auth/logout')) {
        return Promise.reject(error)
      }
      isLoggingOut = true
      const auth = useAuthStore()
      auth.logout().finally(() => {
        isLoggingOut = false
      })
    }
    return Promise.reject(error)
  },
)