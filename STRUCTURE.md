# 🏛️ Arquitectura y Estructura del Código — Atajos

Este documento detalla la organización de capas, responsabilidades y el flujo de datos dentro de la aplicación.

---

## 📐 Patrón Arquitectónico: MVVM + Clean Architecture

La aplicación sigue el patrón **Model-View-ViewModel (MVVM)** recomendado para Android con Compose:

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                     │
│  • Jetpack Compose Screens (Home, Editor, Gallery, History) │
│  • ShortcutsViewModel (StateFlow, UI Events, Coordinación)  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE DOMINIO                        │
│  • ActionExecutor (Motor de ejecución de hardware/software) │
│  • Modelos de datos (ShortcutAction, ActionType)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       CAPA DE DATOS                         │
│  • ShortcutRepository (Acceso unificado a datos)            │
│  • Room Database (ShortcutDao, AutomationDao, LogDao)       │
│  • SQLite Entities (ShortcutEntity, Automation, Log)        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗂️ Árbol de Directorios y Módulos

```
app/src/main/java/com/example/
│
├── MainActivity.kt               # Contenedor raíz, barra de navegación y control de rutas
│
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt        # Definición de la base de datos Room y type converters
│   │   ├── ShortcutDao.kt        # Consultas reactivas (Flow) para atajos
│   │   ├── AutomationDao.kt      # Operaciones CRUD para automatizaciones
│   │   ├── ExecutionLogDao.kt    # Registro de historial de ejecuciones
│   │   └── DefaultShortcutsProvider.kt # Atajos de ejemplo y plantillas iniciales
│   │
│   ├── model/
│   │   ├── ShortcutEntity.kt     # Entidad Room para atajos guardados
│   │   ├── AutomationEntity.kt   # Entidad Room para automatizaciones
│   │   ├── ExecutionLogEntity.kt # Entidad Room para logs de auditoría
│   │   └── ActionType.kt         # Enum y metadatos de las acciones disponibles
│   │
│   └── repository/
│       └── ShortcutRepository.kt # Repositorio que orquesta las llamadas a Room
│
├── engine/
│   └── ActionExecutor.kt         # Motor que interactúa con las APIs del sistema Android:
│                                 # TTS, Vibrator, CameraManager, NotificationManager, etc.
│
└── ui/
    ├── components/               # Componentes reusables de Compose (Cards, Badges, Dialogs)
    ├── screens/
    │   ├── ShortcutsHomeScreen.kt   # Vista principal con cuadrícula de atajos
    │   ├── ShortcutEditorScreen.kt  # Editor visual de pasos, iconos y colores
    │   ├── AutomationsScreen.kt     # Gestión de disparadores horarios/hardware
    │   ├── GalleryScreen.kt         # Catálogo de plantillas prefabricadas
    │   └── HistoryScreen.kt         # Visor de logs y tiempos de ejecución
    │
    ├── theme/
    │   ├── Color.kt              # Paleta moderna y colores temáticos
    │   ├── Theme.kt              # Configuración de Material 3 y Modo Oscuro
    │   └── Type.kt               # Tipografía Material Design
    │
    └── viewmodel/
        └── ShortcutsViewModel.kt # Estado centralizado (UIState), corrutinas y acciones
```

---

## ⚡ Flujo de Ejecución de un Atajo

1. **Usuario presiona "Ejecutar"**: El composable en `ShortcutsHomeScreen` invoca `viewModel.executeShortcut(shortcut)`.
2. **ViewModel actualiza UI State**: Se activa el estado `isExecuting = true` para mostrar indicadores visuales en la tarjeta.
3. **Paso a paso en `ActionExecutor`**:
   - `ActionExecutor.executeAction(action)` evalúa el `ActionType`.
   - Si es TTS, invoca `TextToSpeech.speak()`.
   - Si es Linterna, activa el `CameraManager.setTorchMode()`.
   - Si es Delay, suspende la corrutina con `delay()`.
4. **Persistencia de Resultado**:
   - Se registra la ejecución en `execution_logs` mediante `ExecutionLogDao`.
   - Se actualiza el contador de ejecuciones y la marca de tiempo `lastRunTimestamp` en `ShortcutDao`.
5. **Feedback al Usuario**: Se emite un `Snackbar` o vibración de confirmación y se restablece el estado de ejecución.
