<script setup lang="ts">
import { ref, provide } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { STUDENT_NAV_ITEMS } from '@/lib/constants'
import { Bell, LogOut } from 'lucide-vue-next'
import Toast from '@/components/ui/Toast/Toast.vue'

const router = useRouter()
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
  <div class="min-h-screen bg-[#FAFAFA]">
    <!-- Top Navigation -->
    <header class="sticky top-0 z-50 border-b border-[#E8E8ED]/80 bg-white/75 backdrop-blur-xl">
      <div class="mx-auto flex h-14 max-w-7xl items-center justify-between px-6">
        <!-- Logo -->
        <div class="flex items-center gap-8">
          <span class="text-lg font-bold tracking-tight text-[#1E40AF]">
            CAS
          </span>
          <nav class="hidden items-center gap-0.5 sm:flex">
            <router-link
              v-for="item in STUDENT_NAV_ITEMS"
              :key="item.to"
              :to="item.to"
              class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-[#6B6B7B] transition-all duration-150 hover:bg-[#F5F5F8] hover:text-[#0C0C0D]"
              active-class="bg-[#EFF2FF] text-[#2563EB] font-semibold"
            >
              <component :is="item.icon" class="h-4 w-4" />
              {{ item.label }}
            </router-link>
          </nav>
        </div>

        <!-- User -->
        <div class="flex items-center gap-2">
          <button class="relative rounded-lg p-2 text-[#6B6B7B] transition-all duration-150 hover:bg-[#F5F5F8] hover:text-[#0C0C0D]" @click="router.push('/student/notifications')">
            <Bell class="h-4 w-4" />
          </button>
          <div class="flex items-center gap-2 pl-2 border-l border-[#E8E8ED]">
            <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-[#2563EB] text-[10px] font-semibold text-white">
              {{ auth.user?.realName?.charAt(0) ?? 'U' }}
            </div>
            <div class="hidden md:block">
              <p class="text-sm font-medium text-[#0C0C0D] leading-tight">{{ auth.user?.realName }}</p>
              <p class="text-[11px] text-[#6B6B7B]">学生</p>
            </div>
          </div>
          <button
            class="rounded-lg p-2 text-[#6B6B7B] transition-all duration-150 hover:bg-[#F5F5F8] hover:text-[#0C0C0D]"
            @click="handleLogout"
          >
            <LogOut class="h-3.5 w-3.5" />
          </button>
        </div>
      </div>
    </header>

    <!-- Content -->
    <main class="mx-auto max-w-7xl px-6 py-6">
      <router-view />
    </main>
    <Toast ref="toastRef" />
  </div>
</template>