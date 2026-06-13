import { api } from '@/lib/axios'
import type { CourseOffering, ApiResponse, PageResponse } from '@/types'

export const offeringService = {
  // Admin
  async adminList(params?: { semesterId?: number; status?: string; page?: number; pageSize?: number }) {
    const { data } = await api.get<ApiResponse<PageResponse<CourseOffering>>>('/admin/offerings', {
      params: { page: 1, pageSize: 200, ...params }
    })
    return data.data
  },

  async adminCreate(req: Partial<CourseOffering>) {
    const { data } = await api.post<ApiResponse<CourseOffering>>('/admin/offerings', req)
    return data.data
  },

  async adminUpdate(id: number, req: Partial<CourseOffering>) {
    await api.put(`/admin/offerings/${id}`, req)
  },

  // Student
  async studentList(keyword?: string, type?: string, page = 1, pageSize = 20) {
    const params: Record<string, string | number> = { page, pageSize }
    if (keyword) params.keyword = keyword
    if (type) params.type = type
    const { data } = await api.get<ApiResponse<PageResponse<CourseOffering>>>('/courses', { params })
    return data.data
  },

  async studentDetail(id: number) {
    const { data } = await api.get<ApiResponse<CourseOffering>>(`/courses/${id}`)
    return data.data
  },
}