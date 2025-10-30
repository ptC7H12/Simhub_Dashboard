package com.racingstats.dto

import groovy.transform.ToString

@ToString(includeNames = true)
class DriverStatistics {
    UUID driverId
    String driverName

    // Favorite & Worst Tracks
    String favoriteTrack
    Double favoriteTrackMastery
    String worstTrack
    Double worstTrackMastery

    // Overall Stats
    Double totalLaps
    Double averageConsistency
    Double bestImprovementRate
    String bestImprovementTrack

    // Additional
    Map<String, Object> additionalStats = [:]
}