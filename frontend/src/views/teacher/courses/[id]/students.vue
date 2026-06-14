<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import ErrorState from '@/components/shared/ErrorState.vue'
import { teacherService } from '@/services/teacher'
import type { StudentListItem } from '@/types'
import { ArrowLeft, Download } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const error = ref(false)
const students = ref<StudentListItem[]>([])
const courseName = ref('')

async function loadData() {
  const offeringId = Number(route.params.id)
  if (!offeringId) { loading.value = false; return }
  loading.value = true
  error.value = false
  try {
    const res = await teacherService.listStudents(offeringId)
    students.value = res.items
  } catch {
    error.value = true
    toast?.('error', '加载学生列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function handleExport() {
  const offeringId = Number(route.params.id)
  if (!offeringId) return
  window.open(teacherService.getExportUrl(offeringId), '_blank')
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <button class="text-[#6B7280] hover:text-[#1A1A2E]" @click="router.back()">
          <ArrowLeft class="h-5 w-5" />
        </button>
        <div>
          <h1 class="text-2xl font-bold text-[#1A1A2E]">{{ courseName || '学生名单' }}</h1>
          <p class="mt-1 text-sm text-[#6B7280]">已报名学生列表</p>
        </div>
      </div>
      <Button @click="handleExport">
        <Download class="mr-2 h-4 w-4" />
        导出 Excel
      </Button>
    </div>

    <Skeleton v-if="loading" variant="table" :count="8" />

    <ErrorState v-else-if="error" message="加载失败，请重试" :on-retry="loadData" />

    <EmptyState
      v-else-if="students.length === 0"
      title="暂无学生报名"
      description="还没有学生报名该课程"
    />

    <Card v-else>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>学号</TableHead>
              <TableHead>姓名</TableHead>
              <TableHead>学院</TableHead>
              <TableHead>专业</TableHead>
              <TableHead>报名时间</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="s in students" :key="s.studentId">
              <TableCell class="font-medium">{{ s.studentNumber }}</TableCell>
              <TableCell>{{ s.studentName }}</TableCell>
              <TableCell>{{ s.department }}</TableCell>
              <TableCell>{{ s.major }}</TableCell>
              <TableCell>{{ s.enrolledAt }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>