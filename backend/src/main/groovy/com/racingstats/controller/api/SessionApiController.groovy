package com.racingstats.controller.api

import com.racingstats.repository.SessionRepository
import com.racingstats.service.LeaderboardService
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/sessions')
@CrossOrigin(origins = ['http://localhost:5173'])
@Slf4j
class SessionApiController {

    private final SessionRepository sessionRepository
    private final LeaderboardService leaderboardService

    SessionApiController(
            SessionRepository sessionRepository,
            LeaderboardService leaderboardService
    ) {
        this.sessionRepository = sessionRepository
        this.leaderboardService = leaderboardService
    }

    /**
     * Session Details
     */
    @GetMapping('/{id}')
    ResponseEntity<?> getSession(@PathVariable UUID id) {
        return sessionRepository.findById(id)
                .map { session ->
                    ResponseEntity.ok([
                            id: session.id,
                            driver: [
                                    id: session.driver.id,
                                    name: session.driver.displayNameOrName
                            ],
                            track: [
                                    id: session.track.id,
                                    name: session.track.name
                            ],
                            carModel: session.carModel,
                            game: session.game.name(),
                            sessionType: session.sessionType.name(),
                            startedAt: session.startedAt,
                            endedAt: session.endedAt,
                            isActive: session.isActive,
                            totalLaps: session.totalLaps,
                            validLaps: session.validLaps,
                            bestLapTime: session.bestLapTime,
                            averageLapTime: session.averageLapTime,
                            laps: session.laps.collect { [
                                    lapNumber: it.lapNumber,
                                    time: it.timeMillis,
                                    formattedTime: it.formattedTime,
                                    isValid: it.isValid,
                                    isPersonalBest: it.isPersonalBest,
                                    maxSpeed: it.maxSpeed
                            ]}
                    ])
                }
                .orElse(ResponseEntity.notFound().build())
    }

    /**
     * Personal Progress für einen Driver auf einer Strecke
     */
    @GetMapping('/progress')
    ResponseEntity<?> getPersonalProgress(
            @RequestParam UUID driverId,
            @RequestParam UUID trackId
    ) {
        try {
            def progress = leaderboardService.getPersonalProgress(driverId, trackId)
            return ResponseEntity.ok(progress)
        } catch (Exception e) {
            log.error("Error getting personal progress", e)
            return ResponseEntity.internalServerError().body([error: e.message])
        }
    }
}