<script setup lang="ts">
import { type HTMLAttributes, computed, ref, onMounted, onUnmounted } from 'vue'
import { Check, ChevronDown } from 'lucide-vue-next'

interface SelectOption {
  label: string
  value: string
}

const props = defineProps<{
  options: SelectOption[]
  modelValue?: string
  placeholder?: string
  class?: HTMLAttributes['class']
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const isOpen = ref(false)
const selectedLabel = computed(() => {
  const opt = props.options.find(o => o.value === props.modelValue)
  return opt?.label ?? props.placeholder ?? '请选择'
})

function toggle() {
  isOpen.value = !isOpen.value
}

function select(val: string) {
  emit('update:modelValue', val)
  isOpen.value = false
}

function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('[data-select]')) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
  <div data-select class="relative" :class="props.class">
    <button
      class="flex h-10 w-full items-center justify-between rounded-lg border border-[#E5E7EB] bg-white px-3 py-2 text-sm text-[#111827] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB]"
      @click="toggle"
    >
      <span :class="modelValue ? 'text-[#111827]' : 'text-[#9CA3AF]'">{{ selectedLabel }}</span>
      <ChevronDown class="h-4 w-4 text-[#6B7280]" :class="{ 'rotate-180': isOpen }" />
    </button>
    <Transition name="select-dropdown">
      <div
        v-if="isOpen"
        class="absolute z-50 mt-1 w-full rounded-lg border border-[#E5E7EB] bg-white py-1 shadow-lg"
      >
        <button
          v-for="opt in options"
          :key="opt.value"
          class="flex w-full items-center px-3 py-2 text-sm text-[#111827] hover:bg-[#F3F4F6]"
          @click="select(opt.value)"
        >
          <Check
            v-if="opt.value === modelValue"
            class="mr-2 h-4 w-4 text-[#2563EB]"
          />
          <span v-else class="mr-2 inline-block w-4" />
          {{ opt.label }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.select-dropdown-enter-active,
.select-dropdown-leave-active {
  transition: all 0.15s ease;
}
.select-dropdown-enter-from,
.select-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>