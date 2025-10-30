import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDriverStatistics, getTrackStatistics, calculateStatistics } from '@/services/api'

export const useStatisticsStore = defineStore('statistics', () => {
  const driverStats = ref(null)
  const trackStats = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  async function fetchDriverStatistics(driverId) {
    isLoading.value = true
    error.value = null
    try {
      driverStats.value = await getDriverStatistics(driverId)
    } catch (e) {
      error.value = e.message
      console.error('Error fetching driver statistics:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTrackStatistics(trackId) {
    isLoading.value = true
    error.value = null
    try {
      trackStats.value = await getTrackStatistics(trackId)
    } catch (e) {
      error.value = e.message
      console.error('Error fetching track statistics:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function triggerCalculation() {
    try {
      await calculateStatistics()
    } catch (e) {
      console.error('Error calculating statistics:', e)
    }
  }

  return {
    driverStats,
    trackStats,
    isLoading,
    error,
    fetchDriverStatistics,
    fetchTrackStatistics,
    triggerCalculation
  }
})