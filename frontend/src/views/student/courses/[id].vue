<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCampaignStore } from '@/stores/campaign'
import { offeringService } from '@/services/offering'
import { enrollmentService } from '@/services/enrollment'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Separator } from '@/components/ui/Separator'
import { Skeleton } from '@/components/ui/Skeleton'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { ArrowLeft } from 'lucide-vue-next'
import type { CourseOffering } from '@/types'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const route = useRoute()
const campaign = useCampaignStore()

const loading = ref(true)
const offering = ref<CourseOffering | null>(null)
const hasEnrolled = ref(false)
const enrolling = ref(false)

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) {
    loading.value = false
    return
  }
  try {
    const [detail, enrollments] = await Promise.all([
      offeringService.studentDetail(id),
      enrollmentService.myEnrollments(),
    ])
    offering.value = detail
    hasEnrolled.value = enrollments.items.some(e => e.offeringId === id)
  } catch {
    toast?.('error', '加载课程详情失败')
  } finally {
    loading.value = false
  }
})

async function handleEnroll() {
  if (!offering.value) return
  enrolling.value = true
  try {
    await enrollmentService.enroll(offering.value.id)
    toast?.('success', '抢课成功！')
    hasEnrolled.value = true
  } catch {
    toast?.('error', '抢课失败，请稍后重试')
  } finally {
    enrolling.value = false
  }
}
</script>

<template>
  <div>
    <button class="mb-6 flex items-center gap-2 text-sm text-[#6B6B7B] transition-all duration-150 hover:text-[#0C0C0D]" @click="router.back()">
      <ArrowLeft class="h-4 w-4" />
      返回课程大厅
    </button>

    <Skeleton v-if="loading" class="h-64 w-full rounded-xl" />

    <div v-else-if="offering" class="grid gap-8 lg:grid-cols-3">
      <!-- Left: Course Details -->
      <div class="space-y-6 lg:col-span-2">
        <div>
          <div class="mb-2 flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-[#0C0C0D]">{{ offering.courseName }}</h1>
            <StatusBadge :status="offering.status" type="offering" />
          </div>
          <p class="text-sm text-[#6B6B7B]">{{ offering.teacherName }} · {{ offering.credits }}学分 · {{ offering.hours }}课时</p>
        </div>

        <Separator />

        <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div class="rounded-xl bg-[#F5F5F8] p-3">
            <p class="text-xs text-[#6B6B7B]">上课地点</p>
            <p class="mt-1 text-sm font-medium text-[#0C0C0D]">{{ offering.location || '待定' }}</p>
          </div>
          <div class="rounded-xl bg-[#F5F5F8] p-3">
            <p class="text-xs text-[#6B6B7B]">上课时间</p>
            <p class="mt-1 text-sm font-medium text-[#0C0C0D]">{{ offering.schedule || '待定' }}</p>
          </div>
          <div class="rounded-xl bg-[#F5F5F8] p-3">
            <p class="text-xs text-[#6B6B7B]">开放年级</p>
            <p class="mt-1 text-sm font-medium text-[#0C0C0D]">{{ offering.openGrade || '全部' }}</p>
          </div>
          <div class="rounded-xl bg-[#F5F5F8] p-3">
            <p class="text-xs text-[#6B6B7B]">开放专业</p>
            <p class="mt-1 text-sm font-medium text-[#0C0C0D]">{{ offering.openMajor || '全部' }}</p>
          </div>
        </div>

        <div>
          <h3 class="mb-2 text-base font-semibold tracking-tight text-[#0C0C0D]">课程简介</h3>
          <p class="text-sm leading-relaxed text-[#6B6B7B]">{{ offering.description || '暂无简介' }}</p>
        </div>
      </div>

      <!-- Right: Sticky Sidebar -->
      <div class="lg:col-span-1">
        <div class="sticky top-24 space-y-4">
          <Card class="overflow-hidden">
            <CardContent class="p-5">
              <div class="space-y-4">
                <div class="flex items-center justify-between text-sm">
                  <span class="text-[#6B6B7B]">总容量</span>
                  <span class="font-medium text-[#0C0C0D]">{{ offering.maxCapacity }}</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-[#6B6B7B]">已报名</span>
                  <span class="font-medium text-[#0C0C0D]">{{ offering.enrolledCount }}</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-[#6B6B7B]">剩余席位</span>
                  <span class="font-medium" :class="offering.seatsRemaining > 0 ? 'text-[#22C55E] font-semibold' : 'text-[#EF4444]'">
                    {{ offering.seatsRemaining }}
                  </span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-[#6B6B7B]">最低开课人数</span>
                  <span class="font-medium text-[#0C0C0D]">{{ offering.minEnrollment }}</span>
                </div>
              </div>
              <Separator class="my-4" />
              <Button
                class="w-full"
                :disabled="enrolling || offering.seatsRemaining <= 0 || hasEnrolled || campaign.currentCampaign?.status !== 'ACTIVE'"
                @click="handleEnroll"
              >
                {{ enrolling ? '抢课中...' : hasEnrolled ? '✓ 已选课' : offering.seatsRemaining <= 0 ? '已满员' : '立即抢课' }}
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>

    <div v-else class="py-16 text-center text-sm text-[#6B6B7B]">
      课程不存在
    </div>
  </div>
</template>