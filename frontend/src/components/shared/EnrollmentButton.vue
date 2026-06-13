<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/components/ui/Button'
import type { CampaignStatus } from '@/types'

interface Props {
  campaignStatus: CampaignStatus
  seatsRemaining: number
  hasEnrolled: boolean
  loading?: boolean
  onEnroll?: () => void
  class?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{ enroll: [] }>()

const displayText = computed(() => {
  if (props.hasEnrolled) return '✓ 已选课'
  if (props.loading) return '抢课中...'
  if (props.seatsRemaining <= 0) return '已满员'
  if (props.campaignStatus === 'PENDING') return '未开始'
  if (props.campaignStatus === 'ENDED') return '已结束'
  return '立即抢课'
})

const isDisabled = computed(() =>
  props.hasEnrolled || props.seatsRemaining <= 0 || props.campaignStatus !== 'ACTIVE' || props.loading
)

const variant = computed(() => {
  if (props.hasEnrolled) return 'secondary'
  if (props.seatsRemaining <= 0) return 'destructive'
  if (props.campaignStatus === 'ACTIVE') return 'default'
  return 'outline'
})
</script>

<template>
  <Button
    :variant="variant"
    :disabled="isDisabled"
    :class="props.class"
    @click="emit('enroll')"
  >
    {{ displayText }}
  </Button>
</template>