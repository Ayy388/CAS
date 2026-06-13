import { api } from '@/lib/axios'
import type { Notification, ApiResponse, PageResponse } from '@/types'

export const notificationService = {
  async list(type?: string, page = 1, pageSize = 20) {
    const params: Record<string, string | number> = { page, pageSize }
    if (type) params.type = type
    const { data } = await api.get<ApiResponse<PageResponse<Notification>>>('/notifications', { params })
    return data.data
  },

  async markRead(id: number) {
    await api.put(`/notifications/${id}/read`)
  },
}