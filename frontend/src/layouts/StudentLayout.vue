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
  <div class="min-h-screen bg-[#F8F6F3]">
    <!-- Top Navigation -->
    <header class="sticky top-0 z-50 border-b border-[#E5E0D8] glass-surface bg-white/70">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
        <!-- Logo -->
        <div class="flex items-center gap-8">
          <span class="text-xl font-bold text-[#0D9488]">
            CAS
          </span>
          <nav class="hidden items-center gap-1 sm:flex">
            <router-link
              v-for="item in STUDENT_NAV_ITEMS"
              :key="item.to"
              :to="item.to"
              class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-[#6B7280] transition-colors hover:bg-[#F3F0EB] hover:text-[#1A1A2E]"
              active-class="bg-[#F0FDFA] text-[#0D9488] font-semibold"
            >
              <component :is="item.icon" class="h-4 w-4" />
              {{ item.label }}
            </router-link>
          </nav>
        </div>

        <!-- User -->
        <div class="flex items-center gap-3">
          <button class="relative rounded-xl p-2 text-[#6B7280] transition-colors hover:bg-[#F3F0EB]" @click="router.push('/student/notifications')">
            <Bell class="h-5 w-5" />
          </button>
          <div class="flex items-center gap-2 text-sm">
            <div class="flex h-8 w-8 items-center justify-center rounded-2xl bg-[#0D9488] text-xs font-medium text-white">
              {{ auth.user?.realName?.charAt(0) ?? 'U' }}
            </div>
            <div class="hidden md:block">
              <p class="text-sm font-medium text-[#1A1A2E]">{{ auth.user?.realName }}</p>
              <p class="text-xs text-[#6B7280]">学生</p>
            </div>
          </div>
          <button
            class="rounded-lg p-2 text-[#6B7280] transition-colors hover:bg-[#F3F0EB]"
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