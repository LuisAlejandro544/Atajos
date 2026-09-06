# Atajos Android (Shortcuts & Pure Lua 5.4.7 Automation)

Aplicación moderna para Android de automatización mediante atajos interactivos, inspirada en flujos de tareas rápidas y personalizadas, con soporte para secuencias de múltiples bloques y ejecución nativa en **Lua 5.4.7 puro** (código C oficial compilado con Android NDK y CMake, sin wrappers ni intérpretes en Java).

Diseñada para ser ligera, ultra-rápida y 100% funcional en dispositivos móviles independientes, orientada a distribución directa (APK para Uptodown y tiendas de terceros).

---

## 🚀 Características Principales

- **Atajos Multi-Bloque en Cadena y Bloque de Espera**:
  - Encadena múltiples acciones en un solo atajo (por ejemplo: activar linterna, esperar tiempo personalizado, ajustar volumen multimedia al 70%, abrir una aplicación específica y lanzar mapas).
  - Pausa exacta y calibrada por defecto de **1003 ms** entre bloques para garantizar que Android procese cada Intent sin colisiones ni descartes de eventos.
  - Bloque de acción **"Esperar (Pausa)" (`WAIT`)**: permite definir tiempos de espera personalizados en milisegundos para atajos que requieran pausas más largas o más cortas que el valor estándar.
  - Bloque de acción **"Abrir Aplicación" (`OPEN_APP`)**: selector interactivo con buscador en vivo que lista todas las aplicaciones instaladas en el dispositivo móvil para abrirlas al instante.
  - Bloque de acción **"Ajustar Volumen" (`SET_VOLUME`)**: control deslizante interactivo (*Slider 0-100%*) con indicador visual de decibelios y botones de acceso rápido (*Mute, 30%, 70%, 100%*) adaptados a dedos en pantalla táctil.
  - Bloque de acción **"Brillo de Pantalla" (`SET_BRIGHTNESS`)**: control táctil con deslizador manual (*Slider 0-100%*), porcentaje en vivo y botones de presets (*20%, 50%, 80%, 100%*) para adaptar la pantalla al instante según el entorno.
  - Bloque de acción **"Abrir Sitio Web" (`OPEN_URL`)**: lanzamiento fluido de direcciones URL en el navegador predeterminado del sistema.
  - Bloque de acción **"Mostrar Notificación" (`NOTIFICATION`)**: notificaciones nativas de alta prioridad configuradas con bypass del modo No Molestar (*Bypass DND*), asegurando que los avisos cruciales se muestren en cualquier circunstancia.
  - **Selector de Sonido de Notificación y Preescucha**: permite alternar entre el sonido original del dispositivo o el sonido *Pop Notification* (licencia CC0 de GabrielAraujo), con botón de prueba directa en el editor para escuchar la muestra antes de guardar.
  - **Pipeline Automatizado de Audio sin Delay (`scripts/convert_audio.sh`)**: script de optimización que convierte archivos de audio a formato Ogg Vorbis (`.ogg`) sin pérdida perceptible y con respuesta acústica instantánea (cero latencia) durante la compilación del APK.
  - **Motor de Variables Dinámicas en Tiempo Real**: tanto las notificaciones como los bloques de Texto a Voz admiten etiquetas evaluadas al momento de ejecución: `{hora}`, `{hora_segundos}`, `{fecha}`, `{dia}`, `{bateria}` y `{portapapeles}`. Incluye chips táctiles en el editor para insertarlas con un toque sin escribir llaves.
  - **Acceso Directo a Favoritos en Tarjeta (1-Click)**: botón de estrella interactivo sobre cada tarjeta para marcar o desmarcar favoritos inmediatamente sin necesidad de ingresar al menú de opciones.
  - **Bloque de Interacción con el Usuario ("Preguntar antes de continuar", `USER_INTERACTION`)**:
    - Permite condicionar o pausar la ejecución de cualquier atajo solicitando confirmación interactiva al usuario antes de proceder.
    - Soporta dos diseños ergonómicos:
      1. *Modal Emergente (`UserPromptDialog`)*: despliega una tarjeta con icono y paleta temática del atajo, mensaje personalizado, campo de entrada de palabra clave (ej. "Si", "No") o botones directos de confirmación/cancelación.
      2. *Notificación en Barra del Sistema (`UserInteractionNotificationHelper`)*: lanza una notificación interactiva con respuesta de texto directo o botones para validar la palabra clave antes de continuar.
  - **Orden de cuadrícula estable**: los atajos conservan su posición exacta en pantalla tras ser ejecutados, evitando saltos molestos hacia la parte superior.
