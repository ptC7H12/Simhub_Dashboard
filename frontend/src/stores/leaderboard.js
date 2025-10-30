import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getGlobalLeaderboard, getTrackLeaderboard, getCarComparison } from '@/services/api'

export const useLeaderboardStore = defineStore('leaderboard', () => {
  const globalLeaderboard = ref([])
  const trackLeaderboard = ref([])
  const carComparison = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  async function fetchGlobalLeaderboard() {
    isLoading.value = true
    error.value = null
    try {
      globalLeaderboard.value = await getGlobalLeaderboard()
    } catch (e) {
      error.value = e.message
      console.error('Error fetching global leaderboard:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTrackLeaderboard(trackId, carModel = null) {
    isLoading.value = true
    error.value = null
    try {
      trackLeaderboard.value = await getTrackLeaderboard(trackId, carModel)
    } catch (e) {
      error.value = e.message
      console.error('Error fetching track leaderboard:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCarComparison(trackId, driverId) {
    try {
      carComparison.value = await getCarComparison(trackId, driverId)
    } catch (e) {
      console.error('Error fetching car comparison:', e)
    }
  }

  return {
    globalLeaderboard,
    trackLeaderboard,
    carComparison,
    isLoading,
    error,
    fetchGlobalLeaderboard,
    fetchTrackLeaderboard,
    fetchCarComparison
  }
})