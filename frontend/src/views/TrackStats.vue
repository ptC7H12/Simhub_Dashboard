<template>
  <div v-if="loading" class="flex justify-center items-center h-64">
    <LoadingSpinner />
  </div>

  <div v-else-if="track" class="space-y-6">
    <!-- Header -->
    <div class="bg-white shadow rounded-lg p-6">
      <h1 class="text-3xl font-bold text-gray-900">{{ track.name }}</h1>
      <p class="mt-1 text-sm text-gray-600">{{ track.game }}</p>
    </div>

    <!-- Track Leaderboard -->
    <div class="bg-white shadow rounded-lg p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Leaderboard</h2>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Pos
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fahrer
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Best Lap
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Avg Lap
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Consistency
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Track Mastery
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Laps
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="(stat, index) in trackStatistics"
              :key="stat.driverId"
              class="hover:bg-gray-50 cursor-pointer"
              @click="navigateToDriver(stat.driverId)"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div
                    class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-white"
                    :class="getPositionColor(index)"
                  >
                    {{ index + 1 }}
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ stat.driverName }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-mono text-gray-900">{{ stat.formattedBestLap }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-mono text-gray-600">{{ stat.formattedAvgLap }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <ConsistencyBadge :score="stat.consistencyScore" />
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="w-full bg-gray-200 rounded-full h-2 mr-2" style="width: 100px">
                    <div
                      class="bg-blue-600 h-2 rounded-full"
                      :style="{ width: `${stat.trackMastery}%` }"
                    ></div>
                  </div>
                  <span class="text-sm text-gray-600">{{ stat.trackMastery?.toFixed(0) }}%</span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-600">{{ stat.lapCount }}</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Lap Time Comparison Chart -->
    <div class="bg-white shadow rounded-lg p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Lap Time Vergleich</h2>
      <LapTimeComparisonChart :track-id="trackId" :statistics="trackStatistics" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTrack, getTrackStatistics } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ConsistencyBadge from '@/components/common/ConsistencyBadge.vue'
import LapTimeComparisonChart from '@/components/statistics/LapTimeComparisonChart.vue'

const route = useRoute()
const router = useRouter()
const trackId = ref(route.params.id)
const track = ref(null)
const trackStatistics = ref([])
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    track.value = await getTrack(trackId.value)
    trackStatistics.value = await getTrackStatistics(trackId.value)
  } catch (error) {
    console.error('Error loading track statistics:', error)
  } finally {
    loading.value = false
  }
})

const getPositionColor = (index) => {
  if (index === 0) return 'bg-yellow-500'
  if (index === 1) return 'bg-gray-400'
  if (index === 2) return 'bg-orange-600'
  return 'bg-gray-600'
}

const navigateToDriver = (driverId) => {
  router.push(`/driver/${driverId}`)
}
</script>