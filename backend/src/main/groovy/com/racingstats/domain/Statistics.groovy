package com.racingstats.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = 'statistics', indexes = [
        @Index(name = 'idx_stats_driver_track', columnList = 'driver_id,track_id'),
        @Index(name = 'idx_stats_type', columnList = 'stat_type')
])
class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'driver_id', nullable = false)
    Driver driver

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'track_id')
    Track track

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Game game

    @Enumerated(EnumType.STRING)
    @Column(name = 'stat_type', nullable = false)
    StatType statType

    @Column(nullable = false)
    Double value

    @Column(name = 'lap_count')
    Integer lapCount

    @Column(name = 'last_updated', nullable = false)
    LocalDateTime lastUpdated = LocalDateTime.now()
}