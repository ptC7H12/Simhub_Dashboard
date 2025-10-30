# 🚀 Racing Stats - Quick Start Guide

## Schnellstart in 5 Minuten

### Voraussetzungen ✅
- Docker & Docker Compose installiert
- PS4 Pro im gleichen Netzwerk
- Mind. 4 GB freier RAM

---

## Schritt 1: Projektstruktur erstellen

```bash
# Erstelle Hauptverzeichnis
mkdir racing-stats
cd racing-stats

# Erstelle Backend-Struktur
mkdir -p backend/src/main/groovy/com/racingstats
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/groovy
```

---

## Schritt 2: Dateien platzieren

Kopiere die folgenden Dateien in die Struktur:

```
racing-stats/
├── docker-compose.yml          ← Root-Verzeichnis
├── init.sql                    ← Root-Verzeichnis
├── .env                        ← Root-Verzeichnis (kopiere .env.example)
│
└── backend/
    ├── Dockerfile
    ├── build.gradle
    ├── settings.gradle
    │
    └── src/
        └── main/
            ├── groovy/
            │   └── com/racingstats/
            │       ├── RacingStatsApplication.groovy
            │       ├── config/
            │       ├── domain/
            │       │   ├── Driver.groovy
            │       │   ├── Track.groovy
            │       │   ├── Session.groovy
            │       │   ├── Lap.groovy
            │       │   ├── Statistics.groovy
            │       │   └── [Enums].groovy
            │       ├── repository/
            │       │   ├── DriverRepository.groovy
            │       │   ├── TrackRepository.groovy
            │       │   ├── SessionRepository.groovy
            │       │   ├── LapRepository.groovy
            │       │   └── StatisticsRepository.groovy
            │       ├── service/
            │       │   ├── SessionService.groovy
            │       │   ├── StatisticsService.groovy
            │       │   ├── LeaderboardService.groovy
            │       │   └── udp/
            │       │       ├── UdpListenerService.groovy
            │       │       └── parser/
            │       │           ├── AssettoCorsaParser.groovy
            │       │           ├── F1Parser.groovy
            │       │           └── GT7Parser.groovy
            │       ├── controller/api/
            │       │   ├── DashboardApiController.groovy
            │       │   ├── LeaderboardApiController.groovy
            │       │   ├── StatisticsApiController.groovy
            │       │   ├── DriversApiController.groovy
            │       │   ├── TracksApiController.groovy
            │       │   └── SessionApiController.groovy
            │       └── dto/
            │           ├── TelemetryData.groovy
            │           ├── DriverStatistics.groovy
            │           ├── TrackStatistics.groovy
            │           └── LeaderboardEntry.groovy
            │
            └── resources/
                ├── application.yml
                └── static/          ← Vue.js build (später)
```

---

## Schritt 3: Environment-Variablen setzen

```bash
# Kopiere .env.example zu .env
cp .env.example .env

# Optional: Passwörter ändern
nano .env
```

**Minimale .env:**
```bash
POSTGRES_PASSWORD=deinPasswort123
SPRING_DATASOURCE_PASSWORD=deinPasswort123
```

---

## Schritt 4: Docker Container starten

```bash
# Im racing-stats/ Verzeichnis

# Build & Start
docker-compose up -d

# Logs verfolgen
docker-compose logs -f
```

**Erwartete Ausgabe:**
```
racing-stats-db   | database system is ready to accept connections
racing-stats-app  | Started RacingStatsApplication in 12.345 seconds
```

---

## Schritt 5: Verbindung testen

### Backend Health Check
```bash
curl http://localhost:8080/actuator/health
```

Erwartete Antwort:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### API testen
```bash
# Alle Fahrer abrufen (sollte leer sein)
curl http://localhost:8080/api/drivers

# Dashboard
curl http://localhost:8080/api/dashboard/overview
```

---

## Schritt 6: PlayStation konfigurieren

### Finde deine Docker-Host IP
```bash
# Linux/Mac
ip addr show | grep "inet "

# Oder
hostname -I
```

### Assetto Corsa (PS4 Pro)
1. **Optionen** → **Netzwerk**
2. **UDP Output**: `EIN`
3. **IP-Adresse**: `192.168.1.XXX` ← Deine Docker-Host IP
4. **Port**: `9996`
5. **Übernehmen** & Spiel neu starten

### F1 2024
1. **Settings** → **Telemetry Settings**
2. **UDP Telemetry**: `ON`
3. **UDP IP Address**: `192.168.1.XXX`
4. **UDP Port**: `20777`
5. **UDP Send Rate**: `20 Hz` oder höher

### Gran Turismo 7
1. **Optionen** → **Netzwerk**
2. **Datenlogger**: `Aktivieren`
3. **IP**: `192.168.1.XXX`
4. **Port**: `33740`

---

## Schritt 7: Erste Fahrt! 🏎️

