package com.racingstats.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import java.net.HttpURLConnection
import java.net.URL

@Service
@Slf4j
class GT7MappingService {

    private final Map<Integer, String> carNames = [:].asSynchronized()
    private final Map<Integer, String> makerNames = [:].asSynchronized()
    private final Map<Integer, String> trackNames = [:].asSynchronized()

    private static final String CARS_CSV_URL = "https://ddm999.github.io/gt7info/data/db/cars.csv"
    private static final String MAKERS_CSV_URL = "https://ddm999.github.io/gt7info/data/db/maker.csv"
    private static final int TIMEOUT_MS = 10000

    @PostConstruct
    void init() {
        log.info("═══════════════════════════════════════════════════════")
        log.info("  GT7 Mapping Service - Loading data from GitHub...")
        log.info("═══════════════════════════════════════════════════════")

        try {
            loadMakers()
            loadCars()
            loadTracks()

            log.info("═══════════════════════════════════════════════════════")
            log.info("  GT7 Mappings loaded successfully!")
            log.info("  - Cars: {}", carNames.size())
            log.info("  - Makers: {}", makerNames.size())
            log.info("  - Tracks: {}", trackNames.size())
            log.info("═══════════════════════════════════════════════════════")
        } catch (Exception e) {
            log.error("CRITICAL: Failed to load GT7 mappings", e)
            log.error("═══════════════════════════════════════════════════════")
            log.error("  GT7 will use fallback IDs (Car XXX, Track YYY)")
            log.error("═══════════════════════════════════════════════════════")
        }
    }

    /**
     * Download und parse makers.csv von GitHub
     */
    private void loadMakers() {
        log.info("Downloading makers.csv from GitHub...")

        try {
            String csvContent = downloadCSV(MAKERS_CSV_URL)

            csvContent.eachLine { line, index ->
                if (index == 0) return // Skip header

                def parts = line.split(',')
                if (parts.length >= 2) {
                    try {
                        int id = parts[0].trim() as Integer
                        String name = parts[1].trim()
                        makerNames[id] = name
                    } catch (NumberFormatException e) {
                        log.trace("Skipped invalid maker line: {}", line)
                    }
                }
            }

            log.info("✓ Loaded {} makers", makerNames.size())

        } catch (Exception e) {
            log.error("Failed to load makers.csv", e)
            throw e
        }
    }

    /**
     * Download und parse cars.csv von GitHub
     */
    private void loadCars() {
        log.info("Downloading cars.csv from GitHub...")

        try {
            String csvContent = downloadCSV(CARS_CSV_URL)

            csvContent.eachLine { line, index ->
                if (index == 0) return // Skip header

                def parts = line.split(',')
                if (parts.length >= 3) {
                    try {
                        int id = parts[0].trim() as Integer
                        String shortName = parts[1].trim().replaceAll("'", "'") // Fix encoding
                        int makerId = parts[2].trim() as Integer

                        String makerName = makerNames[makerId] ?: "Unknown Maker"
                        carNames[id] = "${makerName} ${shortName}"
                    } catch (NumberFormatException e) {
                        log.trace("Skipped invalid car line: {}", line)
                    }
                }
            }

            log.info("✓ Loaded {} cars", carNames.size())

        } catch (Exception e) {
            log.error("Failed to load cars.csv", e)
            throw e
        }
    }

