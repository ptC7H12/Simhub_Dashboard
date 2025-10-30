<template>
  <div class="h-80">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
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

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
  trackId: String,
  statistics: Array
})

const chartData = computed(() => {
  const labels = props.statistics.map(s => s.driverName)
  const bestLaps = props.statistics.map(s => s.bestLapTime / 1000) // Convert to seconds
  const avgLaps = props.statistics.map(s => s.averageLapTime / 1000)

  return {
    labels,
    datasets: [
      {
        label: 'Best Lap (s)',
        data: bestLaps,
        backgroundColor: 'rgba(59, 130, 246, 0.8)',
        borderColor: 'rgb(59, 130, 246)',
        borderWidth: 1
      },
      {
        label: 'Average Lap (s)',
        data: avgLaps,
        backgroundColor: 'rgba(156, 163, 175, 0.8)',
        borderColor: 'rgb(156, 163, 175)',
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
      beginAtZero: false,
      title: {
        display: true,
        text: 'Zeit (Sekunden)'
      }
    }
  }
}
</script>