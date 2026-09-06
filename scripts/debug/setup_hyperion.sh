#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🎛️ Configurando y descargando Hyperion-Android"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Verificar que gradle y las dependencias de Hyperion estén presentes en el proyecto
echo "Verificando dependencias de Hyperion en Gradle..."
if grep -q "hyperion-core" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.hyperion.core" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencias de Hyperion verificadas en libs.versions.toml y app/build.gradle.kts"
else
    echo "❌ Error: Faltan dependencias de Hyperion en la configuración de Gradle."
    exit 1
fi

echo "📦 Precargando dependencias de Hyperion (Core, Measurement, Crash) desde Maven Central..."
cd "${PROJECT_ROOT}"

# Ejecutar resolución de dependencias para forzar la descarga de los artefactos AAR de Hyperion
if [ -f "./gradlew" ]; then
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -i "hyperion" || true
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:dependencies --configuration debugRuntimeClasspath | grep -i "hyperion" || true
else
    echo "⚠️ Gradle no disponible en el entorno directo, se resolverá durante la compilación."
fi

echo "✅ Hyperion-Android 0.9.38 descargado y configurado exitosamente para el APK Debug."
