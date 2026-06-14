<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn(
  'border-b border-[#E5E0D8] text-left text-sm font-medium text-[#6B7280]',
  props.class,
))
</script>

<template>
  <thead :class="classes">
    <slot />
  </thead>
</template>