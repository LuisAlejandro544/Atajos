# 🏛️ Arquitectura y Estructura del Código — Atajos

Este documento detalla la organización de capas, responsabilidades y el flujo de datos dentro de la aplicación.

---

## 📐 Patrón Arquitectónico: MVVM + Clean Architecture + Command/Strategy Pattern + Modular Delegations

La aplicación sigue una arquitectura modular y reactiva basada en **Model-View-ViewModel (MVVM)** con separación estricta de responsabilidades:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE PRESENTACIÓN                           │
│  • MainActivity (Ciclo de vida, Intent resolution, Edge-to-edge)        │
│  • AppNavigation (ShortcutsTopBar, ShortcutsBottomBar, MainTabContent)  │
│  • Jetpack Compose Screens (Home, Editor, Gallery, History)             │
│  • Editor Submódulos: LivePreviewCard, MetadataSection, ActionsSection  │
│  • ShortcutsViewModel (StateFlow, Coordinación y fachada MVVM)          │
│  • Gestores Modulares: ShortcutEditorManager, AutomationsManager        │
│  • UI Submódulos: actioninputs/ (Tts, Flashlight, Vibration, Launcher)  │
│  • Componentes Dinámicos: PermissionBanner, VariablePickerChips         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE DOMINIO                              │
│  • ActionExecutor (Despachador y orquestador del ciclo de vida)         │
│  • VariableResolverHelper (Interpolación de variables del sistema)      │
│  • AppShortcutsHelper (Gestor de App Shortcuts en el launcher)          │
│  • ActionHandlers (Tts, Flashlight, Vibration, AppLauncher, etc.)       │
│  • AppScannerHelper (Escaneo asíncrono en Dispatchers.IO)               │
│  • Modelos de datos (ShortcutAction, ActionType, AppInfo, EditorState)  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                             CAPA DE DATOS                               │
│  • ShortcutRepository (Acceso unificado a base de datos Room)           │
│  • Room Database (ShortcutDao, AutomationDao, LogDao)                   │
│  • SQLite Entities (ShortcutEntity, AutomationEntity, ExecutionLog)     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🗂️ Árbol de Directorios y Módulos

