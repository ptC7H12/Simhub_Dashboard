package com.racingstats.repository

import com.racingstats.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LapRepository extends JpaRepository<Lap, UUID> {

    List<Lap> findBySessionDriverAndSessionTrackAndIsValid(
            Driver driver,
            Track track,
            Boolean isValid
    )

    @Query("""
        SELECT new map(
            d.id as driverId,
            d.name as driverName,
            s.carModel as carModel,
            MIN(l.timeMillis) as bestLap,
            AVG(l.timeMillis) as avgLap,
            COUNT(l.id) as validLaps,
            STDDEV(l.timeMillis) as consistency
        )
        FROM Lap l
        JOIN l.session s
        JOIN s.driver d
        WHERE s.track.id = :trackId
        AND (:carModel IS NULL OR s.carModel = :carModel)
        AND l.isValid = true
        GROUP BY d.id, d.name, s.carModel
        ORDER BY MIN(l.timeMillis) ASC
    """)
    List<Map<String, Object>> findTrackLeaderboard(
            @Param('trackId') UUID trackId,
            @Param('carModel') String carModel
    )

    @Query("""
        SELECT new map(
            s.carModel as carModel,
            MIN(l.timeMillis) as bestLap,
            AVG(l.timeMillis) as avgLap,
            COUNT(l.id) as lapCount
        )
        FROM Lap l
        JOIN l.session s
        WHERE s.track.id = :trackId
        AND s.driver.id = :driverId
        AND l.isValid = true
        GROUP BY s.carModel
        ORDER BY MIN(l.timeMillis) ASC
    """)
    List<Map<String, Object>> findCarComparison(
            @Param('trackId') UUID trackId,
            @Param('driverId') UUID driverId
    )

    @Query("""
        SELECT new map(
            s.startedAt as date,
            MIN(l.timeMillis) as bestLap,
            AVG(l.timeMillis) as avgLap
        )
        FROM Lap l
        JOIN l.session s
        WHERE s.driver.id = :driverId
        AND s.track.id = :trackId
        AND l.isValid = true
        GROUP BY s.id, s.startedAt
        ORDER BY s.startedAt ASC
    """)
    List<Map<String, Object>> findSessionProgressByDriverAndTrack(
            @Param('driverId') UUID driverId,
            @Param('trackId') UUID trackId
    )
}

interface StatisticsRepository extends JpaRepository<Statistics, UUID> {

    Optional<Statistics> findByDriverAndTrackAndGameAndStatType(
            Driver driver,
            Track track,
            Game game,
            StatType statType
    )

    List<Statistics> findByDriverAndStatType(Driver driver, StatType statType)

    List<Statistics> findByDriverIdAndStatType(UUID driverId, StatType statType)

    List<Statistics> findByStatType(StatType statType)

    Optional<Statistics> findTopByDriverAndStatTypeOrderByValueDesc(
            Driver driver,
            StatType statType
    )

    Optional<Statistics> findTopByDriverAndStatTypeOrderByValueAsc(
            Driver driver,
            StatType statType
    )
}

interface TrackRepository extends JpaRepository<Track, UUID> {
    Optional<Track> findByNameAndGame(String name, Game game)
}

interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByName(String name)
}

interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByIsActiveTrue()
    List<Session> findByDriverOrderByStartedAtDesc(Driver driver)
}