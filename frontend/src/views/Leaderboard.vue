<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Leaderboard</h1>
      <p class="mt-2 text-sm text-gray-600">Vergleiche deine Performance mit anderen Fahrern</p>
    </div>

    <!-- Tabs -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex space-x-8">
        <button
          @click="activeTab = 'global'"
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm',
            activeTab === 'global'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
        >
          Global Leaderboard
        </button>
        <button
          @click="activeTab = 'track'"
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm',
            activeTab === 'track'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
        >
          Track Leaderboard
        </button>
      </nav>
    </div>

    <!-- Global Leaderboard -->
    <GlobalLeaderboard v-if="activeTab === 'global'" />

    <!-- Track Leaderboard -->
    <div v-else-if="activeTab === 'track'" class="space-y-4">
      <!-- Track & Car Selection -->
      <div class="bg-white shadow rounded-lg p-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Strecke</label>
            <select
              v-model="selectedTrackId"
              class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              @change="loadTrackLeaderboard"
            >
              <option value="">Strecke wählen...</option>
              <option v-for="track in tracks" :key="track.id" :value="track.id">
                {{ track.name }} ({{ track.game }})
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Auto (optional)</label>
            <select
              v-model="selectedCarModel"
              class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              @change="loadTrackLeaderboard"
            >
              <option value="">Alle Autos</option>
              <option v-for="car in availableCars" :key="car" :value="car">
                {{ car }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <TrackLeaderboard
        v-if="selectedTrackId"
        :track-id="selectedTrackId"
        :car-model="selectedCarModel"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useLeaderboardStore } from '@/stores/leaderboard'
import { getAllTracks } from '@/services/api'
import GlobalLeaderboard from '@/components/leaderboard/GlobalLeaderboard.vue'
import TrackLeaderboard from '@/components/leaderboard/TrackLeaderboard.vue'

const leaderboardStore = useLeaderboardStore()

const activeTab = ref('global')
const tracks = ref([])
const selectedTrackId = ref('')
const selectedCarModel = ref('')

const availableCars = computed(() => {
  // Extract unique cars from track leaderboard
  if (!leaderboardStore.trackLeaderboard) return []
  return [...new Set(leaderboardStore.trackLeaderboard.map(entry => entry.carModel))]
    .filter(Boolean)
    .sort()
})

onMounted(async () => {
  tracks.value = await getAllTracks()
})

const loadTrackLeaderboard = () => {
  if (selectedTrackId.value) {
    leaderboardStore.fetchTrackLeaderboard(
      selectedTrackId.value,
      selectedCarModel.value || null
    )
  }
}
</script>