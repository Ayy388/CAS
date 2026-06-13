<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Label } from '@/components/ui/Label'
import { Select } from '@/components/ui/Select'
import { offeringService } from '@/services/offering'
import { semesterService } from '@/services/semester'
import { courseService } from '@/services/course'
import type { Semester, Course } from '@/types'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const emit = defineEmits<{ success: [] }>()
const loading = ref(false)
const semesters = ref<Semester[]>([])
const courses = ref<Course[]>([])

const form = ref({
  semesterId: undefined as number | undefined,
  courseId: undefined as number | undefined,
  teacherId: undefined as number | undefined,
  maxCapacity: 50,
  minEnrollment: 10,
  openGrade: '',
  openMajor: '',
  location: '',
  schedule: '',
})

const semesterOptions = computed(() =>
  semesters.value.map(s => ({ label: s.name, value: s.id }))
)
const courseOptions = computed(() =>
  courses.value.map(c => ({ label: `${c.code} ${c.name}`, value: c.id }))
)

onMounted(async () => {
  try {
    const [semRes, courseRes] = await Promise.all([
      semesterService.list(),
      courseService.list(),
    ])
    semesters.value = semRes.items
    courses.value = courseRes.items
  } catch {
    toast?.('error', '加载选项数据失败')
  }
})

async function handleSubmit() {
  loading.value = true
  try {
    await offeringService.adminCreate(form.value)
    toast?.('success', '开课创建成功')
    emit('success')
  } catch {
    toast?.('error', '创建失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="handleSubmit">
    <div class="space-y-2">
      <Label>学期</Label>
      <Select v-model="form.semesterId as any" :options="semesterOptions" placeholder="请选择学期" />
    </div>
    <div class="space-y-2">
      <Label>课程</Label>
      <Select v-model="form.courseId as any" :options="courseOptions" placeholder="请选择课程" />
    </div>
    <div class="space-y-2">
      <Label>教师 ID</Label>
      <Input v-model.number="form.teacherId as any" type="number" min="1" placeholder="输入教师用户 ID" />
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>最大容量</Label>
        <Input v-model.number="form.maxCapacity" type="number" min="1" />
      </div>
      <div class="space-y-2">
        <Label>最低人数</Label>
        <Input v-model.number="form.minEnrollment" type="number" min="1" />
      </div>
    </div>
    <div class="space-y-2">
      <Label>上课地点</Label>
      <Input v-model="form.location" placeholder="例如：教学楼A-201" />
    </div>
    <div class="space-y-2">
      <Label>上课时间</Label>
      <Input v-model="form.schedule" placeholder="例如：周一 3-4节" />
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>开放年级</Label>
        <Input v-model="form.openGrade" placeholder="留空表示全部" />
      </div>
      <div class="space-y-2">
        <Label>开放专业</Label>
        <Input v-model="form.openMajor" placeholder="留空表示全部" />
      </div>
    </div>
    <div class="flex justify-end pt-4">
      <Button type="submit" :disabled="loading">{{ loading ? '提交中...' : '提交' }}</Button>
    </div>
  </form>
</template>