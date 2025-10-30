<template>
  <div class="bg-white shadow rounded-lg p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-semibold text-gray-900">Global Leaderboard</h2>
        <p class="text-sm text-gray-500 mt-1">Alle Strecken kombiniert</p>
      </div>
      <button
        @click="refresh"
        :disabled="isLoading"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50"
      >
        {{ isLoading ? 'Laden...' : 'Aktualisieren' }}
      </button>
    </div>

    <div v-if="isLoading" class="flex justify-center items-center py-12">
      <LoadingSpinner />
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
      <p class="font-medium">Fehler beim Laden</p>
      <p class="text-sm mt-1">{{ error }}</p>
    </div>

    <div v-else-if="!globalLeaderboard || globalLeaderboard.length === 0"
         class="text-center py-12 text-gray-500">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
      </svg>
      <p class="mt-4 text-lg font-medium">Keine Daten vorhanden</p>
      <p class="mt-2 text-sm">Fahre ein paar Runden um im Leaderboard zu erscheinen!</p>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="(entry, index) in globalLeaderboard"
        :key="entry.driverId"
        class="flex items-center justify-between p-4 rounded-lg hover:bg-gray-50 transition cursor-pointer"
        :class="index < 3 ? 'bg-gradient-to-r from-gray-50 to-white' : 'bg-white border border-gray-200'"
        @click="navigateToDriver(entry.driverId)"
      >
        <div class="flex items-center space-x-4">
          <div
            class="w-12 h-12 rounded-full flex items-center justify-center text-lg font-bold text-white"
            :class="getPositionColor(entry.position)"
          >
            {{ entry.position }}
          </div>

          <div>
            <div class="text-lg font-semibold text-gray-900">{{ entry.driverName }}</div>
            <div class="text-sm text-gray-600">
              {{ entry.details?.tracksCompleted || 0 }} Strecken gefahren
            </div>
          </div>
        </div>

        <div class="text-right">
          <div class="text-2xl font-bold text-gray-900">
            {{ entry.score?.toFixed(1) || 0 }}%
          </div>
          <div class="text-xs text-gray-500">Track Mastery</div>
        </div>
      </div>
    </div>

    <div v-if="globalLeaderboard && globalLeaderboard.length > 0" class="mt-6 pt-6 border-t border-gray-200">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="text-center">
          <div class="text-3xl font-bold text-yellow-600">🥇</div>
          <div class="mt-2 font-semibold text-gray-900">{{ globalLeaderboard[0]?.driverName }}</div>
          <div class="text-sm text-gray-600">{{ globalLeaderboard[0]?.score?.toFixed(1) }}% Mastery</div>
        </div>

        <div v-if="globalLeaderboard[1]" class="text-center">
          <div class="text-3xl font-bold text-gray-400">🥈</div>
          <div class="mt-2 font-semibold text-gray-900">{{ globalLeaderboard[1]?.driverName }}</div>
          <div class="text-sm text-gray-600">{{ globalLeaderboard[1]?.score?.toFixed(1) }}% Mastery</div>
        </div>

        <div v-if="globalLeaderboard[2]" class="text-center">
          <div class="text-3xl font-bold text-orange-600">🥉</div>
          <div class="mt-2 font-semibold text-gray-900">{{ globalLeaderboard[2]?.driverName }}</div>
          <div class="text-sm text-gray-600">{{ globalLeaderboard[2]?.score?.toFixed(1) }}% Mastery</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderboardStore } from '@/stores/leaderboard'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const leaderboardStore = useLeaderboardStore()

const globalLeaderboard = computed(() => leaderboardStore.globalLeaderboard)
const isLoading = computed(() => leaderboardStore.isLoading)
const error = computed(() => leaderboardStore.error)

onMounted(() => {
  leaderboardStore.fetchGlobalLeaderboard()
})

const refresh = () => {
  leaderboardStore.fetchGlobalLeaderboard()
}

const navigateToDriver = (driverId) => {
  router.push(`/driver/${driverId}`)
}

const getPositionColor = (position) => {
  if (position === 1) return 'bg-yellow-500'
  if (position === 2) return 'bg-gray-400'
  if (position === 3) return 'bg-orange-600'
  return 'bg-gray-600'
}
</script>