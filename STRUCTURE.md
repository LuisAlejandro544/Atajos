# Estructura del Proyecto (Architecture & Code Map)

Este documento detalla la jerarquía de directorios, responsabilidades de cada capa y el flujo de datos de la aplicación.

---

## 🏛️ Patrón Arquitectónico

La aplicación sigue los principios de **MVVM (Model-View-ViewModel)** y **Clean Architecture** modularizada, combinando código moderno en Kotlin con un motor de scripting en C nativo compilado mediante Android NDK:

```
[ UI (Jetpack Compose) ]
   ├── ShortcutScreen (Grid de 2 columnas, Filtros, TopBar, Menú de depuración)
   ├── components/
   │     ├── ShortcutCard (Fondo con foto o gradiente, cápsula unificada de cristal, insignias)
   │     ├── ExecutionBanner (Banner flotante con indicador de progreso de ejecución)
   │     ├── ActionBlockCard (Tarjeta de bloque de acción con reordenación y picker modal)
   │     ├── ShortcutEditSheet (Modal principal: selector de foto, disparadores, bloques)
   │     ├── dialogs/
   │     │     ├── ActionTypePickerDialog (Modal BottomSheet estilizado para elegir tipo de acción)
   │     │     ├── AppPickerDialog (Selector asíncrono de aplicaciones instaladas)
   │     │     └── UserPromptDialog (Modal de confirmación interactiva con el usuario)
   │     └── editors/ (Editores especializados: Volumen, Brillo, Notificación, TTS, Lua, Interacción)
   ├── ShortcutViewModel (Gestión de estado reactivo StateFlow, ejecución y persistencia)
   └── debug/
         ├── DebugActivity (Herramienta complementaria de Telemetría e Historial)
         └── telemetry/ (TelemetryManager y métricas de rendimiento por paso)
              │ ▲
              ▼ │ Coroutines / Dispatchers.IO
[ Repositorios & Datos ]
   ├── InstalledAppsRepository (Consulta asíncrona de aplicaciones del lanzador)
   ├── ShortcutRepository (Abstracción reactiva Flow sobre Room Database)
   ├── DefaultShortcuts (Catálogo curado de atajos predeterminados del sistema)
   └── Room Database (AppDatabase v6 con MIGRATION_5_6, ShortcutDao, TypeConverters)
              │ ▲
              ▼ │ Intent Dispatch / Execution
[ Dominio / Ejecución & Triggers ]
   ├── trigger/
   │     └── PowerTriggerReceiver (BroadcastReceiver para conexión y desconexión de cargador)
   ├── ShortcutExecutor (Coordinador de secuencias de acción, cadencia de 1003 ms)
   │     └── handlers/ (Manejadores modulares desacoplados)
   │           ├── DeviceActionHandler (Linterna, brillo de pantalla y retroalimentación háptica)
   │           ├── AudioActionHandler (Control de volumen multimedia y ajustes de audio)
   │           ├── NavigationActionHandler (Apertura de apps instaladas, enlaces web y mapas)
   │           ├── CommunicationActionHandler (Portapapeles, mensajes SMS/WhatsApp, compartir, temporizador)
   │           └── TtsManager (Síntesis de voz nativa del sistema Android con cola segura)
   ├── NotificationHelper (Notificaciones de canal dual con bypass DND y sonido Pop Vorbis Q7)
   ├── VariableResolver (Variables dinámicas: {hora}, {dia}, {bateria}, {portapapeles})
   └── LuaShortcutEngine (Interfaz JNI con libnative-lua.so)
              │ ▲
              ▼ │ JNI Callbacks (flashlight, copy, map, speak, timer, message, etc.)
        [ libnative-lua.so ] (Lua 5.4.7 C Native Engine con traceback y ldebug)
```

---

## 📁 Árbol Detallado de Archivos

