<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { GraduationCap, LogIn } from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const user = await auth.login(username.value, password.value)
    switch (user.role) {
      case 'STUDENT': router.push('/student/dashboard'); break
      case 'TEACHER': router.push('/teacher/courses'); break
      case 'ADMIN': router.push('/admin/dashboard'); break
    }
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message ?? '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (auth.isAuthenticated && auth.user) {
    const roleHome: Record<string, string> = {
      STUDENT: '/student/dashboard',
      TEACHER: '/teacher/courses',
      ADMIN: '/admin/dashboard',
    }
    router.push(roleHome[auth.user.role] || '/login')
  }
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-[#F8F6F3]">
    <!-- Background decoration -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 h-[500px] w-[500px] rounded-full bg-gradient-to-br from-[#0D9488]/15 to-[#14B8A6]/8 blur-3xl" />
      <div class="absolute -bottom-40 -left-40 h-[500px] w-[500px] rounded-full bg-gradient-to-tr from-[#14B8A6]/10 to-[#0D9488]/12 blur-3xl" />
    </div>

    <div class="relative w-full max-w-sm mx-4">
      <div class="rounded-2xl border border-[rgba(229,224,216,0.6)] bg-white/80 shadow-[0_2px_8px_rgba(13,148,136,0.04),0_1px_2px_rgba(0,0,0,0.03)] backdrop-blur-[16px] saturate-[150%]">
        <div class="flex flex-col space-y-1.5 p-6 text-center">
          <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-[#0D9488] to-[#14B8A6] shadow-[0_4px_12px_rgba(13,148,136,0.25)]">
            <GraduationCap class="h-7 w-7 text-white" />
          </div>
          <div class="text-xl font-semibold tracking-tight text-[#1A1A2E]">CAS 选课系统</div>
          <div class="text-sm text-[#6B7280]">登录你的账户</div>
        </div>
        <div class="p-6 pt-0">
          <form class="space-y-4" @submit.prevent="handleLogin">
            <div class="space-y-2">
              <label class="text-sm font-medium text-[#1A1A2E]">用户名</label>
              <Input
                v-model="username"
                type="text"
                placeholder="请输入用户名"
              />
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-[#1A1A2E]">密码</label>
              <Input
                v-model="password"
                type="password"
                placeholder="请输入密码"
              />
            </div>
            <p v-if="errorMsg" class="text-sm text-[#EF4444]">{{ errorMsg }}</p>
            <Button type="submit" class="w-full" :disabled="loading">
              <LogIn v-if="!loading" class="mr-2 h-4 w-4" />
              {{ loading ? '登录中...' : '登录' }}
            </Button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>