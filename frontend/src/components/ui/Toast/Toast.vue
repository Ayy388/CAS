<script setup lang="ts">
import { ref } from 'vue'
import { cn } from '@/lib/utils'
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-vue-next'

interface ToastData {
  id: number
  type: 'success' | 'error' | 'warning' | 'info'
  title: string
  description?: string
}

const toasts = ref<ToastData[]>([])
let nextId = 0

const iconColors: Record<string, string> = {
  success: 'text-[#22C55E]',
  error: 'text-[#EF4444]',
  warning: 'text-[#F59E0B]',
  info: 'text-[#2563EB]',
}

function addToast(type: ToastData['type'], title: string, description?: string) {
  const id = nextId++
  toasts.value.push({ id, type, title, description })
  setTimeout(() => removeToast(id), 4000)
}

function removeToast(id: number) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

function toast(type: ToastData['type'], title: string, description?: string) {
  addToast(type, title, description)
}

defineExpose({ toast })
</script>

<template>
  <Teleport to="body">
    <div class="fixed right-4 top-4 z-[100] flex flex-col gap-2">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="flex w-80 items-start gap-3 rounded-lg border border-[#E5E7EB] bg-white p-4 shadow-lg"
        >
          <CheckCircle v-if="t.type === 'success'" :class="cn('h-5 w-5 mt-0.5', iconColors[t.type])" />
          <XCircle v-else-if="t.type === 'error'" :class="cn('h-5 w-5 mt-0.5', iconColors[t.type])" />
          <AlertTriangle v-else-if="t.type === 'warning'" :class="cn('h-5 w-5 mt-0.5', iconColors[t.type])" />
          <Info v-else :class="cn('h-5 w-5 mt-0.5', iconColors[t.type])" />
          <div class="flex-1">
            <p class="text-sm font-medium text-[#111827]">{{ t.title }}</p>
            <p v-if="t.description" class="mt-0.5 text-xs text-[#6B7280]">{{ t.description }}</p>
          </div>
          <button class="text-[#9CA3AF] hover:text-[#6B7280]" @click="removeToast(t.id)">
            <X class="h-4 w-4" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  transition: all 0.3s ease;
}
.toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>