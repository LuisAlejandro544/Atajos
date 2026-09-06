# Estructura del Proyecto (Architecture & Code Map)

Este documento detalla la jerarquía de directorios, responsabilidades de cada capa y el flujo de datos de la aplicación.

---

## 🏛️ Patrón Arquitectónico

La aplicación sigue los principios de **MVVM (Model-View-ViewModel)** y **Clean Architecture** modularizada, combinando código moderno en Kotlin con un motor de scripting en C nativo compilado mediante Android NDK:

```
[ UI (Jetpack Compose) ]
   ├── ShortcutScreen (Grid, Filtros, TopBar, Menú de herramientas)
   ├── components/
   │     ├── ShortcutCard (Efectos hápticos, Glassmorphism, Insignias)
   │     ├── ExecutionBanner (Banner flotante con indicador de progreso)
   │     ├── ActionBlockCard (Tarjeta de bloque individual con reordenación)
   │     ├── ShortcutEditSheet (Modal principal orquestador de edición)
   │     ├── dialogs/
   │     │     ├── AppPickerDialog (Selector asíncrono de apps)
   │     │     └── UserPromptDialog (Modal de interacción con el usuario y validación de palabras clave)
   │     └── editors/ (Editores especializados: Volumen, Brillo, Notificación, TTS, Lua, Interacción)
   ├── ShortcutViewModel (Gestión de estado reactivo StateFlow, prompts y orquestación)
   └── debug/
         ├── DebugActivity (App complementaria de Telemetría e Historial en Launcher)
         └── telemetry/ (TelemetryManager y modelos de rendimiento por paso)
              │ ▲
              ▼ │ Coroutines / Dispatchers.IO
[ Repositorios & Datos ]
   ├── InstalledAppsRepository (Consulta asíncrona de aplicaciones instaladas)
   ├── ShortcutRepository (Abstracción reactiva Flow sobre Room)
   ├── DefaultShortcuts (Catálogo curado de atajos predeterminados)
   └── Room Database (AppDatabase, ShortcutDao, TypeConverters)
              │ ▲
              ▼ │ Intent Dispatch / Execution
[ Dominio / Ejecución ]
   ├── ShortcutExecutor (Coordinador de secuencias, cadencia fija de 1003 ms)
   │     └── handlers/ (Manejadores modulares desacoplados)
   │           ├── DeviceActionHandler (Linterna, brillo y retroalimentación háptica)
   │           ├── AudioActionHandler (Volumen multimedia y ajustes de audio)
   │           ├── NavigationActionHandler (Apertura de apps, URLs web y mapas)
   │           ├── CommunicationActionHandler (Portapapeles, mensajes, compartir, temporizador)
   │           └── TtsManager (Gestión de TextToSpeech nativo con cola segura)
   ├── NotificationHelper (Canales de notificación duales con bypass DND y sonido Pop)
   ├── VariableResolver (Resolución dinámica de {hora}, {dia}, {bateria}, {portapapeles})
   └── LuaShortcutEngine (Kotlin JNI Interface)
              │ ▲
              ▼ │ JNI Callbacks (flashlight, copy, map, speak, timer, message, etc.)
        [ libnative-lua.so ] (Lua 5.4.7 C Native Engine con traceback)
```

---

## 📁 Árbol Detallado de Archivos

