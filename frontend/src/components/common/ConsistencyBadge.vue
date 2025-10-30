<template>
  <span
    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
    :class="badgeClass"
  >
    {{ score?.toFixed(1) || 0 }}%
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: {
    type: Number,
    default: 0
  }
})

const badgeClass = computed(() => {
  if (props.score >= 90) return 'bg-green-100 text-green-800'
  if (props.score >= 75) return 'bg-blue-100 text-blue-800'
  if (props.score >= 60) return 'bg-yellow-100 text-yellow-800'
  return 'bg-red-100 text-red-800'
})
</script>