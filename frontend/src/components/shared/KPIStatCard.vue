<script setup lang="ts">
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { ArrowUp, ArrowDown } from 'lucide-vue-next'
import type { Component } from 'vue'

interface Props {
  label: string
  value: number | string
  icon?: Component
  trend?: 'up' | 'down'
  trendValue?: string
  class?: string
}

defineProps<Props>()
</script>

<template>
  <Card :class="$props.class">
    <CardContent class="p-5">
      <div class="flex items-start justify-between">
        <div class="space-y-1">
          <p class="text-sm text-[#6B7280]">{{ label }}</p>
          <p class="text-3xl font-bold text-[#111827]">{{ value }}</p>
          <p v-if="trend && trendValue" class="flex items-center gap-1 text-sm" :class="trend === 'up' ? 'text-[#22C55E]' : 'text-[#EF4444]'">
            <ArrowUp v-if="trend === 'up'" class="h-3 w-3" />
            <ArrowDown v-else class="h-3 w-3" />
            {{ trendValue }}
          </p>
        </div>
        <div v-if="icon" class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF6FF]">
          <component :is="icon" class="h-5 w-5 text-[#2563EB]" />
        </div>
      </div>
    </CardContent>
  </Card>
</template>