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
class GT7UdpListener {

    private final SessionService sessionService

    @Value('${racing.udp.gt7-port:33740}')
    private int port

    @Value('${racing.udp.enabled:true}')
    private boolean enabled

    private DatagramSocket socket
    private Thread listenerThread
    private volatile boolean running = false

    private boolean sessionStarted = false
    private int lastLapCount = 0
    private String currentTrack = "Unknown Track"
    private String currentCar = "Unknown Car"

    GT7UdpListener(SessionService sessionService) {
        this.sessionService = sessionService
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Gran Turismo 7 UDP Listener is disabled")
            return
        }

        try {
            socket = new DatagramSocket(port)
            running = true

            listenerThread = new Thread(this::listen)
            listenerThread.name = "GT7-UDP-Listener"
            listenerThread.start()

            log.info("Gran Turismo 7 UDP Listener started on port {}", port)
        } catch (Exception e) {
            log.error("Failed to start GT7 UDP Listener", e)
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
        log.info("Gran Turismo 7 UDP Listener stopped")
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
                    log.error("Error receiving GT7 packet", e)
                }
            }
        }
    }

    private void processPacket(byte[] data, int length) {
        try {
            if (length < 296) return  // GT7 packet is 296 bytes

            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // GT7 uses encrypted packets, but simulator mode sends plain data
            // Header "G7S0" in plain mode

            // Position (0x04) - magic number
            buffer.position(0x04)

            // Position on track (0-1)
            buffer.position(0x04)
            float positionOnTrack = buffer.getFloat()

            // Speed (m/s) at position 0x4C
            buffer.position(0x4C)
            float speedMS = buffer.getFloat()

            // Engine RPM at position 0x3C
            buffer.position(0x3C)
            float rpm = buffer.getFloat()

            // Fuel level at position 0x44
            buffer.position(0x44)
            float fuel = buffer.getFloat()

            // Throttle at position 0x91
            buffer.position(0x91)
            byte throttle = buffer.get()

            // Brake at position 0x92
            byte brake = buffer.get()

            // Current gear at position 0x90
            buffer.position(0x90)
            byte gear = buffer.get()

            // Lap count at position 0x78
            buffer.position(0x78)
            short lapCount = buffer.getShort()

            // Last lap time at position 0x7C
            buffer.position(0x7C)
            int lastLapTime = buffer.getInt()

            // Best lap time at position 0x80
            int bestLapTime = buffer.getInt()

            // Check for new session
            if (!sessionStarted && speedMS > 0) {
                handleNewSession()
                sessionStarted = true
            }

            // Check for lap completion
            if (lapCount > lastLapCount && lastLapTime > 0) {
                handleLapCompleted(lapCount, lastLapTime)
                lastLapCount = lapCount
            }

            // Send telemetry update
            if (sessionStarted && speedMS > 0) {
                TelemetryData telemetry = new TelemetryData(
                        packetType: 'CAR_UPDATE',
                        carId: 0,
                        speed: speedMS * 3.6f,  // Convert m/s to km/h
                        rpm: rpm.intValue(),
                        gear: gear.intValue(),
                        throttle: (throttle & 0xFF) / 255f,
                        brake: (brake & 0xFF) / 255f,
                        normalizedPosition: positionOnTrack
                )

                log.trace("GT7 Telemetry - Speed: {} km/h, Gear: {}, RPM: {}",
                        telemetry.speed, telemetry.gear, telemetry.rpm)

                sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
            }

        } catch (Exception e) {
            log.error("Error processing GT7 packet", e)
        }
    }

    private void handleNewSession() {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'NEW_SESSION',
                carId: 0,
                trackName: currentTrack,
                carModel: currentCar,
                sessionType: 1  // Practice
        )

        log.info("GT7 New Session - Track: {}, Car: {}",
                telemetry.trackName, telemetry.carModel)

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
        lastLapCount = 0
    }

    private void handleLapCompleted(int lapNumber, int lapTimeMS) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'LAP_COMPLETED',
                carId: 0,
                lapTime: lapTimeMS.longValue(),
                lapNumber: lapNumber,
                cuts: 0
        )

        log.info("GT7 Lap Completed - Lap: {}, Time: {}ms",
                telemetry.lapNumber, telemetry.lapTime)

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
    }
}