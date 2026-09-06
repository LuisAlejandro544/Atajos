#!/usr/bin/env bash
set -euo pipefail

AVD_NAME="android10_32bit_test"

echo "=========================================================="
echo "🚀 [3/4] Arrancando Emulador Android 10 (32-bit x86)"
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
fi

# Localizar binario ejecutable de emulator
EMULATOR_CMD=""
if command -v emulator >/dev/null 2>&1; then
    EMULATOR_CMD="$(command -v emulator)"
elif [ -n "${ANDROID_ROOT:-}" ] && [ -x "${ANDROID_ROOT}/emulator/emulator" ]; then
    EMULATOR_CMD="${ANDROID_ROOT}/emulator/emulator"
elif [ -x "/usr/local/lib/android/sdk/emulator/emulator" ]; then
    EMULATOR_CMD="/usr/local/lib/android/sdk/emulator/emulator"
fi

if [ -z "${EMULATOR_CMD}" ]; then
    echo "❌ Error crítico: No se encontró el binario 'emulator' en PATH ni en Android SDK."
    echo "   Ruta buscada: ${ANDROID_ROOT:-}/emulator/emulator"
    echo "   PATH actual: ${PATH}"
    exit 1
fi
echo ">> Binario del emulador localizado en: ${EMULATOR_CMD}"

# Localizar binario de adb
ADB_CMD=""
if command -v adb >/dev/null 2>&1; then
    ADB_CMD="$(command -v adb)"
elif [ -n "${ANDROID_ROOT:-}" ] && [ -x "${ANDROID_ROOT}/platform-tools/adb" ]; then
    ADB_CMD="${ANDROID_ROOT}/platform-tools/adb"
fi

if [ -z "${ADB_CMD}" ]; then
    echo "❌ Error crítico: No se encontró 'adb' en PATH ni en Android SDK."
    exit 1
fi

# Comprobar lista de AVDs registrados
echo ">> Verificando AVD '${AVD_NAME}'..."
AVD_LIST=$("${EMULATOR_CMD}" -list-avds 2>/dev/null || true)
echo "   AVDs registrados: $(echo "${AVD_LIST}" | tr '\n' ' ')"

# Comprobar soporte de virtualización por hardware (KVM)
if [ -e /dev/kvm ] && [ -w /dev/kvm ]; then
    echo "⚡ Aceleración KVM activa y accesible (/dev/kvm)."
    ACCEL_ARGS="-accel on"
else
    echo "⚠️ Advertencia: KVM no accesible directamente en este entorno; arrancando modo estándar."
    ACCEL_ARGS="-accel auto"
fi

# Exportar DISPLAY virtual para renderizado gráfico con noVNC
export DISPLAY="${DISPLAY:-:99}"
echo "🖥️ Renderizando interfaz gráfica del emulador en DISPLAY ${DISPLAY}."

# Limpiar logs previos
rm -f /tmp/emulator.log

# Iniciar emulador en segundo plano conectado al display virtual para streaming interactivo
"${EMULATOR_CMD}" -avd "${AVD_NAME}" \
    -no-boot-anim \
    -no-audio \
    -no-snapshot \
    -camera-back none \
    -camera-front none \
    -gpu swiftshader_indirect \
    -memory 2048 \
    -cores 2 \
    -netfast \
    ${ACCEL_ARGS} > /tmp/emulator.log 2>&1 &

EMULATOR_PID=$!
echo ">> Emulador iniciado con PID: ${EMULATOR_PID}"

# Verificar inmediatamente si el proceso falló en los primeros segundos
sleep 3
if ! kill -0 "${EMULATOR_PID}" 2>/dev/null; then
    echo "❌ Error crítico: El proceso del emulador finalizó inmediatamente tras iniciar."
    echo "=========================================================="
    echo "📋 Logs de inicialización del emulador (/tmp/emulator.log):"
    echo "=========================================================="
    cat /tmp/emulator.log 2>/dev/null || true
    echo "=========================================================="
    exit 1
fi
echo "✅ Proceso del emulador activo en segundo plano (PID: ${EMULATOR_PID})."

echo ">> Esperando a que el emulador responda a ADB (máximo 120s)..."
WAIT_ADB_START=$(date +%s)
TIMEOUT_ADB=120

