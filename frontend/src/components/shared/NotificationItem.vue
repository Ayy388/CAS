<script setup lang="ts">
import type { Notification } from '@/types'
import { CheckCircle, XCircle, Info } from 'lucide-vue-next'

interface Props {
  notification: Notification
  onMarkRead?: (id: number) => void
}

defineProps<Props>()
const emit = defineEmits<{ markRead: [id: number] }>()

const iconMap: Record<string, any> = {
  APPROVED: CheckCircle,
  REJECTED: XCircle,
  SYSTEM: Info,
}

const colorMap: Record<string, string> = {
  APPROVED: 'text-[#22C55E]',
  REJECTED: 'text-[#EF4444]',
  SYSTEM: 'text-[#2563EB]',
}
</script>

<template>
  <div
    class="flex items-start gap-4 rounded-lg border border-[#E5E7EB] p-4 transition-colors"
    :class="!notification.read ? 'bg-[#F9FAFB]' : 'bg-white'"
  >
    <component
      :is="iconMap[notification.type] ?? Info"
      :class="['mt-0.5 h-5 w-5', colorMap[notification.type] ?? 'text-[#6B7280]']"
    />
    <div class="flex-1">
      <div class="flex items-center justify-between">
        <p class="text-sm font-medium text-[#111827]">{{ notification.title }}</p>
        <button
          v-if="!notification.read"
          class="text-xs text-[#2563EB] hover:underline"
          @click="emit('markRead', notification.id)"
        >
          标为已读
        </button>
      </div>
      <p class="mt-1 text-sm text-[#6B7280]">{{ notification.content }}</p>
      <p class="mt-1 text-xs text-[#9CA3AF]">{{ notification.createdAt }}</p>
    </div>
  </div>
</template>