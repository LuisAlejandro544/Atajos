# 🧠 AI Context & Development Knowledge Base

Este documento proporciona contexto técnico estructurado y directo para modelos de lenguaje (LLMs) y agentes de codificación que trabajen en este repositorio.

---

## 📌 Resumen del Proyecto

- **Nombre**: Flurix para Android
- **Versión Actual**: `0.1.0-E` (Canal Estable)
- **Esquema de Prefijos de Versión**:
  - `-E` (Estable): Versiones consolidadas y verificadas para uso general.
  - `-DEV` (Desarrollo): Funciones puras en desarrollo que pueden contener errores, inestabilidad y características experimentales que pueden aparecer o desaparecer.
  - `-B` (Beta): Funciones que llegarán a la versión estable pero que aún pueden presentar inestabilidad.
- **Propósito**: Automatización local y secuencial de tareas en el dispositivo Android mediante disparadores y acciones encadenadas.
- **Licencia y Modelo**: Source-Available bajo **PolyForm Noncommercial License 1.0.0** (Licensor / Autor: Luis Alejandro Sosa Camacho). Código visible para inspección y uso personal no comercial; redistribución y republicación por terceros estrictamente prohibida.
- **Enfoque de Distribución**: Sin ataduras a Google Play. Diseñado para funcionar de manera independiente, compatible con distribución directa (APK sideloading) o tiendas de terceros (Uptodown).
- **Paradigma**: 100% Kotlin nativo con Jetpack Compose y Room SQLite.

---

## 🔑 Entidades Clave del Modelo de Datos

1. **`ShortcutEntity`** (`data/model/ShortcutEntity.kt`):
   - Representa un atajo configurable.
   - Campos: `id`, `title`, `description`, `iconKey`, `colorHex`, `category`, `actionsJson` (lista serializada de `ShortcutAction`), `isFavorite`, `trigger` (`NONE`, `POWER_CONNECTED`, `POWER_DISCONNECTED`, `POWER_BOTH`), `runCount`, `lastRunTimestamp`, `createdAt`.

2. **`ShortcutTrigger`** (`data/model/ShortcutTrigger.kt`):
   - Enum que define los disparadores automáticos nativos del sistema asociados directamente a cada atajo (`NONE`, `BATTERY_EXACT`, `TIME_EXACT`, `POWER_CONNECTED`, `POWER_DISCONNECTED`, `POWER_BOTH`, `BATTERY_LOW`, `BATTERY_OK`, `BATTERY_FULL`).
   - Métodos auxiliares: `buildBatteryExactKey(percent)`, `getBatteryExactLevel(key)`, `buildTimeExactKey(time)`, `getTimeExactValue(key)`, `getBaseKey(key)`.

3. **`ShortcutTileService`** (`engine/tiles/ShortcutTileService.kt`):
   - Servicio `TileService` de Android que expone un Mosaico en la cortina de Ajustes Rápidos (`Quick Settings`) para ejecutar el atajo favorito o más reciente con un solo toque desde cualquier pantalla o bloqueo.

3. **`ShortcutAction`** (`data/model/ShortcutAction.kt`):
   - Paso individual dentro de un atajo.
   - Campos: `id`, `type` (`ActionType`), `title`, `param1`, `param2`, `param3`, `orderIndex`.

