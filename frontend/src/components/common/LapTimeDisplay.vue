<template>
  <span class="font-mono" :class="sizeClass">
    {{ formattedTime }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  milliseconds: {
    type: Number,
    required: true
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  }
})

const formattedTime = computed(() => {
  if (!props.milliseconds) return '-'

  const minutes = Math.floor(props.milliseconds / 60000)
  const seconds = Math.floor((props.milliseconds % 60000) / 1000)
  const ms = props.milliseconds % 1000

  return `${minutes}:${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`
})

const sizeClass = computed(() => {
  const sizes = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg'
  }
  return sizes[props.size]
})
</script>