```
/
├── .github/
│   └── workflows/
│       ├── build_debug.yml                   # Compilación manual de APK Debug (workflow_dispatch)
│       ├── emulate_android10_32bit.yml       # Emulación de Android 10 (32-bit x86) y streaming interactivo noVNC
│       └── override_commit_message.yml       # Sincronización automática de mensaje desde commit_message.txt
├── audio_assets/
│   ├── LICENSE.txt                           # Atribución y licencia Creative Commons 0 (CC0)
│   └── raw/
│       └── 242502__gabrielaraujo__pop-upnotification.wav # Audio fuente WAV sin delay
├── scripts/
│   ├── apk/
│   │   └── build_apk_debug.sh                # Compilación limpia del APK Debug sin dependencias de caché
│   ├── audio/
│   │   └── convert_audio.sh                  # Conversión de audio a OGG Vorbis Q7 sin retardo
│   ├── build/
│   │   ├── generate_keystore.sh              # Generación y verificación del keystore de depuración
│   │   └── setup_cmake.sh                    # Configuración de CMake 3.22.1 y NDK 27
│   ├── debug/
│   │   ├── setup_anr_watchdog.sh             # Descarga y configuración de ANR-WatchDog 1.4.0
│   │   ├── setup_chucker.sh                  # Descarga y configuración de Chucker 4.1.0 para APK Debug
│   │   ├── setup_dbinspector.sh              # Descarga y configuración de Infinum DbInspector 6.0.0
│   │   ├── setup_hyperion.sh                 # Descarga y configuración de Hyperion-Android 0.9.38
│   │   └── setup_leakcanary.sh               # Descarga y configuración de LeakCanary 2.14
│   ├── emulator/
│   │   ├── setup_emulator_32bit.sh           # Descarga de imagen x86 y creación de AVD Android 10 (32-bit)
│   │   ├── setup_novnc.sh                    # Configuración de servidor Xvfb, fluxbox y cliente HTML5 noVNC
│   │   ├── start_emulator.sh                 # Arranque con KVM y sincronización con sys.boot_completed
│   │   ├── start_web_tunnel.sh               # Servidor x11vnc y túnel HTTPS Cloudflare para control móvil
│   │   └── test_32bit.sh                     # Instalación de APK, pruebas de heap 32-bit y captura de pantalla
│   └── lua/
│       ├── setup_lua.sh                      # Descarga y extracción de código fuente oficial de Lua 5.4.7
│       └── setup_lua_debug.sh                # Integración de Lua Debug Library en native-lua.cpp
├── commit_message.txt                        # Mensaje de commit actual en español
├── .env.example                               # Variables de entorno seguras
├── app/
│   ├── build.gradle.kts                      # NDK 27, CMake 3.22.1, abiFilters, Coil y librerías de depuración
│   ├── proguard-rules.pro                    # Reglas Proguard/R8
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Permisos de hardware y registro de PowerTriggerReceiver
│       │   ├── cpp/                          # Capa Nativa C / C++
│       │   │   ├── CMakeLists.txt            # Compilación de Lua y puente JNI nativo
│       │   │   ├── native-lua.cpp            # Bindings JNI entre Kotlin y Lua 5.4.7 con traceback
│       │   │   └── lua/                      # Código fuente en C de Lua 5.4.7 (lapi, ldo, lvm, ldebug, etc.)
│       │   ├── res/
│       │   │   ├── raw/
│       │   │   │   └── pop_notification.ogg  # Sonido Pop Vorbis Q7 (cero delay) para notificaciones
│       │   │   └── values/strings.xml
│       │   └── java/com/example/
│       │       ├── ShortcutsApp.kt           # Application class con registro de PowerTriggerReceiver
│       │       ├── MainActivity.kt           # Host Activity con enableEdgeToEdge y Compose
│       │       ├── trigger/
│       │       │   └── PowerTriggerReceiver.kt # Detección de conexión y desconexión de cargador
│       │       ├── util/
│       │       │   └── ImageCompressor.kt     # Utilidad de compresión y procesamiento de imágenes
│       │       ├── data/
│       │       │   ├── DefaultShortcuts.kt   # Atajos predeterminados del sistema
│       │       │   ├── db/
│       │       │   │   ├── AppDatabase.kt    # Base de datos Room v6 con migración MIGRATION_5_6
│       │       │   │   └── ShortcutDao.kt    # Consultas SQL reactivas Flow y filtrado por disparador
│       │       │   ├── model/
│       │       │   │   ├── ActionBlock.kt    # Modelo atómico de un paso de acción
│       │       │   │   ├── ActionBlockConverter.kt # Serializador JSON Room para lista de bloques
│       │       │   │   ├── InstalledAppItem.kt # Modelo ligero de app para el selector
│       │       │   │   ├── ShortcutEntity.kt # Entidad de atajo (triggerType, backgroundImageUri)
│       │       │   │   └── UserInteractionConfig.kt # Configuración para bloque de interacción
│       │       │   └── repository/
│       │       │       ├── ShortcutRepository.kt # Repositorio de atajos con Flow
│       │       │       └── InstalledAppsRepository.kt # Consulta en Dispatchers.IO de apps del sistema
│       │       ├── debug/
│       │       │   ├── DebugActivity.kt      # Actividad de telemetría y diagnóstico
│       │       │   └── telemetry/
│       │       │       ├── TelemetryModels.kt # Modelos de telemetría y métricas de pasos
│       │       │       └── TelemetryManager.kt # Historial de ejecuciones y duración en disco
│       │       ├── executor/
│       │       │   ├── ShortcutExecutor.kt   # Orquestador secuencial con cadencia de 1003 ms
│       │       │   ├── NotificationHelper.kt # Gestor de notificaciones con canal dual y audio
│       │       │   ├── VariableResolver.kt   # Motor de resolución dinámica ({hora}, {bateria}, etc.)
│       │       │   ├── UserInteractionBridge.kt # Puente asíncrono para pausas de confirmación
│       │       │   ├── UserInteractionReceiver.kt # BroadcastReceiver de respuestas desde notificación
│       │       │   ├── LuaShortcutEngine.kt  # Enlace Kotlin JNI con libnative-lua.so
│       │       │   └── handlers/
│       │       │       ├── DeviceActionHandler.kt # Linterna, control de brillo y hápticos
│       │       │       ├── AudioActionHandler.kt  # Volumen multimedia y sonido
│       │       │       ├── NavigationActionHandler.kt # Lanzamiento de apps, URLs y Maps
│       │       │       ├── CommunicationActionHandler.kt # Portapapeles, mensajes, compartir y timer
│       │       │       ├── TtsManager.kt      # TextToSpeech nativo del sistema Android
│       │       │       └── UserInteractionNotificationHelper.kt # Notificaciones interactivas de confirmación
│       │       └── ui/
│       │           ├── ShortcutScreen.kt     # Pantalla principal (Categorías, buscador, Grid espaciado)
│       │           ├── ShortcutViewModel.kt  # ViewModel (estado UI, filtros, persistencia y ejecución)
│       │           ├── components/
│       │           │   ├── ActionBlockCard.kt    # Tarjeta de bloque con ActionTypePickerDialog
│       │           │   ├── ExecutionBanner.kt    # Banner flotante superior con progreso
│       │           │   ├── IconHelper.kt         # Catálogo de iconos vectoriales y paleta de colores
│       │           │   ├── ShortcutCard.kt       # Tarjeta con soporte de foto, cápsula unificada e insignias
│       │           │   ├── ShortcutEditSheet.kt  # BottomSheet: selector de foto, disparador automático y bloques
│       │           │   ├── dialogs/
│       │           │   │   ├── ActionTypePickerDialog.kt # Selector modal de acciones con iconos y descripciones
│       │           │   │   ├── AppPickerDialog.kt # Diálogo de selección de apps con búsqueda en tiempo real
│       │           │   │   └── UserPromptDialog.kt # Modal interactivo de confirmación y palabra clave
│       │           │   └── editors/
│       │           │       ├── VolumeBlockEditor.kt # Selector y deslizador de volumen
│       │           │       ├── BrightnessBlockEditor.kt # Control de brillo
│       │           │       ├── NotificationBlockEditor.kt # Editor de variables y sonido de notificación
│       │           │       ├── SpeakBlockEditor.kt # Editor de texto TTS con variables
│       │           │       ├── LuaScriptBlockEditor.kt # Editor de código Lua monoespaciado
│       │           │       └── UserInteractionBlockEditor.kt # Editor de bloque interactivo
│       │           └── theme/
│       │               ├── Color.kt          # Paleta base Material 3
│       │               ├── Theme.kt          # Material 3 Dynamic Theme
│       │               └── Type.kt           # Tipografía
│       └── test/
│           ├── screenshots/
│           │   └── greeting.png
│           └── java/com/example/
│               ├── ExampleRobolectricTest.kt # Tests JVM con Robolectric
│               ├── ExampleUnitTest.kt        # Pruebas unitarias JUnit
│               ├── GreetingScreenshotTest.kt # Verificación visual Roborazzi
│               └── ShortcutLogicTest.kt      # Tests de lógica de atajos y cadencia de 1003 ms
├── metadata.json                             # Metadatos para la plataforma AI Studio
├── README.md                                 # Descripción general y guía
├── ROADMAP.md                                # Hitos de desarrollo
├── STRUCTURE.md                              # Este archivo
├── AI_CONTEXT.md                             # Contexto y directivas de desarrollo
└── AGENTS.md                                 # Reglas persistentes para agentes de IA
```

