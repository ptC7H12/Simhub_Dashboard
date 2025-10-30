package com.racingstats.udp

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

@Service
@Slf4j
class UdpListenerService {

    private final AssettoCorsaUdpListener assettoCorsaListener
    private final F1UdpListener f1Listener
    private final GT7UdpListener gt7Listener

    UdpListenerService(
            AssettoCorsaUdpListener assettoCorsaListener,
            F1UdpListener f1Listener,
            GT7UdpListener gt7Listener
    ) {
        this.assettoCorsaListener = assettoCorsaListener
        this.f1Listener = f1Listener
        this.gt7Listener = gt7Listener
    }

    @PostConstruct
    void init() {
        log.info("═══════════════════════════════════════════════════════════")
        log.info("  UDP Listener Service Initialized")
        log.info("═══════════════════════════════════════════════════════════")
        log.info("  Listeners will start automatically via @PostConstruct")
        log.info("  - Assetto Corsa: Port 9996")
        log.info("  - F1 2024:       Port 20777")
        log.info("  - GT7:           Port 33740")
        log.info("═══════════════════════════════════════════════════════════")
    }

    Map<String, Object> getStatus() {
        return [
                assettoCorsaRunning: assettoCorsaListener != null,
                f1Running: f1Listener != null,
                gt7Running: gt7Listener != null
        ]
    }
}