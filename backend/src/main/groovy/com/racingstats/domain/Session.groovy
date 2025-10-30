package com.racingstats.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = 'sessions')
class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'driver_id', nullable = false)
    Driver driver

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'track_id', nullable = false)
    Track track

    @Column(name = 'car_model')
    String carModel

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Game game

    @Enumerated(EnumType.STRING)
    @Column(name = 'session_type', nullable = false)
    SessionType sessionType

    @Column(name = 'started_at', nullable = false)
    LocalDateTime startedAt = LocalDateTime.now()

    @Column(name = 'ended_at')
    LocalDateTime endedAt

    @Column(name = 'is_active', nullable = false)
    Boolean isActive = true

    @Column(name = 'total_laps', nullable = false)
    Integer totalLaps = 0

    @Column(name = 'valid_laps', nullable = false)
    Integer validLaps = 0

    @Column(name = 'best_lap_time')
    Long bestLapTime

    @Column(name = 'average_lap_time')
    Long averageLapTime

    @OneToMany(mappedBy = 'session', cascade = CascadeType.ALL, orphanRemoval = true)
    List<Lap> laps = []

    void endSession() {
        this.isActive = false
        this.endedAt = LocalDateTime.now()

        if (laps) {
            def validLapTimes = laps.findAll { it.isValid }.collect { it.timeMillis }
            if (validLapTimes) {
                this.averageLapTime = (long) validLapTimes.average()
            }
        }
    }
}