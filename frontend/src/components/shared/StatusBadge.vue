<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/components/ui/Badge'
import {
  CAMPAIGN_STATUS_MAP,
  OFFERING_STATUS_MAP,
  ENROLLMENT_STATUS_MAP,
} from '@/types'
import type { CampaignStatus, OfferingStatus, EnrollmentStatus } from '@/types'

type BadgeStatus = CampaignStatus | OfferingStatus | EnrollmentStatus

const props = withDefaults(defineProps<{
  status: BadgeStatus
  type?: 'campaign' | 'offering' | 'enrollment'
  class?: string
}>(), {
  type: 'campaign',
})

const colorMap: Record<string, 'default' | 'secondary' | 'destructive'> = {
  ACTIVE: 'default',       // blue
  PENDING: 'secondary',    // gray
  ENDED: 'secondary',      // gray
  ENROLLED: 'default',     // blue
  APPROVED: 'default',     // green in default
  REJECTED: 'destructive', // red
  FULL: 'destructive',     // red
}

const labelMap: Record<string, string> = {
  ...CAMPAIGN_STATUS_MAP,
  ...OFFERING_STATUS_MAP,
  ...ENROLLMENT_STATUS_MAP,
  FULL: '已满员',
}

const label = computed(() => labelMap[props.status] ?? props.status)
const variant = computed(() => colorMap[props.status] ?? 'secondary')
</script>

<template>
  <Badge :variant="variant">{{ label }}</Badge>
</template>