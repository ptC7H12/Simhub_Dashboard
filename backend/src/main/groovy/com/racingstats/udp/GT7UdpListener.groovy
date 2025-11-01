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
    private boolean wasInGame = false
    private int lastLapCount = 0
    private int lastCarCode = 0
    private String currentTrack = "Unknown Track"
    private String currentCar = "Unknown Car"
    private int menuPacketCount = 0

    // GT7 Salsa20 Key
    private static final byte[] SALSA20_KEY = [
            0x47, 0x52, 0x41, 0x4E, 0x44, 0x20, 0x54, 0x55,
            0x52, 0x49, 0x53, 0x4D, 0x4F, 0x20, 0x37, 0x20,
            0x54, 0x45, 0x4C, 0x45, 0x4D, 0x45, 0x54, 0x52,
            0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    ] as byte[]

    private static final int PACKET_SIZE = 296
    private static final byte[] MAGIC_PLAIN = [0x47, 0x37, 0x53, 0x30] as byte[]

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
            log.error("═══════════════════════════════════════════════════════")
            return
        }

        try {
            receiveSocket = new DatagramSocket(receivePort)
            heartbeatSocket = new DatagramSocket()
            running = true

            listenerThread = new Thread(this::listen)
            listenerThread.name = "GT7-UDP-Listener"
            listenerThread.daemon = false
            listenerThread.start()

            heartbeatThread = new Thread(this::sendHeartbeats)
            heartbeatThread.name = "GT7-Heartbeat"
            heartbeatThread.daemon = false
            heartbeatThread.start()

            log.info("═══════════════════════════════════════════════════════")
            log.info("  GT7 UDP Listener STARTED")
            log.info("  Receive Port: {}", receivePort)
            log.info("  Heartbeat Port: {}", heartbeatPort)
            log.info("  Target PS4: {}", ps4IpAddress)
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
        if (receiveSocket) receiveSocket.close()
        if (heartbeatSocket) heartbeatSocket.close()
        if (listenerThread) listenerThread.interrupt()
        if (heartbeatThread) heartbeatThread.interrupt()
        log.info("GT7 UDP Listener stopped")
    }

    private void sendHeartbeats() {
        byte[] heartbeatData = "A".getBytes()

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

                Thread.sleep(10000)

            } catch (InterruptedException e) {
                if (running) log.warn("Heartbeat thread interrupted")
                break
            } catch (Exception e) {
                if (running) log.error("Error sending heartbeat", e)
            }
        }
    }

    private void listen() {
        byte[] buffer = new byte[PACKET_SIZE + 100]

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length)
                receiveSocket.receive(packet)

                byte[] data = Arrays.copyOf(packet.data, packet.length)
                processPacket(data)

            } catch (Exception e) {
                if (running) log.error("Error receiving GT7 packet", e)
            }
        }
    }

    private void processPacket(byte[] rawData) {
        try {
            if (rawData.length < PACKET_SIZE) {
                log.trace("Packet too small: {} bytes", rawData.length)
                return
            }

            byte[] data

            if (isPlainPacket(rawData)) {
                log.trace("Plain packet detected")
                data = rawData
            } else {
                data = decryptSalsa20(rawData)
                if (data == null) {
                    log.warn("Failed to decrypt GT7 packet")
                    return
                }
            }

            parseTelemetry(data)

        } catch (Exception e) {
            log.error("Error processing GT7 packet", e)
        }
    }

    private boolean isPlainPacket(byte[] data) {
        if (data.length < 4) return false

        return data[0] == MAGIC_PLAIN[0] &&
                data[1] == MAGIC_PLAIN[1] &&
                data[2] == MAGIC_PLAIN[2] &&
                data[3] == MAGIC_PLAIN[3]
    }

    private byte[] decryptSalsa20(byte[] encrypted) {
        try {
            if (encrypted.length < PACKET_SIZE) return null

            byte[] iv = new byte[8]
            System.arraycopy(encrypted, 0x40, iv, 0, 4)
            System.arraycopy(encrypted, 0x44, iv, 4, 4)

            Salsa20Engine salsa = new Salsa20Engine()
            KeyParameter keyParam = new KeyParameter(SALSA20_KEY)
            ParametersWithIV params = new ParametersWithIV(keyParam, iv)
            salsa.init(false, params)

            byte[] decrypted = new byte[PACKET_SIZE]
            salsa.processBytes(encrypted, 0, PACKET_SIZE, decrypted, 0)

            return decrypted

        } catch (Exception e) {
            log.error("Salsa20 decryption failed", e)
            return null
        }
    }

    /**
     * Parse GT7 telemetry mit In-Game Detection
     */
    private void parseTelemetry(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // Magic number check
            buffer.position(0x00)
            int magic = buffer.getInt()
            if (magic != 0x30533747) { // "G7S0"
                log.trace("Invalid magic: 0x{}", Integer.toHexString(magic))
                return
            }

            // ═══════════════════════════════════════════════════════
            // IN-GAME DETECTION - Prüfe ob wirklich gespielt wird
            // ═══════════════════════════════════════════════════════

            // Flags (Offset 0x149 - 1 byte)
            buffer.position(0x149)
            byte flags = buffer.get()

            // Bit 0: Paused
            // Bit 1: Loading
            // Bit 2: In Gear (not in neutral)
            boolean isPaused = (flags & 0x02) != 0
            boolean isLoading = (flags & 0x04) != 0

            // Speed (m/s)
            buffer.position(0x4C)
            float speedMS = buffer.getFloat()

            // Current Lap
            buffer.position(0x78)
            int currentLap = buffer.getShort() & 0xFFFF

            // Car Code
            buffer.position(0x124)
            int carCode = buffer.getInt()

            // Engine RPM
            buffer.position(0x3C)
            float engineRPM = buffer.getFloat()

            // ═══════════════════════════════════════════════════════
            // VALIDIERUNG: Sind wir WIRKLICH im Spiel?
            // ═══════════════════════════════════════════════════════
            boolean isInGame =
                    !isPaused &&
                            !isLoading &&
                            carCode > 0 &&
                            carCode < 10000 &&
                            currentLap > 0 &&
                            currentLap < 1000 &&
                            speedMS >= 0 &&
                            speedMS < 200 && // Max ~720 km/h
                            engineRPM >= 0 &&
                            engineRPM < 15000

            // Debug Logging für Menü-Pakete (nur alle 100 Pakete)
            if (!isInGame) {
                menuPacketCount++
                if (menuPacketCount % 100 == 0) {
                    log.debug("GT7 Menu/Paused (#{}) - Paused: {}, Loading: {}, Car: {}, Lap: {}, Speed: {:.1f}",
                            menuPacketCount, isPaused, isLoading, carCode, currentLap, speedMS)
                }

                // Session beenden wenn wir ins Menü gehen
                if (wasInGame && sessionStarted) {
                    handleEndSession()
                    sessionStarted = false
                    wasInGame = false
                    log.info("GT7 Session ended (returned to menu)")
                }

                return // Ignoriere Menü-Pakete
            }

            // Reset Menu Counter
            if (menuPacketCount > 0) {
                log.info("GT7 Game resumed (menu packets: {})", menuPacketCount)
                menuPacketCount = 0
            }

            wasInGame = true

            // ═══════════════════════════════════════════════════════
            // AB HIER: Wir sind definitiv im Spiel!
            // ═══════════════════════════════════════════════════════

            // Position on track
            buffer.position(0x04)
            float positionOnTrack = buffer.getFloat()

            // Gear & Inputs
            buffer.position(0x90)
            byte gear = buffer.get()
            buffer.position(0x91)
            byte suggestedGear = buffer.get()
            buffer.position(0x92)
            byte throttle = buffer.get()
            buffer.position(0x93)
            byte brake = buffer.get()

            // Update car wenn geändert
            if (carCode != lastCarCode) {
                lastCarCode = carCode
                currentCar = mappingService.getCarName(carCode)
                log.info("GT7 Car detected: {} (Code: {})", currentCar, carCode)
            }

            // Lap Times
            buffer.position(0x76)
            int totalLaps = buffer.getShort() & 0xFFFF

            buffer.position(0x80)
            int bestLapTime = buffer.getInt()

            buffer.position(0x84)
            int lastLapTime = buffer.getInt()

            // Check for new session
            if (!sessionStarted) {
                handleNewSession()
                sessionStarted = true
            }

            // Check for lap completion
            if (currentLap > lastLapCount && lastLapTime > 0 && lastLapTime < 600000) {
                handleLapCompleted(currentLap, lastLapTime)
                lastLapCount = currentLap
            }

            // Send telemetry update (nur alle 10 Pakete = ~600ms bei 60Hz)
            if (System.currentTimeMillis() % 10 < 2) {
                TelemetryData telemetry = new TelemetryData(
                        packetType: 'CAR_UPDATE',
                        carId: 0,
                        speed: speedMS * 3.6f,
                        rpm: (int) engineRPM,
                        gear: Math.max(0, gear - 1),
                        throttle: (throttle & 0xFF) / 255f,
                        brake: (brake & 0xFF) / 255f,
                        normalizedPosition: positionOnTrack
                )

                log.trace("GT7 Live - Speed: {:.1f} km/h, Gear: {}, RPM: {}, Lap: {}/{}",
                        telemetry.speed, telemetry.gear, telemetry.rpm, currentLap, totalLaps)

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
                sessionType: 1
        )

        log.info("═══════════════════════════════════════════════════════")
        log.info("  GT7 Session STARTED")
        log.info("  Car: {}", currentCar)
        log.info("═══════════════════════════════════════════════════════")

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
        lastLapCount = 0
    }

    private void handleLapCompleted(int lapNumber, int lapTimeMs) {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'LAP_COMPLETED',
                carId: 0,
                lapTime: lapTimeMs.longValue(),
                lapNumber: lapNumber,
                cuts: 0
        )

        log.info("GT7 Lap #{} - {} - {}",
                lapNumber,
                formatLapTime(lapTimeMs),
                currentCar)

        sessionService.processTelemetry('GRAN_TURISMO_7', telemetry)
    }

    private void handleEndSession() {
        TelemetryData telemetry = new TelemetryData(
                packetType: 'END_SESSION',
                carId: 0
        )

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
