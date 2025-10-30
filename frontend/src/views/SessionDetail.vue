<template>
  <div v-if="loading" class="flex justify-center items-center h-64">
    <LoadingSpinner />
  </div>

  <div v-else-if="session" class="space-y-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Session Details</h1>
          <p class="mt-1 text-sm text-gray-600">
            {{ session.driver.name }} • {{ session.track.name }} • {{ session.carModel }}
          </p>
        </div>
        <div class="text-right">
          <div class="text-sm text-gray-500">{{ formatDate(session.startedAt) }}</div>
          <div :class="session.isActive ? 'text-green-600' : 'text-gray-500'" class="text-sm font-medium">
            {{ session.isActive ? 'Live' : 'Beendet' }}
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white shadow rounded-lg p-6">
        <div class="text-sm text-gray-500">Total Laps</div>
        <div class="text-2xl font-bold text-gray-900">{{ session.totalLaps }}</div>
        <div class="text-xs text-gray-600">{{ session.validLaps }} gültig</div>
      </div>

      <div class="bg-white shadow rounded-lg p-6">
        <div class="text-sm text-gray-500">Best Lap</div>
        <div class="text-2xl font-bold text-gray-900 font-mono">
          {{ formatTime(session.bestLapTime) }}
        </div>
      </div>

      <div class="bg-white shadow rounded-lg p-6">
        <div class="text-sm text-gray-500">Average Lap</div>
        <div class="text-2xl font-bold text-gray-900 font-mono">
          {{ formatTime(session.averageLapTime) }}
        </div>
      </div>

      <div class="bg-white shadow rounded-lg p-6">
        <div class="text-sm text-gray-500">Session Type</div>
        <div class="text-2xl font-bold text-gray-900">{{ session.sessionType }}</div>
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-6">
      <h2 class="text-xl font-semibold text-gray-900 mb-4">Laps</h2>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Lap</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Time</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Valid</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Personal Best</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Max Speed</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="lap in session.laps" :key="lap.lapNumber" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ lap.lapNumber }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-mono text-gray-900">{{ lap.formattedTime }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="lap.isValid" class="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded">
                  Valid
                </span>
                <span v-else class="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 rounded">
                  Invalid
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="lap.isPersonalBest" class="text-yellow-600">
                  <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-600">{{ lap.maxSpeed || '-' }} km/h</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSession } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { format } from 'date-fns'
import { de } from 'date-fns/locale'

const route = useRoute()
const sessionId = ref(route.params.id)
const session = ref(null)
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    session.value = await getSession(sessionId.value)
  } catch (error) {
    console.error('Error loading session:', error)
  } finally {
    loading.value = false
  }
})

const formatTime = (millis) => {
  if (!millis) return '-'
  const minutes = Math.floor(millis / 60000)
  const seconds = Math.floor((millis % 60000) / 1000)
  const ms = millis % 1000
  return `${minutes}:${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`
}

const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd. MMM yyyy, HH:mm', { locale: de })
}
</script>