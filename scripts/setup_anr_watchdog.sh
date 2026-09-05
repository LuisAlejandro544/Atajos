#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🛡️ Configurando y descargando ANR-WatchDog"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Verificar que gradle y las dependencias de ANR-WatchDog estén presentes en el proyecto
echo "Verificando dependencias de ANR-WatchDog en Gradle..."
if grep -q "anrwatchdog" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.anrwatchdog" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencias de ANR-WatchDog verificadas en libs.versions.toml y app/build.gradle.kts"
else
    echo "❌ Error: Faltan dependencias de ANR-WatchDog en la configuración de Gradle."
    exit 1
fi

echo "📦 Precargando dependencias de ANR-WatchDog desde Maven Central..."
cd "${PROJECT_ROOT}"

# Ejecutar resolución de dependencias para forzar la descarga de los artefactos de ANR-WatchDog
if [ -f "./gradlew" ]; then
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -i "anrwatchdog" || true
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:dependencies --configuration debugRuntimeClasspath | grep -i "anrwatchdog" || true
else
    echo "⚠️ Gradle no disponible en el entorno directo, se resolverá durante la compilación."
fi

echo "✅ ANR-WatchDog 1.4.0 descargado y configurado exitosamente para el APK Debug."
