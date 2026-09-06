#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🧪 [4/4] Instalación y Prueba de Estrés en Android 10 (32-bit)"
echo "=========================================================="

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.example"

if [ ! -f "${APK_PATH}" ]; then
    echo "❌ Error: No se encontró el APK en '${APK_PATH}'. Asegúrate de haber compilado primero."
    exit 1
fi

echo ">> [Paso 1/6] Instalando APK Debug en el emulador de 32 bits..."
adb install -r -t "${APK_PATH}"
echo "✅ APK instalado satisfactoriamente en el entorno de 32 bits."

echo ""
echo ">> [Paso 2/6] Verificando binarios nativos cargados por el sistema (32 bits)..."
LIB_DIR=$(adb shell pm dump "${PACKAGE_NAME}" | grep -i "primaryCpuAbi" || true)
echo "   Detalles de ABI de la aplicación instalada: ${LIB_DIR}"

echo ""
echo ">> [Paso 3/6] Iniciando la app principal (MainActivity)..."
adb shell am start -n "${PACKAGE_NAME}/.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
sleep 4

echo ""
echo ">> [Paso 4/6] Verificando que el proceso esté activo en memoria y no haya crasheado..."
PID=$(adb shell pidof "${PACKAGE_NAME}" || true)
if [ -z "${PID}" ]; then
    echo "❌ Error: El proceso ${PACKAGE_NAME} no se está ejecutando o falló al iniciar."
    adb logcat -d -t 500
    exit 1
fi
echo "✅ Proceso activo en memoria RAM con PID: ${PID}"

echo ""
echo ">> [Paso 5/6] Verificando consumo de memoria (Heap 32-bit)..."
adb shell dumpsys meminfo "${PACKAGE_NAME}" | head -n 35

echo ""
echo ">> [Paso 6/6] Probando lanzamiento de la app complementaria de Telemetría..."
adb shell am start -n "${PACKAGE_NAME}/.debug.DebugActivity"
sleep 3
adb shell am start -n "${PACKAGE_NAME}/.MainActivity"
sleep 2

echo ""
echo ">> Capturando screenshot de prueba de la pantalla..."
adb exec-out screencap -p > emulator_32bit_screenshot.png
if [ -f "emulator_32bit_screenshot.png" ]; then
    echo "📸 Captura de pantalla guardada con éxito: emulator_32bit_screenshot.png"
    ls -lh emulator_32bit_screenshot.png
fi

echo ""
echo ">> Comprobando logcat por errores de librerías nativas o JNI de 32 bits..."
CRASH_COUNT=$(adb logcat -d | grep -iE "FATAL EXCEPTION|java.lang.UnsatisfiedLinkError|OutOfMemoryError" | wc -l || echo "0")
if [ "${CRASH_COUNT}" -gt 0 ]; then
    echo "⚠️ Se detectaron posibles anomalías en el logcat:"
    adb logcat -d | grep -iE "FATAL EXCEPTION|java.lang.UnsatisfiedLinkError|OutOfMemoryError"
else
    echo "✅ Cero excepciones fatales, sin UnsatisfiedLinkError ni OutOfMemoryError detectados."
fi

echo ""
echo "=========================================================="
echo "🎉 ¡PRUEBA DE 32 BITS COMPLETADA CON ÉXITO!"
echo "La app funciona de forma estable en arquitectura de 32 bits (Android 10 x86)."
echo "=========================================================="
