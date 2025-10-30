#!/bin/bash

# Script zum Generieren der package-lock.json und Docker Build
# Racing Stats Frontend Build Script

echo "🏁 Racing Stats Frontend Build"
echo "================================"

# Prüfe ob wir im richtigen Verzeichnis sind
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Fehler: Bitte im Projekt-Root ausführen"
    exit 1
fi

# Gehe ins Frontend-Verzeichnis
cd frontend

echo "📦 Schritt 1: Generiere package-lock.json..."

# Lösche alte node_modules und package-lock.json
rm -rf node_modules package-lock.json

# Installiere Dependencies und generiere package-lock.json
npm install --legacy-peer-deps

if [ $? -ne 0 ]; then
    echo "❌ Fehler beim Generieren der package-lock.json"
    exit 1
fi

echo "✅ package-lock.json erfolgreich generiert"

# Gehe zurück zum Root
cd ..

echo ""
echo "🐳 Schritt 2: Baue Docker Images..."

# Baue alle Images neu
docker-compose build

if [ $? -ne 0 ]; then
    echo "❌ Fehler beim Docker Build"
    exit 1
fi

echo ""
echo "✅ Build erfolgreich abgeschlossen!"
echo ""
echo "Starte Container mit:"
echo "  docker-compose up -d"
echo ""
echo "Oder direkt starten:"
echo "  docker-compose up -d && docker-compose logs -f"