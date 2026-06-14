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
  <div class="flex min-h-screen bg-[#F8F6F3]">
    <!-- Sidebar -->
    <aside class="flex w-64 flex-col border-r border-[#E5E0D8] glass-surface bg-white/70 shadow-[0_0_20px_rgba(13,148,136,0.04)]">
      <div class="flex h-16 items-center gap-2 border-b border-[#E5E0D8] bg-[#F0FDFA] px-6">
        <BookCheck class="h-6 w-6 text-[#0D9488]" />
        <span class="text-lg font-bold text-[#0D9488]">
          CAS 教师端
        </span>
      </div>
      <nav class="flex-1 space-y-1 p-4">
        <router-link
          v-for="item in TEACHER_NAV_ITEMS"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F0EB] hover:text-[#1A1A2E]"
          active-class="bg-[#F0FDFA] text-[#0D9488] font-semibold"
        >
          <component :is="item.icon" class="h-5 w-5" />
          {{ item.label }}
        </router-link>
      </nav>
      <div class="border-t border-[#E5E0D8] p-4">
        <div class="mb-3 flex items-center gap-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-2xl bg-[#0D9488] text-xs font-medium text-white">
            {{ auth.user?.realName?.charAt(0) ?? 'T' }}
          </div>
          <div class="text-sm">
            <p class="font-medium text-[#1A1A2E]">{{ auth.user?.realName }}</p>
            <p class="text-xs text-[#6B7280]">教师</p>
          </div>
        </div>
        <button
          class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F0EB]"
          @click="handleLogout"
        >
          <LogOut class="h-4 w-4" />
          退出登录
        </button>
      </div>
    </aside>

    <!-- Content -->
    <main class="flex-1 overflow-auto">
      <div class="mx-auto max-w-7xl px-8 py-8">
        <router-view />
      </div>
    </main>
    <Toast ref="toastRef" />
  </div>
</template>