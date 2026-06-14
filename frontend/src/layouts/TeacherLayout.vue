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
    <aside class="flex w-64 flex-col border-r border-[#E8E8ED] bg-white shadow-[1px_0_2px_rgba(0,0,0,0.02)]">
      <div class="flex h-16 items-center gap-2.5 border-b border-[#E8E8ED] px-6">
        <BookCheck class="h-5 w-5 text-[#2563EB]" />
        <span class="text-base font-bold tracking-tight text-[#1E40AF]">
          CAS 教师端
        </span>
      </div>
      <nav class="flex-1 space-y-0.5 p-3">
        <router-link
          v-for="item in TEACHER_NAV_ITEMS"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-[#6B6B7B] transition-all duration-150 hover:bg-[#F5F5F8] hover:text-[#0C0C0D]"
          active-class="bg-[#EFF2FF] text-[#2563EB] font-semibold"
        >
          <component :is="item.icon" class="h-5 w-5" />
          {{ item.label }}
        </router-link>
      </nav>
      <div class="border-t border-[#E8E8ED] p-4">
        <div class="mb-3 flex items-center gap-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-[#2563EB] text-xs font-medium text-white">
            {{ auth.user?.realName?.charAt(0) ?? 'T' }}
          </div>
          <div class="text-sm">
            <p class="font-medium text-[#0C0C0D]">{{ auth.user?.realName }}</p>
            <p class="text-xs text-[#6B6B7B]">教师</p>
          </div>
        </div>
        <button
          class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-[#6B6B7B] transition-all duration-150 hover:bg-[#F5F5F8] hover:text-[#0C0C0D]"
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