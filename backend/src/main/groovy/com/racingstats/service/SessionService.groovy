package com.racingstats.service

import com.racingstats.domain.*
import com.racingstats.dto.TelemetryData
import com.racingstats.repository.*
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Slf4j
@Transactional
class SessionService {

    private final SessionRepository sessionRepository
    private final DriverRepository driverRepository
    private final TrackRepository trackRepository
    private final LapRepository lapRepository

    // Active sessions cache
    private final Map<String, Session> activeSessions = [:].asSynchronized()
    private final Map<String, TelemetryData> lastTelemetry = [:].asSynchronized()

    SessionService(
            SessionRepository sessionRepository,
            DriverRepository driverRepository,
            TrackRepository trackRepository,
            LapRepository lapRepository
    ) {
        this.sessionRepository = sessionRepository
        this.driverRepository = driverRepository
        this.trackRepository = trackRepository
        this.lapRepository = lapRepository
    }

    /**
     * Hauptmethode: Verarbeitet eingehende Telemetriedaten
     */
    void processTelemetry(String gameName, TelemetryData data) {
        try {
            switch (data.packetType) {
                case 'NEW_SESSION':
                    handleNewSession(gameName, data)
                    break

                case 'CAR_UPDATE':
                    handleCarUpdate(gameName, data)
                    break

                case 'LAP_COMPLETED':
                    handleLapCompleted(gameName, data)
                    break

                case 'END_SESSION':
                    handleEndSession(gameName, data)
                    break

                default:
                    log.debug("Unhandled packet type: ${data.packetType}")
            }
        } catch (Exception e) {
            log.error("Error processing telemetry", e)
        }
    }

    private void handleNewSession(String gameName, TelemetryData data) {
        log.info("Starting new session - Game: ${gameName}, Track: ${data.trackName}, Car: ${data.carModel}")

        // Finde oder erstelle Driver
        Driver driver = findOrCreateDriver(data.driverName ?: "Player${data.carId}")

        // Finde oder erstelle Track
        Track track = findOrCreateTrack(
                data.trackName,
                data.trackConfig,
                Game.valueOf(gameName)
        )

        // Erstelle neue Session
        Session session = new Session(
                driver: driver,
                track: track,
                carModel: data.carModel ?: "Unknown",
                game: Game.valueOf(gameName),
                sessionType: determineSessionType(data.sessionType),
                startedAt: LocalDateTime.now(),
                isActive: true
        )

        session = sessionRepository.save(session)

        // Cache session
        String sessionKey = "${gameName}-${data.carId}"
        activeSessions[sessionKey] = session

        log.info("Session created: ${session.id}")
    }

    private void handleCarUpdate(String gameName, TelemetryData data) {
        String sessionKey = "${gameName}-${data.carId}"
        Session session = activeSessions[sessionKey]

        if (!session) {
            // Auto-create session if not exists
            handleNewSession(gameName, data)
            session = activeSessions[sessionKey]
        }

        // Store last telemetry for live updates
        lastTelemetry[sessionKey] = data

        log.trace("Car update - Speed: ${data.speed} km/h, Gear: ${data.gear}")
    }

    private void handleLapCompleted(String gameName, TelemetryData data) {
        String sessionKey = "${gameName}-${data.carId}"
        Session session = activeSessions[sessionKey]

        if (!session) {
            log.warn("Lap completed but no active session found")
            return
        }

        // Berechne Lap-Nummer
        int lapNumber = session.laps.size() + 1

        // Erstelle Lap
        Lap lap = new Lap(
                session: session,
                lapNumber: lapNumber,
                timeMillis: data.lapTime,
                isValid: data.cuts == 0,
                completedAt: LocalDateTime.now()
        )

        // Optional: Füge Telemetrie-Snapshot hinzu
        if (lastTelemetry[sessionKey]) {
            TelemetryData tel = lastTelemetry[sessionKey]
            lap.maxSpeed = tel.speed
            lap.telemetryData = groovy.json.JsonOutput.toJson([
                    maxSpeed: tel.speed,
                    avgRpm: tel.rpm
            ])
        }

        // Check if personal best
        if (session.bestLapTime == null || data.lapTime < session.bestLapTime) {
            lap.isPersonalBest = true
            session.bestLapTime = data.lapTime
        }

        lap = lapRepository.save(lap)
        session.laps.add(lap)
        session.totalLaps++

        if (lap.isValid) {
            session.validLaps++
        }

        sessionRepository.save(session)

        log.info("Lap completed - #${lapNumber}, Time: ${lap.formattedTime}, Valid: ${lap.isValid}, PB: ${lap.isPersonalBest}")
    }

    private void handleEndSession(String gameName, TelemetryData data) {
        String sessionKey = "${gameName}-${data.carId}"
        Session session = activeSessions[sessionKey]

        if (session) {
            session.endSession()
            sessionRepository.save(session)
            activeSessions.remove(sessionKey)
            lastTelemetry.remove(sessionKey)

            log.info("Session ended: ${session.id}, Total laps: ${session.totalLaps}, Best: ${session.bestLapTime}ms")
        }
    }

    private Driver findOrCreateDriver(String name) {
        driverRepository.findByName(name).orElseGet {
            Driver driver = new Driver(name: name, displayName: name)
            driverRepository.save(driver)
        }
    }

    private Track findOrCreateTrack(String name, String config, Game game) {
        String fullName = config ? "${name} - ${config}" : name

        trackRepository.findByNameAndGame(fullName, game).orElseGet {
            Track track = new Track(
                    name: fullName,
                    game: game,
                    gameIdentifier: name
            )
            trackRepository.save(track)
        }
    }

    private SessionType determineSessionType(Integer type) {
        // AC Session Types: 0=Unknown, 1=Practice, 2=Qualify, 3=Race
        switch (type) {
            case 1: return SessionType.PRACTICE
            case 2: return SessionType.QUALIFYING
            case 3: return SessionType.RACE
            default: return SessionType.PRACTICE
        }
    }

    /**
     * Live-Daten für Dashboard
     */
    Map<String, Object> getLiveTelemetry() {
        return lastTelemetry.collectEntries { key, data ->
            [key, [
                    speed: data.speed,
                    gear: data.gear,
                    rpm: data.rpm,
                    position: data.normalizedPosition
            ]]
        }
    }

    List<Session> getActiveSessions() {
        return activeSessions.values().toList()
    }
}