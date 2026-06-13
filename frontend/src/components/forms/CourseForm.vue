<script setup lang="ts">
import { ref, inject, watch } from 'vue'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Label } from '@/components/ui/Label'
import { Select } from '@/components/ui/Select'
import { courseService } from '@/services/course'
import type { Course } from '@/types'

const props = withDefaults(defineProps<{
  course?: Course | null
}>(), {
  course: null,
})

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const emit = defineEmits<{ success: [] }>()
const loading = ref(false)
const isEdit = ref(false)

const form = ref({
  code: '',
  name: '',
  type: 'ELECTIVE_GENERAL' as string,
  credits: 2,
  hours: 32,
  description: '',
})

const typeOptions = [
  { label: '通识课', value: 'ELECTIVE_GENERAL' },
  { label: '专业选修课', value: 'ELECTIVE_MAJOR' },
  { label: '必修课', value: 'REQUIRED' },
]

watch(() => props.course, (course) => {
  if (course) {
    isEdit.value = true
    form.value = {
      code: course.code,
      name: course.name,
      type: course.type,
      credits: course.credits,
      hours: course.hours,
      description: course.description ?? '',
    }
  } else {
    isEdit.value = false
    form.value = { code: '', name: '', type: 'ELECTIVE_GENERAL', credits: 2, hours: 32, description: '' }
  }
}, { immediate: true })

async function handleSubmit() {
  loading.value = true
  try {
    const payload = {
      ...form.value,
      type: form.value.type as 'REQUIRED' | 'ELECTIVE_MAJOR' | 'ELECTIVE_GENERAL',
    }
    if (isEdit.value && props.course) {
      await courseService.update(props.course.id, payload)
      toast?.('success', '课程更新成功')
    } else {
      await courseService.create(payload)
      toast?.('success', '课程创建成功')
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
      <Label>课程编号</Label>
      <Input v-model="form.code" placeholder="例如：CS101" />
    </div>
    <div class="space-y-2">
      <Label>课程名称</Label>
      <Input v-model="form.name" placeholder="例如：影视鉴赏" />
    </div>
    <div class="space-y-2">
      <Label>课程类型</Label>
      <Select v-model="form.type" :options="typeOptions" />
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>学分</Label>
        <Input v-model.number="form.credits" type="number" min="1" max="10" />
      </div>
      <div class="space-y-2">
        <Label>课时</Label>
        <Input v-model.number="form.hours" type="number" min="1" max="100" />
      </div>
    </div>
    <div class="space-y-2">
      <Label>课程简介</Label>
      <textarea
        v-model="form.description"
        class="flex min-h-[80px] w-full rounded-lg border border-[#E5E7EB] bg-white px-3 py-2 text-sm placeholder:text-[#9CA3AF] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB]"
        placeholder="课程简介"
      />
    </div>
    <div class="flex justify-end pt-4">
      <Button type="submit" :disabled="loading">{{ loading ? '提交中...' : (isEdit ? '更新' : '提交') }}</Button>
    </div>
  </form>
</template>