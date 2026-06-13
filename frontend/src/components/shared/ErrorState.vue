<script setup lang="ts">
import { Button } from '@/components/ui/Button'
import { AlertTriangle } from 'lucide-vue-next'

interface Props {
  message?: string
  onRetry?: () => void
  class?: string
}

defineProps<Props>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 text-center">
    <AlertTriangle class="mb-4 h-12 w-12 text-[#F59E0B]" />
    <p class="text-base font-medium text-[#6B7280]">{{ message ?? '加载失败，请稍后重试' }}</p>
    <Button v-if="onRetry" variant="outline" class="mt-4" @click="onRetry">
      重新加载
    </Button>
  </div>
</template>