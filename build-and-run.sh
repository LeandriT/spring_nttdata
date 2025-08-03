#!/bin/bash

echo "🧹 Limpiando contenedores, redes y volúmenes de Docker previos..."
docker compose down --volumes --remove-orphans
docker system prune -f

echo "🔨 Compilando accounts-movements-service..."
cd ./accounts-movements-service || exit
./gradlew clean build || { echo "❌ Error al compilar accounts-movements-service"; exit 1; }
cd ..

echo "🔨 Compilando customer-service..."
cd ./customer-service || exit
./gradlew clean build || { echo "❌ Error al compilar customer-service"; exit 1; }
cd ..

echo "🐳 Levantando servicios con Docker Compose..."
docker compose up --build -d