# 🧠 AI Context & Development Knowledge Base

Este documento proporciona contexto técnico estructurado y directo para modelos de lenguaje (LLMs) y agentes de codificación que trabajen en este repositorio.

---

## 📌 Resumen del Proyecto

- **Nombre**: Atajos (Shortcuts) para Android
- **Propósito**: Automatización local y secuencial de tareas en el dispositivo Android mediante disparadores y acciones encadenadas.
- **Enfoque de Distribución**: Sin ataduras a Google Play. Diseñado para funcionar de manera independiente, compatible con distribución directa (APK sideloading) o tiendas de terceros (Uptodown, F-Droid).
- **Paradigma**: 100% Kotlin nativo con Jetpack Compose y Room SQLite.

---

## 🔑 Entidades Clave del Modelo de Datos

1. **`ShortcutEntity`** (`data/model/ShortcutEntity.kt`):
   - Representa un atajo configurable.
   - Campos: `id`, `title`, `description`, `iconKey`, `colorHex`, `category`, `actionsJson` (lista serializada de `ShortcutAction`), `isFavorite`, `runCount`, `lastRunTimestamp`, `createdAt`.

2. **`ShortcutAction`** (`data/model/ShortcutAction.kt`):
   - Paso individual dentro de un atajo.
   - Campos: `id`, `type` (`ActionType`), `title`, `param1`, `param2`, `param3`, `orderIndex`.

3. **`ActionType`** (`data/model/ActionType.kt`):
   - Tipos soportados:
     - `LAUNCH_APP`: Iniciar juego o aplicación instalada. `param1`: `packageName`, `param2`: `appName`.
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

1. **`ActionExecutor`**: Orquestador principal que despacha cada paso a un `ActionHandler` específico según su `ActionType`.
2. **`VariableResolverHelper`**: Interpola variables del sistema en tiempo real (`{HORA}`, `{FECHA}`, `{DIA_SEMANA}`, `{BATERIA}`, `{ESTADO_BATERIA}`, `{PORTAPAPELES}`, `{DISPOSITIVO}`) para personalizar textos de voz, notificaciones y mensajería.
3. **`AppShortcutsHelper`**: Sincroniza dinámicamente los atajos favoritos con el launcher de Android mediante `ShortcutManagerCompat` para ejecución directa desde el icono de la app.
4. **`ActionHandler`** (`engine/handlers/`):
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
   - `sync-from-zip.yml`: Extracción y sincronización desde zip con activación automática ante push en `zip/`, y eliminación automática de archivos comprimidos y carpetas temporales.
   - `repo-size-report.yml`: Auditoría automática del peso del proyecto, desglose por directorios, extensiones y generación de `REPO_SIZE_REPORT.md`.

---

## 🚫 Restricciones Críticas
- **Distribución Independiente**: No asumir la presencia de Google Play Services ni librerías propietarias cerradas.
- **Manejo Seguro de Excepciones**: Toda interacción con hardware o paquetes del sistema debe estar protegida con `try-catch`.
- **Compatibilidad Offline**: Las acciones principales deben operar sin requerir conexión a internet obligatoria.
