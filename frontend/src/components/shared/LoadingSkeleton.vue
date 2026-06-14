<script setup lang="ts">
import { Skeleton } from '@/components/ui/Skeleton'

interface Props {
  variant?: 'card' | 'table' | 'detail'
  count?: number
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'card',
  count: 3,
})
</script>

<template>
  <div v-if="variant === 'card'" class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
    <div v-for="i in count" :key="i" class="rounded-xl border border-[#E8E8ED] bg-white p-5">
      <Skeleton class="mb-3 h-5 w-3/4" />
      <Skeleton class="mb-2 h-4 w-1/2" />
      <Skeleton class="mb-4 h-4 w-2/3" />
      <Skeleton class="h-10 w-full rounded-lg" />
    </div>
  </div>
  <div v-else-if="variant === 'table'" class="space-y-3">
    <Skeleton v-for="i in count" :key="i" class="h-12 w-full rounded-lg" />
  </div>
  <div v-else class="space-y-4">
    <Skeleton class="h-8 w-1/3" />
    <Skeleton class="h-4 w-2/3" />
    <Skeleton class="h-32 w-full rounded-lg" />
  </div>
</template>