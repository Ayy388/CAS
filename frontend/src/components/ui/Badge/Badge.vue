<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(defineProps<{
  variant?: 'default' | 'secondary' | 'destructive' | 'outline'
  class?: HTMLAttributes['class']
}>(), {
  variant: 'default',
})

const variants: Record<string, string> = {
  default: 'border-transparent bg-[#EFF2FF] text-[#2563EB] hover:bg-[#DBEAFE]',
  secondary: 'border-transparent bg-[#F5F5F8] text-[#6B6B7B] hover:bg-[#E8E8ED]',
  destructive: 'border-transparent bg-[#FEF2F2] text-[#EF4444] hover:bg-[#FEE2E2]',
  outline: 'text-[#6B6B7B]',
}

const classes = computed(() => cn(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors',
  variants[props.variant],
  props.class,
))
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>