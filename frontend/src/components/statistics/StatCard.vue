<template>
  <div class="p-4 bg-white rounded-lg shadow flex items-center">
    <div class="w-12 h-12 flex items-center justify-center bg-gray-100 rounded-full mr-4 text-indigo-600">
      <template v-if="icon === 'trophy'">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V4h8v3M5 7h14a1 1 0 011 1v2a5 5 0 01-5 5H9a5 5 0 01-5-5V8a1 1 0 011-1zM9 19h6" />
        </svg>
      </template>
      <template v-else-if="icon === 'warning'">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.29 3.86L1.82 18a1 1 0 00.86 1.5h18.64a1 1 0 00.86-1.5L13.71 3.86a1 1 0 00-1.72 0zM12 9v4m0 4h.01" />
        </svg>
      </template>
      <template v-else-if="icon === 'chart'">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 17a1 1 0 001 1h6M3 3v18h18" />
        </svg>
      </template>
      <template v-else-if="icon === 'target'">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3M12 2a10 10 0 100 20 10 10 0 000-20z" />
        </svg>
      </template>
      <template v-else>
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-6a2 2 0 012-2h2a2 2 0 012 2v6" />
        </svg>
      </template>
    </div>

    <div class="flex-1">
      <div class="text-sm text-gray-500">{{ title }}</div>
      <div class="text-2xl font-bold text-gray-900">{{ value }}</div>
      <div v-if="subtitle" class="text-xs text-gray-400 mt-1">{{ subtitle }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  value: { type: [String, Number], default: '' },
  subtitle: { type: String, default: '' },
  icon: { type: String, default: '' }
})
</script>