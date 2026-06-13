<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn(
  'border-b border-[#E5E7EB] transition-colors hover:bg-[#F9FAFB] data-[state=selected]:bg-[#F3F4F6]',
  props.class,
))
</script>

<template>
  <tr :class="classes">
    <slot />
  </tr>
</template>