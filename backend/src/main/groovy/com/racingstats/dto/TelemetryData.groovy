package com.racingstats.dto

import groovy.transform.ToString

@ToString(includeNames = true)
class TelemetryData {

    // Packet Type
    String packetType  // NEW_SESSION, CAR_UPDATE, LAP_COMPLETED, END_SESSION

    // Car/Driver Info
    Integer carId = 0
    String driverName
    String carModel

    // Track Info
    String trackName
    String trackConfig

    // Session Info
    Integer sessionType

    // Lap Info
    Long lapTime
    Integer lapNumber
    Integer cuts = 0

    // Live Telemetry
    Float speed = 0f
    Integer gear = 0
    Integer rpm = 0
    Float throttle = 0f
    Float brake = 0f
    Float steer = 0f

    // Position
    Float normalizedPosition = 0f

    // Sectors
    Long sector1Time
    Long sector2Time
    Long sector3Time

    // Timestamps
    Long timestamp = System.currentTimeMillis()
}