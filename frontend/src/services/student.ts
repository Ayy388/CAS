import { api } from '@/lib/axios'
import type { ApiResponse, StudentDashboardData } from '@/types'

export const studentService = {
  async getDashboard() {
    const { data } = await api.get<ApiResponse<StudentDashboardData>>('/student/dashboard')
    return data.data
  },
}