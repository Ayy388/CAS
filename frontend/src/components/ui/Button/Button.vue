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

const base = 'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-medium transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0D9488] focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0'

const variants: Record<string, string> = {
  default: 'bg-[#0D9488] text-white shadow-sm hover:bg-[#0F766E] active:scale-[0.97]',
  destructive: 'bg-[#EF4444] text-white shadow-sm hover:bg-[#DC2626] active:scale-[0.97]',
  outline: 'border border-[#E5E0D8] bg-white text-[#111827] shadow-sm hover:bg-[#F3F0EB] hover:text-[#111827] active:scale-[0.97]',
  secondary: 'bg-[#F3F0EB] text-[#111827] hover:bg-[#E5E0D8] active:scale-[0.97]',
  ghost: 'text-[#6B7280] hover:bg-[#F3F0EB] hover:text-[#111827] active:scale-[0.97]',
  link: 'text-[#0D9488] underline-offset-4 hover:underline active:scale-[0.97]',
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