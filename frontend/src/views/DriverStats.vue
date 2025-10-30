<template>
  <div v-if="loading" class="flex justify-center items-center h-64">
    <LoadingSpinner />
  </div>

  <div v-else-if="statistics" class="space-y-6">
    <!-- Header -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">{{ statistics.driverName }}</h1>
          <p class="mt-1 text-sm text-gray-600">Fahrer-Statistiken</p>
        </div>

        <button
          @click="refreshStats"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
        >
          Aktualisieren
        </button>
      </div>
    </div>

    <!-- Overview Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <StatCard
        title="Favorite Track"
        :value="statistics.favoriteTrack || 'N/A'"
        :subtitle="`Mastery: ${statistics.favoriteTrackMastery?.toFixed(1) || 0}%`"
        icon="trophy"
      />

      <StatCard
        title="Worst Track"
        :value="statistics.worstTrack || 'N/A'"
        :subtitle="`Mastery: ${statistics.worstTrackMastery?.toFixed(1) || 0}%`"
        icon="warning"
      />

      <StatCard
        title="Total Laps"
        :value="statistics.totalLaps || 0"
        subtitle="Gefahrene Runden"
        icon="chart"
      />

      <StatCard
        title="Avg Consistency"
        :value="`${statistics.averageConsistency?.toFixed(1) || 0}%`"
        subtitle="Durchschnitt"
        icon="target"
      />
    </div>

    <!-- Improvement Section -->
    <div v-if="statistics.bestImprovementRate" class="bg-white shadow rounded-lg p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Beste Verbesserung</h2>
      <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg">
        <div>
          <div class="text-lg font-medium text-green-900">
            {{ statistics.bestImprovementTrack }}
          </div>
          <div class="text-sm text-green-700">
            Strecke mit der größten Verbesserung
          </div>
        </div>
        <div class="text-2xl font-bold text-green-600">
          +{{ statistics.bestImprovementRate.toFixed(1) }}%
        </div>
      </div>
    </div>

    <!-- Track Performance -->
    <div class="bg-white shadow rounded-lg p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Track Performance</h2>
      <TrackPerformanceChart :driver-id="driverId" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getDriverStatistics, calculateStatistics } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import StatCard from '@/components/statistics/StatCard.vue'
import TrackPerformanceChart from '@/components/statistics/TrackPerformanceChart.vue'

const route = useRoute()
const driverId = ref(route.params.id)
const statistics = ref(null)
const loading = ref(true)

onMounted(async () => {
  await loadStatistics()
})

const loadStatistics = async () => {
  loading.value = true
  try {
    statistics.value = await getDriverStatistics(driverId.value)
  } catch (error) {
    console.error('Error loading driver statistics:', error)
  } finally {
    loading.value = false
  }
}

const refreshStats = async () => {
  await calculateStatistics()
  await loadStatistics()
}
</script>