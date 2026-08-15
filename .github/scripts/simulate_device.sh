#!/usr/bin/env bash
set -e

API_LEVEL="${1:-34}"
TEST_BATTERY="${2:-true}"

echo "=== INICIANDO SIMULACIÓN DE DISPOSITIVO ANDROID ==="
adb devices

# Obtener paquete de la aplicación
PACKAGE_NAME="com.aistudio.atajos.app"
if [ -f "app/build.gradle.kts" ]; then
  DETECTED_PKG=$(grep -o 'applicationId = "[^"]*"' app/build.gradle.kts | head -n 1 | cut -d'"' -f2 || true)
  if [ -n "$DETECTED_PKG" ]; then
    PACKAGE_NAME="$DETECTED_PKG"
  fi
fi
echo "Target Package: $PACKAGE_NAME"

# 1. Instalar el APK en el emulador
echo "Instalando APK en el emulador..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Conceder permisos necesarios
echo "Configurando permisos de sistema..."
adb shell pm grant "$PACKAGE_NAME" android.permission.POST_NOTIFICATIONS 2>/dev/null || true

# 3. Limpiar logcat para capturar solo la sesión de simulación
adb logcat -c

# 4. Lanzar la aplicación para inicializar Room y servicios en background
echo "Lanzando MainActivity en el emulador..."
adb shell am start -n "$PACKAGE_NAME/com.example.MainActivity"
sleep 5

SUMMARY_FILE="simulation_summary.md"
echo "### 📱 Reporte de Simulación de Hardware Android" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "- **Nivel de API:** $API_LEVEL" >> "$SUMMARY_FILE"
echo "- **Paquete verificado:** \`$PACKAGE_NAME\`" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "| Evento de Hardware Simulado | Comando Inyectado | Resultado |" >> "$SUMMARY_FILE"
echo "| :--- | :--- | :--- |" >> "$SUMMARY_FILE"

# 5. Simulación de eventos de energía y batería
if [ "$TEST_BATTERY" != "false" ]; then
  echo ">>> Simulación 1: Conexión de cargador (AC ON)..."
  adb shell cmd battery set ac 1
  adb shell cmd battery unplug false 2>/dev/null || true
  sleep 3
  echo "| 🔌 Conexión de cargador | \`cmd battery set ac 1\` | ✅ Inyectado correctamente |" >> "$SUMMARY_FILE"

  echo ">>> Simulación 2: Desconexión de cargador (AC OFF)..."
  adb shell cmd battery unplug
  sleep 3
  echo "| 🔋 Desconexión de cargador | \`cmd battery unplug\` | ✅ Inyectado correctamente |" >> "$SUMMARY_FILE"

  echo ">>> Simulación 3: Nivel de batería al 15% (Batería baja)..."
  adb shell cmd battery set level 15
  sleep 3
  echo "| ⚠️ Batería crítica (15%) | \`cmd battery set level 15\` | ✅ Inyectado correctamente |" >> "$SUMMARY_FILE"

  echo ">>> Simulación 4: Nivel de batería al 80%..."
  adb shell cmd battery set level 80
  sleep 3
  echo "| ⚡ Batería al 80% | \`cmd battery set level 80\` | ✅ Inyectado correctamente |" >> "$SUMMARY_FILE"

  echo ">>> Simulación 5: Nivel de batería al 100% (Carga completa)..."
  adb shell cmd battery set level 100
  sleep 3
  echo "| 🔋 Batería al 100% | \`cmd battery set level 100\` | ✅ Inyectado correctamente |" >> "$SUMMARY_FILE"

  echo ">>> Restableciendo estado de batería del dispositivo..."
  adb shell cmd battery reset
fi

# 6. Captura de Logs e inspección de errores de ejecución
echo "Recopilando logs de la sesión..."
adb logcat -d -s PowerTriggerReceiver:V ActionExecutor:V AutomationService:V AndroidRuntime:E > simulation_logs.txt

CRASH_COUNT=$(grep -c "FATAL EXCEPTION" simulation_logs.txt || true)
echo "Excepciones fatales encontradas: $CRASH_COUNT"

if [ -n "$GITHUB_STEP_SUMMARY" ]; then
  cat "$SUMMARY_FILE" >> "$GITHUB_STEP_SUMMARY"
fi

if [ "$CRASH_COUNT" -gt 0 ]; then
  echo "❌ SE DETECTARON CRASHES DURANTE LA SIMULACIÓN:"
  grep -A 10 "FATAL EXCEPTION" simulation_logs.txt
  if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo -e "\n> ❌ **Fallo detectado:** Se encontraron $CRASH_COUNT excepciones fatales en runtime." >> "$GITHUB_STEP_SUMMARY"
  fi
  exit 1
else
  echo "✅ No se detectaron excepciones fatales en tiempo de ejecución."
  if [ -n "$GITHUB_STEP_SUMMARY" ]; then
    echo -e "\n> ✅ **Simulación exitosa:** Ningún crash detectado en runtime durante los eventos de hardware." >> "$GITHUB_STEP_SUMMARY"
  fi
fi
