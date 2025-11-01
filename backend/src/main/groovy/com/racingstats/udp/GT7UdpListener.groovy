package com.racingstats.udp

import com.racingstats.dto.TelemetryData
import com.racingstats.service.SessionService
import com.racingstats.service.GT7MappingService
import groovy.util.logging.Slf4j
import org.bouncycastle.crypto.engines.Salsa20Engine
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gran Turismo 7 UDP Telemetry Listener
 *
 * Basiert auf Community Reverse-Engineering:
 * - https://github.com/snipem/gt7dashboard (MIT License)
 * - https://github.com/Nenkai/PDTools/wiki/GT7-Telemetry
 * - https://ddm999.github.io/gt7info/ (Car/Track Mappings)
 *
 * Features:
 * - Salsa20 Decryption (Bouncy Castle)
 * - Heartbeat-Mechanismus (keep-alive)
 * - Auto-Downloaded Car & Track Names (551+ cars, 39+ tracks)
 * - Full Telemetry Parsing
 */
@Component
@Slf4j
class GT7UdpListener {

    private final SessionService sessionService
    private final GT7MappingService mappingService

    @Value('${racing.udp.gt7-port:33740}')
    private int receivePort

    @Value('${racing.udp.gt7-heartbeat-port:33739}')
    private int heartbeatPort

    @Value('${racing.udp.gt7-ps4-ip:}')
    private String ps4IpAddress

    @Value('${racing.udp.enabled:true}')
    private boolean enabled

    private DatagramSocket receiveSocket
    private DatagramSocket heartbeatSocket
    private Thread listenerThread
    private Thread heartbeatThread
    private volatile boolean running = false

    private boolean sessionStarted = false
    private int lastLapCount = 0
    private String currentTrack = "Unknown Track"
    private String currentCar = "Unknown Car"

    // GT7 Salsa20 Key (from community reverse engineering)
    // Represents "GRAND TURISMO 7 TELEMETRY" (sic - typo in game)
    private static final byte[] SALSA20_KEY = [
            0x47, 0x52, 0x41, 0x4E, 0x44, 0x20, 0x54, 0x55,  // "GRAND TU"
            0x52, 0x49, 0x53, 0x4D, 0x4F, 0x20, 0x37, 0x20,  // "RISMO 7 "
            0x54, 0x45, 0x4C, 0x45, 0x4D, 0x45, 0x54, 0x52,  // "TELEMETR"
            0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00   // "Y\0\0\0..."
    ] as byte[]

    private static final int PACKET_SIZE = 296
    private static final int MAGIC_OFFSET = 0x00
    private static final byte[] MAGIC_PLAIN = [0x47, 0x37, 0x53, 0x30] as byte[] // "G7S0"

    GT7UdpListener(SessionService sessionService, GT7MappingService mappingService) {
        this.sessionService = sessionService
        this.mappingService = mappingService
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("GT7 UDP Listener is disabled")
            return
        }

        if (!ps4IpAddress || ps4IpAddress.trim().isEmpty()) {
            log.error("═══════════════════════════════════════════════════════")
            log.error("  GT7 ERROR: PS4 IP address not configured!")
            log.error("  Set 'racing.udp.gt7-ps4-ip' in application.yml")
            log.error("  Example: racing.udp.gt7-ps4-ip: \"192.168.1.100\"")
            log.error("═══════════════════════════════════════════════════════")
            return
        }

