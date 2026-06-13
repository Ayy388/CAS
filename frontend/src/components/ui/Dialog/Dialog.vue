<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'
import { X } from 'lucide-vue-next'

const props = defineProps<{
  open?: boolean
  class?: HTMLAttributes['class']
  title?: string
  description?: string
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
    <Transition name="dialog">
      <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="close" />
        <!-- Content -->
        <div :class="cn(
          'relative z-50 w-full max-w-md rounded-xl border border-[#E5E7EB] bg-white p-6 shadow-lg',
          props.class,
        )">
          <button
            class="absolute right-4 top-4 rounded-sm p-1 text-[#6B7280] hover:text-[#111827]"
            @click="close"
          >
            <X class="h-4 w-4" />
          </button>
          <h2 v-if="title" class="text-lg font-semibold text-[#111827]">{{ title }}</h2>
          <p v-if="description" class="mt-1 text-sm text-[#6B7280]">{{ description }}</p>
          <div class="mt-4">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}
.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}
</style>