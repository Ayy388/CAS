<script setup lang="ts">
import { computed, type HTMLAttributes } from 'vue'
import { cn } from '@/lib/utils'

interface TabItem {
  label: string
  value: string
}

const props = defineProps<{
  tabs: TabItem[]
  modelValue?: string
  class?: HTMLAttributes['class']
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const activeValue = computed({
  get: () => props.modelValue ?? props.tabs[0]?.value ?? '',
  set: (v: string) => emit('update:modelValue', v),
})
</script>

<template>
  <div :class="cn('inline-flex items-center rounded-lg bg-[#F3F4F6] p-1', props.class)">
    <button
      v-for="tab in tabs"
      :key="tab.value"
      class="rounded-md px-4 py-2 text-sm font-medium transition-all"
      :class="activeValue === tab.value
        ? 'bg-white text-[#111827] shadow-sm'
        : 'text-[#6B7280] hover:text-[#111827]'"
      @click="activeValue = tab.value"
    >
      {{ tab.label }}
    </button>
  </div>
</template>