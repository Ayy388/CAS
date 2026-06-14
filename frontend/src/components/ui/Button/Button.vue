<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(defineProps<{
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
  size?: 'default' | 'sm' | 'lg' | 'icon'
  class?: HTMLAttributes['class']
}>(), {
  variant: 'default',
  size: 'default',
})

const base = 'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-medium transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0'

const variants: Record<string, string> = {
  default: 'bg-[#2563EB] text-white shadow-sm hover:bg-[#1D4ED8] active:scale-[0.98] active:bg-[#1E40AF]',
  destructive: 'bg-[#EF4444] text-white shadow-sm hover:bg-[#DC2626] active:scale-[0.98]',
  outline: 'border border-[#E8E8ED] bg-white text-[#0C0C0D] shadow-sm hover:bg-[#F5F5F8] active:scale-[0.98]',
  secondary: 'bg-[#F5F5F8] text-[#0C0C0D] shadow-sm hover:bg-[#E8E8ED] active:scale-[0.98]',
  ghost: 'text-[#6B6B7B] hover:bg-[#F5F5F8] hover:text-[#0C0C0D] active:scale-[0.98]',
  link: 'text-[#2563EB] underline-offset-4 hover:underline',
}

const sizes: Record<string, string> = {
  default: 'h-10 px-4 py-2',
  sm: 'h-9 rounded-md px-3',
  lg: 'h-11 rounded-lg px-8',
  icon: 'h-10 w-10',
}

const classes = computed(() => cn(base, variants[props.variant], sizes[props.size], props.class))
</script>

<template>
  <button :class="classes">
    <slot />
  </button>
</template>