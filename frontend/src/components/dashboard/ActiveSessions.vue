<template>
  <div class="bg-white shadow rounded-lg p-6">
    <h2 class="text-xl font-semibold text-gray-900 mb-4">Aktive Sessions</h2>

    <div v-if="sessions.length === 0" class="text-center py-8 text-gray-500">
      Keine aktiven Sessions. Starte ein Spiel um Daten zu empfangen.
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="session in sessions"
        :key="session.id"
        class="flex items-center justify-between p-4 bg-green-50 border border-green-200 rounded-lg"
      >
        <div class="flex-1">
          <div class="flex items-center">
            <div class="w-2 h-2 bg-green-500 rounded-full animate-pulse mr-2"></div>
            <span class="font-medium text-gray-900">{{ session.driverName }}</span>
          </div>
          <div class="mt-1 text-sm text-gray-600">
            {{ session.trackName }} • {{ session.carModel }}
          </div>
        </div>

        <div class="text-right">
          <div class="text-sm font-medium text-gray-900">Lap {{ session.currentLap }}</div>
          <div class="text-xs text-gray-600">Best: {{ session.bestLap }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  sessions: {
    type: Array,
    default: () => []
  }
})
</script>