while true; do
    if ! kill -0 "${EMULATOR_PID}" 2>/dev/null; then
        echo "❌ Error: El proceso del emulador murió inesperadamente mientras se esperaba ADB."
        echo "=========================================================="
        echo "📋 Logs del emulador (/tmp/emulator.log):"
        echo "=========================================================="
        cat /tmp/emulator.log 2>/dev/null || true
        echo "=========================================================="
        exit 1
    fi

    # Comprobar si adb detecta el dispositivo
    if "${ADB_CMD}" devices 2>/dev/null | grep -E "emulator-[0-9]+" | grep -q "device"; then
        echo "✅ Emulador detectado y en estado activo por ADB."
        break
    fi

    NOW=$(date +%s)
    ELAPSED=$(( NOW - WAIT_ADB_START ))
    if [ ${ELAPSED} -gt ${TIMEOUT_ADB} ]; then
        echo "❌ Error: Tiempo de espera agotado (${TIMEOUT_ADB}s) esperando a que ADB detecte el emulador."
        echo "=========================================================="
        echo "📋 Estado actual de ADB:"
        "${ADB_CMD}" devices -l || true
        echo "📋 Logs del emulador (/tmp/emulator.log):"
        cat /tmp/emulator.log 2>/dev/null || true
        echo "=========================================================="
        kill -9 "${EMULATOR_PID}" 2>/dev/null || true
        exit 1
    fi

    echo "   ... Esperando detección por ADB (${ELAPSED}s / ${TIMEOUT_ADB}s)"
    sleep 3
done

echo ">> Esperando a que finalice el arranque de Android (sys.boot_completed)..."
START_TIME=$(date +%s)
TIMEOUT_SECONDS=300

while true; do
    if ! kill -0 "${EMULATOR_PID}" 2>/dev/null; then
        echo "❌ Error: El emulador murió durante la fase de inicio del sistema operativo."
        echo "=========================================================="
        echo "📋 Logs del emulador (/tmp/emulator.log):"
        echo "=========================================================="
        cat /tmp/emulator.log 2>/dev/null || true
        echo "=========================================================="
        exit 1
    fi

    BOOT_COMPLETED=$("${ADB_CMD}" shell getprop sys.boot_completed 2>/dev/null || echo "0")
    if [ "${BOOT_COMPLETED}" = "1" ]; then
        echo "✅ El sistema Android 10 completó el arranque."
        break
    fi

    CURRENT_TIME=$(date +%s)
    ELAPSED=$(( CURRENT_TIME - START_TIME ))
    if [ ${ELAPSED} -gt ${TIMEOUT_SECONDS} ]; then
        echo "❌ Error: Tiempo de espera agotado (${TIMEOUT_SECONDS}s) esperando el arranque del emulador."
        echo "=========================================================="
        echo "📋 Logs del emulador (/tmp/emulator.log):"
        echo "=========================================================="
        cat /tmp/emulator.log 2>/dev/null || true
        echo "=========================================================="
        kill -9 "${EMULATOR_PID}" 2>/dev/null || true
        exit 1
    fi

    echo "   ... Arrancando sistema operativo (${ELAPSED}s / ${TIMEOUT_SECONDS}s)"
    sleep 5
done

echo ""
echo "=========================================================="
echo "🔍 Diagnóstico de Arquitectura y CPU del Sistema Emulado:"
echo "=========================================================="
ABI=$("${ADB_CMD}" shell getprop ro.product.cpu.abi)
ABI_LIST=$("${ADB_CMD}" shell getprop ro.product.cpu.abilist)
ANDROID_VER=$("${ADB_CMD}" shell getprop ro.build.version.release)
API_LEVEL=$("${ADB_CMD}" shell getprop ro.build.version.sdk)
MODEL=$("${ADB_CMD}" shell getprop ro.product.model)

echo "📌 Dispositivo: ${MODEL}"
echo "📌 Versión Android: ${ANDROID_VER} (API ${API_LEVEL})"
echo "📌 Arquitectura Principal (ABI): ${ABI}"
echo "📌 Arquitecturas Soportadas: ${ABI_LIST}"

if [ "${ABI}" = "x86" ]; then
    echo "✅ CONFIRMADO: El entorno se está ejecutando en arquitectura nativa de 32 BITS (x86)."
else
    echo "⚠️ Información: La arquitectura detectada es ${ABI}."
fi
echo "=========================================================="
