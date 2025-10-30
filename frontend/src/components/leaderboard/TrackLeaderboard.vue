<template>
  <div class="bg-white shadow rounded-lg p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900">Track Leaderboard</h2>
        <p class="text-sm text-gray-500 mt-1">
          {{ carModel ? `Nur ${carModel}` : 'Alle Autos' }}
        </p>
      </div>
      <button
        @click="refresh"
        :disabled="isLoading"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50"
      >
        {{ isLoading ? 'Laden...' : 'Aktualisieren' }}
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <LoadingSpinner />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
      <p class="font-medium">Fehler beim Laden des Leaderboards</p>
      <p class="text-sm mt-1">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!trackLeaderboard || trackLeaderboard.length === 0"
         class="text-center py-12 text-gray-500">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="mt-4 text-lg font-medium">Keine Daten für diese Strecke</p>
      <p class="mt-2 text-sm">Wähle eine andere Strecke oder fahre ein paar Runden!</p>
    </div>

    <!-- Leaderboard Table -->
    <LeaderboardTable
      v-else
      :entries="trackLeaderboard"
      :show-car-model="!carModel"
      @driver-click="handleDriverClick"
    />

    <!-- Track Info & Stats -->
    <div v-if="trackLeaderboard && trackLeaderboard.length > 0"
         class="mt-6 grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-blue-50 rounded-lg p-4">
        <div class="text-sm text-blue-700 font-medium">Bester Fahrer</div>
        <div class="text-xl font-bold text-blue-900 truncate">
          {{ trackLeaderboard[0]?.driverName || '-' }}
        </div>
        <div class="text-xs text-blue-600 mt-1 font-mono">
          {{ trackLeaderboard[0]?.formattedLapTime || '-' }}
        </div>
      </div>

      <div class="bg-green-50 rounded-lg p-4">
        <div class="text-sm text-green-700 font-medium">Beste Zeit</div>
        <div class="text-xl font-bold text-green-900 font-mono">
          {{ trackLeaderboard[0]?.formattedLapTime || '-' }}
        </div>
        <div class="text-xs text-green-600 mt-1">
          {{ trackLeaderboard[0]?.lapCount || 0 }} Runden
        </div>
      </div>

      <div class="bg-yellow-50 rounded-lg p-4">
        <div class="text-sm text-yellow-700 font-medium">Teilnehmer</div>
        <div class="text-xl font-bold text-yellow-900">
          {{ trackLeaderboard.length }}
        </div>
        <div class="text-xs text-yellow-600 mt-1">
          {{ getTotalLaps() }} Runden gesamt
        </div>
      </div>

      <div class="bg-purple-50 rounded-lg p-4">
        <div class="text-sm text-purple-700 font-medium">Ø Consistency</div>
        <div class="text-xl font-bold text-purple-900">
          {{ getAverageConsistency() }}%
        </div>
        <div class="text-xs text-purple-600 mt-1">
          Durchschnitt aller Fahrer
        </div>
      </div>
    </div>

    <!-- Gap Analysis -->
    <div v-if="trackLeaderboard && trackLeaderboard.length > 1" class="mt-6">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">Gap zur Spitze</h3>
      <div class="space-y-2">
        <div
          v-for="(entry, index) in trackLeaderboard.slice(0, 5)"
          :key="entry.driverId"
          class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
        >
          <div class="flex items-center space-x-3">
            <div
              class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold"
              :class="index === 0 ? 'bg-yellow-500 text-white' : 'bg-gray-300 text-gray-700'"
            >
              {{ index + 1 }}
            </div>
            <span class="font-medium text-gray-900">{{ entry.driverName }}</span>
          </div>

          <div class="flex items-center space-x-4">
            <span class="font-mono text-sm text-gray-700">{{ entry.formattedLapTime }}</span>
            <span
              v-if="index > 0"
              class="text-xs font-medium px-2 py-1 bg-red-100 text-red-800 rounded"
            >
              +{{ calculateGap(entry.lapTime, trackLeaderboard[0].lapTime) }}
            </span>
            <span
              v-else
              class="text-xs font-medium px-2 py-1 bg-green-100 text-green-800 rounded"
            >
              Leader
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderboardStore } from '@/stores/leaderboard'
import LeaderboardTable from './LeaderboardTable.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const props = defineProps({
  trackId: {
    type: String,
    required: true
  },
  carModel: {
    type: String,
    default: null
  }
})

const router = useRouter()
const leaderboardStore = useLeaderboardStore()

const trackLeaderboard = computed(() => leaderboardStore.trackLeaderboard)
const isLoading = computed(() => leaderboardStore.isLoading)
const error = computed(() => leaderboardStore.error)

// Watch for prop changes and reload
watch(() => [props.trackId, props.carModel], () => {
  loadLeaderboard()
}, { immediate: true })

const loadLeaderboard = () => {
  if (props.trackId) {
    leaderboardStore.fetchTrackLeaderboard(props.trackId, props.carModel)
  }
}

const refresh = () => {
  loadLeaderboard()
}

const handleDriverClick = (driverId) => {
  router.push(`/driver/${driverId}`)
}

const getTotalLaps = () => {
  if (!trackLeaderboard.value) return 0
  return trackLeaderboard.value.reduce((sum, entry) => sum + (entry.lapCount || 0), 0)
}

const getAverageConsistency = () => {
  if (!trackLeaderboard.value || trackLeaderboard.value.length === 0) return 0

  const sum = trackLeaderboard.value.reduce((acc, entry) => acc + (entry.consistencyScore || 0), 0)
  return (sum / trackLeaderboard.value.length).toFixed(1)
}

const calculateGap = (lapTime, bestLapTime) => {
  const gapMs = lapTime - bestLapTime
  const seconds = Math.floor(gapMs / 1000)
  const ms = gapMs % 1000

  if (seconds > 0) {
    return `${seconds}.${ms.toString().padStart(3, '0')}s`
  } else {
    return `${ms}ms`
  }
}
</script>