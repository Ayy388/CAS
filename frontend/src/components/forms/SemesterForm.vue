<script setup lang="ts">
import { ref, inject, watch } from 'vue'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Label } from '@/components/ui/Label'
import { Select } from '@/components/ui/Select'
import { semesterService } from '@/services/semester'
import type { Semester } from '@/types'

const props = withDefaults(defineProps<{
  semester?: Semester | null
}>(), {
  semester: null,
})

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const emit = defineEmits<{ success: [] }>()
const loading = ref(false)
const isEdit = ref(false)

const form = ref({
  name: '',
  academicYear: '',
  semesterType: 'FIRST' as string,
  startDate: '',
  endDate: '',
})

const semesterTypeOptions = [
  { label: '第一学期', value: 'FIRST' },
  { label: '第二学期', value: 'SECOND' },
]

watch(() => props.semester, (semester) => {
  if (semester) {
    isEdit.value = true
    form.value = {
      name: semester.name,
      academicYear: semester.academicYear,
      semesterType: semester.semesterType,
      startDate: semester.startDate,
      endDate: semester.endDate,
    }
  } else {
    isEdit.value = false
    form.value = { name: '', academicYear: '', semesterType: 'FIRST', startDate: '', endDate: '' }
  }
}, { immediate: true })

async function handleSubmit() {
  loading.value = true
  try {
    const payload = {
      ...form.value,
      semesterType: form.value.semesterType as 'FIRST' | 'SECOND',
    }
    if (isEdit.value && props.semester) {
      await semesterService.update(props.semester.id, payload)
      toast?.('success', '学期更新成功')
    } else {
      await semesterService.create(payload)
      toast?.('success', '学期创建成功')
    }
    emit('success')
  } catch {
    toast?.('error', `${isEdit.value ? '更新' : '创建'}失败，请重试`)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="handleSubmit">
    <div class="space-y-2">
      <Label>学期名称</Label>
      <Input v-model="form.name" placeholder="例如：2025-2026 第一学期" />
    </div>
    <div class="space-y-2">
      <Label>学年</Label>
      <Input v-model="form.academicYear" placeholder="例如：2025-2026" />
    </div>
    <div class="space-y-2">
      <Label>学期</Label>
      <Select v-model="form.semesterType" :options="semesterTypeOptions" />
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>开始日期</Label>
        <Input v-model="form.startDate" type="date" />
      </div>
      <div class="space-y-2">
        <Label>结束日期</Label>
        <Input v-model="form.endDate" type="date" />
      </div>
    </div>
    <div class="flex justify-end pt-4">
      <Button type="submit" :disabled="loading">{{ loading ? '提交中...' : (isEdit ? '更新' : '提交') }}</Button>
    </div>
  </form>
</template>