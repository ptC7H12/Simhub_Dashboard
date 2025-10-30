<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Statistiken</h1>
      <p class="mt-2 text-sm text-gray-600">Detaillierte Analysen und Performance-Übersichten</p>
    </div>

    <!-- Tab Navigation -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex space-x-8">
        <button
          @click="activeTab = 'overview'"
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm transition',
            activeTab === 'overview'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
        >
          Übersicht
        </button>
        <button
          @click="activeTab = 'drivers'"
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm transition',
            activeTab === 'drivers'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
        >
          Fahrer-Statistiken
        </button>
        <button
          @click="activeTab = 'tracks'"
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm transition',
            activeTab === 'tracks'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
        >
          Strecken-Statistiken
        </button>
      </nav>
    </div>

    <!-- Overview Tab -->
    <div v-if="activeTab === 'overview'" class="space-y-6">
      <!-- Quick Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-blue-100 rounded-md p-3">
              <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Registrierte Fahrer</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">{{ totalDrivers }}</div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-green-100 rounded-md p-3">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Verfügbare Strecken</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">{{ totalTracks }}</div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-purple-100 rounded-md p-3">
              <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Gesamt Sessions</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">{{ totalSessions }}</div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="flex-shrink-0 bg-yellow-100 rounded-md p-3">
              <svg class="h-6 w-6 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <div class="ml-4 flex-1">
              <div class="text-sm font-medium text-gray-500">Gesamtrunden</div>
              <div class="mt-1 text-2xl font-semibold text-gray-900">{{ totalLaps }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Letzte Aktivität</h2>
        <div class="text-center py-8 text-gray-500">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <p class="mt-4">Keine letzten Aktivitäten</p>
          <p class="text-sm mt-2">Starte eine Session um Daten zu sehen</p>
        </div>
      </div>
    </div>

    <!-- Drivers Tab -->
    <div v-if="activeTab === 'drivers'" class="space-y-6">
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Fahrer-Auswahl</h2>

        <div v-if="loading" class="flex justify-center items-center py-12">
          <LoadingSpinner />
        </div>

        <div v-else-if="drivers.length === 0" class="text-center py-12 text-gray-500">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <p class="mt-4 text-lg font-medium">Keine Fahrer registriert</p>
          <p class="text-sm mt-2">Starte ein Spiel um als Fahrer registriert zu werden</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="driver in drivers"
            :key="driver.id"
            class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer"
            @click="viewDriverStats(driver.id)"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <h3 class="font-semibold text-gray-900">{{ driver.displayName || driver.name }}</h3>
                <p class="text-sm text-gray-500 mt-1">
                  Registriert: {{ formatDate(driver.createdAt) }}
                </p>
              </div>
              <div class="ml-4">
                <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tracks Tab -->
    <div v-if="activeTab === 'tracks'" class="space-y-6">
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">Strecken-Auswahl</h2>

        <div v-if="loading" class="flex justify-center items-center py-12">
          <LoadingSpinner />
        </div>

        <div v-else-if="tracks.length === 0" class="text-center py-12 text-gray-500">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
          </svg>
          <p class="mt-4 text-lg font-medium">Keine Strecken verfügbar</p>
          <p class="text-sm mt-2">Fahre eine Session um Strecken zu registrieren</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="track in tracks"
            :key="track.id"
            class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer"
            @click="viewTrackStats(track.id)"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <h3 class="font-semibold text-gray-900">{{ track.name }}</h3>
                <div class="flex items-center mt-2 space-x-2">
                  <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                    {{ track.game }}
                  </span>
                  <span v-if="track.trackLength" class="text-sm text-gray-500">
                    {{ (track.trackLength / 1000).toFixed(2) }} km
                  </span>
                </div>
              </div>
              <div class="ml-4">
                <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllDrivers, getAllTracks } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { format } from 'date-fns'
import { de } from 'date-fns/locale'

const router = useRouter()
const activeTab = ref('overview')
const loading = ref(false)
const drivers = ref([])
const tracks = ref([])

const totalDrivers = computed(() => drivers.value.length)
const totalTracks = computed(() => tracks.value.length)
const totalSessions = computed(() => 0) // TODO: Add API endpoint
const totalLaps = computed(() => 0) // TODO: Add API endpoint

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    [drivers.value, tracks.value] = await Promise.all([
      getAllDrivers(),
      getAllTracks()
    ])
  } catch (error) {
    console.error('Error loading statistics data:', error)
  } finally {
    loading.value = false
  }
}

const viewDriverStats = (driverId) => {
  router.push(`/driver/${driverId}`)
}

const viewTrackStats = (trackId) => {
  router.push(`/track/${trackId}`)
}

const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd.MM.yyyy', { locale: de })
}
</script>