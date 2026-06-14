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
  <Card :class="[$props.class, 'overflow-hidden']">
    <CardContent class="p-5">
      <div class="flex items-start justify-between">
        <div class="space-y-1.5">
          <p class="text-sm font-medium text-[#6B6B7B]">{{ label }}</p>
          <p class="text-3xl font-bold tracking-tight text-[#0C0C0D]">{{ value }}</p>
          <p v-if="trend && trendValue" class="flex items-center gap-1 text-sm font-medium" :class="trend === 'up' ? 'text-[#22C55E]' : 'text-[#EF4444]'">
            <ArrowUp v-if="trend === 'up'" class="h-3.5 w-3.5" />
            <ArrowDown v-else class="h-3.5 w-3.5" />
            {{ trendValue }}
          </p>
        </div>
        <div v-if="icon" class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF2FF]">
          <component :is="icon" class="h-5 w-5 text-[#2563EB]" />
        </div>
      </div>
    </CardContent>
  </Card>
</template>