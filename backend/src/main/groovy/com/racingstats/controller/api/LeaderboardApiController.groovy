package com.racingstats.controller.api

import com.racingstats.service.LeaderboardService
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/leaderboard')
@CrossOrigin(origins = ['http://localhost:5173'])
@Slf4j
class LeaderboardApiController {

    private final LeaderboardService leaderboardService

    LeaderboardApiController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService
    }

    /**
     * Global Leaderboard
     */
    @GetMapping('/global')
    ResponseEntity<?> getGlobalLeaderboard() {
        try {
            def leaderboard = leaderboardService.globalLeaderboard
            return ResponseEntity.ok(leaderboard)
        } catch (Exception e) {
            log.error("Error getting global leaderboard", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }

    /**
     * Track-spezifisches Leaderboard
     */
    @GetMapping('/track/{trackId}')
    ResponseEntity<?> getTrackLeaderboard(
            @PathVariable UUID trackId,
            @RequestParam(required = false) String carModel
    ) {
        try {
            def leaderboard = leaderboardService.getTrackLeaderboard(trackId, carModel)
            return ResponseEntity.ok(leaderboard)
        } catch (Exception e) {
            log.error("Error getting track leaderboard", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }

    /**
     * Auto-Vergleich für eine Strecke
     */
    @GetMapping('/track/{trackId}/car-comparison')
    ResponseEntity<?> getCarComparison(
            @PathVariable UUID trackId,
            @RequestParam UUID driverId
    ) {
        try {
            def comparison = leaderboardService.getCarComparison(trackId, driverId)
            return ResponseEntity.ok(comparison)
        } catch (Exception e) {
            log.error("Error getting car comparison", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }
}