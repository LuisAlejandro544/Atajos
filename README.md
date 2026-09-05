# Atajos Android (Shortcuts & Pure Lua 5.4.7 Automation)

Aplicación moderna para Android de automatización mediante atajos interactivos, inspirada en flujos de tareas rápidas y personalizadas, con soporte para secuencias de múltiples bloques y ejecución nativa en **Lua 5.4.7 puro** (código C oficial compilado con Android NDK y CMake, sin wrappers ni intérpretes en Java).

Diseñada para ser ligera, ultra-rápida y 100% funcional en dispositivos móviles independientes, orientada a distribución directa (APK para Uptodown y tiendas de terceros).

---

## 🚀 Características Principales

- **Atajos Multi-Bloque en Cadena**:
  - Encadena múltiples acciones en un solo atajo (por ejemplo: activar linterna, copiar texto de aviso y abrir mapas).
  - Pausa exacta y calibrada de **1003 ms** entre bloques para garantizar que Android procese cada Intent sin colisiones ni descartes de eventos.
- **Motor de Scripting Lua 5.4.7 Puro (Nativo en C)**:
  - Compilado nativamente para arquitecturas móviles (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) mediante Android NDK y CMake (`libnative-lua.so`).
  - Sin intérpretes lentos ni wrappers de terceros; ejecuta directamente el motor oficial de PUC-Rio.
  - Soporte total para características avanzadas de Lua 5.4:
    - Variables locales `<const>` y `<close>` (gestión determinista de recursos).
    - Recolector de basura generacional rápido.
    - Enteros nativos de 64 bits y operadores a nivel de bits (`&`, `|`, `~`, `>>`, `<<`).
    - Manipulación completa de cadenas, tablas y funciones matemáticas.
  - Funciones de Android integradas mediante bindings JNI:
    - `flashlight()` / `flashlight(bool)`: Controla la linterna física.
    - `copy(texto)`: Copia contenido al portapapeles.
    - `open_url(url)`: Abre enlaces en el navegador web.
    - `map(destino)`: Inicia navegación en mapas.
    - `timer(minutos)`: Configura temporizador en el reloj del sistema.
    - `message(texto)` / `message(tel, texto)`: Abre selector o envío de mensaje.
    - `share(texto)`: Comparte contenido mediante el menú del sistema.
    - `sound_settings()`: Abre el panel de volumen y audio.
    - `get_hour()`: Retorna la hora actual del día (0-23).
    - `print(...)`: Registra y formatea mensajes capturados en el banner.
- **Interfaz iOS-Inspired con Material Design 3**:
  - Banner superior de ejecución paso a paso con barra de progreso fluida.
  - Cuadrícula adaptable de tarjetas de atajos con gradientes, colores personalizables e iconos temáticos.
  - Selector de categorías y barra de búsqueda en tiempo real.
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