1. **Starte Assetto Corsa** auf PS4
2. **Wähle eine Strecke** und ein Auto
3. **Fahre ein paar Runden**
4. **Beobachte die Logs:**

```bash
docker-compose logs -f racing-stats-app
```

Du solltest sehen:
```
AC New Session - Track: Monza, Car: Ferrari 488 GT3
AC Lap Completed - Car: 0, Time: 94532ms, Cuts: 0
Session created: abc-123-def-456
Lap completed - #1, Time: 1:34.532, Valid: true, PB: true
```

---

## Schritt 8: Daten überprüfen

### Via API
```bash
# Fahrer
curl http://localhost:8080/api/drivers

# Sessions
curl http://localhost:8080/api/dashboard/overview

# Statistiken berechnen (manuell triggern)
curl -X POST http://localhost:8080/api/statistics/calculate
```

### Via pgAdmin (optional)
```bash
# pgAdmin starten
docker-compose --profile dev up -d pgadmin

# Browser öffnen
http://localhost:5050

# Login: admin@racing.local / admin123
```

**Datenbank verbinden:**
- Host: `postgres`
- Port: `5432`
- Database: `racing_stats`
- User: `racinguser`
- Password: `changeme123`

---

## Troubleshooting 🔧

### Problem: Keine UDP-Daten ankommen

**Check 1: Container lauscht?**
```bash
docker exec racing-stats-app netstat -uln | grep -E '9996|20777|33740'

# Sollte zeigen:
# udp        0      0 0.0.0.0:9996      0.0.0.0:*
```

**Check 2: Firewall-Regeln**
```bash
# Linux
sudo ufw allow 9996/udp
sudo ufw allow 20777/udp
sudo ufw allow 33740/udp

# Status
sudo ufw status
```

**Check 3: Pakete kommen an?**
```bash
# Am Host
sudo tcpdump -i any -n udp port 9996 -c 5

# Sollte Pakete zeigen wenn PS4 sendet
```

**Check 4: PlayStation IP richtig?**
- **NICHT** `127.0.0.1` verwenden!
- **NICHT** `localhost` verwenden!
- Verwende die **LAN-IP** des Docker-Hosts

---

### Problem: Backend startet nicht

**Check Logs:**
```bash
docker-compose logs racing-stats-app | grep ERROR
```

**Häufige Fehler:**

**"Connection refused to postgres"**
```bash
# Database noch nicht bereit, warte 30 Sekunden
docker-compose restart racing-stats-app
```

**"Port 8080 already in use"**
```bash
# Ändere Port in docker-compose.yml
ports:
  - "8081:8080"  # Verwende 8081 statt 8080
```

---

### Problem: Database-Verbindung schlägt fehl

```bash
# Prüfe ob PostgreSQL läuft
docker-compose ps postgres

# Logs
docker-compose logs postgres

# Manuell in DB einloggen
docker exec -it racing-stats-db psql -U racinguser -d racing_stats

# Test-Query
SELECT * FROM drivers;
```

---

## Nächste Schritte 📈

### Frontend hinzufügen (Vue.js)
```bash
# In racing-stats/
mkdir frontend
cd frontend

# Vue.js setup (siehe Teil 4 der Dokumentation)
npm init vue@latest
```

### Mehr Daten sammeln
- Fahre verschiedene Strecken
- Teste verschiedene Autos
- Erstelle mehrere Sessions
- Beobachte wie Statistiken wachsen

### Statistiken analysieren
```bash
# Best Laps pro Strecke
curl http://localhost:8080/api/statistics/track/{TRACK_ID}

# Fahrer-Profil
curl http://localhost:8080/api/statistics/driver/{DRIVER_ID}

# Global Leaderboard
curl http://localhost:8080/api/leaderboard/global
```

---

## Wichtige Commands

```bash
# Container stoppen
docker-compose down

# Container stoppen + Datenbank löschen
docker-compose down -v

# Logs anzeigen
docker-compose logs -f

# Nur Backend neu bauen
docker-compose up -d --build racing-stats-app

# Container Status
docker-compose ps

# In Container einloggen
docker exec -it racing-stats-app sh
```

---

## Performance-Tipps

### Stats-Berechnung anpassen
```yaml
# docker-compose.yml
environment:
  - RACING_STATISTICS_CALCULATION_RATE=60000  # 60 Sekunden
```

### UDP Update-Rate reduzieren
In PlayStation Game Settings:
- F1: 10 Hz statt 60 Hz
- AC: Default ist ok

---

## Support & Hilfe

1. **Logs checken**: `docker-compose logs -f`
2. **Health Check**: `curl localhost:8080/actuator/health`
3. **Database prüfen**: pgAdmin oder psql
4. **Netzwerk testen**: tcpdump

---

**Du bist bereit! Viel Erfolg beim Racing! 🏁**

Bei Fragen: Prüfe die vollständige Dokumentation in `README.md`