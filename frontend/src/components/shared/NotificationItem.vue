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
    class="flex items-start gap-4 rounded-2xl border border-[#E5E0D8] p-4 transition-all duration-200"
    :class="!notification.read ? 'bg-[#F3F0EB]' : 'bg-white/80'"
  >
    <component
      :is="iconMap[notification.type] ?? Info"
      :class="['mt-0.5 h-5 w-5', colorMap[notification.type] ?? 'text-[#6B7280]']"
    />
    <div class="flex-1">
      <div class="flex items-center justify-between">
        <p class="text-sm font-medium text-[#1A1A2E]">{{ notification.title }}</p>
        <button
          v-if="!notification.read"
          class="text-xs font-medium text-[#0D9488] hover:text-[#0F766E] transition-colors"
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