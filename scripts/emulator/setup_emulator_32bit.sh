#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "📱 [1/4] Instalando Imagen de Sistema Android 10 (32-bit x86)"
echo "=========================================================="

# Auto-detección del Android SDK y configuración robusta de PATH
ANDROID_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "${ANDROID_ROOT}" ]; then
    if [ -d "/usr/local/lib/android/sdk" ]; then
        ANDROID_ROOT="/usr/local/lib/android/sdk"
    elif [ -d "/opt/android/sdk" ]; then
        ANDROID_ROOT="/opt/android/sdk"
    elif [ -d "${HOME}/Android/Sdk" ]; then
        ANDROID_ROOT="${HOME}/Android/Sdk"
    fi
fi

if [ -n "${ANDROID_ROOT}" ]; then
    export ANDROID_HOME="${ANDROID_ROOT}"
    export ANDROID_SDK_ROOT="${ANDROID_ROOT}"
    export PATH="${ANDROID_ROOT}/emulator:${ANDROID_ROOT}/platform-tools:${ANDROID_ROOT}/cmdline-tools/latest/bin:${ANDROID_ROOT}/tools:${ANDROID_ROOT}/tools/bin:${PATH}"
    
    # Exportar a GITHUB_PATH y GITHUB_ENV si se ejecuta en GitHub Actions
    if [ -n "${GITHUB_PATH:-}" ] && [ -f "${GITHUB_PATH}" ]; then
        echo "${ANDROID_ROOT}/emulator" >> "${GITHUB_PATH}"
        echo "${ANDROID_ROOT}/platform-tools" >> "${GITHUB_PATH}"
        echo "${ANDROID_ROOT}/cmdline-tools/latest/bin" >> "${GITHUB_PATH}"
    fi
    if [ -n "${GITHUB_ENV:-}" ] && [ -f "${GITHUB_ENV}" ]; then
        echo "ANDROID_HOME=${ANDROID_ROOT}" >> "${GITHUB_ENV}"
        echo "ANDROID_SDK_ROOT=${ANDROID_ROOT}" >> "${GITHUB_ENV}"
    fi
fi

# Localizar binario de sdkmanager
SDKMANAGER_CMD=""
if command -v sdkmanager >/dev/null 2>&1; then
    SDKMANAGER_CMD="$(command -v sdkmanager)"
elif [ -n "${ANDROID_ROOT:-}" ] && [ -x "${ANDROID_ROOT}/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_CMD="${ANDROID_ROOT}/cmdline-tools/latest/bin/sdkmanager"
fi

if [ -z "${SDKMANAGER_CMD}" ]; then
    echo "❌ Error: No se encontró 'sdkmanager' en PATH ni en Android SDK."
    exit 1
fi

# Aceptar licencias del SDK de Android
yes | "${SDKMANAGER_CMD}" --licenses >/dev/null 2>&1 || true

# Imagen de 32 bits: Android 10 (API 29) arquitectura x86 pura con Google APIs
SYSTEM_IMAGE="system-images;android-29;google_apis;x86"

echo ">> Verificando / Descargando imagen de sistema: ${SYSTEM_IMAGE}..."
"${SDKMANAGER_CMD}" "${SYSTEM_IMAGE}" "platform-tools" "emulator"

# Verificar que el binario de emulator se encuentre presente
if [ -n "${ANDROID_ROOT:-}" ] && [ -x "${ANDROID_ROOT}/emulator/emulator" ]; then
    echo "✅ Binario de 'emulator' verificado en: ${ANDROID_ROOT}/emulator/emulator"
fi

echo "=========================================================="
echo "📱 [2/4] Creando Dispositivo Virtual Android (AVD) de 32 bits"
echo "=========================================================="

AVDMANAGER_CMD=""
if command -v avdmanager >/dev/null 2>&1; then
    AVDMANAGER_CMD="$(command -v avdmanager)"
elif [ -n "${ANDROID_ROOT:-}" ] && [ -x "${ANDROID_ROOT}/cmdline-tools/latest/bin/avdmanager" ]; then
    AVDMANAGER_CMD="${ANDROID_ROOT}/cmdline-tools/latest/bin/avdmanager"
fi

if [ -z "${AVDMANAGER_CMD}" ]; then
    echo "❌ Error: No se encontró 'avdmanager' en PATH ni en Android SDK."
    exit 1
fi

AVD_NAME="android10_32bit_test"

# Eliminar si existía previamente
"${AVDMANAGER_CMD}" delete avd -n "${AVD_NAME}" 2>/dev/null || true

# Crear AVD con hardware móvil estándar tipo Pixel
echo "no" | "${AVDMANAGER_CMD}" create avd \
    --name "${AVD_NAME}" \
    --package "${SYSTEM_IMAGE}" \
    --device "pixel" \
    --force

echo "✅ Dispositivo virtual '${AVD_NAME}' (Android 10 x86 32-bit) creado con éxito."
