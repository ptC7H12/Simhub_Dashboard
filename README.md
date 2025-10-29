# Racing Statistics Dashboard - Complete Project Guide
## PlayStation UDP Telemetry → Spring Boot (Groovy) → PostgreSQL → Vue.js

![Status](https://img.shields.io/badge/status-ready_to_implement-green)
![Tech Stack](https://img.shields.io/badge/stack-Spring_Boot_|_Vue.js_|_PostgreSQL-blue)

## 🎯 Projektziel

Ein vollständiges Telemetrie- und Statistik-Dashboard für PlayStation Racing Games mit Multi-Spieler-Vergleichen.

**Unterstützte Spiele:**
- ✅ Assetto Corsa (Priorität!)
- ✅ F1 2024
- ✅ Gran Turismo 7

## 📋 Feature-Übersicht

### Core Features
- 🎮 **UDP Telemetrie-Empfang** von PS4 Pro
- 📊 **Echtzeit-Dashboard** mit Live-Daten
- 🏆 **Leaderboards** (Global & Track-spezifisch)
- 📈 **Umfangreiche Statistiken** pro Fahrer & Strecke
- 🚗 **Auto-Vergleiche** (welches Auto ist am besten?)
- 📉 **Progress Tracking** (Verbesserung über Zeit)
- 💾 **Persistente Datenspeicherung** in PostgreSQL

### Statistiken
1. **Best Lap Time** - Schnellste Runde pro Strecke/Auto
2. **Average Lap Time** - Durchschnitt aller gültigen Runden
3. **Consistency Score** (0-100) - Wie konstant fährst du?
4. **Track Mastery** (0-100) - Gesamtbewertung Streckenbeherrschung
5. **Improvement Rate** - Verbesserung zwischen erster und letzter Session
6. **Personal Progress** - Zeitliche Entwicklung visualisiert
7. **Favorite/Worst Track** - Beste und schlechteste Strecken
8. **Auto-Performance** - Vergleich verschiedener Autos auf gleicher Strecke

## 🏗️ Architektur

```
┌─────────────────────────────────────────────────────────┐
│                    PlayStation 4 Pro                     │
│  ┌──────────┬──────────┬──────────┐                    │
│  │    AC    │  F1 2024 │   GT7    │                    │
│  └────┬─────┴────┬─────┴────┬─────┘                    │
└───────┼──────────┼──────────┼────────────────────────────┘
        │          │          │
        │ UDP 9996 │ UDP 20777│ UDP 33740
        └──────────┴──────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│              Docker Compose Stack                         │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │         Spring Boot Application (Groovy)           │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │ UDP Listener Service                         │  │  │
│  │  │ - AssettoCorsaParser                         │  │  │
│  │  │ - F1Parser                                   │  │  │
│  │  │ - GT7Parser                                  │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │ Business Logic                               │  │  │
│  │  │ - SessionService                             │  │  │
│  │  │ - StatisticsService (Scheduled)              │  │  │
│  │  │ - LeaderboardService                         │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │ REST API                                     │  │  │
│  │  │ - Dashboard API                              │  │  │
│  │  │ - Leaderboard API                            │  │  │
│  │  │ - Statistics API                             │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
│                          │                                │
│                          ▼                                │
│  ┌────────────────────────────────────────────────────┐  │
│  │              PostgreSQL 16                         │  │
│  │  - drivers                                         │  │
│  │  - tracks                                          │  │
│  │  - sessions                                        │  │
│  │  - laps                                            │  │
│  │  - statistics                                      │  │
│  └────────────────────────────────────────────────────┘  │
│                                                           │
└──────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP/REST
┌──────────────────────────────────────────────────────────┐
│              Vue.js Frontend (SPA)                        │
│  - Dashboard (Live Telemetry & Active Sessions)          │
│  - Global & Track Leaderboards                           │
│  - Driver Statistics & Profile                           │
│  - Track Analysis                                        │
│  - Session Details                                       │
│  - Interactive Charts (Chart.js)                         │
└──────────────────────────────────────────────────────────┘
```

## 🔧 Tech Stack

### Backend
| Component | Technology | Version |
|-----------|------------|---------|
| Language | Apache Groovy | 4.0.x |
| Framework | Spring Boot | 3.2.x |
| Database | PostgreSQL | 16 |
| Build Tool | Gradle | 8.5+ |
| Container | Docker | Latest |

### Frontend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Vue.js | 3.4.x |
| State Management | Pinia | 2.1.x |
| Routing | Vue Router | 4.2.x |
| HTTP Client | Axios | 1.6.x |
| Charts | Chart.js + vue-chartjs | 4.4.x / 5.3.x |
| UI Components | Headless UI | 1.7.x |
| Icons | Heroicons | 2.1.x |
| Build Tool | Vite | 5.0.x |
| Styling | Tailwind CSS | 3.4.x |

## 📁 Projektstruktur

```
racing-stats-project/
├── docker-compose.yml
├── README.md
├── backend/                        # Spring Boot App
│   ├── Dockerfile
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       ├── main/
│       │   ├── groovy/
│       │   │   └── com/racingstats/
│       │   │       ├── RacingStatsApplication.groovy
│       │   │       ├── config/
│       │   │       ├── domain/
│       │   │       ├── repository/
│       │   │       ├── service/
│       │   │       │   ├── udp/
│       │   │       │   └── parser/
│       │   │       ├── controller/api/
│       │   │       └── dto/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── static/
│       └── test/
└── frontend/                       # Vue.js App
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/
        ├── stores/
        ├── services/
        ├── components/
        │   ├── layout/
        │   ├── dashboard/
        │   ├── leaderboard/
        │   ├── statistics/
        │   └── common/
        └── views/
```

## 🚀 Quick Start

### Voraussetzungen
- Docker & Docker Compose
- Git
- PS4 Pro im gleichen Netzwerk
- (Optional) Node.js 18+ für lokale Frontend-Entwicklung
- (Optional) Java 17+ & Gradle für lokale Backend-Entwicklung

### 1. Repository Setup
```bash
# Erstelle Projektverzeichnis
mkdir racing-stats-project
cd racing-stats-project

# Erstelle Unterverzeichnisse
mkdir -p backend/src/main/{groovy,resources}
mkdir -p frontend/src
```

### 2. Dateien erstellen
Kopiere alle Dateien aus den Konzept-Dokumenten:
- Teil 1: Domain Models, Database Schema
- Teil 2: Services & Repositories
- Teil 3: REST API Controllers
- Teil 4: Vue.js Frontend

### 3. Docker Compose starten
```bash
# Im Hauptverzeichnis
docker-compose up -d

# Logs verfolgen
docker-compose logs -f
```

### 4. PlayStation konfigurieren

#### Assetto Corsa (PS4 Pro)
```
1. Optionen → Netzwerk
2. UDP Output aktivieren
3. IP-Adresse: [Docker-Host IP] (z.B. 192.168.1.100)
4. Port: 9996
5. Übernehmen & Spiel neu starten
```

#### F1 2024
```
1. Settings → Telemetry Settings
2. UDP Telemetry: ON
3. UDP IP Address: [Docker-Host IP]
4. UDP Port: 20777
5. UDP Send Rate: 20 Hz
```

#### Gran Turismo 7
```
1. Optionen → Netzwerk
2. Datenlogger aktivieren
3. IP: [Docker-Host IP]
4. Port: 33740
```

### 5. Dashboard öffnen
```
Browser: http://localhost:8080
```

## 🎮 Development Workflow

### Backend Development
```bash
cd backend

# Tests ausführen
./gradlew test

# Application lokal starten
./gradlew bootRun

# Port: 8080
```

### Frontend Development
```bash
cd frontend

# Dependencies installieren
npm install

# Dev Server starten
npm run dev

# Port: 5173 (mit Proxy zu Backend)
```

### Live Reload
- Backend: Spring Boot DevTools aktiviert
- Frontend: Vite HMR (Hot Module Replacement)

## 📊 API Endpoints

### Dashboard
```
GET  /api/dashboard/overview          # Dashboard Übersicht
GET  /api/dashboard/telemetry/live    # Live Telemetrie
```

### Leaderboards
```
GET  /api/leaderboard/global                    # Global Leaderboard
GET  /api/leaderboard/track/{trackId}           # Track Leaderboard
GET  /api/leaderboard/track/{trackId}/car-comparison  # Auto-Vergleich
```

### Statistics
```
GET  /api/statistics/driver/{driverId}  # Driver Stats
GET  /api/statistics/track/{trackId}    # Track Stats
POST /api/statistics/calculate          # Trigger Stats Calculation
```

### Data
```
GET  /api/drivers               # Alle Fahrer
GET  /api/drivers/{id}          # Einzelner Fahrer
GET  /api/tracks                # Alle Strecken
GET  /api/tracks/{id}           # Einzelne Strecke
GET  /api/sessions/{id}         # Session Details
GET  /api/sessions/progress     # Personal Progress
```

## 🎨 Frontend Routes

```
/                                # Dashboard
/leaderboard                     # Leaderboards
/driver/:id                      # Driver Profile
/track/:id                       # Track Analysis
/session/:id                     # Session Details
```

## 🔍 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Statistics Endpoint
```bash
curl http://localhost:8080/stats
```

### Logs
```bash
# Alle Container
docker-compose logs -f

# Nur Backend
docker-compose logs -f racing-stats-app

# Nur Database
docker-compose logs -f postgres
```

### Database Management
```bash
# pgAdmin öffnen (wenn aktiviert)
http://localhost:5050

# Login: admin@racing.local / admin123
```

## 🐛 Troubleshooting

### Problem: Keine UDP-Daten ankommen
```bash
# 1. Prüfe ob Container lauscht
docker exec racing-stats-app netstat -uln | grep -E '9996|20777|33740'

# 2. Prüfe Firewall
sudo ufw allow 9996/udp
sudo ufw allow 20777/udp
sudo ufw allow 33740/udp

# 3. Test mit tcpdump
sudo tcpdump -i any -n udp port 9996 -X

# 4. Prüfe PlayStation IP-Konfiguration
# Verwende NICHT 127.0.0.1!
# Verwende die lokale IP des Docker-Hosts
```

### Problem: Backend startet nicht
```bash
# Prüfe Logs
docker-compose logs racing-stats-app

# Prüfe Datenbank
docker-compose logs postgres

# Neustart
docker-compose restart racing-stats-app
```

### Problem: Frontend lädt nicht
```bash
# Im Development Mode
cd frontend
npm run dev

# Im Production Mode (eingebettet in Backend)
# Prüfe ob build/libs/*.jar die static files enthält
jar tf build/libs/racing-stats-1.0.0.jar | grep static
```

## 📈 Performance-Optimierung

### UDP Rate Limiting
```yaml
# docker-compose.yml
environment:
  - UPDATE_RATE=10  # Reduziere auf 10 Hz für bessere Performance
```

### Database Indexing
Bereits optimiert in `init.sql`:
- Sessions: driver_id, track_id, started_at
- Laps: session_id, completed_at
- Statistics: driver_id + track_id

### Frontend Optimization
```javascript
// Lazy Loading für Routes
const Dashboard = () => import('@/views/Dashboard.vue')

// Debouncing für Live Updates
const debouncedUpdate = debounce(fetchTelemetry, 2000)
```

## 🔒 Security

### Production Checklist
- [ ] Ändere alle Default-Passwörter in `docker-compose.yml`
- [ ] Aktiviere HTTPS für REST API
- [ ] Füge API Rate Limiting hinzu
- [ ] Konfiguriere CORS richtig
- [ ] Aktiviere Spring Security für sensible Endpoints
- [ ] Verwende Secrets für Passwörter (nicht in Git!)

### Environment Variables
```bash
# .env Datei erstellen
POSTGRES_PASSWORD=secure_password_here
N8N_WEBHOOK_SECRET=webhook_secret_here
JWT_SECRET=jwt_secret_here
```

## 📦 Deployment

### Production Build
```bash
# Frontend bauen
cd frontend
npm run build

# Backend mit Frontend bauen
cd ../backend
./gradlew bootJar

# JAR läuft auf Port 8080
java -jar build/libs/racing-stats-1.0.0.jar
```

### Docker Production
```bash
# Build
docker-compose build

# Start mit Production Profile
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Mit Resource Limits
docker-compose --compatibility up -d
```

## 🎯 Roadmap / Future Features

### Phase 2 (Optional)
- [ ] WebSocket für Echtzeit-Updates (statt Polling)
- [ ] Export Funktionen (CSV, JSON)
- [ ] Session Replay Feature
- [ ] Mobile App (React Native)
- [ ] Weather Tracking (für Spiele mit Wetter)
- [ ] Tire Wear Analysis
- [ ] Fuel Strategy Calculator
- [ ] AI-basierte Tipps ("Du verlierst Zeit in Sektor 2")
- [ ] Multiplayer-Session Recording
- [ ] Discord Bot Integration
- [ ] Twitch/YouTube Overlay

### Phase 3 (Nice-to-have)
- [ ] Machine Learning: Optimal Racing Line Prediction
- [ ] VR Dashboard Integration
- [ ] Arduino/ESP32 Dashboard (externes Display)
- [ ] Race Strategy Simulator
- [ ] Social Features (Challenges, Achievements)

## 📚 Dokumentation

Vollständige technische Dokumentation in:
- `racing-stats-konzept-teil1.md` - Domain Models & Database
- `racing-stats-konzept-teil2.md` - Services & Business Logic
- `racing-stats-konzept-teil3.md` - REST API & Vue.js Setup
- `racing-stats-konzept-teil4.md` - Frontend Views & Charts

## 🤝 Contributing

1. Fork the project
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📝 License

This project is for personal use. Modify as needed.

## 👨‍💻 Entwickelt mit

- ❤️ für Racing Games
- ☕ viel Kaffee
- 🏎️ Leidenschaft für Daten & Statistiken

## 🆘 Support

Bei Fragen oder Problemen:
1. Prüfe Logs: `docker-compose logs -f`
2. Checke Troubleshooting-Sektion oben
3. Verifiziere PlayStation-Konfiguration
4. Prüfe Netzwerk-Verbindung (Firewall, Router)

## ⭐ Star History

Wenn dir dieses Projekt gefällt, gib ihm einen Star! 🌟

---

**Happy Racing! 🏁**

Let's track those lap times and become faster together!
