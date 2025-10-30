package com.racingstats.service

import com.racingstats.domain.*
import com.racingstats.dto.LeaderboardEntry
import com.racingstats.repository.*
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional(readOnly = true)
class LeaderboardService {

    private final StatisticsRepository statisticsRepository
    private final LapRepository lapRepository
    private final TrackRepository trackRepository

    LeaderboardService(
            StatisticsRepository statisticsRepository,
            LapRepository lapRepository,
            TrackRepository trackRepository
    ) {
        this.statisticsRepository = statisticsRepository
        this.lapRepository = lapRepository
        this.trackRepository = trackRepository
    }

    /**
     * Globales Leaderboard: Alle Strecken kombiniert
     */
    List<LeaderboardEntry> getGlobalLeaderboard() {
        def driverStats = [:]

        statisticsRepository.findByStatType(StatType.BEST_LAP).each { stat ->
            String key = stat.driver.id.toString()

            if (!driverStats[key]) {
                driverStats[key] = [
                        driverId: stat.driver.id,
                        driverName: stat.driver.displayNameOrName,
                        totalBestLaps: 0,
                        averageBestLap: 0,
                        totalMastery: 0,
                        trackCount: 0
                ]
            }

            driverStats[key].totalBestLaps += stat.value
            driverStats[key].trackCount++
        }

        // Berechne Durchschnitte und hole Mastery
        driverStats.values().each { stats ->
            stats.averageBestLap = stats.totalBestLaps / stats.trackCount

            def masteryStats = statisticsRepository.findByDriverIdAndStatType(
                    UUID.fromString(stats.driverId.toString()),
                    StatType.TRACK_MASTERY
            )

            if (masteryStats) {
                stats.totalMastery = masteryStats.collect { it.value }.average()
            }
        }

        // Sortiere nach durchschnittlicher Track Mastery
        return driverStats.values()
                .sort { -it.totalMastery }
                .withIndex()
                .collect { entry, index ->
                    new LeaderboardEntry(
                            position: index + 1,
                            driverId: entry.driverId,
                            driverName: entry.driverName,
                            score: entry.totalMastery,
                            details: [
                                    tracksCompleted: entry.trackCount,
                                    avgMastery: Math.round(entry.totalMastery * 100) / 100
                            ]
                    )
                }
    }

    /**
     * Track-spezifisches Leaderboard
     */
    List<LeaderboardEntry> getTrackLeaderboard(UUID trackId, String carModel = null) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow { new IllegalArgumentException("Track not found") }

        def query = """
            SELECT 
                d.id as driver_id,
                d.name as driver_name,
                s.car_model,
                MIN(l.time_millis) as best_lap,
                AVG(l.time_millis) FILTER (WHERE l.is_valid) as avg_lap,
                COUNT(l.id) FILTER (WHERE l.is_valid) as valid_laps,
                STDDEV(l.time_millis) FILTER (WHERE l.is_valid) as consistency
            FROM laps l
            JOIN sessions s ON l.session_id = s.id
            JOIN drivers d ON s.driver_id = d.id
            WHERE s.track_id = :trackId
            ${carModel ? "AND s.car_model = :carModel" : ""}
            AND l.is_valid = true
            GROUP BY d.id, d.name, s.car_model
            ORDER BY best_lap ASC
        """

        def results = lapRepository.findTrackLeaderboard(trackId, carModel)

        return results.withIndex().collect { result, index ->
            def consistency = 100 - Math.min((result.consistency / result.avgLap * 100) * 10, 100)

            new LeaderboardEntry(
                    position: index + 1,
                    driverId: result.driverId,
                    driverName: result.driverName,
                    lapTime: result.bestLap,
                    averageLapTime: result.avgLap.longValue(),
                    consistencyScore: Math.round(consistency * 100) / 100,
                    lapCount: result.validLaps,
                    carModel: result.carModel
            )
        }
    }

    /**
     * Car-Comparison: Welches Auto ist am besten für diese Strecke?
     */
    List<Map<String, Object>> getCarComparison(UUID trackId, UUID driverId) {
        def query = """
            SELECT 
                s.car_model,
                MIN(l.time_millis) as best_lap,
                AVG(l.time_millis) FILTER (WHERE l.is_valid) as avg_lap,
                COUNT(l.id) FILTER (WHERE l.is_valid) as lap_count
            FROM laps l
            JOIN sessions s ON l.session_id = s.id
            WHERE s.track_id = :trackId
            AND s.driver_id = :driverId
            AND l.is_valid = true
            GROUP BY s.car_model
            ORDER BY best_lap ASC
        """

        return lapRepository.findCarComparison(trackId, driverId)
                .collect { result ->
                    [
                            carModel: result.carModel,
                            bestLap: result.bestLap,
                            averageLap: result.avgLap,
                            lapCount: result.lapCount,
                            improvement: null // TODO: Calculate vs. other cars
                    ]
                }
    }

    /**
     * Personal Progress: Wie hat sich ein Fahrer auf einer Strecke verbessert?
     */
    Map<String, Object> getPersonalProgress(UUID driverId, UUID trackId) {
        def sessions = lapRepository.findSessionProgressByDriverAndTrack(driverId, trackId)

        if (!sessions) {
            return [hasData: false]
        }

        def firstSession = sessions.first()
        def lastSession = sessions.last()

        def improvement = ((firstSession.avgLap - lastSession.avgLap) / firstSession.avgLap) * 100

        return [
                hasData: true,
                firstSession: [
                        date: firstSession.date,
                        bestLap: firstSession.bestLap,
                        avgLap: firstSession.avgLap
                ],
                lastSession: [
                        date: lastSession.date,
                        bestLap: lastSession.bestLap,
                        avgLap: lastSession.avgLap
                ],
                improvement: Math.round(improvement * 100) / 100,
                totalSessions: sessions.size(),
                progressChart: sessions.collect {
                    [
                            date: it.date,
                            bestLap: it.bestLap,
                            avgLap: it.avgLap
                    ]
                }
        ]
    }
}