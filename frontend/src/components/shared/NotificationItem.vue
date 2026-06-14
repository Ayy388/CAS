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
    class="flex items-start gap-4 rounded-xl border border-[#E8E8ED] p-4 transition-all duration-150"
    :class="!notification.read ? 'bg-[#F5F5F8]' : 'bg-white'"
  >
    <component
      :is="iconMap[notification.type] ?? Info"
      :class="['mt-0.5 h-5 w-5', colorMap[notification.type] ?? 'text-[#6B6B7B]']"
    />
    <div class="flex-1">
      <div class="flex items-center justify-between">
        <p class="text-sm font-medium text-[#0C0C0D]">{{ notification.title }}</p>
        <button
          v-if="!notification.read"
          class="text-xs font-medium text-[#2563EB] transition-colors hover:text-[#1D4ED8]"
          @click="emit('markRead', notification.id)"
        >
          标为已读
        </button>
      </div>
      <p class="mt-1 text-sm text-[#6B6B7B]">{{ notification.content }}</p>
      <p class="mt-1 text-xs text-[#9C9CAB]">{{ notification.createdAt }}</p>
    </div>
  </div>
</template>