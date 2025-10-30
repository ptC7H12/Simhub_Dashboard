import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'
import Leaderboard from '@/views/Leaderboard.vue'
import Statistics from '@/views/Statistics.vue'
import Drivers from '@/views/Drivers.vue'
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
    path: '/statistics',
    name: 'Statistics',
    component: Statistics
  },
  {
    path: '/drivers',
    name: 'Drivers',
    component: Drivers
  },
  {
    path: '/driver/:id',
    name: 'DriverStats',
    component: DriverStats,
    props: true
  },
  {
    path: '/track/:id',
    name: 'TrackStats',
    component: TrackStats,
    props: true
  },
  {
    path: '/session/:id',
    name: 'SessionDetail',
    component: SessionDetail,
    props: true
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router