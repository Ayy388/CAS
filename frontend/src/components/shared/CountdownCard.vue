<script setup lang="ts">
import { computed } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { CAMPAIGN_STATUS_MAP } from '@/types'
import type { CampaignStatus } from '@/types'
import { Clock } from 'lucide-vue-next'

interface Props {
  campaignName: string
  status: CampaignStatus
  endTime?: string
  startTime?: string
  timeRemaining?: number
  class?: string
}

const props = defineProps<Props>()

const statusLabel = computed(() => CAMPAIGN_STATUS_MAP[props.status] ?? props.status)

function formatCountdown(seconds: number): string {
  if (seconds <= 0) return ''
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (d > 0) return `${d}天${h}小时${m}分钟${s}秒`
  if (h > 0) return `${h}小时${m}分钟${s}秒`
  if (m > 0) return `${m}分钟${s}秒`
  return `${s}秒`
}

const countdownText = computed(() => {
  if (props.timeRemaining === undefined || props.timeRemaining <= 0) return ''
  return props.status === 'ACTIVE'
    ? `距离结束：${formatCountdown(props.timeRemaining)}`
    : `距离开始：${formatCountdown(props.timeRemaining)}`
})
</script>

<template>
  <Card :class="props.class">
    <CardContent class="p-5">
      <div class="flex items-start gap-4">
        <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#F0FDFA]">
          <Clock class="h-6 w-6 text-[#0D9488]" />
        </div>
        <div class="flex-1">
          <p class="text-sm font-medium text-[#6B7280]">{{ campaignName }}</p>
          <p class="mt-1 font-mono text-2xl font-bold tracking-tight text-[#1A1A2E]">
            {{ status === 'ACTIVE' ? '进行中' : statusLabel }}
          </p>
          <p v-if="countdownText" class="mt-1 text-sm font-medium text-[#0D9488]">
            {{ countdownText }}
          </p>
        </div>
      </div>
    </CardContent>
  </Card>
</template>