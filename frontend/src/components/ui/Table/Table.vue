<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{ class?: HTMLAttributes['class'] }>()
const classes = computed(() => cn('w-full', props.class))
</script>

<template>
  <table :class="classes">
    <slot />
  </table>
</template>