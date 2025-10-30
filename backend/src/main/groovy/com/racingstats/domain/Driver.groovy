package com.racingstats.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = 'drivers')
class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id

    @Column(nullable = false, unique = true)
    String name

    @Column(name = 'display_name')
    String displayName

    @Column(name = 'created_at', nullable = false)
    LocalDateTime createdAt = LocalDateTime.now()

    @Column(name = 'last_seen')
    LocalDateTime lastSeen

    String getDisplayNameOrName() {
        displayName ?: name
    }
}