<template>
  <div class="space-y-6">
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <LoadingSpinner />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
      <p class="font-medium">Fehler beim Laden des Fahrerprofils</p>
      <p class="text-sm mt-1">{{ error }}</p>
    </div>

    <!-- Driver Profile Content -->
    <template v-else-if="statistics">
      <!-- Header Card -->
      <div class="bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg shadow-lg p-6 text-white">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold">{{ statistics.driverName }}</h1>
            <p class="mt-2 text-blue-100">Fahrer-Profil & Statistiken</p>
          </div>
          <div class="text-right">
            <div class="text-4xl font-bold">{{ statistics.totalLaps || 0 }}</div>
            <div class="text-sm text-blue-200">Gesamtrunden</div>
          </div>
        </div>
      </div>

      <!-- Key Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Favorite Track -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-green-100 rounded-md p-3">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Lieblingsstrecke</div>
              <div class="mt-1 text-lg font-semibold text-gray-900 truncate">
                {{ statistics.favoriteTrack || 'N/A' }}
              </div>
              <div class="mt-1 text-xs text-gray-600">
                Mastery: {{ statistics.favoriteTrackMastery?.toFixed(1) || 0 }}%
              </div>
            </div>
          </div>
        </div>

        <!-- Worst Track -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-red-100 rounded-md p-3">
              <svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Schwierigste Strecke</div>
              <div class="mt-1 text-lg font-semibold text-gray-900 truncate">
                {{ statistics.worstTrack || 'N/A' }}
              </div>
              <div class="mt-1 text-xs text-gray-600">
                Mastery: {{ statistics.worstTrackMastery?.toFixed(1) || 0 }}%
              </div>
            </div>
          </div>
        </div>

        <!-- Average Consistency -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-blue-100 rounded-md p-3">
              <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Ø Konsistenz</div>
              <div class="mt-1 text-lg font-semibold text-gray-900">
                {{ statistics.averageConsistency?.toFixed(1) || 0 }}%
              </div>
              <div class="mt-1">
                <ConsistencyBadge :score="statistics.averageConsistency" />
              </div>
            </div>
          </div>
        </div>

        <!-- Best Improvement -->
        <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-purple-100 rounded-md p-3">
              <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Beste Verbesserung</div>
              <div class="mt-1 text-lg font-semibold text-gray-900">
                {{ statistics.bestImprovementRate ? `+${statistics.bestImprovementRate.toFixed(1)}%` : 'N/A' }}
              </div>
              <div class="mt-1 text-xs text-gray-600 truncate">
                {{ statistics.bestImprovementTrack || '-' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Performance Overview Chart -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Performance Übersicht</h2>
        <TrackPerformanceChart :driver-id="driverId" />
      </div>

      <!-- Track Breakdown -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Strecken-Performance</h2>

        <div v-if="trackBreakdown && trackBreakdown.length > 0" class="space-y-4">
          <div
            v-for="track in trackBreakdown"
            :key="track.trackId"
            class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer"
            @click="navigateToTrack(track.trackId)"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex-1">
                <h3 class="font-semibold text-gray-900">{{ track.trackName }}</h3>
                <p class="text-sm text-gray-500">{{ track.lapCount }} Runden gefahren</p>
              </div>
              <div class="text-right">
                <div class="text-sm font-mono font-semibold text-gray-900">
                  {{ track.formattedBestLap }}
                </div>
                <div class="text-xs text-gray-500">Best Lap</div>
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4">
              <!-- Track Mastery -->
              <div>
                <div class="text-xs text-gray-500 mb-1">Track Mastery</div>
                <div class="flex items-center">
                  <div class="w-full bg-gray-200 rounded-full h-2 mr-2">
                    <div
                      class="bg-blue-600 h-2 rounded-full"
                      :style="{ width: `${track.trackMastery}%` }"
                    ></div>
                  </div>
                  <span class="text-xs font-semibold text-gray-700">
                    {{ track.trackMastery?.toFixed(0) }}%
                  </span>
                </div>
              </div>

              <!-- Consistency -->
              <div>
                <div class="text-xs text-gray-500 mb-1">Konsistenz</div>
                <ConsistencyBadge :score="track.consistencyScore" />
              </div>

              <!-- Avg Lap -->
              <div>
                <div class="text-xs text-gray-500 mb-1">Ø Lap Time</div>
                <div class="text-sm font-mono text-gray-700">
                  {{ track.formattedAvgLap }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-gray-500">
          <p>Keine Strecken-Daten verfügbar</p>
          <p class="text-sm mt-2">Fahre ein paar Runden um Statistiken zu sehen!</p>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Letzte Aktivität</h2>

        <div v-if="recentSessions && recentSessions.length > 0" class="space-y-3">
          <div
            v-for="session in recentSessions"
            :key="session.id"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
          >
            <div class="flex-1">
              <div class="font-medium text-gray-900">{{ session.trackName }}</div>
              <div class="text-sm text-gray-500">{{ session.carModel }} • {{ formatDate(session.date) }}</div>
            </div>
            <div class="text-right">
              <div class="text-sm font-mono font-semibold text-gray-900">
                {{ session.bestLapFormatted }}
              </div>
              <div class="text-xs text-gray-500">{{ session.totalLaps }} Runden</div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-gray-500">
          Keine letzten Sessions verfügbar
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDriverStatistics, getTrackStatistics } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ConsistencyBadge from '@/components/common/ConsistencyBadge.vue'
import TrackPerformanceChart from './TrackPerformanceChart.vue'
import { format } from 'date-fns'
import { de } from 'date-fns/locale'

const props = defineProps({
  driverId: {
    type: String,
    required: true
  }
})

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const statistics = ref(null)
const trackBreakdown = ref([])
const recentSessions = ref([])

onMounted(async () => {
  await loadDriverProfile()
})

const loadDriverProfile = async () => {
  loading.value = true
  error.value = null

  try {
    // Load driver statistics
    statistics.value = await getDriverStatistics(props.driverId)

    // Load track breakdown (mock data - should come from API)
    // TODO: Create API endpoint for this
    trackBreakdown.value = []

    // Load recent sessions (mock data - should come from API)
    // TODO: Create API endpoint for this
    recentSessions.value = []

  } catch (e) {
    error.value = e.message
    console.error('Error loading driver profile:', e)
  } finally {
    loading.value = false
  }
}

const navigateToTrack = (trackId) => {
  router.push(`/track/${trackId}`)
}

const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd. MMM yyyy', { locale: de })
}
</script>