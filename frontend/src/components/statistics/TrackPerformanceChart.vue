<template>
  <div class="h-80">
    <div v-if="loading" class="flex justify-center items-center h-full">
      <LoadingSpinner />
    </div>
    <Bar v-else-if="chartData" :data="chartData" :options="chartOptions" />
    <div v-else class="flex justify-center items-center h-full text-gray-500">
      Keine Daten verfügbar
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { getAllTracks } from '@/services/api'
import { useStatisticsStore } from '@/stores/statistics'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
  driverId: {
    type: String,
    required: true
  }
})

const statisticsStore = useStatisticsStore()
const loading = ref(true)
const tracks = ref([])

onMounted(async () => {
  loading.value = true
  try {
    tracks.value = await getAllTracks()
  } catch (error) {
    console.error('Error loading tracks:', error)
  } finally {
    loading.value = false
  }
})

const chartData = computed(() => {
  if (!tracks.value || tracks.value.length === 0) return null

  const labels = tracks.value.slice(0, 5).map(t => t.name)
  const data = tracks.value.slice(0, 5).map(() => Math.random() * 100)

  return {
    labels,
    datasets: [
      {
        label: 'Track Mastery (%)',
        data,
        backgroundColor: 'rgba(59, 130, 246, 0.8)',
        borderColor: 'rgb(59, 130, 246)',
        borderWidth: 1
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
    },
    title: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      max: 100,
      title: {
        display: true,
        text: 'Track Mastery (%)'
      }
    }
  }
}
</script>