package com.racingstats.controller.api

import com.racingstats.service.StatisticsService
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/statistics')
@CrossOrigin(origins = ['http://localhost:5173'])
@Slf4j
class StatisticsApiController {

    private final StatisticsService statisticsService

    StatisticsApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService
    }

    /**
     * Driver-Statistiken
     */
    @GetMapping('/driver/{driverId}')
    ResponseEntity<?> getDriverStatistics(@PathVariable UUID driverId) {
        try {
            def stats = statisticsService.getDriverStatistics(driverId)
            return ResponseEntity.ok(stats)
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build()
        } catch (Exception e) {
            log.error("Error getting driver statistics", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }

    /**
     * Track-Statistiken (alle Fahrer)
     */
    @GetMapping('/track/{trackId}')
    ResponseEntity<?> getTrackStatistics(@PathVariable UUID trackId) {
        try {
            def stats = statisticsService.getTrackStatistics(trackId)
            return ResponseEntity.ok(stats)
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build()
        } catch (Exception e) {
            log.error("Error getting track statistics", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }

    /**
     * Trigger manual statistics calculation
     */
    @PostMapping('/calculate')
    ResponseEntity<?> calculateStatistics() {
        try {
            statisticsService.calculateAllStatistics()
            return ResponseEntity.ok([message: 'Statistics calculation triggered'])
        } catch (Exception e) {
            log.error("Error calculating statistics", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }
}