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
class AssettoCorsaUdpListener {

    private final SessionService sessionService

    @Value('${racing.udp.ac-port:9996}')
    private int port

    @Value('${racing.udp.enabled:true}')
    private boolean enabled

    private DatagramSocket socket
    private Thread listenerThread
    private volatile boolean running = false

    AssettoCorsaUdpListener(SessionService sessionService) {
        this.sessionService = sessionService
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Assetto Corsa UDP Listener is disabled")
            return
        }

        try {
            socket = new DatagramSocket(port)
            running = true

            listenerThread = new Thread(this::listen)
            listenerThread.name = "AC-UDP-Listener"
            listenerThread.start()

            log.info("Assetto Corsa UDP Listener started on port {}", port)
        } catch (Exception e) {
            log.error("Failed to start Assetto Corsa UDP Listener", e)
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
        log.info("Assetto Corsa UDP Listener stopped")
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
                    log.error("Error receiving AC packet", e)
                }
            }
        }
    }

    private void processPacket(byte[] data, int length) {
        try {
            if (length < 4) return

            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // AC UDP Format: First 4 bytes = packet ID
            int packetId = buffer.getInt()

            switch (packetId) {
                case 0: // RTCAR_UPDATE
                    handleCarUpdate(buffer)
                    break
                case 1: // RT_LAP_COMPLETED
                    handleLapCompleted(buffer)
                    break
                case 2: // RT_SESSION_INFO
                    handleSessionInfo(buffer)
                    break
                default:
                    log.trace("Unknown AC packet ID: {}", packetId)
            }
        } catch (Exception e) {
            log.error("Error processing AC packet", e)
        }
    }

    private void handleSessionInfo(ByteBuffer buffer) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'NEW_SESSION',
                carId: 0
        )

        // Parse session info
        telemetry.sessionType = buffer.get() & 0xFF  // 1=Practice, 2=Qualify, 3=Race

        // Track name (50 bytes)
        byte[] trackBytes = new byte[50]
        buffer.get(trackBytes)
        telemetry.trackName = new String(trackBytes).trim().replaceAll('\0', '')

        // Track config (50 bytes)
        byte[] configBytes = new byte[50]
        buffer.get(configBytes)
        telemetry.trackConfig = new String(configBytes).trim().replaceAll('\0', '')

        // Car model (50 bytes)
        byte[] carBytes = new byte[50]
        buffer.get(carBytes)
        telemetry.carModel = new String(carBytes).trim().replaceAll('\0', '')

        log.info("AC Session Info - Track: {}, Config: {}, Car: {}",
                telemetry.trackName, telemetry.trackConfig, telemetry.carModel)

        sessionService.processTelemetry('ASSETTO_CORSA', telemetry)
    }

    private void handleCarUpdate(ByteBuffer buffer) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'CAR_UPDATE',
                carId: 0
        )

        // Car ID
        telemetry.carId = buffer.get() & 0xFF

        // Speed (m/s -> km/h)
        telemetry.speed = buffer.getFloat() * 3.6f

        // RPM
        telemetry.rpm = buffer.getInt()

        // Gear
        telemetry.gear = buffer.get()

        // Normalized position
        telemetry.normalizedPosition = buffer.getFloat()

        // Inputs
        telemetry.throttle = buffer.getFloat()
        telemetry.brake = buffer.getFloat()
        telemetry.steer = buffer.getFloat()

        log.trace("AC Car Update - Speed: {} km/h, Gear: {}, RPM: {}",
                telemetry.speed, telemetry.gear, telemetry.rpm)

        sessionService.processTelemetry('ASSETTO_CORSA', telemetry)
    }

    private void handleLapCompleted(ByteBuffer buffer) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'LAP_COMPLETED',
                carId: 0
        )

        // Car ID
        telemetry.carId = buffer.get() & 0xFF

        // Lap time (milliseconds)
        telemetry.lapTime = buffer.getInt().longValue()

        // Lap number
        telemetry.lapNumber = buffer.getInt()

        // Cuts
        telemetry.cuts = buffer.get() & 0xFF

        log.info("AC Lap Completed - Lap: {}, Time: {}ms, Cuts: {}",
                telemetry.lapNumber, telemetry.lapTime, telemetry.cuts)

        sessionService.processTelemetry('ASSETTO_CORSA', telemetry)
    }
}