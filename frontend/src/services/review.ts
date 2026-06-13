import { api } from '@/lib/axios'
import type { ReviewItem, ApiResponse, PageResponse } from '@/types'

export const reviewService = {
  async list(page = 1, pageSize = 20) {
    const { data } = await api.get<ApiResponse<PageResponse<ReviewItem>>>('/admin/review', {
      params: { page, pageSize }
    })
    return data.data
  },

  async approve(offeringId: number) {
    await api.post(`/admin/review/${offeringId}/approve`)
  },

  async reject(offeringId: number) {
    await api.post(`/admin/review/${offeringId}/reject`)
  },
}