package com.racingstats.service

import com.racingstats.domain.*
import com.racingstats.dto.DriverStatistics
import com.racingstats.dto.TrackStatistics
import com.racingstats.repository.*
import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional
class StatisticsService {

    private final StatisticsRepository statisticsRepository
    private final LapRepository lapRepository
    private final SessionRepository sessionRepository
    private final DriverRepository driverRepository
    private final TrackRepository trackRepository

    StatisticsService(
            StatisticsRepository statisticsRepository,
            LapRepository lapRepository,
            SessionRepository sessionRepository,
            DriverRepository driverRepository,
            TrackRepository trackRepository
    ) {
        this.statisticsRepository = statisticsRepository
        this.lapRepository = lapRepository
        this.sessionRepository = sessionRepository
        this.driverRepository = driverRepository
        this.trackRepository = trackRepository
    }

    /**
     * Scheduled Task: Berechne Statistiken alle 30 Sekunden
     */
    @Scheduled(fixedRate = 30000)
    void calculateAllStatistics() {
        log.info("Calculating statistics...")

        try {
            driverRepository.findAll().each { driver ->
                trackRepository.findAll().each { track ->
                    calculateDriverTrackStatistics(driver, track)
                }
            }
            log.info("Statistics calculation completed")
        } catch (Exception e) {
            log.error("Error calculating statistics", e)
        }
    }

    /**
     * Berechne alle Statistiken für einen Fahrer auf einer Strecke
     */
    void calculateDriverTrackStatistics(Driver driver, Track track) {
        // Hole alle gültigen Laps
        List<Lap> validLaps = lapRepository.findBySessionDriverAndSessionTrackAndIsValid(
                driver, track, true
        )

        if (!validLaps) return

        Game game = track.game

        // 1. Best Lap
        Long bestLapTime = validLaps.collect { it.timeMillis }.min()
        saveStatistic(driver, track, game, StatType.BEST_LAP, bestLapTime.doubleValue(), validLaps.size())

        // 2. Average Lap
        Double avgLapTime = validLaps.collect { it.timeMillis }.average()
        saveStatistic(driver, track, game, StatType.AVERAGE_LAP, avgLapTime, validLaps.size())

        // 3. Consistency Score (0-100)
        Double consistencyScore = calculateConsistencyScore(validLaps)
        saveStatistic(driver, track, game, StatType.CONSISTENCY_SCORE, consistencyScore, validLaps.size())

        // 4. Track Mastery (0-100)
        Double trackMastery = calculateTrackMastery(driver, track, validLaps)
        saveStatistic(driver, track, game, StatType.TRACK_MASTERY, trackMastery, validLaps.size())

        // 5. Improvement Rate
        Double improvementRate = calculateImprovementRate(validLaps)
        saveStatistic(driver, track, game, StatType.IMPROVEMENT_RATE, improvementRate, validLaps.size())

        // 6. Max Speed
        Float maxSpeed = validLaps.collect { it.maxSpeed }.findAll { it != null }.max() ?: 0f
        saveStatistic(driver, track, game, StatType.MAX_SPEED, maxSpeed.doubleValue(), validLaps.size())

        // 7. Total/Valid Laps
        saveStatistic(driver, track, game, StatType.TOTAL_LAPS, validLaps.size().doubleValue(), validLaps.size())
    }

    /**
     * Consistency Score: Wie konstant sind die Rundenzeiten?
     * 100 = perfekt konstant, 0 = sehr inkonsistent
     */
    private Double calculateConsistencyScore(List<Lap> laps) {
        if (laps.size() < 2) return 100.0

        List<Long> times = laps.collect { it.timeMillis }
        Double avg = times.average()

        // Standardabweichung
        Double variance = times.collect { (it - avg) ** 2 }.average()
        Double stdDev = Math.sqrt(variance)

        // Score: Je kleiner stdDev im Verhältnis zum Durchschnitt, desto besser
        Double coefficientOfVariation = (stdDev / avg) * 100
        Double score = 100 - Math.min(coefficientOfVariation * 10, 100)

        return Math.max(0, Math.min(100, score))
    }

    /**
     * Track Mastery: Gesamtbewertung der Streckenbeherrschung
     * Kombiniert: Konsistenz (40%) + Nähe zu Best Lap (30%) + Anzahl Runden (30%)
     */
    private Double calculateTrackMastery(Driver driver, Track track, List<Lap> laps) {
        if (!laps) return 0.0

        // 1. Konsistenz (40%)
        Double consistency = calculateConsistencyScore(laps)
        Double consistencyPoints = (consistency / 100) * 40

        // 2. Performance: Wie nah am persönlichen Best? (30%)
        Long bestLap = laps.collect { it.timeMillis }.min()
        Double avgLap = laps.collect { it.timeMillis }.average()
        Double performance = (bestLap / avgLap) * 100 // Je näher an 100, desto besser
        Double performancePoints = ((performance - 90) / 10) * 30 // 90-100% = 0-30 Punkte
        performancePoints = Math.max(0, Math.min(30, performancePoints))

        // 3. Erfahrung: Anzahl gefahrener Runden (30%)
        // 10 Runden = 0%, 100+ Runden = 100%
        Double experiencePercentage = Math.min(laps.size() / 100.0, 1.0) * 100
        Double experiencePoints = (experiencePercentage / 100) * 30

        return consistencyPoints + performancePoints + experiencePoints
    }

