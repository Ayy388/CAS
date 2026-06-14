<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { CardHeader } from '@/components/ui/CardHeader'
import { CardTitle } from '@/components/ui/CardTitle'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Sheet } from '@/components/ui/Sheet'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import OfferingForm from '@/components/forms/OfferingForm.vue'
import { courseService } from '@/services/course'
import { offeringService } from '@/services/offering'
import type { Course, CourseOffering } from '@/types'
import { Plus } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const courses = ref<Course[]>([])
const allOfferings = ref<CourseOffering[]>([])
const selectedCourse = ref<Course | null>(null)
const showSheet = ref(false)

const filteredOfferings = computed(() => {
  if (!selectedCourse.value) return []
  return allOfferings.value.filter(o => o.courseId === selectedCourse.value!.id)
})

async function loadData() {
  loading.value = true
  try {
    const [courseRes, offeringRes] = await Promise.all([
      courseService.list(),
      offeringService.adminList(),
    ])
    courses.value = courseRes.items
    allOfferings.value = offeringRes.items
    if (courseRes.items.length > 0 && !selectedCourse.value) {
      selectedCourse.value = courseRes.items[0]
    }
  } catch {
    toast?.('error', '加载开课数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function handleCreate() {
  showSheet.value = true
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A2E]">开课管理</h1>
        <p class="mt-1 text-sm text-[#6B7280]">管理课程的开课配置</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="mr-2 h-4 w-4" />
        新建开课
      </Button>
    </div>

    <div class="grid gap-6 lg:grid-cols-3">
      <!-- Left: Course List -->
      <Card class="lg:col-span-1">
        <CardHeader>
          <CardTitle class="text-base">课程列表</CardTitle>
        </CardHeader>
        <CardContent class="p-0">
          <Skeleton v-if="loading" variant="table" :count="5" />
          <EmptyState
            v-else-if="courses.length === 0"
            title="暂无课程"
            description="请先在课程管理中创建课程"
          />
          <div v-else class="divide-y divide-[#E5E0D8]">
            <button
              v-for="c in courses"
              :key="c.id"
              class="w-full px-5 py-3 text-left text-sm transition-colors hover:bg-[#F8F6F3]"
              :class="selectedCourse?.id === c.id ? 'bg-[#F0FDFA]' : ''"
              @click="selectedCourse = c"
            >
              <p class="font-medium text-[#1A1A2E]">{{ c.name }}</p>
              <p class="mt-0.5 text-xs text-[#6B7280]">{{ c.code }}</p>
            </button>
          </div>
        </CardContent>
      </Card>

      <!-- Right: Offering Details -->
      <Card class="lg:col-span-2">
        <CardHeader>
          <CardTitle class="text-base">{{ selectedCourse ? `${selectedCourse.name} 的开课记录` : '请选择课程' }}</CardTitle>
        </CardHeader>
        <CardContent class="p-0">
          <EmptyState
            v-if="!selectedCourse"
            title="请从左侧选择一个课程"
            description="选择课程后查看其开课配置"
          />
          <Skeleton v-else-if="loading" variant="table" :count="3" />
          <EmptyState
            v-else-if="filteredOfferings.length === 0"
            title="暂无开课记录"
            description="点击新建开课按钮创建"
          />
          <Table v-else>
            <TableHeader>
              <TableRow>
                <TableHead>学期</TableHead>
                <TableHead>教师</TableHead>
                <TableHead>容量</TableHead>
                <TableHead>状态</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="o in filteredOfferings" :key="o.id">
                <TableCell>{{ o.semesterName }}</TableCell>
                <TableCell>{{ o.teacherName }}</TableCell>
                <TableCell>{{ o.maxCapacity }}</TableCell>
                <TableCell>
                  <Badge :variant="o.status === 'APPROVED' ? 'default' : 'secondary'">
                    {{ o.status }}
                  </Badge>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>

    <Sheet v-model:open="showSheet" title="新建开课">
      <OfferingForm @success="showSheet = false" />
    </Sheet>
  </div>
</template>