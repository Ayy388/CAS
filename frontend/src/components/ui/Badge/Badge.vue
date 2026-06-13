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
  default: 'border-transparent bg-[#EFF6FF] text-[#2563EB] hover:bg-[#DBEAFE]',
  secondary: 'border-transparent bg-[#F3F4F6] text-[#6B7280] hover:bg-[#E5E7EB]',
  destructive: 'border-transparent bg-[#FEF2F2] text-[#EF4444] hover:bg-[#FEE2E2]',
  outline: 'text-[#6B7280]',
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