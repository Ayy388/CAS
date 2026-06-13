<script setup lang="ts">
import { ref, provide } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { TEACHER_NAV_ITEMS } from '@/lib/constants'
import { LogOut, BookCheck } from 'lucide-vue-next'
import Toast from '@/components/ui/Toast/Toast.vue'

const auth = useAuthStore()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
provide('toast', (type: 'success' | 'error' | 'warning' | 'info', title: string) => {
  toastRef.value?.toast(type, title)
})

function handleLogout() {
  auth.logout()
}
</script>

<template>
  <div class="flex min-h-screen bg-[#FAFAFA]">
    <!-- Sidebar -->
    <aside class="flex w-64 flex-col border-r border-[#E5E7EB] bg-white">
      <div class="flex h-16 items-center gap-2 border-b border-[#E5E7EB] px-6">
        <BookCheck class="h-6 w-6 text-[#2563EB]" />
        <span class="bg-gradient-to-r from-[#2563EB] to-[#7C3AED] bg-clip-text text-lg font-bold text-transparent">
          CAS 教师端
        </span>
      </div>
      <nav class="flex-1 space-y-1 p-4">
        <router-link
          v-for="item in TEACHER_NAV_ITEMS"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F4F6] hover:text-[#111827]"
          active-class="bg-[#EFF6FF] text-[#2563EB] font-medium"
        >
          <component :is="item.icon" class="h-5 w-5" />
          {{ item.label }}
        </router-link>
      </nav>
      <div class="border-t border-[#E5E7EB] p-4">
        <div class="mb-3 flex items-center gap-3">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-[#2563EB] text-xs font-medium text-white">
            {{ auth.user?.realName?.charAt(0) ?? 'T' }}
          </div>
          <div class="text-sm">
            <p class="font-medium text-[#111827]">{{ auth.user?.realName }}</p>
            <p class="text-xs text-[#6B7280]">教师</p>
          </div>
        </div>
        <button
          class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F4F6]"
          @click="handleLogout"
        >
          <LogOut class="h-4 w-4" />
          退出登录
        </button>
      </div>
    </aside>

    <!-- Content -->
    <main class="flex-1 overflow-auto">
      <div class="mx-auto max-w-6xl px-8 py-8">
        <router-view />
      </div>
    </main>
    <Toast ref="toastRef" />
  </div>
</template>