<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Dialog } from '@/components/ui/Dialog'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import { reviewService } from '@/services/review'
import type { ReviewItem } from '@/types'
import { Check, X } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const items = ref<ReviewItem[]>([])
const showDialog = ref(false)
const dialogAction = ref<'approve' | 'reject'>('approve')
const selectedOffering = ref<number | null>(null)
const dialogTitle = ref('')
const dialogDesc = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await reviewService.list()
    items.value = res.items
  } catch {
    toast?.('error', '加载审核列表失败')
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function handleApprove(offeringId: number, courseName: string) {
  selectedOffering.value = offeringId
  dialogAction.value = 'approve'
  dialogTitle.value = '确认开课'
  dialogDesc.value = `确认开课「${courseName}」？开课后将向学生发送通知`
  showDialog.value = true
}

function handleReject(offeringId: number, courseName: string) {
  selectedOffering.value = offeringId
  dialogAction.value = 'reject'
  dialogTitle.value = '取消开课'
  dialogDesc.value = `确认取消开课「${courseName}」？取消后将向学生发送通知`
  showDialog.value = true
}

async function confirmAction() {
  if (!selectedOffering.value) return
  showDialog.value = false
  try {
    if (dialogAction.value === 'approve') {
      await reviewService.approve(selectedOffering.value)
      toast?.('success', '审核通过，通知正在发送')
    } else {
      await reviewService.reject(selectedOffering.value)
      toast?.('success', '已驳回，通知正在发送')
    }
    await loadData()
  } catch {
    toast?.('error', '操作失败，请重试')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-[#111827]">开课审核</h1>
      <p class="mt-1 text-sm text-[#6B7280]">审批开课申请，决定是否开课</p>
    </div>

    <Skeleton v-if="loading" variant="table" :count="5" />

    <EmptyState
      v-else-if="items.length === 0"
      title="暂无待审核开课"
      description="所有开课都已审核完成"
    />

    <Card v-else>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>课程名称</TableHead>
              <TableHead>授课教师</TableHead>
              <TableHead>报名人数</TableHead>
              <TableHead>最低人数</TableHead>
              <TableHead>建议结果</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in items" :key="item.offeringId">
              <TableCell class="font-medium">{{ item.courseName }}</TableCell>
              <TableCell>{{ item.teacherName }}</TableCell>
              <TableCell>
                <span :class="item.enrolledCount >= item.minEnrollment ? 'text-[#22C55E]' : 'text-[#F59E0B]'">
                  {{ item.enrolledCount }}
                </span>
              </TableCell>
              <TableCell>{{ item.minEnrollment }}</TableCell>
              <TableCell>
                <Badge
                  :variant="item.enrolledCount >= item.minEnrollment ? 'default' : 'destructive'"
                >
                  {{ item.enrolledCount >= item.minEnrollment ? '建议开课' : '人数不足' }}
                </Badge>
              </TableCell>
              <TableCell>
                <div class="flex gap-2">
                  <Button
                    size="sm"
                    @click="handleApprove(item.offeringId, item.courseName)"
                  >
                    <Check class="mr-1 h-3 w-3" />
                    通过
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    class="text-[#EF4444] border-[#EF4444] hover:bg-[#FEF2F2]"
                    @click="handleReject(item.offeringId, item.courseName)"
                  >
                    <X class="mr-1 h-3 w-3" />
                    不通过
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Dialog v-model:open="showDialog" :title="dialogTitle" :description="dialogDesc">
      <div class="flex justify-end gap-3">
        <Button variant="outline" @click="showDialog = false">取消</Button>
        <Button
          :variant="dialogAction === 'approve' ? 'default' : 'destructive'"
          @click="confirmAction"
        >
          {{ dialogAction === 'approve' ? '确认开课' : '确认取消' }}
        </Button>
      </div>
    </Dialog>
  </div>
</template>