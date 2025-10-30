package com.racingstats.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

import java.time.LocalDateTime

@Entity
@Table(name = 'laps')
class Lap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'session_id', nullable = false)
    Session session

    @Column(name = 'lap_number', nullable = false)
    Integer lapNumber

    @Column(name = 'time_millis', nullable = false)
    Long timeMillis

    @Column(name = 'is_valid', nullable = false)
    Boolean isValid = true

    @Column(name = 'is_personal_best', nullable = false)
    Boolean isPersonalBest = false

    @Column(name = 'completed_at', nullable = false)
    LocalDateTime completedAt = LocalDateTime.now()

    @Column(name = 'max_speed')
    Float maxSpeed

    @Column(name = 'sector_1_time')
    Long sector1Time

    @Column(name = 'sector_2_time')
    Long sector2Time

    @Column(name = 'sector_3_time')
    Long sector3Time

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = 'telemetry_data', columnDefinition = 'jsonb')
    String telemetryData

    String getFormattedTime() {
        if (!timeMillis) return "-"

        long minutes = timeMillis / 60000
        long seconds = (timeMillis % 60000) / 1000
        long ms = timeMillis % 1000

        return String.format("%d:%02d.%03d", minutes, seconds, ms)
    }
}