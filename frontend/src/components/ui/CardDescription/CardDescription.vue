<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn('text-sm text-[#6B7280]', props.class))
</script>

<template>
  <p :class="classes">
    <slot />
  </p>
</template>