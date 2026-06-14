<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Tabs } from '@/components/ui/Tabs'
import { Badge } from '@/components/ui/Badge'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import { offeringService } from '@/services/offering'
import { Search, Users } from 'lucide-vue-next'
import { COURSE_TYPE_MAP } from '@/types'
import type { CourseOffering } from '@/types'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()

const searchQuery = ref('')
const typeFilter = ref('all')
const loading = ref(true)
const courses = ref<CourseOffering[]>([])

const courseTypes = [
  { label: '全部', value: 'all' },
  { label: '通识课', value: 'ELECTIVE_GENERAL' },
  { label: '专业选修课', value: 'ELECTIVE_MAJOR' },
]

const filteredCourses = computed(() => {
  return courses.value.filter(c => {
    if (searchQuery.value && !c.courseName.includes(searchQuery.value) && !c.teacherName.includes(searchQuery.value)) {
      return false
    }
    if (typeFilter.value !== 'all' && c.courseType !== typeFilter.value) {
      return false
    }
    return true
  })
})

onMounted(async () => {
  try {
    const res = await offeringService.studentList()
    courses.value = res.items
  } catch {
    toast?.('error', '加载课程列表失败')
  } finally {
    loading.value = false
  }
})

function viewDetail(id: number) {
  router.push(`/student/courses/${id}`)
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#1A1A2E]">课程大厅</h1>
      <p class="mt-1 text-sm text-[#6B7280]">浏览可选课程，选择你感兴趣的课程</p>
    </div>

    <!-- Search & Filter -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="relative w-full sm:max-w-xs">
        <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#9CA3AF]" />
        <Input v-model="searchQuery" placeholder="搜索课程名称或教师..." class="pl-10" />
      </div>
      <Tabs v-model="typeFilter" :tabs="courseTypes" />
    </div>

    <!-- Course Grid -->
    <div v-if="loading" class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      <Skeleton v-for="i in 6" :key="i" class="h-48 rounded-2xl" />
    </div>

    <EmptyState
      v-else-if="filteredCourses.length === 0"
      title="暂无课程"
      description="当前学期还没有开放的选课活动"
    />

    <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      <Card
        v-for="course in filteredCourses"
        :key="course.id"
        class="card-hover cursor-pointer"
        @click="viewDetail(course.id)"
      >
        <CardContent class="p-5">
          <div class="mb-3 flex items-start justify-between">
            <h3 class="text-base font-semibold text-[#1A1A2E]">{{ course.courseName }}</h3>
            <Badge :variant="course.seatsRemaining > 0 ? 'default' : 'destructive'">
              {{ course.seatsRemaining > 0 ? '可报名' : '已满员' }}
            </Badge>
          </div>
          <div class="mb-3 space-y-1 text-sm text-[#6B7280]">
            <p>{{ COURSE_TYPE_MAP[course.courseType] }} · {{ course.teacherName }} · {{ course.credits }}学分 · {{ course.hours }}课时</p>
          </div>
          <div class="flex items-center justify-between pt-3 text-sm">
            <div class="flex items-center gap-1 text-[#6B7280]">
              <Users class="h-4 w-4" />
              <span>剩余 {{ course.seatsRemaining }}/{{ course.maxCapacity }} 名额</span>
            </div>
            <Button size="sm" :disabled="course.seatsRemaining <= 0" @click.stop="viewDetail(course.id)">
              {{ course.seatsRemaining > 0 ? '立即抢课' : '已满员' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>