    /**
     * Load track mappings (hardcoded - kein offizielles CSV verfügbar)
     */
    private void loadTracks() {
        log.info("Loading track mappings...")

        trackNames.putAll([
                // Real Tracks - Europe
                0: "Circuit de la Sarthe",
                1: "Brands Hatch Grand Prix Circuit",
                2: "Brands Hatch Indy Circuit",
                3: "Goodwood Motor Circuit",
                4: "Nürburgring 24h",
                5: "Nürburgring Nordschleife",
                6: "Circuit de Barcelona-Catalunya GP",
                7: "Circuit de Barcelona-Catalunya National",
                8: "Red Bull Ring",
                9: "Red Bull Ring Short Track",
                10: "Autodromo Nazionale Monza",
                11: "Autodromo Nazionale Monza No Chicane",
                12: "Circuit de Spa-Francorchamps",

                // Real Tracks - Asia/Oceania
                20: "Tokyo Expressway - Central Outer Loop",
                21: "Tokyo Expressway - Central Inner Loop",
                22: "Tokyo Expressway - South Outer Loop",
                23: "Tokyo Expressway - South Inner Loop",
                24: "Tokyo Expressway - East Outer Loop",
                25: "Tsukuba Circuit",
                26: "Suzuka Circuit",
                27: "Fuji Speedway",
                28: "Autopolis International Racing Course",
                29: "Mount Panorama Circuit",

                // Real Tracks - Americas
                40: "WeatherTech Raceway Laguna Seca",
                41: "Daytona International Speedway - Road Course",
                42: "Daytona International Speedway - Oval",
                43: "Autódromo José Carlos Pace (Interlagos)",
                44: "Willow Springs International Raceway - Big Willow",
                45: "Willow Springs - Streets of Willow Springs",
                46: "Willow Springs - Horse Thief Mile",
                47: "Road Atlanta",

                // Original GT Tracks - Classics
                100: "Trial Mountain Circuit",
                101: "Deep Forest Raceway",
                102: "Grand Valley Highway",
                103: "High Speed Ring",
                104: "Dragon Trail - Seaside",
                105: "Dragon Trail - Gardens",

                // Original GT Tracks - Lago Maggiore
                106: "Autodrome Lago Maggiore - GP",
                107: "Autodrome Lago Maggiore - Centre",
                108: "Autodrome Lago Maggiore - East",
                109: "Autodrome Lago Maggiore - West",
                110: "Autodrome Lago Maggiore - East End",
                111: "Autodrome Lago Maggiore - West End",

                // Original GT Tracks - Blue Moon Bay
                112: "Blue Moon Bay Speedway",
                113: "Blue Moon Bay Speedway Infield A",
                114: "Blue Moon Bay Speedway Infield B",

                // Original GT Tracks - Other
                115: "Northern Isle Speedway",
                116: "Special Stage Route X",

                // Original GT Tracks - Alsace
                120: "Alsace - Village",
                121: "Alsace - Test Course",

                // Original GT Tracks - Sainte-Croix
                122: "Circuit de Sainte-Croix - Layout A",
                123: "Circuit de Sainte-Croix - Layout B",
                124: "Circuit de Sainte-Croix - Layout C",

                // Original GT Tracks - Sardegna
                130: "Sardegna - Road Track A",
                131: "Sardegna - Road Track B",
                132: "Sardegna - Road Track C",
                133: "Sardegna - Windmills",

                // Dirt/Rally Tracks
                200: "Fishermans Ranch",
                201: "Colorado Springs - Lake",
                202: "Colorado Springs - Club",
                203: "Sardegna - Windmills (Dirt)",
                204: "Lake Louise",
                205: "Eiger Nordwand",

                // Special
                999: "Special Stage Route X"
        ])

        log.info("✓ Loaded {} tracks", trackNames.size())
    }

    /**
     * Download CSV content from URL
     */
    private String downloadCSV(String urlString) {
        URL url = new URL(urlString)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()

        try {
            connection.requestMethod = 'GET'
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty('User-Agent', 'RacingStats-GT7/1.0')

            int responseCode = connection.responseCode

            if (responseCode != 200) {
                throw new IOException("HTTP ${responseCode} when downloading ${urlString}")
            }

            return connection.inputStream.getText('UTF-8')

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Get car name by GT7 car code
     */
    String getCarName(int carCode) {
        String name = carNames[carCode]

        if (!name) {
            log.debug("Unknown car code: {}", carCode)
            return "Car ${carCode}"
        }

        return name
    }

    /**
     * Get track name by GT7 track code
     */
    String getTrackName(int trackCode) {
        String name = trackNames[trackCode]

        if (!name) {
            log.debug("Unknown track code: {}", trackCode)
            return "Track ${trackCode}"
        }

        return name
    }

    /**
     * Get manufacturer name by GT7 maker ID
     */
    String getMakerName(int makerId) {
        String name = makerNames[makerId]

        if (!name) {
            log.debug("Unknown maker ID: {}", makerId)
            return "Maker ${makerId}"
        }

        return name
    }

    /**
     * Statistics for monitoring
     */
    Map<String, Object> getStatistics() {
        return [
                carsLoaded: carNames.size(),
                makersLoaded: makerNames.size(),
                tracksLoaded: trackNames.size(),
                isFullyLoaded: carNames.size() > 0 && makerNames.size() > 0
        ]
    }
}