#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🚀 Compilación de APK Debug de Atajos (Sin Caché)"
echo "=========================================================="

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPTS_DIR}/../.." && pwd)"
cd "${PROJECT_ROOT}"

echo ""
echo ">> [1/4] Asegurando Keystore para compilación Debug..."
bash "${PROJECT_ROOT}/scripts/build/generate_keystore.sh"

echo ""
echo ">> [2/4] Verificando dependencias de CMake y NDK..."
bash "${PROJECT_ROOT}/scripts/build/setup_cmake.sh"

echo ""
echo ">> [3/5] Asegurando fuentes oficiales de Lua 5.4.7..."
bash "${PROJECT_ROOT}/scripts/lua/setup_lua.sh"

echo ""
echo ">> [4/5] Convirtiendo y preparando recursos de audio (OGG sin delay)..."
bash "${PROJECT_ROOT}/scripts/audio/convert_audio.sh"

echo ""
echo ">> [5/5] Compilando APK Debug (sin caché de compilación)..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew :app:assembleDebug --no-daemon --no-build-cache --stacktrace
elif command -v gradle >/dev/null 2>&1; then
    gradle :app:assembleDebug --no-daemon --no-build-cache --stacktrace
else
    echo "❌ Error: Ni gradlew ni gradle están disponibles en el entorno."
    exit 1
fi

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "${APK_PATH}" ]; then
    echo ""
    echo "=========================================================="
    echo "✅ ¡ÉXITO! APK Debug generado correctamente:"
    echo "📦 Ubicación: ${APK_PATH}"
    ls -lh "${APK_PATH}"
    echo "=========================================================="
else
    echo "❌ Error: El archivo app-debug.apk no fue encontrado tras la compilación."
    exit 1
fi