3. **`ActionType`** (`data/model/ActionType.kt`):
   - Tipos soportados:
     - `LAUNCH_APP`: Iniciar juego o aplicación instalada. `param1`: `packageName`, `param2`: `appName`.
     - `HTTP_REQUEST`: Peticiones HTTP y Webhooks (GET, POST, PUT, DELETE) con variables en URL y Body. `param1`: URL, `param2`: método, `param3`: cuerpo JSON/texto. Almacena la respuesta en `{RESPUESTA_WEB}` y el código en `{HTTP_STATUS}`.
     - `SET_BRIGHTNESS`: Control de brillo de pantalla (`WRITE_SETTINGS`). `param1`: porcentaje "0"-"100" o acción "increase" / "decrease".
     - `SET_VOLUME`: Control de volumen de audio. `param1`: canal ("music", "notification", "ring", "alarm"), `param2`: nivel ("0"-"100", "raise", "lower", "mute", "max").
     - `OPEN_CAMERA`: Apertura directa de la cámara nativa en modo Foto, Selfie o Grabación de Vídeo. `param1`: modo ("photo", "selfie", "video"), `param2`: lente ("back", "front", "video").
     - `SPEAK_TEXT`: Síntesis de voz con soporte para tags dinámicos y lectura de notificaciones previas (`{ULTIMA_NOTIFICACION}`). `param1`: texto a hablar, `param2`: idioma o "read_notification".
     - `TOGGLE_FLASHLIGHT`: Control de linterna. `param1`: "toggle" / "on" / "off".
     - `VIBRATE`: Vibración háptica avanzada. `param1`: duración en ms / "sos", `param2`: patrón ("click", "heavy", "double", "triple", "heartbeat", "sos", "custom").
     - `SHOW_NOTIFICATION`: Notificación local con tags dinámicos. `param1`: título, `param2`: mensaje.
     - `COPY_CLIPBOARD`: Copiar al portapapeles con tags dinámicos. `param1`: texto.
     - `WAIT_DELAY`: Pausa en segundos. `param1`: duración en seg (ej: "2").
     - `OPEN_URL`: Abrir enlace web en navegador. `param1`: URL.
     - `SEARCH_WEB`: Búsqueda en Google. `param1`: término de búsqueda.
     - `SHARE_TEXT`: Compartir texto mediante Intent chooser con tags dinámicos. `param1`: contenido.
     - `SEND_WHATSAPP`: Enviar mensaje por WhatsApp con tags dinámicos. `param1`: teléfono, `param2`: mensaje.
     - `SEND_SMS`: Enviar mensaje por SMS vía Intent con tags dinámicos. `param1`: teléfono, `param2`: mensaje.
     - `SET_TIMER`: Iniciar temporizador de alarma. `param1`: segundos, `param2`: nombre.
     - `QUICK_CALCULATOR`: Evaluación de expresión o propina rápida. `param1`: base, `param2`: porcentaje.

4. **`AutomationEntity`** (`data/model/AutomationEntity.kt`):
   - Disparador de automatización. Campos: `id`, `title`, `triggerType`, `triggerValue`, `shortcutId`, `shortcutTitle`, `isEnabled`, `notifyWhenRun`.

5. **`ExecutionLogEntity`** (`data/model/ExecutionLogEntity.kt`):
   - Registro histórico de ejecuciones. Campos: `id`, `shortcutId`, `shortcutTitle`, `timestamp`, `durationMs`, `status`, `summary`.

---

## ⚙️ Arquitectura Modular del Motor de Ejecución

