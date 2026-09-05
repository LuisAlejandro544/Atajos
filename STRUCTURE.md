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

1. **Interacción de Usuario**: El usuario pulsa una tarjeta de atajo en `ShortcutScreen.kt`.
2. **ViewModel**: Se activa `viewModelScope.launch` en `ShortcutViewModel.executeShortcut(shortcut)`.
3. **Resolución de Bloques**: Se obtienen las acciones definidas (`ActionBlock`).
4. **Ciclo Secuencial**:
   - Se actualiza el banner flotante con el paso actual: *"Paso X de Y: [Nombre]"*.
   - Se ejecuta el bloque en `ShortcutExecutor.executeSingleBlock(type, param)`.
   - Si el bloque es `LUA_SCRIPT`, entra en acción `LuaShortcutEngine.executeScript(code)` llamando a `libnative-lua.so`.
   - El motor nativo C de Lua 5.4.7 procesa el script y, ante invocaciones como `flashlight()` o `copy()`, emite callbacks JNI hacia la instancia de Kotlin.
   - Se activa la vibración háptica en el dispositivo.
   - Si quedan bloques pendientes, se ejecuta la cadencia exacta de `delay(1003L)`.
5. **Finalización**: Se notifica en el banner el resultado final y se programa el auto-ocultamiento.
