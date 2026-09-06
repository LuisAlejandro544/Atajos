#!/usr/bin/env bash
set -euo pipefail

AVD_NAME="android10_32bit_test"

echo "=========================================================="
echo "🚀 [3/4] Arrancando Emulador Android 10 (32-bit x86)"
echo "=========================================================="

# Comprobar soporte de virtualización por hardware (KVM)
if [ -e /dev/kvm ] && [ -w /dev/kvm ]; then
    echo "⚡ Aceleración KVM activa y accesible."
    ACCEL_ARGS="-accel on"
else
    echo "⚠️ Advertencia: KVM no accesible directamente en este entorno; arrancando modo estándar."
    ACCEL_ARGS="-accel auto"
fi

# Exportar DISPLAY virtual para renderizado gráfico con noVNC si está disponible
if [ -n "${DISPLAY:-}" ]; then
    echo "🖥️ Renderizando interfaz gráfica del emulador en DISPLAY ${DISPLAY}."
    WINDOW_ARG=""
else
    export DISPLAY=:99
    WINDOW_ARG=""
fi

# Iniciar emulador en segundo plano conectado al display virtual para streaming interactivo
emulator -avd "${AVD_NAME}" \
    ${WINDOW_ARG} \
    -no-boot-anim \
    -no-audio \
    -gpu swiftshader_indirect \
    -memory 2048 \
    -cores 2 \
    -netfast \
    ${ACCEL_ARGS} &

EMULATOR_PID=$!
echo ">> Emulador iniciado con PID: ${EMULATOR_PID}"

echo ">> Esperando a que el emulador responda a ADB..."
adb wait-for-device

echo ">> Esperando a que finalice el arranque de Android (sys.boot_completed)..."
START_TIME=$(date +%s)
TIMEOUT_SECONDS=300

while true; do
    BOOT_COMPLETED=$(adb shell getprop sys.boot_completed 2>/dev/null || echo "0")
    if [ "${BOOT_COMPLETED}" = "1" ]; then
        echo "✅ El sistema Android 10 completó el arranque."
        break
    fi

    CURRENT_TIME=$(date +%s)
    ELAPSED=$(( CURRENT_TIME - START_TIME ))
    if [ ${ELAPSED} -gt ${TIMEOUT_SECONDS} ]; then
        echo "❌ Error: Tiempo de espera agotado (${TIMEOUT_SECONDS}s) esperando el arranque del emulador."
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
ABI=$(adb shell getprop ro.product.cpu.abi)
ABI_LIST=$(adb shell getprop ro.product.cpu.abilist)
ANDROID_VER=$(adb shell getprop ro.build.version.release)
API_LEVEL=$(adb shell getprop ro.build.version.sdk)
MODEL=$(adb shell getprop ro.product.model)

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
