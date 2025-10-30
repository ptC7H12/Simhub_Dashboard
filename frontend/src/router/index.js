import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'
import Leaderboard from '@/views/Leaderboard.vue'
import DriverStats from '@/views/DriverStats.vue'
import TrackStats from '@/views/TrackStats.vue'
import SessionDetail from '@/views/SessionDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: Leaderboard
  },
  {
    path: '/driver/:id',
    name: 'DriverStats',
    component: DriverStats
  },
  {
    path: '/track/:id',
    name: 'TrackStats',
    component: TrackStats
  },
  {
    path: '/session/:id',
    name: 'SessionDetail',
    component: SessionDetail
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router