<template>
  <div class="space-y-6">
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <LoadingSpinner />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
      <p class="font-medium">Fehler beim Laden der Streckenanalyse</p>
      <p class="text-sm mt-1">{{ error }}</p>
    </div>

    <!-- Track Analysis Content -->
    <template v-else-if="track && trackStats">
      <!-- Header Card -->
      <div class="bg-gradient-to-r from-purple-600 to-purple-700 rounded-lg shadow-lg p-6 text-white">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold">{{ track.name }}</h1>
            <p class="mt-2 text-purple-100">{{ track.game }} • {{ track.country || 'International' }}</p>
          </div>
          <div class="text-right">
            <div class="text-sm text-purple-200">Streckenlänge</div>
            <div class="text-2xl font-bold">
              {{ track.trackLength ? `${(track.trackLength / 1000).toFixed(2)} km` : 'N/A' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <!-- Fastest Driver -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-yellow-100 rounded-md p-3">
              <svg class="h-6 w-6 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Schnellster Fahrer</div>
              <div class="mt-1 text-lg font-semibold text-gray-900 truncate">
                {{ fastestDriver?.driverName || 'N/A' }}
              </div>
              <div class="mt-1 text-xs text-gray-600 font-mono">
                {{ fastestDriver?.formattedBestLap || '-' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Total Drivers -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-blue-100 rounded-md p-3">
              <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Teilnehmer</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">
                {{ trackStats.length }}
              </div>
              <div class="mt-1 text-xs text-gray-600">
                Fahrer auf dieser Strecke
              </div>
            </div>
          </div>
        </div>

        <!-- Total Laps -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-green-100 rounded-md p-3">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Gesamtrunden</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">
                {{ totalLaps }}
              </div>
              <div class="mt-1 text-xs text-gray-600">
                Alle Fahrer kombiniert
              </div>
            </div>
          </div>
        </div>

        <!-- Avg Consistency -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-purple-100 rounded-md p-3">
              <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Ø Konsistenz</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">
                {{ averageConsistency }}%
              </div>
              <div class="mt-1">
                <ConsistencyBadge :score="parseFloat(averageConsistency)" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Lap Time Distribution Chart -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Rundenzeiten-Vergleich</h2>
        <LapTimeComparisonChart :track-id="trackId" :statistics="trackStats" />
      </div>

      <!-- Leaderboard Table -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Strecken-Leaderboard</h2>

        <div v-if="trackStats && trackStats.length > 0">
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Pos</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Fahrer</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Best Lap</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Avg Lap</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Consistency</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Track Mastery</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Laps</th>
                  <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Aktion</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr
                  v-for="(stat, index) in trackStats"
                  :key="stat.driverId"
                  class="hover:bg-gray-50 cursor-pointer transition"
                  @click="navigateToDriver(stat.driverId)"
                >
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div
                      class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-white"
                      :class="getPositionColor(index)"
                    >
                      {{ index + 1 }}
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">{{ stat.driverName }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-mono font-semibold text-gray-900">{{ stat.formattedBestLap }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-mono text-gray-600">{{ stat.formattedAvgLap }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <ConsistencyBadge :score="stat.consistencyScore" />
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <div class="w-20 bg-gray-200 rounded-full h-2 mr-2">
                        <div
                          class="bg-purple-600 h-2 rounded-full"
                          :style="{ width: `${stat.trackMastery}%` }"
                        ></div>
                      </div>
                      <span class="text-sm text-gray-600">{{ stat.trackMastery?.toFixed(0) }}%</span>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ stat.lapCount }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm">
                    <router-link
                      :to="`/driver/${stat.driverId}`"
                      class="text-blue-600 hover:text-blue-900"
                      @click.stop
                    >
                      Details →
                    </router-link>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else class="text-center py-8 text-gray-500">
          Keine Statistiken verfügbar
        </div>
      </div>

      <!-- Track Records -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Strecken-Rekorde</h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Fastest Lap -->
          <div class="border-l-4 border-yellow-500 bg-yellow-50 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <div class="text-sm font-medium text-yellow-800">Schnellste Runde</div>
                <div class="text-2xl font-bold text-yellow-900 font-mono mt-1">
                  {{ fastestDriver?.formattedBestLap || '-' }}
                </div>
                <div class="text-sm text-yellow-700 mt-1">
                  {{ fastestDriver?.driverName || 'N/A' }}
                </div>
              </div>
              <svg class="h-12 w-12 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
          </div>

          <!-- Most Consistent -->
          <div class="border-l-4 border-green-500 bg-green-50 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <div class="text-sm font-medium text-green-800">Konsistentester</div>
                <div class="text-2xl font-bold text-green-900 mt-1">
                  {{ mostConsistent?.consistencyScore?.toFixed(1) || 0 }}%
                </div>
                <div class="text-sm text-green-700 mt-1">
                  {{ mostConsistent?.driverName || 'N/A' }}
                </div>
              </div>
              <svg class="h-12 w-12 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>

          <!-- Most Experienced -->
          <div class="border-l-4 border-blue-500 bg-blue-50 p-4 rounded">
            <div class="flex items-center justify-between">
              <div>
                <div class="text-sm font-medium text-blue-800">Meiste Runden</div>
                <div class="text-2xl font-bold text-blue-900 mt-1">
                  {{ mostExperienced?.lapCount || 0 }}
                </div>
                <div class="text-sm text-blue-700 mt-1">
                  {{ mostExperienced?.driverName || 'N/A' }}
                </div>
              </div>
              <svg class="h-12 w-12 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTrack, getTrackStatistics } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ConsistencyBadge from '@/components/common/ConsistencyBadge.vue'
import LapTimeComparisonChart from './LapTimeComparisonChart.vue'

const props = defineProps({
  trackId: {
    type: String,
    required: true
  }
})

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const track = ref(null)
const trackStats = ref([])

onMounted(async () => {
  await loadTrackAnalysis()
})

const loadTrackAnalysis = async () => {
  loading.value = true
  error.value = null

  try {
    track.value = await getTrack(props.trackId)
    trackStats.value = await getTrackStatistics(props.trackId)
  } catch (e) {
    error.value = e.message
    console.error('Error loading track analysis:', e)
  } finally {
    loading.value = false
  }
}

const fastestDriver = computed(() => {
  if (!trackStats.value || trackStats.value.length === 0) return null
  return trackStats.value[0]
})

const mostConsistent = computed(() => {
  if (!trackStats.value || trackStats.value.length === 0) return null
  return [...trackStats.value].sort((a, b) => (b.consistencyScore || 0) - (a.consistencyScore || 0))[0]
})

const mostExperienced = computed(() => {
  if (!trackStats.value || trackStats.value.length === 0) return null
  return [...trackStats.value].sort((a, b) => (b.lapCount || 0) - (a.lapCount || 0))[0]
})

const totalLaps = computed(() => {
  if (!trackStats.value) return 0
  return trackStats.value.reduce((sum, stat) => sum + (stat.lapCount || 0), 0)
})

const averageConsistency = computed(() => {
  if (!trackStats.value || trackStats.value.length === 0) return '0.0'
  const sum = trackStats.value.reduce((acc, stat) => acc + (stat.consistencyScore || 0), 0)
  return (sum / trackStats.value.length).toFixed(1)
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