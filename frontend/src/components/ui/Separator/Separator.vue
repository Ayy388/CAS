<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(defineProps<{
  orientation?: 'horizontal' | 'vertical'
  decorative?: boolean
  class?: HTMLAttributes['class']
}>(), {
  orientation: 'horizontal',
  decorative: true,
})

const classes = computed(() => cn(
  'shrink-0 bg-[#E8E8ED]',
  props.orientation === 'horizontal' ? 'h-[1px] w-full' : 'h-full w-[1px]',
  props.class,
))
</script>

<template>
  <hr v-if="orientation === 'horizontal'" :class="classes" />
  <div v-else :class="classes" />
</template>