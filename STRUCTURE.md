# 🏛️ Arquitectura y Estructura del Código — Flurix

Este documento detalla la organización de capas, responsabilidades y el flujo de datos dentro de la aplicación.

---

## 📐 Patrón Arquitectónico: MVVM + Clean Architecture + Command/Strategy Pattern + Modular Delegations

La aplicación sigue una arquitectura modular y reactiva basada en **Model-View-ViewModel (MVVM)** con separación estricta de responsabilidades:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE PRESENTACIÓN                           │
│  • MainActivity (Ciclo de vida, Intent resolution, Edge-to-edge)        │
│  • Navigation Contracts (MainTabActions, Home/Automations/Gallery tabs) │
│  • AppNavigation (ShortcutsTopBar, ShortcutsBottomBar, MainTabContent)  │
│  • Jetpack Compose Screens (Home, Editor, Gallery, History, Auto)       │
│  • Automations Submódulos: AutomationCard, NewAutomationDialog          │
│  • Editor Submódulos: LivePreviewCard, MetadataSection, ActionsSection  │
│  • ShortcutsViewModel (StateFlow, Coordinación y fachada MVVM)          │
│  • Gestores Modulares: ShortcutEditorManager, AutomationsManager        │
│  • UI Submódulos: actioninputs/ (Tts, Flashlight, Vibration, Launcher)  │
│  • Componentes Dinámicos: PermissionBanner, VariablePickerChips, Item   │
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
│   │   └── DefaultShortcutsProvider.kt # Fachada modular para atajos y plantillas iniciales
│   │
│   ├── defaults/                 # Proveedores modulares de plantillas y datos iniciales
│   │   ├── DefaultShortcutsList.kt   # Atajos base sembrados en SQLite
│   │   ├── GalleryTemplatesList.kt   # Catálogo completo de plantillas de la galería
│   │   └── DefaultAutomationsList.kt # Automatizaciones de ejemplo iniciales
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
│   ├── service/                  # Servicios en segundo plano
│   │   └── AutomationService.kt  # Foreground Service persistente para monitorización de hardware
│   ├── tiles/                    # Servicios de Mosaico en el panel de Ajustes Rápidos (Quick Settings)
│   │   └── ShortcutTileService.kt# TileService para ejecución instantánea de atajos favoritos desde la cortina
│   ├── triggers/                 # Disparadores nativos de sistema en segundo plano
│   │   ├── BootReceiver.kt         # Auto-arranque tras reinicio del sistema (BOOT_COMPLETED)
│   │   ├── PowerTriggerReceiver.kt # BroadcastReceiver para eventos de alimentación y batería
│   │   ├── TimeSchedulerHelper.kt  # Asistente de programación con AlarmManager exacto
│   │   └── TimeTriggerReceiver.kt  # BroadcastReceiver activado por AlarmManager a la hora exacta
│   └── handlers/                 # Manejadores de acciones modulares individuales
│       ├── ActionHandler.kt              # Interfaz base con contrato de ejecución y release
│       ├── HttpRequestActionHandler.kt   # Peticiones HTTP y Webhooks (GET, POST, PUT, DELETE) con variables dinámicas
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
    │   ├── ActionTypeItem.kt     # Componente modularizado para ítems de selección de acción
    │   ├── AddActionBottomSheet.kt # Diálogo modal para agregar acciones
    │   ├── AppPickerBottomSheet.kt # Contenedor orquestador del selector de aplicaciones
    │   ├── CustomizationPickers.kt # Selectores de paletas de color e iconos
    │   ├── ExecutionStatusBanner.kt# Banner dinámico de progreso en vivo
    │   ├── IconHelper.kt         # Mapeo tipado de iconos de Material Symbols
    │   ├── PermissionBanner.kt   # Banner informativo para activación de permisos en tiempo de ejecución
    │   ├── ShortcutCard.kt       # Tarjeta de atajo en la pantalla principal
    │   ├── VariablePickerChips.kt# Selector horizontal para autocompletar variables dinámicas
    │   │
    │   ├── apppicker/            # Submódulos desacoplados del selector de aplicaciones
    │   │   ├── AppPickerHeader.kt       # Cabecera con título, icono temático y botón de cierre
    │   │   ├── AppPickerLoadingState.kt # Indicador y textos de carga durante el escaneo
    │   │   ├── AppPickerSearchBar.kt    # Campo de búsqueda interactivo
    │   │   ├── AppPickerFilterRow.kt    # Fila de FilterChips (Todas / Juegos / Apps)
    │   │   └── AppPickerListItem.kt     # Ítem de aplicación con icono, badge y selección
    │   │
    │   ├── automations/          # Submódulos desacoplados del sistema de automatizaciones
    │   │   ├── AutomationCard.kt       # Tarjeta individual con switches y acciones de prueba/borrado
    │   │   └── NewAutomationDialog.kt  # Modal de creación de automatización y configuración de triggers
    │   │
    │   ├── actioninputs/         # Submódulos de parametrización por tipo de acción
    │   │   ├── AppLauncherInputSection.kt # Selector y visualizador de juego/app configurado
    │   │   ├── HttpRequestInputSection.kt # Peticiones Web / Webhooks (GET, POST, Body y variables)
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
    │   ├── NavigationContracts.kt# Contratos tipados de callbacks (HomeTabActions, AutomationsTabActions, etc.)
    │   ├── ShortcutsTopBar.kt    # Barra superior con branding, banner de permisos y monitor
    │   ├── ShortcutsBottomBar.kt # Barra de navegación inferior (Tabs M3)
    │   └── MainTabContent.kt     # Transición y orquestación de pantallas principales
    │
    ├── screens/
    │   ├── ShortcutsHomeScreen.kt   # Vista principal con cuadrícula de atajos
    │   ├── ShortcutEditorScreen.kt  # Editor visual modularizado
    │   ├── AutomationsScreen.kt     # Orquestador desacoplado de automatizaciones
    │   ├── GalleryScreen.kt         # Catálogo de plantillas prefabricadas
    │   ├── HistoryScreen.kt         # Visor de logs y tiempos de ejecución
    │   └── SettingsScreen.kt        # Apartado de configuración, visor legal in-app y enlaces web
    │
    ├── theme/
    │   ├── Color.kt              # Paleta moderna y colores temáticos
    │   ├── Theme.kt              # Configuración de Material 3 y Modo Oscuro
    │   └── Type.kt               # Tipografía Material Design
    │
    └── viewmodel/
        ├── ShortcutsViewModel.kt        # Fachada central y coordinación de flujos (StateFlow)
        ├── ShortcutExecutionManager.kt  # Gestor modular de ejecución de atajos y guardado de logs
        ├── ShortcutEditorManager.kt     # Gestor modular de mutaciones del editor
        ├── AutomationsManager.kt        # Gestor modular de lógica y CRUD de automatizaciones
        ├── EditorState.kt               # Modelo de estado inmutable para el editor
        └── DefaultActionFactory.kt      # Fábrica de acciones predeterminadas