```
app/src/main/java/com/example/
│
├── MainActivity.kt               # Contenedor raíz, permisos dinámicos y launcher shortcuts
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
│   │   ├── AppInfo.kt            # Modelo de metadatos de aplicaciones instaladas
│   │   ├── ShortcutEntity.kt     # Entidad Room para atajos guardados
│   │   ├── AutomationEntity.kt   # Entidad Room para automatizaciones
│   │   ├── ExecutionLogEntity.kt # Entidad Room para logs de auditoría
│   │   ├── ShortcutAction.kt     # Data class de acción individual y lista JSON
│   │   └── ActionType.kt         # Enum y metadatos de las acciones disponibles
│   │
│   └── repository/
│       └── ShortcutRepository.kt # Repositorio que orquesta las llamadas a Room
│
├── engine/
│   ├── ActionExecutor.kt         # Orquestador del motor de ejecución (Strategy Dispatcher)
│   ├── AppScannerHelper.kt       # Escaneo en segundo plano (Dispatchers.IO) de apps y juegos
│   ├── AppShortcutsHelper.kt     # Gestor de accesos directos dinámicos en el icono del launcher
│   ├── VariableResolverHelper.kt # Motor de interpolación de variables del sistema en tiempo real
│   └── handlers/                 # Manejadores de acciones modulares individuales
│       ├── ActionHandler.kt              # Interfaz base con contrato de ejecución y release
│       ├── AppLauncherActionHandler.kt   # Lanzador directo de juegos y aplicaciones instaladas
│       ├── CameraActionHandler.kt        # Lanzador nativo de cámara (Foto trasera, Selfie frontal, Vídeo)
│       ├── BrightnessActionHandler.kt    # Control y ajuste de brillo de pantalla del sistema (WRITE_SETTINGS)
│       ├── VolumeActionHandler.kt        # Gestor de canales de audio (Multimedia, Notificaciones, Llamadas, Alarma)
│       ├── TtsActionHandler.kt           # Manejo de Text-To-Speech con soporte para lectura de notificaciones previas
│       ├── FlashlightActionHandler.kt    # Control de CameraManager y linterna
│       ├── VibrationActionHandler.kt     # Patrones hápticos avanzados (Ligero, Fuerte, Doble, SOS)
│       ├── NotificationActionHandler.kt  # Despacho de notificaciones locales y caching de último mensaje
│       ├── SystemIntentsActionHandler.kt # Intents: Web, WhatsApp, SMS, Portapapeles, Timer
│       └── UtilityActionHandler.kt       # Delays de suspensión y cálculo de propinas
│
└── ui/
    ├── components/               # Componentes reutilizables de Compose
    │   ├── ActionCard.kt         # Tarjeta de acción reordenable y contenedor modular
    │   ├── AddActionBottomSheet.kt # Diálogo modal para agregar acciones
    │   ├── AppPickerBottomSheet.kt # Selector visual de juegos/apps con buscador y estados de carga
    │   ├── CustomizationPickers.kt # Selectores de paletas de color e iconos
    │   ├── ExecutionStatusBanner.kt# Banner dinámico de progreso en vivo
    │   ├── IconHelper.kt         # Mapeo tipado de iconos de Material Symbols
    │   ├── PermissionBanner.kt   # Banner informativo para activación de permisos en tiempo de ejecución
    │   ├── ShortcutCard.kt       # Tarjeta de atajo en la pantalla principal
    │   ├── VariablePickerChips.kt# Selector horizontal para autocompletar variables dinámicas
    │   │
    │   ├── actioninputs/         # Submódulos de parametrización por tipo de acción
    │   │   ├── AppLauncherInputSection.kt # Selector y visualizador de juego/app configurado
    │   │   ├── CameraInputSection.kt      # Selector de modo de cámara (Foto, Selfie, Vídeo) y transparencia
    │   │   ├── BrightnessInputSection.kt  # Controles de brillo (+20%, -20%, presets y slider porcentual)
    │   │   ├── VolumeInputSection.kt      # Controles de volumen para multimedia, llamadas y alertas
    │   │   ├── TtsInputSection.kt         # Campo de voz, botón de lectura de notificaciones y chips
    │   │   ├── FlashlightInputSection.kt
    │   │   ├── VibrationInputSection.kt   # Selector de patrones hápticos y test en tiempo real
    │   │   ├── NotificationInputSection.kt# Título y mensaje con chips de variables dinámicas
    │   │   ├── WebUrlInputSection.kt
    │   │   ├── MessagingInputSection.kt   # WhatsApp/SMS con chips de variables dinámicas
    │   │   └── UtilitiesInputSection.kt
    │   │
    │   └── editor/               # Submódulos desacoplados del editor de atajos
    │       ├── ShortcutLivePreviewCard.kt # Tarjeta de previsualización reactiva en tiempo real
    │       ├── ShortcutMetadataSection.kt # Formulario de título, descripción, categoría, color e icono
    │       └── ShortcutActionsSection.kt  # Pipeline de acciones: header, lista y empty states
    │
    ├── navigation/               # Módulos de enrutamiento y barras de navegación
    │   ├── ShortcutsTopBar.kt    # Barra superior con branding, banner de permisos y monitor
    │   ├── ShortcutsBottomBar.kt # Barra de navegación inferior (Tabs M3)
    │   └── MainTabContent.kt     # Transición y orquestación de pantallas principales
    │
    ├── screens/
    │   ├── ShortcutsHomeScreen.kt   # Vista principal con cuadrícula de atajos
    │   ├── ShortcutEditorScreen.kt  # Editor visual modularizado
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
        ├── ShortcutsViewModel.kt    # Fachada central y coordinación de flujos (StateFlow)
        ├── ShortcutEditorManager.kt # Gestor modular de mutaciones del editor
        ├── AutomationsManager.kt    # Gestor modular de lógica y CRUD de automatizaciones
        ├── EditorState.kt           # Modelo de estado inmutable para el editor
        └── DefaultActionFactory.kt  # Fábrica de acciones predeterminadas
```

---

## ⚡ Flujo de Ejecución de un Atajo

1. **Usuario presiona "Ejecutar" o toca un App Shortcut en el launcher**: Inicia la invocación en `ShortcutsViewModel`.
2. **ViewModel actualiza UI State**: `ActionExecutor` emite a través de `status: StateFlow<ExecutionStatus>` el paso actual, título y progreso.
3. **Paso a paso en `ActionExecutor` con patrón Strategy y Resolución de Variables**:
   - `ActionExecutor` busca el `ActionHandler` asignado en su mapa de tipos (`handlerMap[action.type]`).
   - Si el paso contiene texto (Voz, Notificaciones, SMS, WhatsApp), `VariableResolverHelper` interpola las variables (`{HORA}`, `{BATERIA}`, `{FECHA}`, `{PORTAPAPELES}`) con datos del sistema en tiempo real.
   - El manejador especializado (`AppLauncherActionHandler`, `TtsActionHandler`, `VibrationActionHandler`, etc.) ejecuta la lógica de hardware o sistema de forma segura.
4. **Persistencia de Resultado**:
   - Se registra la ejecución en `execution_logs` mediante `ShortcutRepository.logExecution()`.
   - Se actualiza el contador de ejecuciones y la marca de tiempo `lastRunTimestamp` en `ShortcutDao`.
5. **Feedback al Usuario**: La UI muestra en tiempo real `ExecutionStatusBanner` y notifica la culminación exitosa o cualquier error capturado.
