<template>
  <nav class="bg-gray-900 text-white shadow-lg">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <h1 class="text-2xl font-bold">🏁 Racing Stats</h1>
          </div>
        </div>

        <div class="flex items-center space-x-4">
          <div v-if="isReceivingData" class="flex items-center">
            <div class="w-3 h-3 bg-green-500 rounded-full animate-pulse mr-2"></div>
            <span class="text-sm">Live</span>
          </div>
          <div v-else class="flex items-center">
            <div class="w-3 h-3 bg-red-500 rounded-full mr-2"></div>
            <span class="text-sm">Offline</span>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()

const isReceivingData = computed(() => dashboardStore.isReceivingData)

onMounted(() => {
  dashboardStore.startLiveUpdates()
})

onUnmounted(() => {
  dashboardStore.stopLiveUpdates()
})
</script>