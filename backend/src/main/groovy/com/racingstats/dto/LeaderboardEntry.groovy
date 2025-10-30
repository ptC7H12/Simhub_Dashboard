package com.racingstats.dto

import groovy.transform.ToString

@ToString(includeNames = true)
class LeaderboardEntry {
    Integer position
    UUID driverId
    String driverName
    Long lapTime
    Long averageLapTime
    Double consistencyScore
    Integer lapCount
    String carModel
    Double score
    Map<String, Object> details = [:]

    String getFormattedLapTime() {
        formatTime(lapTime)
    }

    String getFormattedAvgLapTime() {
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