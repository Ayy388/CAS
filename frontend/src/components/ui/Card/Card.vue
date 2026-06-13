<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn('rounded-xl border border-[#E5E7EB] bg-white text-[#111827] shadow-sm', props.class))
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>