```
/
├── .github/
│   └── workflows/
│       ├── build_debug.yml                   # Compilación manual de APK Debug (workflow_dispatch)
│       ├── emulate_android10_32bit.yml       # Emulación de Android 10 (32-bit x86) y pruebas automatizadas (manual)
│       └── override_commit_message.yml       # Sincronización automática de mensaje desde commit_message.txt
├── audio_assets/
│   ├── LICENSE.txt                           # Atribución y licencia Creative Commons 0 (CC0)
│   └── raw/
│       └── 242502__gabrielaraujo__pop-upnotification.wav # Audio original (fuente WAV de alta fidelidad)
├── scripts/
│   ├── apk/
│   │   └── build_apk_debug.sh                # Compilación limpia del APK Debug sin caché
│   ├── audio/
│   │   └── convert_audio.sh                  # Conversión universal de audio a OGG sin delay (Vorbis Q7)
│   ├── build/
│   │   ├── generate_keystore.sh              # Generación y verificación del keystore de depuración
│   │   └── setup_cmake.sh                    # Configuración de CMake 3.22.1 y NDK 27
│   ├── debug/
│   │   ├── setup_anr_watchdog.sh             # Descarga y configuración de ANR-WatchDog 1.4.0 (UI Thread)
│   │   ├── setup_chucker.sh                  # Descarga y configuración de Chucker 4.1.0 para el APK Debug
│   │   ├── setup_dbinspector.sh              # Descarga y configuración de Infinum DbInspector 6.0.0 (.db Room)
│   │   ├── setup_hyperion.sh                 # Descarga y configuración de Hyperion-Android 0.9.38
│   │   └── setup_leakcanary.sh               # Descarga y configuración de LeakCanary 2.14 para el APK Debug
│   ├── emulator/
│   │   ├── setup_emulator_32bit.sh           # Descarga de imagen de sistema x86 y creación de AVD Android 10 (32-bit)
│   │   ├── start_emulator.sh                 # Arranque con KVM y sincronización de arranque con sys.boot_completed
│   │   └── test_32bit.sh                     # Instalación de APK, pruebas de heap RAM, librerías 32-bit y screenshot
│   └── lua/
│       ├── setup_lua.sh                      # Descarga y extracción de fuentes oficiales de Lua 5.4.7
│       └── setup_lua_debug.sh                # Verificación de Lua Debug Library e integración con native-lua.cpp
├── commit_message.txt                        # Mensaje de commit actual descriptivo en español
├── .env.example                               # Variables de entorno seguras
├── app/
│   ├── build.gradle.kts                      # NDK 27, CMake 3.22.1, abiFilters y dependencias de depuración
│   ├── proguard-rules.pro                    # Reglas Proguard/R8
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Declaración de permisos de hardware (Flash, Vibración, Audio)
│       │   ├── assets/
│       │   │   └── espeakdata.zip            # Datos fonéticos y diccionarios para síntesis eSpeak
│       │   ├── jniLibs/                      # Binarios nativos precompilados de TTS eSpeak
│       │   │   ├── arm64-v8a/libttsespeak.so
│       │   │   ├── armeabi-v7a/libttsespeak.so
│       │   │   ├── x86/libttsespeak.so
│       │   │   └── x86_64/libttsespeak.so
│       │   ├── cpp/                          # Capa Nativa C / C++
│       │   │   ├── CMakeLists.txt            # Compilación de Lua y del puente JNI nativo
│       │   │   ├── native-lua.cpp            # Bindings JNI entre Kotlin y Lua 5.4.7 con traceback
│       │   │   └── lua/                      # Código fuente oficial en C de Lua 5.4.7 (lapi, ldo, lvm, etc.)
│       │   ├── res/
│       │   │   ├── raw/
│       │   │   │   └── pop_notification.ogg  # Sonido Pop Vorbis Q7 (sin delay) para notificaciones
│       │   │   └── values/strings.xml
│       │   ├── java/
│       │   │   ├── com/reecedunn/espeak/
│       │   │   │   └── SpeechSynthesis.java  # Capa de enlace con motor de síntesis de voz alternativo eSpeak
│       │   │   └── com/example/
│       │   │       ├── ShortcutsApp.kt       # Application class con inicialización de utilidades
│       │   │       ├── MainActivity.kt       # Host Activity con enableEdgeToEdge
│       │   │       ├── util/
│       │   │       │   └── ImageCompressor.kt # Utilidad de compresión y procesamiento de imágenes
│       │   │       ├── espeak/
│       │   │       │   └── EspeakManager.kt  # Gestor de desempaquetado de assets y ciclo de voz eSpeak
│       │   │       ├── data/
│       │   │       │   ├── DefaultShortcuts.kt # Catálogo desacoplado de atajos predeterminados del sistema
│       │   │       │   ├── db/
│       │   │       │   │   ├── AppDatabase.kt    # Base de datos Room v3 con TypeConverters
│       │   │       │   │   └── ShortcutDao.kt    # Consultas SQL reactivas con Flow
│       │   │       │   ├── model/
│       │   │       │   │   ├── ActionBlock.kt    # Modelo atómico de un paso de acción
│       │   │       │   │   ├── ActionBlockConverter.kt # Serializador JSON Room para listas de bloques
│       │   │       │   │   ├── InstalledAppItem.kt # Modelo ligero de app instalada para el selector
│       │   │       │   │   ├── ShortcutEntity.kt # Entidad de atajo con ActionType enum
│       │   │       │   │   └── UserInteractionConfig.kt # Modelo de configuración para el bloque de interacción
│       │   │       │   └── repository/
│       │   │       │       ├── ShortcutRepository.kt # Acceso a datos Room desacoplado
│       │   │       │       └── InstalledAppsRepository.kt # Consulta en segundo plano (IO) de apps instaladas
│       │   │       ├── debug/
│       │   │       │   ├── DebugActivity.kt      # Actividad de telemetría y diagnóstico con launcher propio
│       │   │       │   └── telemetry/
│       │   │       │       ├── TelemetryModels.kt # Entidades de registro de rendimiento por paso
│       │   │       │       └── TelemetryManager.kt # Gestor persistente del historial y estadísticas
│       │   │       ├── executor/
│       │   │       │   ├── ShortcutExecutor.kt   # Orquestador secuencial con cadencia fija de 1003 ms
│       │   │       │   ├── NotificationHelper.kt # Gestor de notificaciones nativas con canales DND
│       │   │       │   ├── VariableResolver.kt   # Motor de variables dinámicas ({hora}, {bateria}, etc.)
│       │   │       │   ├── UserInteractionBridge.kt # Puente asíncrono no bloqueante para pausas de confirmación
│       │   │       │   ├── UserInteractionReceiver.kt # BroadcastReceiver de respuestas desde la barra de notificaciones
│       │   │       │   ├── LuaShortcutEngine.kt  # Enlace Kotlin JNI con libnative-lua.so
│       │   │       │   └── handlers/
│       │   │       │       ├── DeviceActionHandler.kt # Linterna, control de brillo y hápticos
│       │   │       │       ├── AudioActionHandler.kt  # Volumen multimedia y ajustes de sonido
│       │   │       │       ├── NavigationActionHandler.kt # Lanzamiento de apps, URLs web y Maps
│       │   │       │       ├── CommunicationActionHandler.kt # Portapapeles, mensajes, compartir y timer
│       │   │       │       ├── TtsManager.kt      # Encapsulación de TextToSpeech nativo y cola segura
│       │   │       │       └── UserInteractionNotificationHelper.kt # Notificaciones interactivas de confirmación
│       │   │       └── ui/
│       │   │           ├── ShortcutScreen.kt     # Pantalla principal (TopBar, categorías, Grid de tarjetas)
│       │   │           ├── ShortcutViewModel.kt  # ViewModel modularizado (estado UI, filtros y ejecución)
│       │   │           ├── components/
│       │   │           │   ├── ActionBlockCard.kt    # Componente de tarjeta de paso individual
│       │   │           │   ├── ExecutionBanner.kt    # Banner flotante superior con progreso
│       │   │           │   ├── IconHelper.kt         # Catálogo de iconos vectoriales y paleta de colores
│       │   │           │   ├── ShortcutCard.kt       # Tarjeta de atajo con glassmorphism y haptics
│       │   │           │   ├── ShortcutEditSheet.kt  # BottomSheet orquestador de creación y edición
│       │   │           │   ├── dialogs/
│       │   │           │   │   ├── AppPickerDialog.kt # Diálogo de selección de apps con buscador
│       │   │           │   │   └── UserPromptDialog.kt # Modal interactivo con validación de palabra clave
│       │   │           │   └── editors/
│       │   │           │       ├── VolumeBlockEditor.kt # Editor de volumen con presets
│       │   │           │       ├── BrightnessBlockEditor.kt # Editor táctil de brillo
│       │   │           │       ├── NotificationBlockEditor.kt # Editor de sonido y variables de notificación
│       │   │           │       ├── SpeakBlockEditor.kt # Editor de texto TTS con inserción de variables
│       │   │           │       ├── LuaScriptBlockEditor.kt # Editor monoespaciado para código Lua
│       │   │           │       └── UserInteractionBlockEditor.kt # Editor del bloque de interacción (Notificación / Modal)
│       │   │           └── theme/
│       │   │               ├── Color.kt          # Paleta base
│       │   │               ├── Theme.kt          # Material 3 Dynamic Theme
│       │   │               └── Type.kt           # Tipografía
│       └── test/
│           ├── screenshots/
│           │   └── greeting.png                      # Captura de referencia para prueba Roborazzi
│           └── java/com/example/
│               ├── ExampleRobolectricTest.kt         # Tests instrumentados locales en JVM con Robolectric
│               ├── ExampleUnitTest.kt                # Pruebas unitarias estándar JUnit
│               ├── GreetingScreenshotTest.kt         # Verificación visual con Roborazzi
│               └── ShortcutLogicTest.kt              # Tests unitarios para bloques, tipos y cadencia de 1003 ms
├── metadata.json                             # Metadatos para AI Studio
├── README.md                                 # Descripción y guía de compilación
├── ROADMAP.md                                # Hitos de desarrollo
├── STRUCTURE.md                              # Este archivo
├── AI_CONTEXT.md                             # Contexto y restricciones del sistema para IA
└── AGENTS.md                                 # Guía de comportamiento y directivas para agentes
```

