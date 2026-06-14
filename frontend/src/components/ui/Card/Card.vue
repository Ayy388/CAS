<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn('rounded-xl border border-[#E8E8ED] bg-white text-[#0C0C0D] shadow-sm', props.class))
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>