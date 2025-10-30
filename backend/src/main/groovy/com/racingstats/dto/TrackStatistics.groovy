package com.racingstats.dto

import groovy.transform.ToString

@ToString(includeNames = true)
class TrackStatistics {
    UUID driverId
    String driverName
    UUID trackId
    String trackName

    Long bestLapTime
    Long averageLapTime
    Double consistencyScore
    Double trackMastery
    Integer lapCount

    String getFormattedBestLap() {
        formatTime(bestLapTime)
    }

    String getFormattedAvgLap() {
        formatTime(averageLapTime)
    }

    private String formatTime(Long millis) {
        if (!millis) return "-"

        long minutes = millis / 60000
        long seconds = (millis % 60000) / 1000
        long ms = millis % 1000

        return String.format("%d:%02d.%03d", minutes, seconds, ms)
    }
}