import { api } from '@/lib/axios'
import type { Enrollment, ApiResponse, PageResponse } from '@/types'

export const enrollmentService = {
  async enroll(offeringId: number) {
    const { data } = await api.post<ApiResponse<Enrollment>>('/enrollments', { offeringId })
    return data.data
  },

  async myEnrollments(page = 1, pageSize = 20) {
    const { data } = await api.get<ApiResponse<PageResponse<Enrollment>>>('/student/enrollments', {
      params: { page, pageSize }
    })
    return data.data
  },

  async drop(enrollmentId: number) {
    await api.delete(`/enrollments/${enrollmentId}`)
  },
}