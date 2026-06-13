import { api } from '@/lib/axios'
import type { LoginResponse, User, ApiResponse } from '@/types'

export const authService = {
  async login(username: string, password: string) {
    const { data } = await api.post<ApiResponse<LoginResponse>>('/auth/login', { username, password })
    return data.data
  },

  async getMe() {
    const { data } = await api.get<ApiResponse<User>>('/auth/me')
    return data.data
  },

  async logout() {
    await api.post('/auth/logout')
  },
}