<template>
  <div v-for="stat in stats" :key="stat.title" class="bg-white shadow rounded-lg p-6">
    <div class="flex items-center">
      <div class="flex-shrink-0">
        <component :is="stat.icon" class="h-8 w-8 text-gray-400" />
      </div>
      <div class="ml-5 w-0 flex-1">
        <dl>
          <dt class="text-sm font-medium text-gray-500 truncate">
            {{ stat.title }}
          </dt>
          <dd class="flex items-baseline">
            <div class="text-2xl font-semibold text-gray-900">
              {{ stat.value }}
            </div>
            <div v-if="stat.change" class="ml-2 flex items-baseline text-sm font-semibold"
                 :class="stat.change >= 0 ? 'text-green-600' : 'text-red-600'">
              {{ stat.change >= 0 ? '+' : '' }}{{ stat.change }}%
            </div>
          </dd>
        </dl>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import {
  ChartBarIcon,
  ClockIcon,
  TrophyIcon,
  UserGroupIcon
} from '@heroicons/vue/24/outline'

const dashboardStore = useDashboardStore()

const stats = computed(() => [
  {
    title: 'Active Sessions',
    value: dashboardStore.activeSessions.length,
    icon: ChartBarIcon
  },
  {
    title: 'Total Drivers',
    value: dashboardStore.topDrivers.length,
    icon: UserGroupIcon
  },
  {
    title: 'Today\'s Laps',
    value: '0', // TODO: Calculate from API
    icon: ClockIcon
  },
  {
    title: 'Best Performance',
    value: dashboardStore.topDrivers[0]?.score?.toFixed(0) + '%' || 'N/A',
    icon: TrophyIcon
  }
])
</script>