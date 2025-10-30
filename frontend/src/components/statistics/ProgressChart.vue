<template>
  <div class="h-80">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { format } from 'date-fns'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

const props = defineProps({
  progressData: {
    type: Array,
    required: true
  }
})

const chartData = computed(() => {
  const labels = props.progressData.map(d => format(new Date(d.date), 'dd.MM.yy'))
  const bestLaps = props.progressData.map(d => d.bestLap / 1000)
  const avgLaps = props.progressData.map(d => d.avgLap / 1000)

  return {
    labels,
    datasets: [
      {
        label: 'Best Lap',
        data: bestLaps,
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        tension: 0.3,
        fill: true
      },
      {
        label: 'Average Lap',
        data: avgLaps,
        borderColor: 'rgb(156, 163, 175)',
        backgroundColor: 'rgba(156, 163, 175, 0.1)',
        tension: 0.3,
        fill: true
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
    tooltip: {
      callbacks: {
        label: function(context) {
          return `${context.dataset.label}: ${context.parsed.y.toFixed(3)}s`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: false,
      title: {
        display: true,
        text: 'Lap Time (s)'
      }
    },
    x: {
      title: {
        display: true,
        text: 'Session Date'
      }
    }
  }
}
</script>