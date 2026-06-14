<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Sheet } from '@/components/ui/Sheet'
import { Dialog } from '@/components/ui/Dialog'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import ErrorState from '@/components/shared/ErrorState.vue'
import SemesterForm from '@/components/forms/SemesterForm.vue'
import { semesterService } from '@/services/semester'
import type { Semester } from '@/types'
import { Plus, Power, Pencil, Trash2 } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const error = ref(false)
const semesters = ref<Semester[]>([])
const showSheet = ref(false)
const editItem = ref<Semester | null>(null)
const showDeleteDialog = ref(false)
const deletingItem = ref<Semester | null>(null)

async function loadData() {
  loading.value = true
  error.value = false
  try {
    const res = await semesterService.list()
    semesters.value = res.items
  } catch {
    error.value = true
    semesters.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

function handleCreate() {
  editItem.value = null
  showSheet.value = true
}

function handleEdit(semester: Semester) {
  editItem.value = semester
  showSheet.value = true
}

async function handleActivate(id: number) {
  try {
    await semesterService.activate(id)
    toast?.('success', '学期已激活')
    await loadData()
  } catch {
    toast?.('error', '激活失败，请重试')
  }
}

function handleDeleteClick(semester: Semester) {
  deletingItem.value = semester
  showDeleteDialog.value = true
}

async function handleDeleteConfirm() {
  if (!deletingItem.value) return
  try {
    await semesterService.delete(deletingItem.value.id)
    toast?.('success', '学期已删除')
    showDeleteDialog.value = false
    deletingItem.value = null
    await loadData()
  } catch {
    toast?.('error', '删除失败')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A2E]">学期管理</h1>
        <p class="mt-1 text-sm text-[#6B7280]">管理学期信息</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="mr-2 h-4 w-4" />
        新建学期
      </Button>
    </div>

    <Skeleton v-if="loading" variant="table" :count="5" />

    <ErrorState v-else-if="error" message="加载失败，请重试" :on-retry="loadData" />

    <EmptyState
      v-else-if="semesters.length === 0"
      title="暂无学期"
      description="点击新建按钮创建学期"
      :action="{ label: '新建学期', onClick: handleCreate }"
    />

    <Card v-else>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>学期名称</TableHead>
              <TableHead>学年</TableHead>
              <TableHead>开始日期</TableHead>
              <TableHead>结束日期</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="s in semesters" :key="s.id">
              <TableCell class="font-medium">{{ s.name }}</TableCell>
              <TableCell>{{ s.academicYear }}</TableCell>
              <TableCell>{{ s.startDate }}</TableCell>
              <TableCell>{{ s.endDate }}</TableCell>
              <TableCell>
                <Badge :variant="s.status === 'ACTIVE' ? 'default' : 'secondary'">
                  {{ s.status === 'ACTIVE' ? '激活' : '未激活' }}
                </Badge>
              </TableCell>
              <TableCell>
                <div class="flex items-center gap-2">
                  <Button size="sm" variant="outline" @click="handleEdit(s)">
                    <Pencil class="h-3 w-3" />
                  </Button>
                  <Button
                    v-if="s.status !== 'ACTIVE'"
                    size="sm"
                    variant="outline"
                    @click="handleActivate(s.id)"
                  >
                    <Power class="mr-1 h-3 w-3" />
                    激活
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    class="text-[#EF4444] hover:text-[#EF4444]"
                    @click="handleDeleteClick(s)"
                  >
                    <Trash2 class="h-3 w-3" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Sheet v-model:open="showSheet" :title="editItem ? '编辑学期' : '新建学期'">
      <SemesterForm :semester="editItem" @success="showSheet = false; loadData()" />
    </Sheet>

    <Dialog v-model:open="showDeleteDialog" title="确认删除" description="确定要删除学期「{{ deletingItem?.name }}」吗？此操作不可撤销。">
      <div class="flex justify-end gap-3 pt-4">
        <Button variant="outline" @click="showDeleteDialog = false">取消</Button>
        <Button variant="destructive" @click="handleDeleteConfirm">删除</Button>
      </div>
    </Dialog>
  </div>
</template>