<script setup lang="ts">
import { type HTMLAttributes, computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(defineProps<{
  type?: 'text' | 'email' | 'password' | 'number' | 'search'
  placeholder?: string
  modelValue?: string | number
  class?: HTMLAttributes['class']
}>(), {
  type: 'text',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const classes = computed(() => cn(
  'flex h-10 w-full rounded-xl border border-[#D6D0C8] bg-white px-3 py-2 text-sm text-[#1A1A2E] placeholder:text-[#9CA3AF] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0D9488] focus-visible:ring-offset-1 disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200',
  props.class,
))
</script>

<template>
  <input
    :type="type"
    :placeholder="placeholder"
    :value="modelValue"
    :class="classes"
    @input="onInput"
  />
</template>