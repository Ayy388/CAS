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
    <header class="sticky top-0 z-50 border-b border-[#E5E7EB] bg-white/80 backdrop-blur-lg">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
        <!-- Logo -->
        <div class="flex items-center gap-8">
          <span class="bg-gradient-to-r from-[#2563EB] to-[#7C3AED] bg-clip-text text-xl font-bold text-transparent">
            CAS
          </span>
          <nav class="hidden items-center gap-1 sm:flex">
            <router-link
              v-for="item in STUDENT_NAV_ITEMS"
              :key="item.to"
              :to="item.to"
              class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F4F6] hover:text-[#111827]"
              active-class="bg-[#EFF6FF] text-[#2563EB] font-medium"
            >
              <component :is="item.icon" class="h-4 w-4" />
              {{ item.label }}
            </router-link>
          </nav>
        </div>

        <!-- User -->
        <div class="flex items-center gap-3">
          <button class="relative rounded-full p-2 text-[#6B7280] transition-colors hover:bg-[#F3F4F6]" @click="router.push('/student/notifications')">
            <Bell class="h-5 w-5" />
          </button>
          <div class="flex items-center gap-2 text-sm">
            <div class="flex h-8 w-8 items-center justify-center rounded-full bg-[#2563EB] text-xs font-medium text-white">
              {{ auth.user?.realName?.charAt(0) ?? 'U' }}
            </div>
            <div class="hidden md:block">
              <p class="text-sm font-medium text-[#111827]">{{ auth.user?.realName }}</p>
              <p class="text-xs text-[#6B7280]">学生</p>
            </div>
          </div>
          <button
            class="rounded-lg p-2 text-[#6B7280] transition-colors hover:bg-[#F3F4F6]"
            @click="handleLogout"
          >
            <LogOut class="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>

    <!-- Content -->
    <main class="mx-auto max-w-7xl px-6 py-8">
      <router-view />
    </main>
    <Toast ref="toastRef" />
  </div>
</template>