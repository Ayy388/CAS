<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import { teacherService } from '@/services/teacher'
import type { StudentListItem } from '@/types'
import { ArrowLeft, Download } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const students = ref<StudentListItem[]>([])
const courseName = ref('')

onMounted(async () => {
  const offeringId = Number(route.params.id)
  if (!offeringId) { loading.value = false; return }
  try {
    const res = await teacherService.listStudents(offeringId)
    students.value = res.items
  } catch {
    toast?.('error', '加载学生列表失败')
  } finally {
    loading.value = false
  }
})

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
        <button class="text-[#6B6B7B] transition-all duration-150 hover:text-[#0C0C0D]" @click="router.back()">
          <ArrowLeft class="h-5 w-5" />
        </button>
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-[#0C0C0D]">{{ courseName || '学生名单' }}</h1>
          <p class="mt-1 text-sm text-[#6B6B7B]">已报名学生列表</p>
        </div>
      </div>
      <Button @click="handleExport">
        <Download class="mr-2 h-4 w-4" />
        导出 Excel
      </Button>
    </div>

    <Skeleton v-if="loading" variant="table" :count="8" />

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