```

---

## 🌐 Estructura del Portal Web (`/website` — Astro + Tailwind)

```
website/
├── src/
│   ├── layouts/
│   │   ├── Layout.astro          # Layout global con cabecera y pie de página
│   │   └── LegalLayout.astro     # Contenedor con estilos tipográficos para documentos legales
│   └── pages/
│       ├── index.astro           # Landing page principal
│       ├── docs/
│       │   └── index.astro       # Documentación de variables y triggers
│       └── legal/
│           ├── terminos.astro    # Términos y Condiciones de Uso
│           └── privacidad.astro  # Política de Privacidad (Offline-first / Sin rastreo)
├── public/
│   └── favicon.svg               # Icono vectorial del sitio
├── astro.config.mjs              # Configuración de Astro (Cloudflare Pages Static Output)
├── tailwind.config.mjs           # Tema de colores oscuros y brand
└── package.json                  # Dependencias de Astro v4 y Tailwind CSS
```

---

## 🏷️ Esquema de Versiones y Canales de Publicación

Flurix sigue la convención de sufijos con resolución dinámica de identificadores de paquete y nombres en launcher:
- **`-E` (Estable)**: `com.flurix.app` (Launcher: `Flurix`). Canal de producción principal.
- **`-DEV` / `-D` (Desarrollo)**: `com.flurix.app.dev` (Launcher: `Flurix Dev`). Canal experimental activo.
- **`-B` / `-BETA` (Beta)**: `com.flurix.app.beta` (Launcher: `Flurix Beta`). Canal de pruebas antes del paso a estable (versión actual: `0.1.0-B`).
- **`-CANARY` / `-CREATOR`**: `com.flurix.app.canary` (Launcher: `Flurix Canary`). Canal del autor para pruebas continuas.

---

## 🤖 Workflows de Integración Continua (`.github/workflows/`)

- `build-release-beta.yml`: Compilación y empaquetado optimizado con ProGuard/R8 de APK Release Beta activado por Pre-release (tag `-B` / `-beta`) y subida a Release Assets.
- `update-release-notes-beta.yml`: Sincronización automática del cuerpo de la Pre-release con el contenido de `Chanelog-beta.md`.
- `build-apk.yml`: Compilación de APK Debug y envío directo a Telegram vía bot.
- `android-device-simulation.yml`: Simulación en emulador oficial de Android (KVM / API 34), inyección de eventos de hardware (`adb shell cmd battery`) y auditoría de excepciones fatales en runtime con caché de AVD y Gradle.
- `sync-from-zip.yml`: Sincronización automática de código desde archivos comprimidos y auto-limpieza.
- `repo-size-report.yml`: Auditoría y reporte del peso del repositorio y métricas.
- `clean-workflow-runs.yml`: Purgado manual y seguro (`workflow_dispatch`) de todo el historial de ejecuciones previas de GitHub Actions con compatibilidad para GitHub Secrets (`PAT_TOKEN`, `GH_TOKEN`, `GITHUB_TOKEN`).

---

## ⚡ Flujo de Ejecución de un Atajo

1. **Usuario presiona "Ejecutar" o toca un App Shortcut en el launcher**: Inicia la invocación en `ShortcutsViewModel`.
2. **ViewModel actualiza UI State**: `ActionExecutor` emite a través de `status: StateFlow<ExecutionStatus>` el paso actual, título y progreso.
3. **Paso a paso en `ActionExecutor` con patrón Strategy y Resolución de Variables**:
   - `ActionExecutor` busca el `ActionHandler` asignado en su mapa de tipos (`handlerMap[action.type]`).
   - Si el paso contiene texto (Voz, Notificaciones, SMS, WhatsApp), `VariableResolverHelper` interpola las variables (`{HORA}`, `{BATERIA}`, `{FECHA}`, `{PORTAPAPELES}`) con datos del sistema en tiempo real.
   - El manejador especializado (`AppLauncherActionHandler`, `TtsActionHandler`, `VibrationActionHandler`, etc.) ejecuta la lógica de hardware o sistema de forma segura.
4. **Persistencia de Resultado**:
   - Se registra la ejecución en `execution_logs` mediante `ShortcutRepository.logExecution()` con estado `SUCCESS`, `FAILED` o `CANCELLED`.
   - Se actualiza el contador de ejecuciones y la marca de tiempo `lastRunTimestamp` en `ShortcutDao` (solo en ejecuciones exitosas no canceladas).
5. **Feedback al Usuario**: La UI muestra en tiempo real `ExecutionStatusBanner` y notifica la culminación exitosa o cualquier error capturado.
6. **Cancelación Interactiva**: En cualquier momento durante la ejecución, el usuario puede presionar "Cancelar" en `ExecutionStatusBanner`. El `ActionExecutor` cancela la corrutina en curso, invoca `onCancelled()` en los handlers activos (deteniendo TTS o vibraciones) y actualiza el banner y los registros históricos a estado "CANCELLED".
