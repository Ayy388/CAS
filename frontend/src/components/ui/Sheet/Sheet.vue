<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'
import { X } from 'lucide-vue-next'

const props = defineProps<{
  open?: boolean
  side?: 'left' | 'right'
  title?: string
  class?: HTMLAttributes['class']
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const isOpen = computed({
  get: () => props.open ?? false,
  set: (v: boolean) => emit('update:open', v),
})

function close() {
  isOpen.value = false
}
</script>

<template>
  <Teleport to="body">
    <Transition name="sheet">
      <div v-if="isOpen" class="fixed inset-0 z-50 flex" :class="side === 'left' ? 'justify-start' : 'justify-end'">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="close" />
        <!-- Content -->
        <div
          :class="cn(
            'relative z-50 h-full w-full max-w-lg border-[#E5E7EB] bg-white shadow-xl',
            side === 'left' ? 'border-r' : 'border-l',
            props.class,
          )"
        >
          <div class="flex items-center justify-between border-b border-[#E5E7EB] px-6 py-4">
            <h2 v-if="title" class="text-lg font-semibold text-[#111827]">{{ title }}</h2>
            <button class="rounded-sm p-1 text-[#6B7280] hover:text-[#111827]" @click="close">
              <X class="h-5 w-5" />
            </button>
          </div>
          <div class="overflow-y-auto p-6">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.sheet-enter-active,
.sheet-leave-active {
  transition: all 0.3s ease;
}
.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}
.sheet-enter-from .relative,
.sheet-leave-to .relative {
  transform: translateX(100%);
}
</style>