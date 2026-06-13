import { api } from '@/lib/axios'
import type { SelectionCampaign, ApiResponse, PageResponse } from '@/types'

export const campaignService = {
  async adminList(params?: { semesterId?: number; page?: number; pageSize?: number }) {
    const { data } = await api.get<ApiResponse<PageResponse<SelectionCampaign>>>('/admin/campaigns', {
      params: { page: 1, pageSize: 20, ...params }
    })
    return data.data
  },

  async adminCreate(req: Partial<SelectionCampaign>) {
    const { data } = await api.post<ApiResponse<SelectionCampaign>>('/admin/campaigns', req)
    return data.data
  },

  async start(id: number) {
    const { data } = await api.patch<ApiResponse<SelectionCampaign>>(`/admin/campaigns/${id}/start`)
    return data.data
  },

  async end(id: number) {
    const { data } = await api.patch<ApiResponse<SelectionCampaign>>(`/admin/campaigns/${id}/end`)
    return data.data
  },

  async getCurrent() {
    const { data } = await api.get<ApiResponse<SelectionCampaign>>('/campaigns/current')
    return data.data
  },
}