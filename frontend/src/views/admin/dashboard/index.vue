<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Skeleton } from '@/components/ui/Skeleton'
import KPIStatCard from '@/components/shared/KPIStatCard.vue'
import BarChart from '@/components/charts/BarChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import { dashboardService } from '@/services/dashboard'
import type { DashboardStats, TopCourse, TrendPoint } from '@/types'
import { BookOpen, Users, GraduationCap, FileCheck } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)

const stats = ref<DashboardStats>({
  totalCourses: 0,
  totalTeachers: 0,
  totalStudents: 0,
  totalEnrollments: 0,
})

const topCourses = ref<TopCourse[]>([])
const trend = ref<TrendPoint[]>([])

async function loadData() {
  loading.value = true
  try {
    const [statsRes, topRes, trendRes] = await Promise.all([
      dashboardService.getStats(),
      dashboardService.getTopCourses(),
      dashboardService.getTrend(),
    ])
    stats.value = statsRes
    topCourses.value = topRes
    trend.value = trendRes
  } catch {
    toast?.('error', '加载看板数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#111827]">数据看板</h1>
      <p class="mt-1 text-sm text-[#6B7280]">系统运营数据概览</p>
    </div>

    <!-- KPI Cards -->
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <Skeleton v-if="loading" v-for="i in 4" :key="i" class="h-32 rounded-xl" />
      <template v-else>
        <KPIStatCard label="总课程数" :value="stats.totalCourses" :icon="BookOpen" />
        <KPIStatCard label="总教师数" :value="stats.totalTeachers" :icon="Users" />
        <KPIStatCard label="总学生数" :value="stats.totalStudents" :icon="GraduationCap" />
        <KPIStatCard label="总报名人数" :value="stats.totalEnrollments" :icon="FileCheck" />
      </template>
    </div>

    <!-- Charts -->
    <div class="grid gap-6 lg:grid-cols-2">
      <!-- Top Courses -->
      <Card>
        <CardContent class="p-5">
          <h3 class="mb-4 text-base font-semibold text-[#111827]">热门课程 TOP10</h3>
          <Skeleton v-if="loading" class="h-72 w-full rounded-xl" />
          <BarChart v-else :data="topCourses" />
        </CardContent>
      </Card>

      <!-- Trend -->
      <Card>
        <CardContent class="p-5">
          <h3 class="mb-4 text-base font-semibold text-[#111827]">抢课趋势</h3>
          <Skeleton v-if="loading" class="h-72 w-full rounded-xl" />
          <LineChart v-else :data="trend" />
        </CardContent>
      </Card>
    </div>
  </div>
</template>