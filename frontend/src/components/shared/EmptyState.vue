<script setup lang="ts">
import type { Component } from 'vue'
import { Inbox } from 'lucide-vue-next'

interface Props {
  icon?: Component
  title: string
  description?: string
  action?: { label: string; onClick: () => void }
  class?: string
}

defineProps<Props>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 text-center">
    <component :is="icon ?? Inbox" class="mb-4 h-12 w-12 text-[#D1D1D9]" />
    <p class="text-base font-medium text-[#6B6B7B]">{{ title }}</p>
    <p v-if="description" class="mt-1 text-sm text-[#9C9CAB]">{{ description }}</p>
    <button
      v-if="action"
      class="mt-4 text-sm font-medium text-[#2563EB] hover:underline"
      @click="action.onClick"
    >
      {{ action.label }}
    </button>
  </div>
</template>