import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { api } from '@/lib/axios'
import type { Enrollment } from '@/types'

export function useEnroll() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (offeringId: number) => {
      const { data } = await api.post('/enrollments', { offeringId })
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['courses'] })
      queryClient.invalidateQueries({ queryKey: ['enrollments'] })
    },
  })
}

export function useDrop() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (enrollmentId: number) => {
      const { data } = await api.delete(`/enrollments/${enrollmentId}`)
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['enrollments'] })
      queryClient.invalidateQueries({ queryKey: ['courses'] })
    },
  })
}

export function useMyEnrollments() {
  return useQuery({
    queryKey: ['enrollments', 'mine'],
    queryFn: async () => {
      const { data } = await api.get('/enrollments/mine')
      return data.data as Enrollment[]
    },
  })
}