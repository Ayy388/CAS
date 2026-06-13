<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Button } from '@/components/ui/Button'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { teacherService } from '@/services/teacher'
import type { TeacherCourseItem } from '@/types'
import { Users, Eye } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const loading = ref(true)
const courses = ref<TeacherCourseItem[]>([])

onMounted(async () => {
  try {
    const res = await teacherService.listCourses()
    courses.value = res.items
  } catch {
    toast?.('error', '加载课程列表失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#111827]">我的课程</h1>
      <p class="mt-1 text-sm text-[#6B7280]">查看您负责的课程及报名情况</p>
    </div>

    <Skeleton v-if="loading" variant="card" :count="4" />

    <EmptyState
      v-else-if="courses.length === 0"
      title="暂无课程"
      description="您还没有被分配任何课程"
    />

    <div v-else class="grid gap-6 sm:grid-cols-2">
      <Card v-for="c in courses" :key="c.id" class="card-hover">
        <CardContent class="p-5">
          <div class="mb-3 flex items-start justify-between">
            <h3 class="text-base font-semibold text-[#111827]">{{ c.courseName }}</h3>
            <StatusBadge :status="c.status" type="offering" />
          </div>
          <div class="mb-4 space-y-1 text-sm text-[#6B7280]">
            <p>学期：{{ c.semesterName }}</p>
            <p class="flex items-center gap-1">
              <Users class="h-3.5 w-3.5" />
              报名人数：{{ c.enrolledCount }}/{{ c.maxCapacity }}
            </p>
          </div>
          <Button class="w-full" @click="router.push(`/teacher/courses/${c.id}/students`)">
            <Eye class="mr-2 h-4 w-4" />
            查看学生
          </Button>
        </CardContent>
      </Card>
    </div>
  </div>
</template>