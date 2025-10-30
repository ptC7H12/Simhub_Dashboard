package com.racingstats.domain

import jakarta.persistence.*

@Entity
@Table(name = 'tracks')
class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id

    @Column(nullable = false)
    String name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Game game

    @Column(name = 'game_identifier')
    String gameIdentifier

    @Column(length = 2)
    String country

    @Column(name = 'track_length')
    Integer trackLength
}