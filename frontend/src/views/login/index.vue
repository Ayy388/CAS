<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { CardHeader } from '@/components/ui/CardHeader'
import { CardTitle } from '@/components/ui/CardTitle'
import { CardDescription } from '@/components/ui/CardDescription'
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
  <div class="flex min-h-screen items-center justify-center bg-[#FAFAFA]">
    <!-- Background decoration -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 h-[500px] w-[500px] rounded-full bg-gradient-to-br from-[#2563EB]/8 to-[#3B82F6]/5 blur-3xl" />
      <div class="absolute -bottom-40 -left-40 h-[500px] w-[500px] rounded-full bg-gradient-to-tr from-[#3B82F6]/5 to-[#2563EB]/8 blur-3xl" />
    </div>

    <Card class="relative w-full max-w-sm mx-4 shadow-[0_2px_8px_rgba(37,99,235,0.06),0_1px_2px_rgba(0,0,0,0.04)]">
      <CardHeader class="text-center">
        <div class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-[#1E40AF] to-[#2563EB] shadow-lg shadow-[#2563EB]/20">
          <GraduationCap class="h-7 w-7 text-white" />
        </div>
        <CardTitle class="text-xl font-semibold tracking-tight text-[#0C0C0D]">CAS 选课系统</CardTitle>
        <CardDescription class="text-[#6B6B7B]">登录你的账户</CardDescription>
      </CardHeader>
      <CardContent>
        <form class="space-y-4" @submit.prevent="handleLogin">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-[#0C0C0D]">用户名</label>
            <Input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              class="h-10"
            />
          </div>
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-[#0C0C0D]">密码</label>
            <Input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              class="h-10"
            />
          </div>
          <p v-if="errorMsg" class="text-sm text-[#EF4444]">{{ errorMsg }}</p>
          <Button type="submit" class="w-full" :disabled="loading">
            <LogIn v-if="!loading" class="mr-2 h-4 w-4" />
            {{ loading ? '登录中...' : '登录' }}
          </Button>
        </form>
      </CardContent>
    </Card>
  </div>
</template>