    /**
     * Improvement Rate: Wie schnell wird man besser?
     * Vergleicht erste 10% der Laps mit letzten 10%
     */
    private Double calculateImprovementRate(List<Lap> laps) {
        if (laps.size() < 10) return 0.0

        // Sortiere nach Completion Time
        List<Lap> sortedLaps = laps.sort { it.completedAt }

        int sampleSize = Math.max(3, (int)(laps.size() * 0.1))

        // Erste und letzte Laps
        List<Lap> firstLaps = sortedLaps.take(sampleSize)
        List<Lap> lastLaps = sortedLaps.takeRight(sampleSize)

        Double firstAvg = firstLaps.collect { it.timeMillis }.average()
        Double lastAvg = lastLaps.collect { it.timeMillis }.average()

        // Improvement in Prozent (positiv = schneller geworden)
        Double improvement = ((firstAvg - lastAvg) / firstAvg) * 100

        return improvement
    }

    private void saveStatistic(Driver driver, Track track, Game game, StatType type, Double value, Integer lapCount) {
        Statistics stat = statisticsRepository
                .findByDriverAndTrackAndGameAndStatType(driver, track, game, type)
                .orElseGet {
                    new Statistics(
                            driver: driver,
                            track: track,
                            game: game,
                            statType: type
                    )
                }

        stat.value = value
        stat.lapCount = lapCount
        stat.lastUpdated = LocalDateTime.now()

        statisticsRepository.save(stat)
    }

    /**
     * Hole Driver-Statistiken für Dashboard
     */
    DriverStatistics getDriverStatistics(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow { new IllegalArgumentException("Driver not found") }

        def stats = new DriverStatistics(
                driverId: driver.id,
                driverName: driver.displayNameOrName
        )

        // Favorite Track (höchster Track Mastery Score)
        Statistics favoriteTrack = statisticsRepository
                .findTopByDriverAndStatTypeOrderByValueDesc(driver, StatType.TRACK_MASTERY)

        if (favoriteTrack) {
            stats.favoriteTrack = favoriteTrack.track.name
            stats.favoriteTrackMastery = favoriteTrack.value
        }

        // Worst Track (niedrigster Track Mastery Score)
        Statistics worstTrack = statisticsRepository
                .findTopByDriverAndStatTypeOrderByValueAsc(driver, StatType.TRACK_MASTERY)

        if (worstTrack) {
            stats.worstTrack = worstTrack.track.name
            stats.worstTrackMastery = worstTrack.value
        }

        // Total Laps
        stats.totalLaps = statisticsRepository
                .findByDriverAndStatType(driver, StatType.TOTAL_LAPS)
                .sum { it.value } ?: 0

        // Average Consistency
        def consistencyStats = statisticsRepository
                .findByDriverAndStatType(driver, StatType.CONSISTENCY_SCORE)

        if (consistencyStats) {
            stats.averageConsistency = consistencyStats.collect { it.value }.average()
        }

        // Best overall improvement rate
        Statistics bestImprovement = statisticsRepository
                .findTopByDriverAndStatTypeOrderByValueDesc(driver, StatType.IMPROVEMENT_RATE)

        if (bestImprovement) {
            stats.bestImprovementRate = bestImprovement.value
            stats.bestImprovementTrack = bestImprovement.track.name
        }

        return stats
    }

    /**
     * Track-Statistiken für alle Fahrer
     */
    List<TrackStatistics> getTrackStatistics(UUID trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow { new IllegalArgumentException("Track not found") }

        return driverRepository.findAll().collect { driver ->
            def trackStats = new TrackStatistics(
                    driverId: driver.id,
                    driverName: driver.displayNameOrName,
                    trackId: track.id,
                    trackName: track.name
            )

            // Best Lap
            def bestLapStat = statisticsRepository
                    .findByDriverAndTrackAndGameAndStatType(driver, track, track.game, StatType.BEST_LAP)
                    .orElse(null)

            if (bestLapStat) {
                trackStats.bestLapTime = bestLapStat.value.longValue()
                trackStats.lapCount = bestLapStat.lapCount
            }

            // Average Lap
            def avgLapStat = statisticsRepository
                    .findByDriverAndTrackAndGameAndStatType(driver, track, track.game, StatType.AVERAGE_LAP)
                    .orElse(null)

            if (avgLapStat) {
                trackStats.averageLapTime = avgLapStat.value.longValue()
            }

            // Consistency
            def consistencyStat = statisticsRepository
                    .findByDriverAndTrackAndGameAndStatType(driver, track, track.game, StatType.CONSISTENCY_SCORE)
                    .orElse(null)

            if (consistencyStat) {
                trackStats.consistencyScore = consistencyStat.value
            }

            // Track Mastery
            def masteryStat = statisticsRepository
                    .findByDriverAndTrackAndGameAndStatType(driver, track, track.game, StatType.TRACK_MASTERY)
                    .orElse(null)

            if (masteryStat) {
                trackStats.trackMastery = masteryStat.value
            }

            return trackStats
        }.findAll { it.bestLapTime != null } // Nur Fahrer die die Strecke gefahren sind
                .sort { it.bestLapTime } // Sortiere nach Best Lap
    }
}