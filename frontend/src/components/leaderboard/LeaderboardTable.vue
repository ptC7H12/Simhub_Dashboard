<template>
  <div class="overflow-x-auto">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Pos
          </th>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Fahrer
          </th>
          <th v-if="showCarModel" scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Auto
          </th>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Best Lap
          </th>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Avg Lap
          </th>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
            Consistency
          </th>
          <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider text-center">
            Laps
          </th>
          <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
            Aktion
          </th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr
          v-for="(entry, index) in entries"
          :key="entry.driverId || index"
          class="hover:bg-gray-50 cursor-pointer transition"
          @click="handleRowClick(entry)"
        >
          <!-- Position -->
          <td class="px-6 py-4 whitespace-nowrap">
            <div class="flex items-center">
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-white"
                :class="getPositionColor(entry.position || index + 1)"
              >
                {{ entry.position || index + 1 }}
              </div>
            </div>
          </td>

          <!-- Driver Name -->
          <td class="px-6 py-4 whitespace-nowrap">
            <div class="flex items-center">
              <div class="ml-0">
                <div class="text-sm font-medium text-gray-900">
                  {{ entry.driverName }}
                </div>
              </div>
            </div>
          </td>

          <!-- Car Model (optional) -->
          <td v-if="showCarModel" class="px-6 py-4 whitespace-nowrap">
            <div class="text-sm text-gray-600">
              {{ entry.carModel || '-' }}
            </div>
          </td>

          <!-- Best Lap Time -->
          <td class="px-6 py-4 whitespace-nowrap">
            <div class="flex items-center">
              <span class="font-mono text-sm font-semibold text-gray-900">
                {{ entry.formattedLapTime || formatTime(entry.lapTime) }}
              </span>
              <span
                v-if="entry.isPersonalBest || (index === 0)"
                class="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800"
              >
                <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
                PB
              </span>
            </div>
          </td>

          <!-- Average Lap Time -->
          <td class="px-6 py-4 whitespace-nowrap">
            <span class="font-mono text-sm text-gray-600">
              {{ entry.formattedAvgLapTime || formatTime(entry.averageLapTime) }}
            </span>
          </td>

          <!-- Consistency Score -->
          <td class="px-6 py-4 whitespace-nowrap">
            <ConsistencyBadge :score="entry.consistencyScore" />
          </td>

          <!-- Lap Count -->
          <td class="px-6 py-4 whitespace-nowrap text-center">
            <span class="text-sm text-gray-900 font-medium">
              {{ entry.lapCount || 0 }}
            </span>
          </td>

          <!-- Actions -->
          <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
            <button
              @click.stop="$emit('driver-click', entry.driverId)"
              class="text-blue-600 hover:text-blue-900"
            >
              Details →
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State (if no entries but table is rendered) -->
    <div v-if="!entries || entries.length === 0" class="text-center py-8 text-gray-500">
      Keine Einträge gefunden
    </div>
  </div>
</template>

<script setup>
import ConsistencyBadge from '@/components/common/ConsistencyBadge.vue'

defineProps({
  entries: {
    type: Array,
    required: true,
    default: () => []
  },
  showCarModel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['driver-click', 'row-click'])

const handleRowClick = (entry) => {
  emit('row-click', entry)
}

const getPositionColor = (position) => {
  if (position === 1) return 'bg-yellow-500'
  if (position === 2) return 'bg-gray-400'
  if (position === 3) return 'bg-orange-600'
  return 'bg-gray-600'
}

const formatTime = (millis) => {
  if (!millis) return '-'

  const minutes = Math.floor(millis / 60000)
  const seconds = Math.floor((millis % 60000) / 1000)
  const ms = millis % 1000

  return `${minutes}:${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`
}
</script>

<style scoped>
/* Ensure table is responsive */
@media (max-width: 640px) {
  table {
    font-size: 0.875rem;
  }

  th, td {
    padding: 0.5rem;
  }
}
</style>