        try {
            receiveSocket = new DatagramSocket(receivePort)
            heartbeatSocket = new DatagramSocket()
            running = true

            // Start Listener Thread
            listenerThread = new Thread(this::listen)
            listenerThread.name = "GT7-UDP-Listener"
            listenerThread.daemon = false
            listenerThread.start()

            // Start Heartbeat Thread
            heartbeatThread = new Thread(this::sendHeartbeats)
            heartbeatThread.name = "GT7-Heartbeat"
            heartbeatThread.daemon = false
            heartbeatThread.start()

            log.info("═══════════════════════════════════════════════════════")
            log.info("  GT7 UDP Listener STARTED")
            log.info("  Receive Port: {}", receivePort)
            log.info("  Heartbeat Port: {}", heartbeatPort)
            log.info("  Target PS4: {}", ps4IpAddress)
            log.info("  Salsa20 Decryption: ENABLED")
            log.info("  Car Database: {} cars loaded", mappingService.statistics.carsLoaded)
            log.info("  Track Database: {} tracks loaded", mappingService.statistics.tracksLoaded)
            log.info("═══════════════════════════════════════════════════════")
        } catch (Exception e) {
            log.error("Failed to start GT7 UDP Listener", e)
        }
    }

    @PreDestroy
    void stop() {
        running = false

        if (receiveSocket) {
            receiveSocket.close()
        }

        if (heartbeatSocket) {
            heartbeatSocket.close()
        }

        if (listenerThread) {
            listenerThread.interrupt()
        }

        if (heartbeatThread) {
            heartbeatThread.interrupt()
        }

        log.info("GT7 UDP Listener stopped")
    }

    /**
     * Heartbeat Thread: Sends keep-alive packets to PS4
     * GT7 only sends telemetry if it receives regular heartbeats
     */
    private void sendHeartbeats() {
        byte[] heartbeatData = "A".getBytes()  // Simple "A" as heartbeat

        while (running) {
            try {
                InetAddress ps4Address = InetAddress.getByName(ps4IpAddress)
                DatagramPacket packet = new DatagramPacket(
                        heartbeatData,
                        heartbeatData.length,
                        ps4Address,
                        heartbeatPort
                )

                heartbeatSocket.send(packet)
                log.debug("Heartbeat sent to {}:{}", ps4IpAddress, heartbeatPort)

                Thread.sleep(10000) // Every 10 seconds

            } catch (InterruptedException e) {
                if (running) {
                    log.warn("Heartbeat thread interrupted")
                }
                break
            } catch (Exception e) {
                if (running) {
                    log.error("Error sending heartbeat to {}", ps4IpAddress, e)
                }
            }
        }
    }

    /**
     * Main listener thread
     */
    private void listen() {
        byte[] buffer = new byte[PACKET_SIZE + 100]

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length)
                receiveSocket.receive(packet)

                byte[] data = Arrays.copyOf(packet.data, packet.length)
                processPacket(data)

            } catch (Exception e) {
                if (running) {
                    log.error("Error receiving GT7 packet", e)
                }
            }
        }
    }

    /**
     * Process incoming packet (encrypted or plain)
     */
    private void processPacket(byte[] rawData) {
        try {
            if (rawData.length < PACKET_SIZE) {
                log.trace("Packet too small: {} bytes (expected {})", rawData.length, PACKET_SIZE)
                return
            }

            byte[] data

            // Check if packet is plain or encrypted
            if (isPlainPacket(rawData)) {
                log.trace("Plain packet detected (simulator mode)")
                data = rawData
            } else {
                // Decrypt with Salsa20
                data = decryptSalsa20(rawData)
                if (data == null) {
                    log.warn("Failed to decrypt GT7 packet")
                    return
                }
            }

            // Parse decrypted telemetry
            parseTelemetry(data)

        } catch (Exception e) {
            log.error("Error processing GT7 packet", e)
        }
    }

    /**
     * Check if packet is plain (simulator mode) or encrypted
     */
    private boolean isPlainPacket(byte[] data) {
        if (data.length < 4) return false

        return data[0] == MAGIC_PLAIN[0] &&
                data[1] == MAGIC_PLAIN[1] &&
                data[2] == MAGIC_PLAIN[2] &&
                data[3] == MAGIC_PLAIN[3]
    }

    /**
     * Decrypt GT7 packet using Salsa20
     * Based on gt7dashboard implementation (MIT License)
     */
    private byte[] decryptSalsa20(byte[] encrypted) {
        try {
            if (encrypted.length < PACKET_SIZE) {
                return null
            }

            // IV is in the packet at a specific offset
            // For GT7: IV is constructed from specific packet bytes
            byte[] iv = new byte[8]

            // Extract IV from packet (bytes at specific positions)
            // This is specific to GT7's implementation
            System.arraycopy(encrypted, 0x40, iv, 0, 4)  // Offset 0x40
            System.arraycopy(encrypted, 0x44, iv, 4, 4)  // Offset 0x44

            // Create Salsa20 cipher
            Salsa20Engine salsa = new Salsa20Engine()

            // Initialize with key and IV
            KeyParameter keyParam = new KeyParameter(SALSA20_KEY)
            ParametersWithIV params = new ParametersWithIV(keyParam, iv)
            salsa.init(false, params) // false = decrypt

            // Decrypt
            byte[] decrypted = new byte[PACKET_SIZE]
            salsa.processBytes(encrypted, 0, PACKET_SIZE, decrypted, 0)

            return decrypted

        } catch (Exception e) {
            log.error("Salsa20 decryption failed", e)
            return null
        }
    }

    /**
     * Parse GT7 telemetry packet
     * Structure based on community reverse engineering
     */
    private void parseTelemetry(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // Position on track (0-1)
            buffer.position(0x04)
            float positionOnTrack = buffer.getFloat()

            // Velocity (m/s)
            buffer.position(0x4C)
            float velocityMS = buffer.getFloat()

            // Engine RPM
            buffer.position(0x3C)
            float engineRPM = buffer.getFloat()

            // Car Code (0x94 - 4 bytes int)
            buffer.position(0x94)
            int carCode = buffer.getInt()

            // Track Code (0xA4 - 4 bytes int)
            buffer.position(0xA4)
            int trackCode = buffer.getInt()

            // Nutze GT7MappingService für Namen
            currentTrack = mappingService.getTrackName(trackCode)
            currentCar = mappingService.getCarName(carCode)

            // Gear & Throttle/Brake
            buffer.position(0x90)
            byte currentGear = buffer.get()
            byte throttle = buffer.get()
            byte brake = buffer.get()

            // Lap information
            buffer.position(0x74)
            int lapsCompleted = buffer.getShort() & 0xFFFF

            buffer.position(0x78)
            int totalLaps = buffer.getShort() & 0xFFFF

            buffer.position(0x7C)
            int lastLapTimeMs = buffer.getInt()

            buffer.position(0x80)
            int bestLapTimeMs = buffer.getInt()

            boolean isMoving = velocityMS > 0.1f

            // Check for new session
            if (!sessionStarted && isMoving) {
                handleNewSession()
                sessionStarted = true
            }

            // Check for lap completion
            if (lapsCompleted > lastLapCount && lastLapTimeMs > 0) {
                handleLapCompleted(lapsCompleted, lastLapTimeMs)
                lastLapCount = lapsCompleted
            }

            // Send telemetry update
            if (sessionStarted && isMoving) {
                TelemetryData telemetry = new TelemetryData(
                        packetType: 'CAR_UPDATE',
                        carId: 0,
                        speed: velocityMS * 3.6f,
                        rpm: (int) engineRPM,
                        gear: currentGear.intValue(),
                        throttle: (throttle & 0xFF) / 255f,
                        brake: (brake & 0xFF) / 255f,
                        normalizedPosition: positionOnTrack
                )

                log.trace("GT7 Telemetry - Speed: {} km/h, Gear: {}, RPM: {}, Track: {}, Car: {}",
                        String.format("%.1f", telemetry.speed),
                        telemetry.gear,
                        telemetry.rpm,
                        currentTrack,
                        currentCar)

                sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
            }

        } catch (Exception e) {
            log.error("Error parsing GT7 telemetry", e)
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

        log.info("GT7 New Session Started - Track: {}, Car: {}",
                telemetry.trackName, telemetry.carModel)

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
        lastLapCount = 0
    }

    private void handleLapCompleted(int lapNumber, int lapTimeMs) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'LAP_COMPLETED',
                carId: 0,
                lapTime: lapTimeMs.longValue(),
                lapNumber: lapNumber,
                cuts: 0  // GT7 doesn't provide cut detection easily
        )

        log.info("GT7 Lap Completed - Lap #{}, Time: {} ({}ms) - {} @ {}",
                telemetry.lapNumber,
                formatLapTime(telemetry.lapTime),
                telemetry.lapTime,
                currentCar,
                currentTrack)

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
    }

    private String formatLapTime(long millis) {
        if (millis <= 0) return "-"
        long minutes = millis / 60000
        long seconds = (millis % 60000) / 1000
        long ms = millis % 1000
        return String.format("%d:%02d.%03d", minutes, seconds, ms)
    }
}