<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Sheet } from '@/components/ui/Sheet'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import CampaignForm from '@/components/forms/CampaignForm.vue'
import { campaignService } from '@/services/campaign'
import type { SelectionCampaign } from '@/types'
import { CAMPAIGN_STATUS_MAP } from '@/types'
import { Plus, Play, Square } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const campaigns = ref<SelectionCampaign[]>([])
const showSheet = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await campaignService.adminList()
    campaigns.value = res.items
  } catch {
    toast?.('error', '加载活动列表失败')
    campaigns.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

async function handleStart(id: number) {
  try {
    await campaignService.start(id)
    toast?.('success', '活动已启动')
    await loadData()
  } catch {
    toast?.('error', '启动失败')
  }
}

async function handleEnd(id: number) {
  try {
    await campaignService.end(id)
    toast?.('success', '活动已结束')
    await loadData()
  } catch {
    toast?.('error', '结束失败')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A2E]">选课活动管理</h1>
        <p class="mt-1 text-sm text-[#6B7280]">管理选课活动的创建与状态</p>
      </div>
      <Button @click="showSheet = true">
        <Plus class="mr-2 h-4 w-4" />
        新建活动
      </Button>
    </div>

    <Skeleton v-if="loading" variant="table" :count="4" />

    <EmptyState
      v-else-if="campaigns.length === 0"
      title="暂无选课活动"
      description="点击新建活动按钮创建"
      :action="{ label: '新建活动', onClick: () => showSheet = true }"
    />

    <Card v-else>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>活动名称</TableHead>
              <TableHead>所属学期</TableHead>
              <TableHead>开始时间</TableHead>
              <TableHead>结束时间</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="c in campaigns" :key="c.id">
              <TableCell class="font-medium">{{ c.name }}</TableCell>
              <TableCell>{{ c.semesterName }}</TableCell>
              <TableCell class="text-xs">{{ c.startTime }}</TableCell>
              <TableCell class="text-xs">{{ c.endTime }}</TableCell>
              <TableCell>
                <Badge :variant="c.status === 'ACTIVE' ? 'default' : c.status === 'PENDING' ? 'secondary' : 'outline'">
                  {{ CAMPAIGN_STATUS_MAP[c.status] }}
                </Badge>
              </TableCell>
              <TableCell>
                <div class="flex gap-2">
                  <Button
                    v-if="c.status === 'PENDING'"
                    size="sm"
                    variant="default"
                    @click="handleStart(c.id)"
                  >
                    <Play class="mr-1 h-3 w-3" />
                    启动
                  </Button>
                  <Button
                    v-if="c.status === 'ACTIVE'"
                    size="sm"
                    variant="outline"
                    @click="handleEnd(c.id)"
                  >
                    <Square class="mr-1 h-3 w-3" />
                    结束
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Sheet v-model:open="showSheet" title="新建选课活动">
      <CampaignForm @success="showSheet = false" />
    </Sheet>
  </div>
</template>