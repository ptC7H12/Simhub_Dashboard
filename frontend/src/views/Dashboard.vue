<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-3xl font-bold text-gray-900">Dashboard</h1>
      <p class="mt-2 text-sm text-gray-600">Echtzeit-Übersicht deiner Racing-Sessions</p>
    </div>

    <!-- Active Sessions -->
    <ActiveSessions :sessions="activeSessions" />

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <QuickStats />
    </div>

    <!-- Live Telemetry -->
    <LiveTelemetry v-if="isReceivingData" :telemetry="liveTelemetry" />

    <!-- Top Drivers Preview -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold text-gray-900">Top Fahrer</h2>
        <router-link
          to="/leaderboard"
          class="text-blue-600 hover:text-blue-800 text-sm font-medium"
        >
          Alle anzeigen →
        </router-link>
      </div>

      <div class="space-y-3">
        <div
          v-for="(driver, index) in topDrivers"
          :key="driver.driverId"
          class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition cursor-pointer"
          @click="navigateToDriver(driver.driverId)"
        >
          <div class="flex items-center">
            <div class="w-8 h-8 bg-gray-900 text-white rounded-full flex items-center justify-center font-bold mr-3">
              {{ index + 1 }}
            </div>
            <div>
              <div class="font-medium">{{ driver.driverName }}</div>
              <div class="text-sm text-gray-500">
                Track Mastery: {{ driver.score?.toFixed(1) || 0 }}%
              </div>
            </div>
          </div>

          <div class="text-right">
            <div class="text-sm text-gray-600">
              {{ driver.details?.tracksCompleted || 0 }} Strecken
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import ActiveSessions from '@/components/dashboard/ActiveSessions.vue'
import QuickStats from '@/components/dashboard/QuickStats.vue'
import LiveTelemetry from '@/components/dashboard/LiveTelemetry.vue'

const router = useRouter()
const dashboardStore = useDashboardStore()

const activeSessions = computed(() => dashboardStore.activeSessions)
const topDrivers = computed(() => dashboardStore.topDrivers)
const liveTelemetry = computed(() => dashboardStore.liveTelemetry)
const isReceivingData = computed(() => dashboardStore.isReceivingData)

onMounted(() => {
  dashboardStore.fetchOverview()
})

const navigateToDriver = (driverId) => {
  router.push(`/driver/${driverId}`)
}
</script>