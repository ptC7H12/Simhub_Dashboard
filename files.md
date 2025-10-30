# 📁 Racing Stats - Dateiübersicht

## Alle generierten Dateien

### ✅ Setup & Konfiguration (WICHTIG!)

| Datei | Beschreibung | Speicherort |
|-------|--------------|-------------|
| **docker-compose.yml** | Docker Compose Konfiguration (PostgreSQL + Spring Boot) | `/racing-stats/` |
| **init.sql** | PostgreSQL Initialisierung & Schema | `/racing-stats/` |
| **application.yml** | Spring Boot Konfiguration | `/racing-stats/backend/src/main/resources/` |
| **build.gradle** | Gradle Build-Datei mit Dependencies | `/racing-stats/backend/` |
| **settings.gradle** | Gradle Settings | `/racing-stats/backend/` |
| **Dockerfile** | Docker Image für Spring Boot App | `/racing-stats/backend/` |
| **.env.example** | Beispiel für Umgebungsvariablen | `/racing-stats/` |

### 📚 Dokumentation

| Datei | Inhalt |
|-------|--------|
| **README.md** | Haupt-Dokumentation, Übersicht, API-Docs |
| **QUICK_START.md** | Schritt-für-Schritt Schnellstart-Anleitung |
| **FILES_OVERVIEW.md** | Diese Datei - Übersicht aller Dateien |

### 🗂️ Veraltete Dateien (IGNORIEREN!)

| Datei | Status |
|-------|--------|
| ~~playstation-udp-telemetrie-konzept.md~~ | ⚠️ **VERALTET** - enthält n8n, InfluxDB, Grafana |

### 💻 Code-Dokumentation (Zum Kopieren)

| Datei | Inhalt | Wo dokumentiert? |
|-------|--------|------------------|
| Domain Models | Driver, Track, Session, Lap, Statistics, Enums | Teil 2 |
| Repositories | JPA Repositories mit Custom Queries | Teil 2 |
| Services | SessionService, StatisticsService, LeaderboardService | Teil 2 |
| UDP Listener | UdpListenerService | Teil 2 |
| Parsers | AssettoCorsaParser, F1Parser, GT7Parser | Teil 1 & 2 |
| REST Controllers | Dashboard, Leaderboard, Statistics, etc. | Teil 3 |
| DTOs | TelemetryData, DriverStatistics, etc. | Teil 2 & 3 |
| Vue.js Setup | Router, Stores, Services | Teil 3 |
| Vue.js Views | Dashboard, Leaderboard, DriverStats, etc. | Teil 4 |
| Vue.js Components | Layout, Dashboard, Charts, etc. | Teil 4 |

---

## 📦 Wo findest du was?

### 1. PostgreSQL Verbindung

**docker-compose.yml**
```yaml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: racing_stats
    POSTGRES_USER: racinguser
    POSTGRES_PASSWORD: changeme123
```

**application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/racing_stats
    username: racinguser
    password: changeme123
```

**build.gradle**
```gradle
dependencies {
    implementation 'org.postgresql:postgresql:42.7.1'
}
```

---

### 2. UDP Listener Konfiguration

**docker-compose.yml**
```yaml
racing-stats-app:
  ports:
    - "9996:9996/udp"   # Assetto Corsa
    - "20777:20777/udp" # F1 2024
    - "33740:33740/udp" # GT7
```

**application.yml**
```yaml
racing:
  udp:
    ac-port: 9996
    f1-port: 20777
    gt7-port: 33740
```

---

### 3. Statistik-Berechnung

**application.yml**
```yaml
racing:
  statistics:
    calculation-rate: 30000  # 30 Sekunden
```

**Code:** `StatisticsService.groovy` (Teil 2)
- `@Scheduled(fixedRate = 30000)`
- Berechnet alle 30 Sekunden Statistiken

---

## 🎯 Quick Reference

### Starten
```bash
docker-compose up -d
```

### Stoppen
```bash
docker-compose down
```

### Logs
```bash
docker-compose logs -f
```

### Database zugreifen
```bash
docker exec -it racing-stats-db psql -U racinguser -d racing_stats
```

### API testen
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/drivers
```

---

## 📂 Empfohlene Projekt-Struktur

