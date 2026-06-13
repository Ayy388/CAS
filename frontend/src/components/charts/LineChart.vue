<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart as ELineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { TrendPoint } from '@/types'

use([ELineChart, GridComponent, TooltipComponent, CanvasRenderer])

const props = defineProps<{
  data: TrendPoint[]
}>()

const option = computed(() => ({
  tooltip: {
    trigger: 'axis' as const,
    formatter: (params: { name: string; value: number }[]) =>
      `${params[0].name}<br/>抢课人数：${params[0].value}`
  },
  grid: { left: 60, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: props.data.map(t => t.time),
    axisLabel: { fontSize: 11, color: '#9CA3AF' },
    boundaryGap: false
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#F3F4F6' } },
    axisLabel: { fontSize: 11, color: '#9CA3AF' }
  },
  series: [{
    type: 'line',
    data: props.data.map(t => t.count),
    smooth: true,
    lineStyle: { color: '#2563EB', width: 2 },
    itemStyle: { color: '#2563EB' },
    areaStyle: { color: 'rgba(37,99,235,0.08)' },
    symbol: 'circle',
    symbolSize: 6,
    showSymbol: false
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