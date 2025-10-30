<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Fahrer</h1>
        <p class="mt-2 text-sm text-gray-600">Übersicht aller registrierten Fahrer</p>
      </div>
      <button
        @click="refreshDrivers"
        :disabled="loading"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50"
      >
        {{ loading ? 'Laden...' : 'Aktualisieren' }}
      </button>
    </div>

    <!-- Search & Filter -->
    <div class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center space-x-4">
        <div class="flex-1">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Fahrer suchen..."
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <div>
          <select
            v-model="sortBy"
            class="px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="name">Name (A-Z)</option>
            <option value="created">Neueste zuerst</option>
            <option value="lastSeen">Zuletzt aktiv</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <LoadingSpinner />
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredDrivers.length === 0" class="bg-white rounded-lg shadow p-12 text-center">
      <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
      <h3 class="mt-4 text-lg font-medium text-gray-900">
        {{ searchQuery ? 'Keine Fahrer gefunden' : 'Keine Fahrer registriert' }}
      </h3>
      <p class="mt-2 text-sm text-gray-500">
        {{ searchQuery ? 'Versuche einen anderen Suchbegriff' : 'Starte ein Spiel um als Fahrer registriert zu werden' }}
      </p>
    </div>

    <!-- Drivers Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="driver in filteredDrivers"
        :key="driver.id"
        class="bg-white rounded-lg shadow hover:shadow-lg transition cursor-pointer group"
        @click="viewDriverProfile(driver.id)"
      >
        <div class="p-6">
          <!-- Driver Avatar/Initial -->
          <div class="flex items-center space-x-4 mb-4">
            <div class="flex-shrink-0">
              <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center">
                <span class="text-2xl font-bold text-white">
                  {{ getInitials(driver.displayName || driver.name) }}
                </span>
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-semibold text-gray-900 truncate group-hover:text-blue-600 transition">
                {{ driver.displayName || driver.name }}
              </h3>
              <p class="text-sm text-gray-500">
                ID: {{ driver.name }}
              </p>
            </div>
          </div>

          <!-- Driver Stats -->
          <div class="space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">Registriert</span>
              <span class="font-medium text-gray-900">{{ formatDate(driver.createdAt) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">Zuletzt gesehen</span>
              <span class="font-medium text-gray-900">
                {{ driver.lastSeen ? formatDate(driver.lastSeen) : 'Noch nie' }}
              </span>
            </div>
          </div>

          <!-- View Details Button -->
          <div class="mt-4 pt-4 border-t border-gray-100">
            <button class="w-full text-center text-blue-600 hover:text-blue-700 font-medium text-sm flex items-center justify-center group-hover:underline">
              Profil anzeigen
              <svg class="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination Info -->
    <div v-if="filteredDrivers.length > 0" class="bg-white rounded-lg shadow p-4">
      <div class="flex items-center justify-between">
        <div class="text-sm text-gray-600">
          Zeige {{ filteredDrivers.length }} von {{ drivers.length }} Fahrern
        </div>
        <div class="text-sm text-gray-500">
          {{ searchQuery ? `Gefiltert nach: "${searchQuery}"` : 'Alle Fahrer' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllDrivers } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { format } from 'date-fns'
import { de } from 'date-fns/locale'

const router = useRouter()
const loading = ref(false)
const drivers = ref([])
const searchQuery = ref('')
const sortBy = ref('name')

onMounted(async () => {
  await loadDrivers()
})

const loadDrivers = async () => {
  loading.value = true
  try {
    drivers.value = await getAllDrivers()
  } catch (error) {
    console.error('Error loading drivers:', error)
  } finally {
    loading.value = false
  }
}

const refreshDrivers = async () => {
  await loadDrivers()
}

const filteredDrivers = computed(() => {
  let result = [...drivers.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(driver =>
      (driver.displayName || driver.name).toLowerCase().includes(query) ||
      driver.name.toLowerCase().includes(query)
    )
  }

  // Sort
  if (sortBy.value === 'name') {
    result.sort((a, b) => {
      const nameA = (a.displayName || a.name).toLowerCase()
      const nameB = (b.displayName || b.name).toLowerCase()
      return nameA.localeCompare(nameB)
    })
  } else if (sortBy.value === 'created') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } else if (sortBy.value === 'lastSeen') {
    result.sort((a, b) => {
      if (!a.lastSeen) return 1
      if (!b.lastSeen) return -1
      return new Date(b.lastSeen) - new Date(a.lastSeen)
    })
  }

  return result
})

const viewDriverProfile = (driverId) => {
  router.push(`/driver/${driverId}`)
}

const getInitials = (name) => {
  if (!name) return '??'
  const parts = name.split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
}

const formatDate = (date) => {
  if (!date) return '-'
  return format(new Date(date), 'dd.MM.yyyy', { locale: de })
}
</script>