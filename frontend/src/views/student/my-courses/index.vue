<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Button } from '@/components/ui/Button'
import { Dialog } from '@/components/ui/Dialog'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import ErrorState from '@/components/shared/ErrorState.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { enrollmentService } from '@/services/enrollment'
import { useCampaignStore } from '@/stores/campaign'
import type { Enrollment } from '@/types'
import { BookOpen, Clock, MapPin, Trash2 } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const campaign = useCampaignStore()

const loading = ref(true)
const enrollments = ref<Enrollment[]>([])
const error = ref(false)
const showDropDialog = ref(false)
const droppingEnrollment = ref<Enrollment | null>(null)

async function loadData() {
  error.value = false
  loading.value = true
  try {
    const res = await enrollmentService.myEnrollments()
    enrollments.value = res.items
  } catch {
    error.value = true
    toast?.('error', '加载课程列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function handleDropClick(enrollment: Enrollment) {
  if (campaign.currentCampaign?.status === 'ENDED') {
    toast?.('error', '当前已超过退课时间')
    return
  }
  droppingEnrollment.value = enrollment
  showDropDialog.value = true
}

async function handleDropConfirm() {
  if (!droppingEnrollment.value) return
  showDropDialog.value = false
  try {
    await enrollmentService.drop(droppingEnrollment.value.id)
    toast?.('success', '退课成功')
    enrollments.value = enrollments.value.filter(e => e.id !== droppingEnrollment.value!.id)
  } catch {
    toast?.('error', '退课失败，请重试')
  } finally {
    droppingEnrollment.value = null
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#1A1A2E]">我的课程</h1>
      <p class="mt-1 text-sm text-[#6B7280]">查看已选课程及其审核状态</p>
    </div>

    <Skeleton v-if="loading" variant="card" :count="3" />

    <ErrorState v-else-if="error" message="加载失败，请重试" :on-retry="loadData" />

    <EmptyState
      v-else-if="enrollments.length === 0"
      title="暂无已选课程"
      description="去课程大厅选课吧"
      :action="{ label: '去选课', onClick: () => router.push('/student/courses') }"
    />

    <div v-else class="space-y-4">
      <Card v-for="(e, index) in enrollments" :key="e.id" class="stagger-item" :style="{ 'animation-delay': `${index * 0.05}s` }">
        <CardContent class="p-5">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="mb-2 flex items-center gap-2">
                <h3 class="text-base font-semibold text-[#1A1A2E]">{{ e.offeringName }}</h3>
                <StatusBadge :status="e.status" type="enrollment" />
              </div>
              <div class="flex flex-wrap gap-4 text-sm text-[#6B7280]">
                <span class="flex items-center gap-1">
                  <BookOpen class="h-3.5 w-3.5" />
                  {{ e.teacherName || '未知教师' }}
                </span>
                <span class="flex items-center gap-1">
                  <Clock class="h-3.5 w-3.5" />
                  学分
                </span>
                <span class="flex items-center gap-1">
                  <MapPin class="h-3.5 w-3.5" />
                  {{ e.location || '待定' }}
                </span>
              </div>
              <p class="mt-2 text-xs text-[#9CA3AF]">报名时间：{{ e.enrolledAt }}</p>
            </div>
            <Button
              variant="outline"
              size="sm"
              class="text-[#EF4444]"
              :disabled="droppingEnrollment?.id === e.id || campaign.currentCampaign?.status === 'ENDED'"
              @click="handleDropClick(e)"
            >
              <Trash2 class="h-4 w-4" />
              {{ droppingEnrollment?.id === e.id ? '退课中...' : '退课' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <Dialog v-model:open="showDropDialog" title="确认退课" :description="`确定要退掉「${droppingEnrollment?.offeringName || ''}」吗？`">
      <div class="flex justify-end gap-3">
        <Button variant="outline" @click="showDropDialog = false">取消</Button>
        <Button variant="destructive" @click="handleDropConfirm">确认退课</Button>
      </div>
    </Dialog>
  </div>
</template>