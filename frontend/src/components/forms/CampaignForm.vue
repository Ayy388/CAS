<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Label } from '@/components/ui/Label'
import { Select } from '@/components/ui/Select'
import { campaignService } from '@/services/campaign'
import { semesterService } from '@/services/semester'
import type { Semester } from '@/types'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const emit = defineEmits<{ success: [] }>()
const loading = ref(false)
const semesters = ref<Semester[]>([])

const form = ref({
  name: '',
  semesterId: undefined as number | undefined,
  startTime: '',
  endTime: '',
})

const semesterOptions = computed(() =>
  semesters.value.map(s => ({ label: s.name, value: s.id }))
)

onMounted(async () => {
  try {
    const res = await semesterService.list()
    semesters.value = res.items
  } catch {
    toast?.('error', '加载学期数据失败')
  }
})

async function handleSubmit() {
  loading.value = true
  try {
    // Convert datetime-local (YYYY-MM-DDTHH:mm) to backend format (YYYY-MM-dd HH:mm:ss)
    const payload = {
      ...form.value,
      startTime: form.value.startTime.replace('T', ' ') + ':00',
      endTime: form.value.endTime.replace('T', ' ') + ':00',
    }
    await campaignService.adminCreate(payload)
    toast?.('success', '活动创建成功')
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
      <Label>活动名称</Label>
      <Input v-model="form.name" placeholder="例如：2025-2026 第一学期选课" />
    </div>
    <div class="space-y-2">
      <Label>所属学期</Label>
      <Select v-model="form.semesterId as any" :options="semesterOptions" placeholder="请选择学期" />
    </div>
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>开始时间</Label>
        <Input v-model="form.startTime" type="datetime-local" />
      </div>
      <div class="space-y-2">
        <Label>结束时间</Label>
        <Input v-model="form.endTime" type="datetime-local" />
      </div>
    </div>
    <div class="flex justify-end pt-4">
      <Button type="submit" :disabled="loading">{{ loading ? '提交中...' : '提交' }}</Button>
    </div>
  </form>
</template>