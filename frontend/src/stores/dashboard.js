import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getDashboardOverview, getLiveTelemetry } from '@/services/api'

export const useDashboardStore = defineStore('dashboard', () => {
  const overview = ref(null)
  const liveTelemetry = ref({})
  const isLoading = ref(false)
  const error = ref(null)

  const activeSessions = computed(() => overview.value?.activeSessions || [])
  const topDrivers = computed(() => overview.value?.topDrivers || [])
  const isReceivingData = computed(() => overview.value?.stats?.isReceivingData || false)

  async function fetchOverview() {
    isLoading.value = true
    error.value = null
    try {
      overview.value = await getDashboardOverview()
    } catch (e) {
      error.value = e.message
      console.error('Error fetching dashboard overview:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchLiveTelemetry() {
    try {
      liveTelemetry.value = await getLiveTelemetry()
    } catch (e) {
      console.error('Error fetching live telemetry:', e)
    }
  }

  // Auto-refresh live telemetry every 2 seconds
  let intervalId = null
  function startLiveUpdates() {
    if (intervalId) return
    intervalId = setInterval(() => {
      fetchLiveTelemetry()
      fetchOverview()
    }, 2000)
  }

  function stopLiveUpdates() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  return {
    overview,
    liveTelemetry,
    isLoading,
    error,
    activeSessions,
    topDrivers,
    isReceivingData,
    fetchOverview,
    fetchLiveTelemetry,
    startLiveUpdates,
    stopLiveUpdates
  }
})