---

## ⚡ Flujo de Ejecución de un Atajo Multi-Bloque

1. **Interacción de Usuario**: El usuario pulsa una tarjeta de atajo en `ShortcutScreen.kt`. La tarjeta permanece en su posición fija en la lista (orden `isFavorite DESC, id ASC`).
2. **ViewModel**: Se activa `viewModelScope.launch` en `ShortcutViewModel.executeShortcut(shortcut)` y se registra el conteo estadístico sin alterar el orden visual.
3. **Resolución de Bloques**: Se obtienen las acciones definidas (`ActionBlock`).
4. **Ciclo Secuencial**:
   - Se actualiza el banner flotante con el paso actual: *"Paso X de Y: [Nombre]"*.
   - Si el bloque es de tipo `WAIT`, se ejecuta una pausa personalizada de `N` milisegundos (`delay(waitMs)`), reemplazando el valor estándar.
   - Si el bloque es `LUA_SCRIPT`, entra en acción `LuaShortcutEngine.executeScript(code)` llamando a `libnative-lua.so`.
   - El motor nativo C de Lua 5.4.7 procesa el script y, ante invocaciones como `flashlight()`, `speak()` o `copy()`, emite callbacks JNI hacia la instancia de Kotlin.
   - En bloques estándar de hardware o voz (`SPEAK` / TextToSpeech nativo), se despachan a través de `ShortcutExecutor.executeSingleBlock(type, param)` delegando a los handlers especializados (`DeviceActionHandler`, `AudioActionHandler`, `NavigationActionHandler`, `CommunicationActionHandler` y `TtsManager`), y se activa vibración háptica.
   - Si quedan bloques pendientes y no se trata de una pausa explícita, se aplica la cadencia por defecto de `delay(1003L)` (`ShortcutExecutor.STEP_DELAY_MS`).
