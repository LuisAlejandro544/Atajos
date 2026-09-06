#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "📱 [1/4] Instalando Imagen de Sistema Android 10 (32-bit x86)"
echo "=========================================================="

# Aceptar licencias del SDK de Android
yes | sdkmanager --licenses >/dev/null 2>&1 || true

# Imagen de 32 bits: Android 10 (API 29) arquitectura x86 pura con Google APIs
SYSTEM_IMAGE="system-images;android-29;google_apis;x86"

echo ">> Verificando / Descargando imagen de sistema: ${SYSTEM_IMAGE}..."
sdkmanager "${SYSTEM_IMAGE}" "platform-tools" "emulator"

echo "=========================================================="
echo "📱 [2/4] Creando Dispositivo Virtual Android (AVD) de 32 bits"
echo "=========================================================="

AVD_NAME="android10_32bit_test"

# Eliminar si existía previamente
avdmanager delete avd -n "${AVD_NAME}" 2>/dev/null || true

# Crear AVD con hardware móvil estándar tipo Pixel
echo "no" | avdmanager create avd \
    --name "${AVD_NAME}" \
    --package "${SYSTEM_IMAGE}" \
    --device "pixel" \
    --force

echo "✅ Dispositivo virtual '${AVD_NAME}' (Android 10 x86 32-bit) creado con éxito."
