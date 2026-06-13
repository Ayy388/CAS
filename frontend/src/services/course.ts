import { api } from '@/lib/axios'
import type { Course, ApiResponse, PageResponse } from '@/types'

export const courseService = {
  async list(keyword?: string, type?: string, page = 1, pageSize = 20) {
    const params: Record<string, string | number> = {}
    if (keyword) params.keyword = keyword
    if (type) params.type = type
    params.page = page
    params.pageSize = pageSize
    const { data } = await api.get<ApiResponse<PageResponse<Course>>>('/admin/courses', { params })
    return data.data
  },

  async create(req: Partial<Course>) {
    const { data } = await api.post<ApiResponse<Course>>('/admin/courses', req)
    return data.data
  },

  async update(id: number, req: Partial<Course>) {
    await api.put(`/admin/courses/${id}`, req)
  },

  async delete(id: number) {
    await api.delete(`/admin/courses/${id}`)
  },
}