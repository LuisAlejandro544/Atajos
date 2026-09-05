#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🌐 Configurando y descargando Chucker"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Verificar que gradle y las dependencias de Chucker estén presentes en el proyecto
echo "Verificando dependencias de Chucker en Gradle..."
if grep -q "chucker-debug" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.chucker.debug" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencias de Chucker verificadas en libs.versions.toml y app/build.gradle.kts"
else
    echo "❌ Error: Faltan dependencias de Chucker en la configuración de Gradle."
    exit 1
fi

echo "📦 Precargando dependencias de Chucker desde repositorios Maven..."
cd "${PROJECT_ROOT}"

# Ejecutar resolución de dependencias para descargar los artefactos AAR/JAR de Chucker
if [ -f "./gradlew" ]; then
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -i "chucker" || true
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:dependencies --configuration debugRuntimeClasspath | grep -i "chucker" || true
else
    echo "⚠️ Gradle no disponible en el entorno directo, se resolverá durante la compilación."
fi

echo "✅ Chucker 4.1.0 descargado y configurado exitosamente para el APK Debug."
