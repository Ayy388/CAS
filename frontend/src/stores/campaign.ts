import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { SelectionCampaign } from '@/types'

export const useCampaignStore = defineStore('campaign', () => {
  const currentCampaign = ref<SelectionCampaign | null>(null)
  const timeRemaining = ref(0)
  const isCounting = ref(false)
  let intervalId: ReturnType<typeof setInterval> | null = null

  function setCampaign(campaign: SelectionCampaign | null) {
    currentCampaign.value = campaign
    if (campaign) {
      const now = Date.now()
      const end = new Date(campaign.endTime).getTime()
      const diff = Math.max(0, Math.floor((end - now) / 1000))
      timeRemaining.value = diff
    }
  }

  function startCountdown() {
    if (intervalId) return
    isCounting.value = true
    intervalId = setInterval(() => {
      if (timeRemaining.value > 0) {
        timeRemaining.value--
      } else {
        stopCountdown()
      }
    }, 1000)
  }

  function stopCountdown() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
    isCounting.value = false
  }

  function tick() {
    timeRemaining.value--
  }

  return {
    currentCampaign,
    timeRemaining,
    isCounting,
    setCampaign,
    startCountdown,
    stopCountdown,
    tick,
  }
})