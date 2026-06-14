<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn('rounded-2xl border border-[#E5E0D8] bg-white/80 text-[#1A1A2E] shadow-[0_2px_8px_rgba(13,148,136,0.04),0_1px_2px_rgba(0,0,0,0.03)] backdrop-blur-[12px]', props.class))
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>