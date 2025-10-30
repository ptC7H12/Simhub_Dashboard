package com.racingstats.udp

import com.racingstats.dto.TelemetryData
import com.racingstats.service.SessionService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Component
@Slf4j
class F1UdpListener {

    private final SessionService sessionService

    @Value('${racing.udp.f1-port:20777}')
    private int port

    @Value('${racing.udp.enabled:true}')
    private boolean enabled

    private DatagramSocket socket
    private Thread listenerThread
    private volatile boolean running = false

    F1UdpListener(SessionService sessionService) {
        this.sessionService = sessionService
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("F1 2024 UDP Listener is disabled")
            return
        }

        try {
            socket = new DatagramSocket(port)
            running = true

            listenerThread = new Thread(this::listen)
            listenerThread.name = "F1-UDP-Listener"
            listenerThread.start()

            log.info("F1 2024 UDP Listener started on port {}", port)
        } catch (Exception e) {
            log.error("Failed to start F1 2024 UDP Listener", e)
        }
    }

    @PreDestroy
    void stop() {
        running = false
        if (socket) {
            socket.close()
        }
        if (listenerThread) {
            listenerThread.interrupt()
        }
        log.info("F1 2024 UDP Listener stopped")
    }

    private void listen() {
        byte[] buffer = new byte[2048]

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length)
                socket.receive(packet)

                processPacket(packet.data, packet.length)

            } catch (Exception e) {
                if (running) {
                    log.error("Error receiving F1 packet", e)
                }
            }
        }
    }

    private void processPacket(byte[] data, int length) {
        try {
            if (length < 24) return  // F1 header is 24 bytes

            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // F1 2024 Header (24 bytes)
            int packetFormat = buffer.getShort() & 0xFFFF  // 2024
            int gameMajorVersion = buffer.get() & 0xFF
            int gameMinorVersion = buffer.get() & 0xFF
            int packetVersion = buffer.get() & 0xFF
            int packetId = buffer.get() & 0xFF
            long sessionUID = buffer.getLong()
            float sessionTime = buffer.getFloat()
            int frameIdentifier = buffer.getInt()
            int playerCarIndex = buffer.get() & 0xFF
            int secondaryPlayerCarIndex = buffer.get() & 0xFF

            switch (packetId) {
                case 0: // Session
                    handleSessionData(buffer, playerCarIndex)
                    break
                case 2: // Lap Data
                    handleLapData(buffer, playerCarIndex)
                    break
                case 6: // Car Telemetry
                    handleCarTelemetry(buffer, playerCarIndex)
                    break
                case 8: // Session History
                    // handleSessionHistory(buffer, playerCarIndex)
                    break
                default:
                    log.trace("F1 packet ID {} not handled", packetId)
            }
        } catch (Exception e) {
            log.error("Error processing F1 packet", e)
        }
    }

    private void handleSessionData(ByteBuffer buffer, int playerCarIndex) {
        // Parse session packet
        int weather = buffer.get() & 0xFF
        int trackTemperature = buffer.get()
        int airTemperature = buffer.get()
        int totalLaps = buffer.get() & 0xFF
        int trackLength = buffer.getShort() & 0xFFFF
        int sessionType = buffer.get() & 0xFF
        int trackId = buffer.get()

        TelemetryData telemetry = new TelemetryData(
                packetType: 'NEW_SESSION',
                carId: playerCarIndex,
                sessionType: sessionType
        )

        // Track ID to Name mapping (simplified)
        telemetry.trackName = getTrackName(trackId)
        telemetry.carModel = "F1 Car"

        log.info("F1 Session - Track: {}, Type: {}, Laps: {}",
                telemetry.trackName, sessionType, totalLaps)

        sessionService.processTelemetry('F1_2024', telemetry)
    }

    private void handleLapData(ByteBuffer buffer, int playerCarIndex) {
        // Skip to player car data (each car is ~43 bytes)
        buffer.position(buffer.position() + (playerCarIndex * 43))

        int lastLapTimeMS = buffer.getInt()
        int currentLapTimeMS = buffer.getInt()
        int sector1TimeMS = buffer.getShort() & 0xFFFF
        int sector2TimeMS = buffer.getShort() & 0xFFFF
        float lapDistance = buffer.getFloat()
        float totalDistance = buffer.getFloat()
        float safetyCarDelta = buffer.getFloat()
        int carPosition = buffer.get() & 0xFF
        int currentLapNum = buffer.get() & 0xFF
        int pitStatus = buffer.get() & 0xFF
        int numPitStops = buffer.get() & 0xFF
        int sector = buffer.get() & 0xFF
        int currentLapInvalid = buffer.get() & 0xFF
        int penalties = buffer.get() & 0xFF

        // Check if lap completed
        if (lastLapTimeMS > 0 && currentLapTimeMS < 1000) {
            TelemetryData telemetry = new TelemetryData(
                    packetType: 'LAP_COMPLETED',
                    carId: playerCarIndex,
                    lapTime: lastLapTimeMS.longValue(),
                    lapNumber: currentLapNum - 1,
                    cuts: currentLapInvalid > 0 ? 1 : 0,
                    sector1Time: sector1TimeMS.longValue(),
                    sector2Time: sector2TimeMS.longValue()
            )

            log.info("F1 Lap Completed - Lap: {}, Time: {}ms",
                    telemetry.lapNumber, telemetry.lapTime)

            sessionService.processTelemetry('F1_2024', telemetry)
        }
    }

    private void handleCarTelemetry(ByteBuffer buffer, int playerCarIndex) {
        // Skip to player car data (each car is ~60 bytes)
        buffer.position(buffer.position() + (playerCarIndex * 60))

        int speed = buffer.getShort() & 0xFFFF
        float throttle = buffer.getFloat()
        float steer = buffer.getFloat()
        float brake = buffer.getFloat()
        int clutch = buffer.get() & 0xFF
        int gear = buffer.get()
        int engineRPM = buffer.getShort() & 0xFFFF

        TelemetryData telemetry = new TelemetryData(
                packetType: 'CAR_UPDATE',
                carId: playerCarIndex,
                speed: speed.floatValue(),
                rpm: engineRPM,
                gear: gear + 1,  // F1 sends -1 for R, 0 for N, 1+ for gears
                throttle: throttle,
                brake: brake,
                steer: steer
        )

        log.trace("F1 Telemetry - Speed: {} km/h, Gear: {}, RPM: {}",
                telemetry.speed, telemetry.gear, telemetry.rpm)

        sessionService.processTelemetry('F1_2024', telemetry)
    }

    private String getTrackName(int trackId) {
        // Simplified track ID mapping
        switch (trackId) {
            case 0: return "Melbourne"
            case 1: return "Paul Ricard"
            case 2: return "Shanghai"
            case 3: return "Bahrain"
            case 4: return "Barcelona"
            case 5: return "Monaco"
            case 6: return "Montreal"
            case 7: return "Silverstone"
            case 8: return "Hockenheim"
            case 9: return "Hungaroring"
            case 10: return "Spa"
            case 11: return "Monza"
            case 12: return "Singapore"
            case 13: return "Suzuka"
            case 14: return "Abu Dhabi"
            case 15: return "Texas"
            case 16: return "Brazil"
            case 17: return "Austria"
            case 18: return "Sochi"
            case 19: return "Mexico"
            case 20: return "Baku"
            case 21: return "Bahrain Short"
            case 22: return "Silverstone Short"
            case 23: return "Texas Short"
            case 24: return "Suzuka Short"
            case 25: return "Hanoi"
            case 26: return "Zandvoort"
            case 27: return "Imola"
            case 28: return "Portimao"
            case 29: return "Jeddah"
            case 30: return "Miami"
            case 31: return "Las Vegas"
            case 32: return "Losail"
            default: return "Unknown Track $trackId"
        }
    }
}