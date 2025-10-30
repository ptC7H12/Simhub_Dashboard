import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request Interceptor
api.interceptors.request.use(
  config => {
    // Add auth token if needed
    return config
  },
  error => Promise.reject(error)
)

// Response Interceptor
api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// Dashboard
export const getDashboardOverview = () => api.get('/dashboard/overview')
export const getLiveTelemetry = () => api.get('/dashboard/telemetry/live')

// Leaderboard
export const getGlobalLeaderboard = () => api.get('/leaderboard/global')
export const getTrackLeaderboard = (trackId, carModel = null) => {
  const params = carModel ? { carModel } : {}
  return api.get(`/leaderboard/track/${trackId}`, { params })
}
export const getCarComparison = (trackId, driverId) =>
  api.get(`/leaderboard/track/${trackId}/car-comparison`, { params: { driverId } })

// Statistics
export const getDriverStatistics = (driverId) => api.get(`/statistics/driver/${driverId}`)
export const getTrackStatistics = (trackId) => api.get(`/statistics/track/${trackId}`)
export const calculateStatistics = () => api.post('/statistics/calculate')

// Drivers & Tracks
export const getAllDrivers = () => api.get('/drivers')
export const getDriver = (id) => api.get(`/drivers/${id}`)
export const getAllTracks = () => api.get('/tracks')
export const getTrack = (id) => api.get(`/tracks/${id}`)

// Sessions
export const getSession = (id) => api.get(`/sessions/${id}`)
export const getPersonalProgress = (driverId, trackId) =>
  api.get('/sessions/progress', { params: { driverId, trackId } })

export default api