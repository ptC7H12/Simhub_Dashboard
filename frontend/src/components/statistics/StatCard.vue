<template>
  <div class="bg-white rounded-lg shadow p-6 hover:shadow-lg transition">
    <div class="flex items-center">
      <!-- Icon -->
      <div class="flex-shrink-0 rounded-md p-3" :class="iconBgClass">
        <component :is="iconComponent" class="h-6 w-6" :class="iconColorClass" />
      </div>

      <!-- Content -->
      <div class="ml-4 flex-1">
        <div class="text-sm font-medium text-gray-500">{{ title }}</div>
        <div class="mt-1 text-2xl font-semibold text-gray-900">
          {{ value }}
        </div>
        <div v-if="subtitle" class="mt-1 text-xs text-gray-600">
          {{ subtitle }}
        </div>
      </div>
    </div>

    <!-- Optional Footer/Badge -->
    <div v-if="badge" class="mt-4 pt-4 border-t border-gray-100">
      <span
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
        :class="badgeClass"
      >
        {{ badge }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  TrophyIcon,
  ExclamationTriangleIcon,
  ChartBarIcon,
  ArrowTrendingUpIcon,
  ClockIcon,
  UserIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'chart',
    validator: (value) => ['trophy', 'warning', 'chart', 'trend', 'clock', 'user', 'target'].includes(value)
  },
  badge: {
    type: String,
    default: ''
  },
  badgeVariant: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'warning', 'error', 'info'].includes(value)
  }
})

// Icon Component Mapping
const iconComponent = computed(() => {
  const icons = {
    trophy: TrophyIcon,
    warning: ExclamationTriangleIcon,
    chart: ChartBarIcon,
    trend: ArrowTrendingUpIcon,
    clock: ClockIcon,
    user: UserIcon,
    target: ChartBarIcon
  }
  return icons[props.icon] || ChartBarIcon
})

// Icon Background Color
const iconBgClass = computed(() => {
  const bgColors = {
    trophy: 'bg-yellow-100',
    warning: 'bg-red-100',
    chart: 'bg-blue-100',
    trend: 'bg-green-100',
    clock: 'bg-purple-100',
    user: 'bg-indigo-100',
    target: 'bg-pink-100'
  }
  return bgColors[props.icon] || 'bg-gray-100'
})

// Icon Color
const iconColorClass = computed(() => {
  const iconColors = {
    trophy: 'text-yellow-600',
    warning: 'text-red-600',
    chart: 'text-blue-600',
    trend: 'text-green-600',
    clock: 'text-purple-600',
    user: 'text-indigo-600',
    target: 'text-pink-600'
  }
  return iconColors[props.icon] || 'text-gray-600'
})

// Badge Color Classes
const badgeClass = computed(() => {
  const variants = {
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    error: 'bg-red-100 text-red-800',
    info: 'bg-blue-100 text-blue-800'
  }
  return variants[props.badgeVariant] || variants.info
})
</script>