1. **`ActionExecutor`**: Orquestador principal que despacha cada paso a un `ActionHandler` específico según su `ActionType`. Soporta cancelación interactiva en tiempo real (`cancelExecution()`), deteniendo corrutinas y liberando recursos (`onCancelled()` en handlers como TTS y vibración).
2. **`VariableResolverHelper`**: Interpola variables del sistema en tiempo real (`{HORA}`, `{FECHA}`, `{DIA_SEMANA}`, `{BATERIA}`, `{ESTADO_BATERIA}`, `{PORTAPAPELES}`, `{DISPOSITIVO}`) para personalizar textos de voz, notificaciones y mensajería.
3. **`PowerTriggerReceiver`** (`engine/triggers/PowerTriggerReceiver.kt`): `BroadcastReceiver` nativo del sistema que escucha `ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`, `ACTION_BATTERY_LOW`, `ACTION_BATTERY_OKAY`, recarga al 100% y cambios de porcentaje exacto de batería (`ACTION_BATTERY_CHANGED`). Al dispararse, consulta `ShortcutDao` y ejecuta de forma desatendida y asíncrona (`goAsync()`) en `Dispatchers.IO` todos los atajos asociados sin bloquear el hilo principal.
4. **`TimeSchedulerHelper` & `TimeTriggerReceiver`** (`engine/triggers/`): Motor de programación horaria exacta con `AlarmManager` (`setExactAndAllowWhileIdle`). Reprograma automáticamente las alarmas tras el reinicio del dispositivo (`BootReceiver`) y tras guardar, modificar o eliminar atajos/automatizaciones.
5. **`AppShortcutsHelper`**: Sincroniza dinámicamente los atajos favoritos con el launcher de Android mediante `ShortcutManagerCompat` para ejecución directa desde el icono de la app.
4. **`ActionHandler`** (`engine/handlers/`):
   - `HttpRequestActionHandler`: Peticiones HTTP seguras en segundo plano (`Dispatchers.IO`), con resolución de variables en URL y Body, timeouts y caching de respuestas en `{RESPUESTA_WEB}` y `{HTTP_STATUS}`.
   - `AppLauncherActionHandler`: Lanza intents de apps instaladas de forma segura.
   - `BrightnessActionHandler`: Ajuste de brillo con verificación de permisos `WRITE_SETTINGS` y fallback contextual.
   - `VolumeActionHandler`: Ajuste seguro de volumen por canal (AudioManager) con modos subir, bajar, silenciar o porcentaje.
   - `TtsActionHandler`: Inicialización y síntesis con `TextToSpeech`, resolución de variables y lectura directa de notificaciones.
   - `FlashlightActionHandler`: Control de `CameraManager`.
   - `VibrationActionHandler`: Generación de patrones con `Vibrator` / `VibrationEffect` con fallbacks de API.
   - `NotificationActionHandler`: Publicación en `NotificationManager` con resolución de variables.
   - `SystemIntentsActionHandler`: Manejo de intents de navegación, mensajería y portapapeles con resolución de variables.
   - `UtilityActionHandler`: Pausas no bloqueantes con corrutinas y cálculos.

5. **Escaneo Asíncrono de Aplicaciones**:
   - `AppScannerHelper` consulta el `PackageManager` en `Dispatchers.IO` y filtra apps no ejecutables para evitar bloqueos del hilo principal.
   - `AppPickerBottomSheet` presenta buscador en tiempo real y selector con estados de carga.

6. **Automatización en CI/CD (GitHub Actions)**:
   - `build-apk.yml`: Compilación y empaquetado del APK de desarrollo.
   - `android-device-simulation.yml`: Simulación en emulador oficial de Android (KVM / API 34), inyección de eventos de hardware (energía, batería al 15%, 80%, 100%) y auditoría de excepciones fatales en runtime con caché de AVD y Gradle.
   - `sync-from-zip.yml`: Extracción y sincronización desde zip con activación automática ante push en `zip/`, y eliminación automática de archivos comprimidos y carpetas temporales.
   - `repo-size-report.yml`: Auditoría automática del peso del proyecto, desglose por directorios, extensiones y generación de `REPO_SIZE_REPORT.md`.

7. **Portal Web y Documentación Legal (`/website`)**:
   - Construido con **Astro v4 + Tailwind CSS** configurado para compilación estática (`output: 'static'`) en Cloudflare Pages (`https://atajos.pages.dev`).
   - Contiene la Landing Page (`/`), Términos y Condiciones de Uso (`/legal/terminos`), Política de Privacidad (`/legal/privacidad`) y Guía de Documentación (`/docs`).

---

## 🚫 Restricciones Críticas
- **Distribución Independiente**: No asumir la presencia de Google Play Services ni librerías propietarias cerradas.
- **Manejo Seguro de Excepciones**: Toda interacción con hardware o paquetes del sistema debe estar protegida con `try-catch`.
- **Compatibilidad Offline**: Las acciones principales deben operar sin requerir conexión a internet obligatoria.
