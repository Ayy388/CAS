<script setup lang="ts">
import { type HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

const props = defineProps<{
  src?: string
  alt?: string
  fallback?: string
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <div :class="cn('relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full', props.class)">
    <img v-if="src" :src="src" :alt="alt" class="h-full w-full object-cover" />
    <span
      v-else
      class="flex h-full w-full items-center justify-center bg-[#EFF6FF] text-sm font-medium text-[#2563EB]"
    >
      {{ fallback ?? 'U' }}
    </span>
  </div>
</template>