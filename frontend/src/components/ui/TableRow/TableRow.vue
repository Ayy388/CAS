<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn(
  'border-b border-[#E5E0D8] transition-colors hover:bg-[#F3F0EB]/50 data-[state=selected]:bg-[#F3F0EB]',
  props.class,
))
</script>

<template>
  <tr :class="classes">
    <slot />
  </tr>
</template>