```
racing-stats/
├── docker-compose.yml          ← Hier platzieren
├── init.sql                    ← Hier platzieren
├── .env                        ← Erstellen aus .env.example
├── README.md                   ← Dokumentation
├── QUICK_START.md              ← Schnellstart
│
├── backend/
│   ├── Dockerfile              ← Hier platzieren
│   ├── build.gradle            ← Hier platzieren
│   ├── settings.gradle         ← Hier platzieren
│   │
│   └── src/
│       ├── main/
│       │   ├── groovy/
│       │   │   └── com/racingstats/
│       │   │       ├── RacingStatsApplication.groovy
│       │   │       ├── config/
│       │   │       ├── domain/       ← Code aus Teil 2
│       │   │       ├── repository/   ← Code aus Teil 2
│       │   │       ├── service/      ← Code aus Teil 2
│       │   │       ├── controller/   ← Code aus Teil 3
│       │   │       └── dto/          ← Code aus Teil 2 & 3
│       │   │
│       │   └── resources/
│       │       ├── application.yml   ← Hier platzieren
│       │       └── static/           ← Vue.js Build (später)
│       │
│       └── test/
│           └── groovy/
│
└── frontend/                   ← Optional: Vue.js (Teil 3 & 4)
    ├── package.json
    ├── vite.config.js
    ├── src/
    │   ├── main.js
    │   ├── App.vue
    │   ├── router/
    │   ├── stores/
    │   ├── services/
    │   ├── components/
    │   └── views/
    └── public/
```

---

## ✅ Checkliste: Dateien korrekt platziert?

### Root-Verzeichnis (`/racing-stats/`)
- [ ] docker-compose.yml
- [ ] init.sql
- [ ] .env (kopiert von .env.example)

### Backend (`/racing-stats/backend/`)
- [ ] Dockerfile
- [ ] build.gradle
- [ ] settings.gradle

### Resources (`/racing-stats/backend/src/main/resources/`)
- [ ] application.yml

### Groovy Code (`/racing-stats/backend/src/main/groovy/com/racingstats/`)
- [ ] RacingStatsApplication.groovy
- [ ] domain/*.groovy
- [ ] repository/*.groovy
- [ ] service/*.groovy
- [ ] service/udp/*.groovy
- [ ] controller/api/*.groovy
- [ ] dto/*.groovy

---

## 🔗 Verbindungen zwischen Dateien

```
docker-compose.yml
    ↓
    ├─→ environment variables → application.yml
    ├─→ ports → UDP Listener
    └─→ depends_on → postgres → init.sql

application.yml
    ↓
    ├─→ datasource.url → PostgreSQL
    ├─→ racing.udp.* → UdpListenerService
    └─→ racing.statistics.* → StatisticsService

build.gradle
    ↓
    ├─→ dependencies → PostgreSQL Driver
    ├─→ dependencies → Groovy
    └─→ dependencies → Spring Boot

Dockerfile
    ↓
    ├─→ builds → build.gradle
    └─→ creates → app.jar
```

---

## 📝 Wichtige Hinweise

### ⚠️ Ignoriere diese Datei:
- `playstation-udp-telemetrie-konzept.md` - enthält alte Architektur mit n8n

### ✅ Verwende diese Dateien:
- Alles andere - komplett aktuell und bereinigt!

### 🔐 Vor Production:
- [ ] Ändere Passwörter in `.env`
- [ ] Ändere `POSTGRES_PASSWORD`
- [ ] Ändere `SPRING_DATASOURCE_PASSWORD`
- [ ] Füge `.env` zu `.gitignore` hinzu

---

## 🆘 Probleme?

1. **PostgreSQL verbindet nicht?**
    - Prüfe `docker-compose.yml` und `application.yml`
    - Stelle sicher, dass Passwörter übereinstimmen

2. **UDP-Daten kommen nicht an?**
    - Prüfe Ports in `docker-compose.yml`
    - Prüfe Firewall-Regeln
    - Verifiziere PlayStation IP-Konfiguration

3. **Build schlägt fehl?**
    - Prüfe `build.gradle` Dependencies
    - Stelle sicher, dass alle Groovy-Dateien vorhanden sind

4. **Datei fehlt?**
    - Siehe Teile 2, 3, 4 für den kompletten Code
    - Folge der Projektstruktur oben

---

**Alle Dateien vorhanden? Los geht's! 🚀**

Siehe: **QUICK_START.md** für die Schritt-für-Schritt Anleitung!