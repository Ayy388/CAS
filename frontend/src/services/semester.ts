import { api } from '@/lib/axios'
import type { Semester, ApiResponse, PageResponse } from '@/types'

export const semesterService = {
  async list(page = 1, pageSize = 20) {
    const { data } = await api.get<ApiResponse<PageResponse<Semester>>>('/admin/semesters', {
      params: { page, pageSize }
    })
    return data.data
  },

  async getById(id: number) {
    const { data } = await api.get<ApiResponse<Semester>>(`/admin/semesters/${id}`)
    return data.data
  },

  async create(req: Partial<Semester>) {
    const { data } = await api.post<ApiResponse<Semester>>('/admin/semesters', req)
    return data.data
  },

  async update(id: number, req: Partial<Semester>) {
    await api.put(`/admin/semesters/${id}`, req)
  },

  async activate(id: number) {
    await api.patch(`/admin/semesters/${id}/activate`)
  },

  async delete(id: number) {
    await api.delete(`/admin/semesters/${id}`)
  },
}