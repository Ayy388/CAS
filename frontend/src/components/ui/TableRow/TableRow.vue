<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn(
  'border-b border-[#E8E8ED] transition-colors hover:bg-[#F5F5F8] data-[state=selected]:bg-[#F5F5F8]',
  props.class,
))
</script>

<template>
  <tr :class="classes">
    <slot />
  </tr>
</template>