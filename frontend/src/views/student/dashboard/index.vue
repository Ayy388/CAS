<script setup lang="ts">
import { ref, inject, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCampaignStore } from '@/stores/campaign'
import { useAuthStore } from '@/stores/auth'
import { useCurrentCampaign } from '@/composables/useCampaign'
import { studentService } from '@/services/student'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Skeleton } from '@/components/ui/Skeleton'
import { ArrowRight, Clock, BookOpen } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const router = useRouter()
const auth = useAuthStore()
const campaign = useCampaignStore()
useCurrentCampaign()

const loading = ref(true)
const semesterName = ref('')

function formatCountdown(seconds: number): string {
  if (seconds <= 0) return '已结束'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (d > 0) return `${d}天${h}小时${m}分钟`
  if (h > 0) return `${h}小时${m}分钟${s}秒`
  if (m > 0) return `${m}分钟${s}秒`
  return `${s}秒`
}

function goToCourses() {
  router.push('/student/courses')
}

onMounted(async () => {
  try {
    const data = await studentService.getDashboard()
    semesterName.value = data.semester?.name ?? '未知学期'
  } catch {
    toast?.('error', '加载首页数据失败')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  campaign.stopCountdown()
})
</script>

<template>
  <div class="space-y-8">
    <!-- Hero Banner -->
    <div class="hero-banner-animated relative overflow-hidden">
      <div class="relative z-10">
        <p class="mb-2 text-sm font-medium text-white/80">{{ semesterName }}</p>
        <h1 class="mb-2 text-3xl font-bold tracking-tight">
          {{ campaign.currentCampaign ? '选课进行中' : '欢迎回来' }}
        </h1>
        <p class="mb-6 text-sm text-white/70">
          {{ auth.user?.realName }}，{{ campaign.currentCampaign ? '快去选课吧' : '当前没有进行中的选课活动' }}
        </p>
        <div v-if="campaign.currentCampaign" class="mb-6 flex items-center gap-3 text-white/90">
          <Clock class="h-5 w-5" />
          <span class="font-mono text-lg font-semibold tracking-wider">
            {{ formatCountdown(campaign.timeRemaining) }}
          </span>
        </div>
        <Button class="bg-white text-[#2563EB] hover:bg-white/90" @click="goToCourses">
          <BookOpen class="mr-2 h-4 w-4" />
          进入课程大厅
          <ArrowRight class="ml-1 h-4 w-4" />
        </Button>
      </div>
    </div>

    <!-- My Courses -->
    <div>
      <h2 class="mb-4 text-lg font-semibold tracking-tight text-[#0C0C0D]">我的课程</h2>
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Card v-if="!loading" class="card-hover cursor-pointer" @click="router.push('/student/my-courses')">
          <CardContent class="flex flex-col items-center justify-center py-12 text-center">
            <BookOpen class="mb-3 h-10 w-10 text-[#2563EB]/30" />
            <p class="text-sm font-medium text-[#6B6B7B]">查看已选课程</p>
          </CardContent>
        </Card>
        <Skeleton v-else class="h-40 rounded-xl" />
      </div>
    </div>
  </div>
</template>