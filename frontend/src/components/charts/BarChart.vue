<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart as EBarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { TopCourse } from '@/types'

use([EBarChart, GridComponent, TooltipComponent, CanvasRenderer])

const props = defineProps<{
  data: TopCourse[]
}>()

const option = computed(() => ({
  tooltip: {
    trigger: 'axis' as const,
    axisPointer: { type: 'shadow' as const },
    formatter: (params: { name: string; value: number }[]) =>
      `${params[0].name}<br/>报名人数：${params[0].value}`
  },
  grid: { left: 100, right: 20, top: 10, bottom: 20 },
  xAxis: { type: 'value', max: 'dataMax' },
  yAxis: {
    type: 'category',
    data: props.data.map(c => c.courseName).reverse(),
    axisLabel: { fontSize: 12, color: '#6B7280' }
  },
  series: [{
    type: 'bar',
    data: props.data.map(c => ({
      value: c.enrolledCount,
      itemStyle: {
        color: c.filledAt
          ? '#22C55E'
          : (c.enrolledCount / c.maxCapacity >= 1 ? '#F59E0B' : '#2563EB')
      }
    })).reverse(),
    barWidth: 14,
    borderRadius: [0, 4, 4, 0]
  }]
}))
</script>

<template>
  <div class="h-72 w-full">
    <VChart v-if="data.length" :option="option" autoresize />
    <div v-else class="flex h-full items-center justify-center text-sm text-[#6B7280]">
      暂无数据
    </div>
  </div>
</template>