5. **Finalización**: Se notifica en el banner el resultado final y se programa el auto-ocultamiento a los 3.5 segundos.

---

## 🛠️ Herramientas de Depuración Móvil Embebidas

1. **LeakCanary 2.14**:
   - Activo automáticamente en la variante Debug.
   - Crea una aplicación e icono complementario denominado **"Leaks"** en el launcher del dispositivo móvil para auditar retenciones indebidas de Activities y memoria en tiempo real sin requerir PC ni ADB.
2. **Chucker 4.1.0**:
   - Inspector de red y tráfico HTTP con interfaz propia.
   - Muestra notificaciones directas en Android al detectar tráfico y cuenta con acceso directo desde el menú de opciones superior de la app (`Abrir Inspector Chucker (Red)`).
3. **Lua Debug Library 5.4.7**:
   - Módulos C oficiales `ldebug.c` y `ldblib.c` compilados nativamente en `libnative-lua.so`.
   - Utiliza un manejador de errores con `luaL_traceback` en `native-lua.cpp` que captura el archivo y número de línea exactos de cualquier fallo en los scripts Lua.

---

## 📱 Entorno de Emulación y Verificación de 32 Bits (Android 10 x86)

El repositorio incluye un flujo automatizado en GitHub Actions (`emulate_android10_32bit.yml`) activable bajo demanda (`workflow_dispatch`) para someter el APK Debug a pruebas en un entorno estricto de 32 bits:
1. **Configuración de AVD**: Descarga de la imagen oficial del SDK `system-images;android-29;google_apis;x86` y aprovisionamiento con aceleración por hardware KVM.
2. **Arranque y Detección**: Comprobación del estado `sys.boot_completed` y verificación de `ro.product.cpu.abi` confirmando arquitectura nativa `x86` de 32 bits.
3. **Instalación y Verificación de Heap**: Instalación del APK Debug, arranque de `MainActivity` y `DebugActivity`, volcado de consumo de memoria RAM (`dumpsys meminfo`), inspección del logcat para descartar `UnsatisfiedLinkError` o fallos de JNI, y exportación de captura de pantalla como artefacto.

