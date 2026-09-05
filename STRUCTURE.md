# Estructura del Proyecto (Architecture & Code Map)

Este documento detalla la jerarquía de directorios, responsabilidades de cada capa y el flujo de datos de la aplicación.

---

## 🏛️ Patrón Arquitectónico

La aplicación sigue los principios de **MVVM (Model-View-ViewModel)** y **Clean Architecture** combinando código moderno en Kotlin con un motor de scripting en C nativo compilado mediante Android NDK:

```
[ UI (Jetpack Compose) ] 
       │ ▲
       ▼ │ StateFlow (ShortcutUiState)
[ ViewModel (ShortcutViewModel) ]
       │ ▲
       ▼ │ Coroutines / Intent Dispatch
[ Dominio / Motores ] ◄───────► [ Room Database (Local Storage) ]
  ├── ShortcutExecutor (1003ms cadence)
  └── LuaShortcutEngine (Kotlin JNI Interface)
             │ ▲
             ▼ │ JNI Callbacks
       [ libnative-lua.so ] (Lua 5.4.7 C Native Engine)
```

---

## 📁 Árbol Detallado de Archivos

```
/
├── .github/
│   └── workflows/
│       ├── build_debug.yml                   # Compilación manual de APK Debug (workflow_dispatch)
│       └── override_commit_message.yml       # Sincronización automática de mensaje desde commit_message.txt
├── scripts/
│   ├── build_apk_debug.sh                    # Compilación limpia del APK Debug sin caché
│   ├── generate_keystore.sh                  # Generación y verificación del keystore de depuración
│   ├── setup_cmake.sh                        # Configuración de CMake 3.22.1 y NDK 27
│   ├── setup_lua.sh                          # Descarga y extracción de fuentes oficiales de Lua 5.4.7
│   ├── setup_lua_debug.sh                    # Verificación de Lua Debug Library e integración con native-lua.cpp
│   ├── setup_leakcanary.sh                   # Descarga y configuración de LeakCanary 2.14 para el APK Debug
│   └── setup_chucker.sh                      # Descarga y configuración de Chucker 4.1.0 para el APK Debug
├── commit_message.txt                        # Mensaje de commit actual descriptivo en español
├── .env.example                               # Variables de entorno seguras
├── app/
│   ├── build.gradle.kts                      # NDK 27, CMake 3.22.1, abiFilters y dependencias
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Permisos de hardware (Camera/Flash, Vibration, Internet)
│       │   ├── cpp/                          # Capa Nativa C / C++
│       │   │   ├── CMakeLists.txt            # Compilación de Lua y del puente JNI
│       │   │   ├── native-lua.cpp            # Bindings JNI entre Kotlin y Lua 5.4.7
│       │   │   └── lua/                      # Código fuente oficial en C de Lua 5.4.7
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt           # Host Activity con enableEdgeToEdge
│       │   │   ├── data/
│       │   │   │   ├── db/
│       │   │   │   │   ├── AppDatabase.kt    # Base de datos Room v3 con TypeConverters
│       │   │   │   │   └── ShortcutDao.kt    # Consultas SQL reactivas con Flow
│       │   │   │   ├── model/
│       │   │   │   │   ├── ActionBlock.kt    # Unidad atómica de un bloque de acción
│       │   │   │   │   ├── ActionBlockConverter.kt # Serializador JSON para listas de bloques
│       │   │   │   │   └── ShortcutEntity.kt # Entidad de atajo con enum ActionType
│       │   │   │   └── repository/
│       │   │   │       └── ShortcutRepository.kt # Abstracción de datos para el ViewModel
│       │   │   ├── executor/
│       │   │   │   ├── ShortcutExecutor.kt   # Gestor de secuencias y espera fija de 1003 ms
│       │   │   │   └── LuaShortcutEngine.kt  # Enlace Kotlin con la librería nativa libnative-lua.so
│       │   │   └── ui/
│       │   │       ├── ShortcutScreen.kt     # Pantalla principal (TopBar, categorías, Grid)
│       │   │       ├── ShortcutViewModel.kt  # Gestión de estado, filtros y corrutinas
│       │   │       ├── components/
│       │   │       │   ├── ExecutionBanner.kt    # Banner tipo iOS flotante con barra de progreso
│       │   │       │   ├── IconHelper.kt         # Catálogo de iconos y paleta de colores
│       │   │       │   ├── ShortcutCard.kt       # Tarjeta con insignia de pasos y haptics
│       │   │       │   └── ShortcutEditSheet.kt  # Modal para crear/editar bloques y código Lua
│       │   │       └── theme/
│       │   │           ├── Color.kt          # Paleta base
│       │   │           ├── Theme.kt          # Material 3 Dynamic Theme
│       │   │           └── Type.kt           # Tipografía
│       └── test/
│           └── java/com/example/
│               └── ShortcutLogicTest.kt      # Tests unitarios locales JVM para bloques y entidades
├── metadata.json                             # Metadatos para AI Studio
├── README.md                                 # Descripción y guía de compilación
├── ROADMAP.md                                # Hitos de desarrollo
├── STRUCTURE.md                              # Este archivo
├── AI_CONTEXT.md                             # Contexto y restricciones del sistema para IA
└── AGENTS.md                                 # Guía de comportamiento y reglas para agentes
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
   - En bloques estándar de hardware o voz (`SPEAK` / TextToSpeech nativo), se ejecutan en `ShortcutExecutor.executeSingleBlock(type, param)` y se activa vibración háptica.
   - Si quedan bloques pendientes y no se trata de una pausa explícita, se aplica la cadencia por defecto de `delay(1003L)`.
5. **Finalización**: Se notifica en el banner el resultado final y se programa el auto-ocultamiento.

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
