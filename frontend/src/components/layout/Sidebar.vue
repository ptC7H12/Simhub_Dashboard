<template>
  <aside class="w-64 bg-white shadow-md min-h-screen">
    <nav class="mt-5 px-2">
      <router-link
        v-for="item in navigation"
        :key="item.name"
        :to="item.path"
        class="group flex items-center px-2 py-2 text-base font-medium rounded-md hover:bg-gray-100 mb-1 transition"
        :class="isActive(item.path) ? 'bg-gray-100 text-gray-900' : 'text-gray-600'"
      >
        <component :is="item.icon" class="mr-4 h-6 w-6" />
        {{ item.name }}
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeIcon,
  TrophyIcon,
  ChartBarIcon,
  UserGroupIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()

const navigation = [
  { name: 'Dashboard', path: '/', icon: HomeIcon },
  { name: 'Leaderboard', path: '/leaderboard', icon: TrophyIcon },
  { name: 'Statistics', path: '/statistics', icon: ChartBarIcon },
  { name: 'Drivers', path: '/drivers', icon: UserGroupIcon }
]

const isActive = (path) => {
  // Exact match for home
  if (path === '/') {
    return route.path === '/'
  }
  // Partial match for other routes
  return route.path.startsWith(path)
}
</script>