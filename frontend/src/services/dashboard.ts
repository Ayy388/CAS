import { api } from '@/lib/axios'
import type { DashboardStats, TopCourse, TrendPoint, ApiResponse } from '@/types'

export const dashboardService = {
  async getStats() {
    const { data } = await api.get<ApiResponse<DashboardStats>>('/admin/dashboard/stats')
    return data.data
  },

  async getTopCourses() {
    const { data } = await api.get<ApiResponse<TopCourse[]>>('/admin/dashboard/top-courses')
    return data.data
  },

  async getTrend() {
    const { data } = await api.get<ApiResponse<TrendPoint[]>>('/admin/dashboard/trend')
    return data.data
  },
}