#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🛠️ Configurando CMake 3.22.1 y NDK 27"
echo "============================================="

# Detectar directorio del SDK de Android
ANDROID_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"

if [ -z "${ANDROID_DIR}" ]; then
    if [ -d "$HOME/Android/Sdk" ]; then
        ANDROID_DIR="$HOME/Android/Sdk"
    elif [ -d "/usr/local/lib/android/sdk" ]; then
        ANDROID_DIR="/usr/local/lib/android/sdk"
    else
        echo "❌ No se encontró ANDROID_HOME ni ANDROID_SDK_ROOT."
        exit 1
    fi
fi

export ANDROID_HOME="${ANDROID_DIR}"
export ANDROID_SDK_ROOT="${ANDROID_DIR}"
echo "📍 Android SDK ubicado en: ${ANDROID_HOME}"

# Localizar sdkmanager
SDKMANAGER=""
if command -v sdkmanager >/dev/null 2>&1; then
    SDKMANAGER="sdkmanager"
elif [ -f "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
else
    SDKMANAGER=$(find "${ANDROID_HOME}" -name sdkmanager -type f 2>/dev/null | head -n 1 || true)
fi

if [ -n "${SDKMANAGER}" ]; then
    echo "📋 Aceptando licencias del Android SDK..."
    yes | "${SDKMANAGER}" --licenses >/dev/null 2>&1 || true

    echo "⬇️ Asegurando instalación de CMake 3.22.1 y NDK 27.0.12077973..."
    "${SDKMANAGER}" "cmake;3.22.1" "ndk;27.0.12077973"
    echo "✅ Componentes verificados mediante sdkmanager."
else
    echo "ℹ️ sdkmanager no está en el PATH; verificando presencia de carpetas existentes..."
    if [ -d "${ANDROID_HOME}/cmake/3.22.1" ]; then
        echo "✅ CMake 3.22.1 ya está instalado en el SDK."
    else
        echo "❌ Advertencia: CMake 3.22.1 no encontrado."
        exit 1
    fi
fi

echo "✅ Entorno de CMake y NDK listo para compilar librerías nativas."
