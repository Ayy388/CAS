<script setup lang="ts">
import type { CourseOffering, CampaignStatus } from '@/types'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import StatusBadge from './StatusBadge.vue'
import { COURSE_TYPE_MAP } from '@/types'
import { Users } from 'lucide-vue-next'

interface Props {
  offering: CourseOffering
  campaignStatus: CampaignStatus
  hasEnrolled: boolean
  onEnroll?: (offeringId: number) => void
  onViewDetail?: (offeringId: number) => void
  class?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  enroll: [offeringId: number]
  viewDetail: [offeringId: number]
}>()
</script>

<template>
  <Card class="card-hover cursor-pointer overflow-hidden" @click="emit('viewDetail', offering.id)">
    <CardContent class="p-5">
      <!-- Header -->
      <div class="mb-3 flex items-start justify-between">
        <h3 class="text-base font-semibold tracking-tight text-[#0C0C0D]">{{ offering.courseName }}</h3>
        <StatusBadge v-if="hasEnrolled" status="ENROLLED" type="enrollment" />
        <Badge v-else-if="offering.seatsRemaining <= 0" variant="destructive">已满员</Badge>
      </div>

      <!-- Info -->
      <div class="mb-3 space-y-1 text-sm text-[#6B6B7B]">
        <p>
          {{ COURSE_TYPE_MAP[offering.courseType] }} ·
          {{ offering.teacherName }} ·
          {{ offering.credits }}学分 ·
          {{ offering.hours }}课时
        </p>
        <p v-if="offering.schedule" class="text-xs">{{ offering.schedule }}</p>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-3">
        <div class="flex items-center gap-1.5 text-sm text-[#6B6B7B]">
          <Users class="h-3.5 w-3.5" />
          <span>剩余 {{ offering.seatsRemaining }}/{{ offering.maxCapacity }}</span>
        </div>
        <Button
          size="sm"
          :disabled="offering.seatsRemaining <= 0 || hasEnrolled || campaignStatus !== 'ACTIVE'"
          @click.stop="emit('enroll', offering.id)"
        >
          {{ hasEnrolled ? '已选课' : offering.seatsRemaining <= 0 ? '已满员' : campaignStatus !== 'ACTIVE' ? '未开始' : '立即抢课' }}
        </Button>
      </div>
    </CardContent>
  </Card>
</template>