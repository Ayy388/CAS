<script setup lang="ts">
import { ref, inject, watch } from 'vue'
import { Tabs } from '@/components/ui/Tabs'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import NotificationItem from '@/components/shared/NotificationItem.vue'
import { notificationService } from '@/services/notification'
import type { Notification } from '@/types'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const typeFilter = ref('all')
const notifications = ref<Notification[]>([])

const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '审核通过', value: 'APPROVED' },
  { label: '审核不通过', value: 'REJECTED' },
  { label: '系统通知', value: 'SYSTEM' },
]

async function loadData() {
  loading.value = true
  try {
    const res = await notificationService.list(typeFilter.value !== 'all' ? typeFilter.value : undefined)
    notifications.value = res.items
  } catch {
    toast?.('error', '加载通知失败')
  } finally {
    loading.value = false
  }
}

watch(typeFilter, () => { loadData() }, { immediate: true })

async function handleMarkRead(id: number) {
  try {
    await notificationService.markRead(id)
    const n = notifications.value.find(item => item.id === id)
    if (n) n.read = true
  } catch {
    toast?.('error', '标记已读失败')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#111827]">通知中心</h1>
      <p class="mt-1 text-sm text-[#6B7280]">查看选课审核结果和系统通知</p>
    </div>

    <Tabs v-model="typeFilter" :tabs="filterTabs" />

    <Skeleton v-if="loading" variant="card" :count="4" />

    <EmptyState
      v-else-if="notifications.length === 0"
      title="暂无通知"
      description="有新的通知时会在这里显示"
    />

    <div v-else class="space-y-3">
      <NotificationItem
        v-for="n in notifications"
        :key="n.id"
        :notification="n"
        @mark-read="handleMarkRead"
      />
    </div>
  </div>
</template>