---

## ⚡ Flujo de Ejecución de Atajos

### 1. Ejecución Manual (Desde la UI)
1. **Interacción de Usuario**: El usuario pulsa una tarjeta de atajo en `ShortcutScreen.kt`. La tarjeta mantiene su orden visual estable (`isFavorite DESC, id ASC`).
2. **ViewModel**: Se activa `viewModelScope.launch` en `ShortcutViewModel.executeShortcut(shortcut)`.
3. **Resolución de Bloques**: Se procesan las acciones secuenciales (`ActionBlock`).
4. **Ciclo Secuencial**:
   - Se actualiza el banner flotante con el paso actual: *"Paso X de Y: [Nombre]"*.
   - Si el bloque es de tipo `WAIT`, se aplica la pausa personalizada configurada (`delay(waitMs)`).
   - Si el bloque es `LUA_SCRIPT`, `LuaShortcutEngine.executeScript(code)` ejecuta el código nativo en `libnative-lua.so`.
   - En bloques estándar de hardware o voz (`SPEAK`), se despachan a través de `ShortcutExecutor.executeSingleBlock(type, param)` delegando a los handlers especializados (`DeviceActionHandler`, `AudioActionHandler`, `NavigationActionHandler`, `CommunicationActionHandler` y `TtsManager` con el TextToSpeech nativo de Android).
   - Entre bloques normales, se mantiene la cadencia fija de `delay(1003L)` (`ShortcutExecutor.STEP_DELAY_MS`).
5. **Finalización**: Se emite la telemetría a `TelemetryManager` y se muestra el banner de confirmación.

### 2. Ejecución Automática por Disparadores (Triggers)
1. **Detección del Sistema**: `PowerTriggerReceiver` recibe `Intent.ACTION_POWER_CONNECTED` o `Intent.ACTION_POWER_DISCONNECTED`.
2. **Consulta a Base de Datos**: Consulta asíncrona en `Dispatchers.IO` a Room a través de `ShortcutDao.getShortcutsByTrigger(triggerType)`.
3. **Ejecución en Segundo Plano**: Cada atajo coincidente se ejecuta secuencialmente a través de `ShortcutExecutor.executeSingleBlock()`.
4. **Notificación**: Se notifica al usuario con sonido Pop Vorbis de baja latencia mediante `NotificationHelper.notifyShortcutExecution()`.
