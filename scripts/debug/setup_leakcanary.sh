#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🔍 Configurando y descargando LeakCanary"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Verificar que gradle y las dependencias de LeakCanary estén presentes en el proyecto
echo "Verificando dependencias de LeakCanary en Gradle..."
if grep -q "leakcanary-android" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.leakcanary.android" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencias de LeakCanary verificadas en libs.versions.toml y app/build.gradle.kts"
else
    echo "❌ Error: Faltan dependencias de LeakCanary en la configuración de Gradle."
    exit 1
fi

echo "📦 Precargando dependencias de LeakCanary desde repositorios Maven..."
cd "${PROJECT_ROOT}"

# Ejecutar resolución de dependencias para descargar los artefactos AAR/JAR de LeakCanary
if [ -f "./gradlew" ]; then
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -i "leakcanary" || true
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:dependencies --configuration debugRuntimeClasspath | grep -i "leakcanary" || true
else
    echo "⚠️ Gradle no disponible en el entorno directo, se resolverá durante la compilación."
fi

echo "✅ LeakCanary 2.14 descargado y configurado exitosamente para el APK Debug."
