import { api } from '@/lib/axios'
import type { ApiResponse, PageResponse, TeacherCourseItem, StudentListItem } from '@/types'

const BASE = import.meta.env.VITE_API_URL

export const teacherService = {
  async listCourses(semesterId?: number, page = 1, pageSize = 20) {
    const params: Record<string, string | number> = { page, pageSize }
    if (semesterId) params.semesterId = semesterId
    const { data } = await api.get<ApiResponse<PageResponse<TeacherCourseItem>>>('/teacher/courses', { params })
    return data.data
  },

  async listStudents(offeringId: number, page = 1, pageSize = 200) {
    const { data } = await api.get<ApiResponse<PageResponse<StudentListItem>>>(`/teacher/courses/${offeringId}/students`, {
      params: { page, pageSize }
    })
    return data.data
  },

  getExportUrl(offeringId: number) {
    return `${BASE}/teacher/courses/${offeringId}/students/export`
  },
}