- **Motor de Scripting Lua 5.4.7 Puro (Nativo en C)**:
  - Compilado nativamente para arquitecturas móviles (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) mediante Android NDK y CMake (`libnative-lua.so`).
  - Sin intérpretes lentos ni wrappers de terceros; ejecuta directamente el motor oficial de PUC-Rio.
  - Soporte total para características avanzadas de Lua 5.4:
    - Variables locales `<const>` y `<close>` (gestión determinista de recursos).
    - Recolector de basura generacional rápido.
    - Enteros nativos de 64 bits y operadores a nivel de bits (`&`, `|`, `~`, `>>`, `<<`).
    - Manipulación completa de cadenas, tablas y funciones matemáticas.
  - Funciones de Android integradas mediante bindings JNI:
    - `open_app(paquete)`: Abre cualquier app instalada en el dispositivo mediante su identificador de paquete.
    - `set_volume(porcentaje)`: Modifica directamente el volumen del flujo multimedia (0 a 100%).
    - `flashlight()` / `flashlight(bool)`: Controla la linterna física.
    - `copy(texto)`: Copia contenido al portapapeles.
    - `open_url(url)`: Abre enlaces en el navegador web.
    - `map(destino)`: Inicia navegación en mapas.
    - `timer(minutos)`: Configura temporizador en el reloj del sistema.
    - `message(texto)` / `message(tel, texto)`: Abre selector o envío de mensaje.
    - `sound_settings()`: Abre el panel de volumen y audio.
    - `share(texto)`: Comparte contenido mediante el menú del sistema.
    - `speak(texto)` / `speak(texto, motor, voz)`: Lee en voz alta un texto utilizando el motor especificado ("PIPER", "ESPEAK", "SYSTEM") o el predeterminado.
    - `set_tts_engine(motor)`: Cambia dinámicamente el motor de síntesis activo ("PIPER", "ESPEAK", "SYSTEM").
    - `get_hour()`: Retorna la hora actual del día (0-23).
    - `print(...)`: Registra y formatea mensajes capturados en el banner.
- **Síntesis de Voz Neuronal Avanzada Offline (Piper TTS - VITS)**:
  - Motor neuronal integrado localmente mediante **ONNX Runtime** (`onnxruntime-android`), permitiendo generación de voz con redes neuronales profundas (VITS) con calidad humana, 100% offline y privada (sin conexión ni servicios en la nube).
  - Incluye modelo en español empaquetado y listo para usar (`es_ES-carlfm-x_low` a 16 kHz) en los recursos internos de la app.
  - Catálogo descargable en segundo plano para voces de alta resolución a 22.05 kHz (`es_ES-davefx-medium` y `es_ES-sharvard-medium`) con barra de progreso interactiva y feedback visual.
  - Selector global de motor de voz (`TtsEngineSettingsDialog`) en el menú principal y en la app de telemetría:
    - **Piper TTS**: Síntesis neuronal VITS local de alta fidelidad.
    - **eSpeak-NG**: Motor acústico ultra ligero compilado en C++.
    - **Voz del Sistema**: Motor predeterminado de Android (Google TTS, Samsung TTS, etc.).
  - Configuración independiente por bloque de acción (`SpeakBlockEditor`): permite que cada bloque `SPEAK` elija su propio motor y voz o use el predeterminado, con preescucha directa y chips de variables dinámicas (`{hora}`, `{bateria}`, etc.).
  - Herramienta de banco de pruebas y medición de latencia en la pantalla de telemetría (`DebugActivity`).
  - Script automatizado de verificación y aprovisionamiento: `scripts/tts/setup_piper.sh`.
- **Síntesis de Voz Nativa del Sistema (Text-to-Speech)**:
  - Bloque visual `SPEAK` ("Texto a Voz") con fallback transparente al motor del sistema operativo Android en caso de ausencia o fallo de modelos neuronales.
