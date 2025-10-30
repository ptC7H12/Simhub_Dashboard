package com.racingstats.controller.api

import com.racingstats.service.*
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/dashboard')
@CrossOrigin(origins = ['http://localhost:5173']) // Vue.js dev server
@Slf4j
class DashboardApiController {

    private final SessionService sessionService
    private final StatisticsService statisticsService
    private final LeaderboardService leaderboardService

    DashboardApiController(
            SessionService sessionService,
            StatisticsService statisticsService,
            LeaderboardService leaderboardService
    ) {
        this.sessionService = sessionService
        this.statisticsService = statisticsService
        this.leaderboardService = leaderboardService
    }

    /**
     * Dashboard Overview
     */
    @GetMapping('/overview')
    ResponseEntity<Map<String, Object>> getOverview() {
        try {
            def activeSessions = sessionService.activeSessions
            def liveTelemetry = sessionService.liveTelemetry
            def globalLeaderboard = leaderboardService.globalLeaderboard.take(5)

            return ResponseEntity.ok([
                    activeSessions: activeSessions.collect { [
                            id: it.id,
                            driverName: it.driver.displayNameOrName,
                            trackName: it.track.name,
                            carModel: it.carModel,
                            currentLap: it.totalLaps + 1,
                            bestLap: it.bestLapTime ? formatTime(it.bestLapTime) : '-'
                    ]},
                    liveTelemetry: liveTelemetry,
                    topDrivers: globalLeaderboard,
                    stats: [
                            totalActiveSessions: activeSessions.size(),
                            isReceivingData: !liveTelemetry.isEmpty()
                    ]
            ])
        } catch (Exception e) {
            log.error("Error getting dashboard overview", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Live Telemetry Stream (für WebSocket später)
     */
    @GetMapping('/telemetry/live')
    ResponseEntity<Map<String, Object>> getLiveTelemetry() {
        return ResponseEntity.ok(sessionService.liveTelemetry)
    }

    private String formatTime(Long millis) {
        if (!millis) return "-"
        long minutes = millis / 60000
        long seconds = (millis % 60000) / 1000
        long ms = millis % 1000
        return String.format("%d:%02d.%03d", minutes, seconds, ms)
    }
}