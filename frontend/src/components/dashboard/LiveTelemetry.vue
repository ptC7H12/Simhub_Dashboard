<template>
  <div class="bg-white shadow rounded-lg p-6">
    <h2 class="text-xl font-semibold text-gray-900 mb-4">Live Telemetrie</h2>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="(data, key) in telemetry" :key="key" class="bg-gray-50 rounded-lg p-4">
        <div class="text-xs text-gray-500 uppercase tracking-wide mb-1">Session {{ key }}</div>

        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Speed</span>
            <span class="text-lg font-bold text-gray-900">{{ Math.round(data.speed) }} km/h</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">Gear</span>
            <span class="text-lg font-bold text-gray-900">{{ data.gear }}</span>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-600">RPM</span>
            <span class="text-lg font-bold text-gray-900">{{ data.rpm }}</span>
          </div>

          <!-- RPM Bar -->
          <div class="w-full bg-gray-200 rounded-full h-2">
            <div
              class="bg-blue-600 h-2 rounded-full transition-all duration-200"
              :style="{ width: `${(data.rpm / 8000) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  telemetry: {
    type: Object,
    default: () => ({})
  }
})
</script>