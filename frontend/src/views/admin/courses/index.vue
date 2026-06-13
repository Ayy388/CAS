<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { Card } from '@/components/ui/Card'
import { CardContent } from '@/components/ui/CardContent'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Sheet } from '@/components/ui/Sheet'
import { Dialog } from '@/components/ui/Dialog'
import { Badge } from '@/components/ui/Badge'
import { Skeleton } from '@/components/ui/Skeleton'
import EmptyState from '@/components/shared/EmptyState.vue'
import CourseForm from '@/components/forms/CourseForm.vue'
import { courseService } from '@/services/course'
import type { Course } from '@/types'
import { COURSE_TYPE_MAP } from '@/types'
import { Plus, Search, Pencil, Trash2 } from 'lucide-vue-next'

const toast = inject<(type: 'success' | 'error', title: string) => void>('toast')

const loading = ref(true)
const courses = ref<Course[]>([])
const searchQuery = ref('')
const showSheet = ref(false)
const editItem = ref<Course | null>(null)
const showDeleteDialog = ref(false)
const deletingItem = ref<Course | null>(null)

async function loadData() {
  loading.value = true
  try {
    const kw = searchQuery.value || undefined
    const res = await courseService.list(kw)
    courses.value = res.items
  } catch {
    toast?.('error', '加载课程列表失败')
    courses.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

onMounted(loadData)

function handleCreate() {
  editItem.value = null
  showSheet.value = true
}

function handleEdit(course: Course) {
  editItem.value = course
  showSheet.value = true
}

function handleDeleteClick(course: Course) {
  deletingItem.value = course
  showDeleteDialog.value = true
}

async function handleDeleteConfirm() {
  if (!deletingItem.value) return
  try {
    await courseService.delete(deletingItem.value.id)
    toast?.('success', '课程已删除')
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
        <h1 class="text-2xl font-bold text-[#111827]">课程管理</h1>
        <p class="mt-1 text-sm text-[#6B7280]">管理系统中的课程信息</p>
      </div>
      <Button @click="handleCreate">
        <Plus class="mr-2 h-4 w-4" />
        新建课程
      </Button>
    </div>

    <div class="relative w-full sm:max-w-xs">
      <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#9CA3AF]" />
      <Input v-model="searchQuery" placeholder="搜索课程名称..." class="pl-10" @keyup.enter="handleSearch" />
    </div>

    <Skeleton v-if="loading" variant="table" :count="5" />

    <EmptyState
      v-else-if="courses.length === 0"
      title="暂无课程"
      description="点击新建按钮创建课程"
      :action="{ label: '新建课程', onClick: handleCreate }"
    />

    <Card v-else>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>课程编号</TableHead>
              <TableHead>课程名称</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>学分</TableHead>
              <TableHead>课时</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead>操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="c in courses" :key="c.id">
              <TableCell class="font-mono text-xs">{{ c.code }}</TableCell>
              <TableCell class="font-medium">{{ c.name }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ COURSE_TYPE_MAP[c.type] }}</Badge>
              </TableCell>
              <TableCell>{{ c.credits }}</TableCell>
              <TableCell>{{ c.hours }}</TableCell>
              <TableCell class="text-xs">{{ c.createdAt }}</TableCell>
              <TableCell>
                <div class="flex items-center gap-2">
                  <Button size="sm" variant="outline" @click="handleEdit(c)">
                    <Pencil class="h-3 w-3" />
                  </Button>
                  <Button size="sm" variant="outline" class="text-[#EF4444] hover:text-[#EF4444]" @click="handleDeleteClick(c)">
                    <Trash2 class="h-3 w-3" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Sheet v-model:open="showSheet" :title="editItem ? '编辑课程' : '新建课程'">
      <CourseForm :course="editItem" @success="showSheet = false; loadData()" />
    </Sheet>

    <Dialog v-model:open="showDeleteDialog" title="确认删除" description="确定要删除课程「{{ deletingItem?.name }}」吗？此操作不可撤销。">
      <div class="flex justify-end gap-3 pt-4">
        <Button variant="outline" @click="showDeleteDialog = false">取消</Button>
        <Button variant="destructive" @click="handleDeleteConfirm">删除</Button>
      </div>
    </Dialog>
  </div>
</template>