- **Herramientas de Depuración Integradas (Mobile-First Debugging)**:
  - **App de Telemetría e Historial de Ejecuciones (`DebugActivity`)**: Aplicación complementaria autónoma con lanzador propio en el escritorio de Android (*"Telemetría Atajos"*) al estilo de LeakCanary. Registra el historial de ejecuciones, cronometraje milimétrico paso a paso, tasa global de éxito y verificación de la cadencia fija de 1003 ms, con accesos directos a Chucker y DbInspector.
  - **Infinum DbInspector (6.0.0)**: Interfaz gráfica autónoma en el dispositivo para inspeccionar y editar directamente los archivos de base de datos SQLite y Room (`.db`), ver tablas, columnas, registros y ejecutar consultas SQL en tiempo real.
  - **Hyperion-Android (0.9.38)**: Menú lateral de inspección de interfaz activable con gesto deslizante o agitación del teléfono, con plugins de medición dimensional en pantalla (*Measurement*) y capturador de fallos (*Crash*).
  - **ANR-WatchDog (1.4.0)**: Guardián de rendimiento del hilo principal (UI Thread) para detectar bloqueos antes de que congelen la app.
  - **LeakCanary (2.14)**: App complementaria *"Leaks"* generada en el teléfono para inspeccionar fugas de memoria sin necesidad de PC.
  - **Chucker (4.1.0)**: Interfaz de inspección de peticiones de red accesible desde la barra de notificaciones y desde el menú contextual superior.
  - **Lua Debug Library (5.4.7)**: Introspección nativa en C++ con `debug.traceback` para reportar archivos y líneas exactas en caso de errores en scripts.
  - Scripts modulares independientes en `./scripts/` (`setup_dbinspector.sh`, `setup_hyperion.sh`, `setup_anr_watchdog.sh`, `setup_leakcanary.sh`, `setup_chucker.sh`, `setup_lua_debug.sh`) integrados en el flujo de compilación de GitHub Actions.
- **Interfaz iOS-Inspired Ultra HD con Material Design 3**:
  - Tarjetas de atajos con gradientes multicapa de 3 paradas cromáticas y bordes con brillo de luz especular (*Border Glow*).
  - Sombras volumétricas coloreadas proyectadas con el tono ambiental de cada atajo (`ambientColor` y `spotColor`).
  - Badges de iconos translúcidos con efecto de vidrio esmerilado (*Glassmorphism*).
  - Rediseño ergonómico de bloques de acción en el modal de edición: tarjetas modulares con acentos de color y selector compacto desplegable (*Dropdown Picker*).
  - Banner superior de ejecución flotante estilo píldora con micro-borde brillante y barra de progreso fluida.
  - Cuadrícula adaptable con soporte para favoritos fijos y búsqueda en tiempo real.
  - Haptic feedback (vibración háptica) en cada acción.
- **Persistencia Local Segura**:
  - Base de datos Room con migraciones controladas y convertidores de tipos JSON.
  - Funciona 100% offline, sin requerir cuentas obligatorias ni servidores remotos.

---

## 📱 Requisitos y Compatibilidad

- **Sistema Operativo**: Android 8.0 (API 26) o superior (minSdk 24, targetSdk 36).
- **Entorno de Compilación**: Gradle (Kotlin DSL), Android NDK 27, CMake 3.22.1 y JVM 17/21.
- **Distribución**: Compatible con paquetes APK independientes (Uptodown, F-Droid, APKMirror, descarga directa).

---

## 🛠️ Instalación y Compilación

### Compilar el APK desde terminal:
```bash
gradle :app:assembleDebug
```
El archivo generado se ubicará en `app/build/outputs/apk/debug/app-debug.apk`.

### Ejecutar pruebas unitarias locales:
```bash
gradle :app:testDebugUnitTest
```

---

## 📂 Estructura del Código

```
/
├── app/
│   ├── build.gradle.kts                      # NDK 27, CMake y dependencias
│   └── src/main/
│       ├── cpp/                              # Capa Nativa C / C++ (Lua 5.4.7)
│       │   ├── CMakeLists.txt                # Script de compilación CMake
│       │   ├── native-lua.cpp                # Puente JNI bidireccional Android <-> Lua
│       │   └── lua/                          # Fuentes oficiales en C de Lua 5.4.7
│       ├── java/com/example/
│       │   ├── MainActivity.kt               # Punto de entrada con Compose Edge-to-Edge
│       │   ├── data/
│       │   │   ├── db/                       # Room Database (AppDatabase, ShortcutDao)
│       │   │   ├── model/                    # Modelos (ShortcutEntity, ActionBlock, ActionType)
│       │   │   └── repository/               # Repositorio con Kotlin Flow reactivo
│       │   ├── executor/
│       │   │   ├── ShortcutExecutor.kt       # Ejecutor de bloques con retardo de 1003 ms
│       │   │   └── LuaShortcutEngine.kt      # Interfaz Kotlin con carga de libnative-lua.so
│       │   └── ui/
│       │       ├── ShortcutScreen.kt         # Pantalla principal con cuadrícula y buscador
│       │       ├── ShortcutViewModel.kt      # StateFlow y ciclo de vida de ejecución
│       │       └── components/               # Tarjetas, modales, iconos y banner de progreso
```

---

## 📄 Licencia
Distribuido bajo licencia de código abierto con fines de productividad y automatización móvil.
