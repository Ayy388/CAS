import { ref, onUnmounted } from 'vue'

export function useCountdown(initialSeconds: number) {
  const timeRemaining = ref(initialSeconds)
  const isRunning = ref(false)
  let intervalId: ReturnType<typeof setInterval> | null = null

  function start() {
    if (intervalId) return
    isRunning.value = true
    intervalId = setInterval(() => {
      if (timeRemaining.value > 0) {
        timeRemaining.value--
      } else {
        stop()
      }
    }, 1000)
  }

  function stop() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
    isRunning.value = false
  }

  function reset(seconds: number) {
    stop()
    timeRemaining.value = seconds
  }

  onUnmounted(() => stop())

  return { timeRemaining, isRunning, start, stop, reset }
}