package com.racingstats.service

import com.racingstats.domain.*
import com.racingstats.dto.LeaderboardEntry
import com.racingstats.repository.*
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
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
    @Transactional(readOnly = true)
    List<LeaderboardEntry> getGlobalLeaderboard() {
        def driverStats = [:]

        // Lade alle Best Lap Statistics
        def bestLapStats = statisticsRepository.findByStatType(StatType.BEST_LAP)

        bestLapStats.each { stat ->
            // Lade Driver SOFORT innerhalb der Transaktion
            String driverId = stat.driver.id.toString()
            String driverName = stat.driver.displayNameOrName

            if (!driverStats[driverId]) {
                driverStats[driverId] = [
                        driverId: stat.driver.id,
                        driverName: driverName,
                        totalBestLaps: 0,
                        averageBestLap: 0,
                        totalMastery: 0,
                        trackCount: 0
                ]
            }

            driverStats[driverId].totalBestLaps += stat.value
            driverStats[driverId].trackCount++
        }

        // Berechne Durchschnitte und hole Mastery
        driverStats.values().each { driverEntry ->  // <- UMBENANNT von 'stats' zu 'driverEntry'
            driverEntry.averageBestLap = driverEntry.totalBestLaps / driverEntry.trackCount

            def masteryStats = statisticsRepository.findByDriverIdAndStatType(
                    driverEntry.driverId as UUID,
                    StatType.TRACK_MASTERY
            )

            if (masteryStats) {
                driverEntry.totalMastery = masteryStats.collect { it.value }.average() ?: 0
            } else {
                driverEntry.totalMastery = 0
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
    @Transactional(readOnly = true)
    List<LeaderboardEntry> getTrackLeaderboard(UUID trackId, String carModel = null) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow { new IllegalArgumentException("Track not found") }

        def results = lapRepository.findTrackLeaderboard(trackId, carModel)

        return results.withIndex().collect { result, index ->
            // Berechne Consistency
            double avgLap = result.avgLap as double
            double stdDev = result.consistency as double
            double consistency = 100 - Math.min((stdDev / avgLap * 100) * 10, 100)

            new LeaderboardEntry(
                    position: index + 1,
                    driverId: result.driverId as UUID,
                    driverName: result.driverName as String,
                    lapTime: result.bestLap as Long,
                    averageLapTime: avgLap.longValue(),
                    consistencyScore: Math.round(consistency * 100) / 100,
                    lapCount: result.validLaps as Integer,
                    carModel: result.carModel as String
            )
        }
    }

    /**
     * Car-Comparison: Welches Auto ist am besten für diese Strecke?
     */
    @Transactional(readOnly = true)
    List<Map<String, Object>> getCarComparison(UUID trackId, UUID driverId) {
        return lapRepository.findCarComparison(trackId, driverId)
                .collect { result ->
                    [
                            carModel: result.carModel as String,
                            bestLap: result.bestLap as Long,
                            averageLap: (result.avgLap as Double).longValue(),
                            lapCount: result.lapCount as Integer,
                            improvement: null
                    ]
                }
    }

    /**
     * Personal Progress: Wie hat sich ein Fahrer auf einer Strecke verbessert?
     */
    @Transactional(readOnly = true)
    Map<String, Object> getPersonalProgress(UUID driverId, UUID trackId) {
        def sessions = lapRepository.findSessionProgressByDriverAndTrack(driverId, trackId)

        if (!sessions) {
            return [hasData: false]
        }

        def firstSession = sessions.first()
        def lastSession = sessions.last()

        double firstAvg = firstSession.avgLap as Double
        double lastAvg = lastSession.avgLap as Double
        double improvement = ((firstAvg - lastAvg) / firstAvg) * 100

        return [
                hasData: true,
                firstSession: [
                        date: firstSession.date,
                        bestLap: firstSession.bestLap as Long,
                        avgLap: firstAvg.longValue()
                ],
                lastSession: [
                        date: lastSession.date,
                        bestLap: lastSession.bestLap as Long,
                        avgLap: lastAvg.longValue()
                ],
                improvement: Math.round(improvement * 100) / 100,
                totalSessions: sessions.size(),
                progressChart: sessions.collect {
                    [
                            date: it.date,
                            bestLap: it.bestLap as Long,
                            avgLap: (it.avgLap as Double).longValue()
                    ]
                }
        ]
    }
}