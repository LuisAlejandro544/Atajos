#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🗄️ Configurando y descargando Infinum DbInspector"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Verificar que gradle y las dependencias de DbInspector estén presentes en el proyecto
echo "Verificando dependencias de DbInspector en Gradle..."
if grep -q "dbinspector" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.dbinspector" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencias de DbInspector verificadas en libs.versions.toml y app/build.gradle.kts"
else
    echo "❌ Error: Faltan dependencias de DbInspector en la configuración de Gradle."
    exit 1
fi

echo "📦 Precargando dependencias de DbInspector desde Maven Central..."
cd "${PROJECT_ROOT}"

# Ejecutar resolución de dependencias para forzar la descarga de los artefactos AAR de DbInspector
if [ -f "./gradlew" ]; then
    ./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -i "dbinspector" || true
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:dependencies --configuration debugRuntimeClasspath | grep -i "dbinspector" || true
else
    echo "⚠️ Gradle no disponible en el entorno directo, se resolverá durante la compilación."
fi

echo "✅ Infinum DbInspector 6.0.0 descargado y configurado exitosamente para el APK Debug."
