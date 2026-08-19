# Flurix para Android ⚡

> **Automatización rápida y potente de tareas en Android sin complicaciones.**
> **Versión Actual:** `0.1.0-B` (Beta)

---

## 🏷️ Canales y Esquema Dinámico de Versiones

Flurix utiliza una resolución dinámica en `build.gradle.kts` que ajusta automáticamente el identificador de paquete (`applicationId`) y la etiqueta en la pantalla de inicio (`appName`) según el sufijo de versión:

| Canal | Sufijo de Versión | `applicationId` | Nombre en Pantalla (Launcher) | Propósito y Estabilidad |
| :--- | :--- | :--- | :--- | :--- |
| **Estable** | `-E` (ej. `0.1.0-E`) | `com.flurix.app` | **Flurix** | Versiones consolidadas y verificadas para uso diario y distribución principal en Uptodown. |
| **Beta** | `-B` / `-BETA` (ej. `0.1.0-B`) | `com.flurix.app.beta` | **Flurix Beta** | Funciones destinadas a la versión estable en fase de prueba y validación. |
| **Desarrollo** | `-DEV` / `-D` (ej. `0.2.0-DEV`) | `com.flurix.app.dev` | **Flurix Dev** | Entorno de desarrollo activo con funciones volátiles y cambios frecuentes. |
| **Canary / Creador** | `-CANARY` / `-CREATOR` | `com.flurix.app.canary` | **Flurix Canary** | Versión de vanguardia para pruebas directas del creador e integración continua. |

> 💡 **Instalación Coexistente:** Gracias a los diferentes `applicationId`, puedes tener instaladas simultáneamente la versión Estable, Beta y Dev en el mismo teléfono sin que se sobreescriban.

Una aplicación nativa para Android construida con **Kotlin** y **Jetpack Compose (Material Design 3)** que permite crear, editar, organizar y ejecutar flujos de acciones automatizadas en el dispositivo. Diseñada para ser rápida, ligera y totalmente funcional sin depender de servicios externos obligatorios ni tiendas cerradas (optimizada para distribución directa como APK o en tiendas de terceros como Uptodown).

---

## 📱 Características Principales

- ⚡ **Motor de Ejecución de Acciones**: Encadenamiento secuencial de acciones de hardware y software (Peticiones Web / Webhooks, Lanzador de juegos y apps, Apertura directa de Cámara en modo Foto/Selfie/Vídeo, Texto a voz inteligente con lectura de notificaciones, Control de Brillo de pantalla, Ajuste de Volumen/Sonido, Linterna, Vibración háptica con patrones avanzados, Notificaciones, Portapapeles, Delays, URLs, Búsquedas y Mensajería).
- 🎛️ **Quick Settings Tile (Mosaico en Cortina de Ajustes Rápidos)**: Mosaico nativo `ShortcutTileService` accesible desde la barra superior de notificaciones de Android, permitiendo ejecutar con un solo toque el atajo favorito desde cualquier aplicación o pantalla de bloqueo.
- 🔌 **Disparadores Automáticos Nativos (Alimentación y Batería en Segundo Plano)**: Inicia cualquier atajo automáticamente al conectar o desconectar el cargador (`ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED`), ante batería baja (<15% `ACTION_BATTERY_LOW`), batería restablecida (`ACTION_BATTERY_OKAY`) o carga completa al 100% (`BATTERY_FULL`), configurado limpiamente dentro del editor de cada atajo.
- 🛑 **Cancelación Interactiva en Tiempo Real**: Si el usuario decide detener o abortar un atajo en curso, puede cancelarlo instantáneamente desde el banner interactivo superior, liberando inmediatamente recursos de audio (TTS), hápticos (vibrador) y deteniendo los pasos restantes.
- 🌐 **Solicitudes Web y Webhooks (Obtener contenido de URL)**: Peticiones HTTP completas (`GET`, `POST`, `PUT`, `DELETE`) con interpolación de datos dinámicos en la URL y en el cuerpo JSON, almacenando la respuesta en `{RESPUESTA_WEB}` y el código en `{HTTP_STATUS}` para su lectura por voz o notificación.
- 📷 **Apertura Directa de Cámara (Foto / Vídeo / Selfie)**: Lanza la cámara nativa del teléfono en el modo seleccionado (Cámara trasera, Selfie frontal o Grabación de vídeo) sin almacenar fotos en servidores y respetando 100% la privacidad.
- 🛡️ **Claridad y Transparencia en Permisos**: Banners y avisos contextuales que explican de manera transparente el uso de la cámara y notificaciones, garantizando un funcionamiento 100% offline y seguro.
- 📢 **Lectura Inteligente de Notificaciones**: El motor de texto a voz puede leer de forma directa o encadenada el contenido o título de la última notificación del flujo mediante `{ULTIMA_NOTIFICACION}` y `{NOTIFICACION_TITULO}` o con el botón de lectura rápida.
- ☀️ **Control de Brillo de Pantalla**: Acciones dedicadas para subir (+20%), bajar (-20%), definir niveles fijos (10%, 50%, 100%) o deslizar al porcentaje exacto de luminosidad deseado.
- 🔊 **Gestor de Volumen y Canales de Audio**: Ajuste independiente para Multimedia, Tono de llamada, Notificaciones y Alarmas con soporte para subir, bajar, silenciar o porcentaje exacto.
- 🏷️ **Variables Dinámicas del Sistema**: Posibilidad de insertar datos en tiempo real (`{HORA}`, `{FECHA}`, `{DIA_SEMANA}`, `{BATERIA}`, `{ESTADO_BATERIA}`, `{PORTAPAPELES}`, `{DISPOSITIVO}`, `{ULTIMA_NOTIFICACION}`) en tus mensajes de voz, notificaciones, WhatsApp y SMS con chips autocompletables para no tener que memorizar comandos.
- 📲 **App Shortcuts en el Icono**: Mantén presionado el icono de la aplicación en el launcher de tu teléfono para ejecutar al instante tus atajos favoritos sin necesidad de abrir la app.
- 🎮 **Lanzador de Juegos y Apps**: Escaneo asíncrono en segundo plano (`Dispatchers.IO`) de todos los juegos y aplicaciones instaladas en el dispositivo, con selector visual con barra de búsqueda y pantalla de carga.
- 📳 **Vibración Háptica Avanzada**: Soporte para patrones preconfigurados (Pulso suave, Golpe fuerte, Doble pulso, Triple pulso, Latido de corazón, Código Morse SOS y Duración en milisegundos a medida).
- 🔒 **Gestión Dinámica de Permisos**: Detección y solicitud automática en el inicio de la app (`POST_NOTIFICATIONS`, `CAMERA`), con banner de recordatorio si faltan accesos clave.
- 🎨 **Editor Visual de Atajos**: Añade, reordena y configura pasos de ejecución, asigna colores en degradado, nombres personalizados e iconos temáticos.
- ⏰ **Automatizaciones Programadas de Máxima Precisión (`setAlarmClock`)**: Configuración de disparadores por horario con la API de mayor prioridad del sistema (`AlarmManager.setAlarmClock`), garantizando ejecución exacta incluso durante juegos activos, Doze Mode o modos de ahorro de energía, con reprogramación automática tras el reinicio (`BootReceiver`).
- 🪟 **Permiso de Superposición / Segundo Plano (`SYSTEM_ALERT_WINDOW`)**: Solicitud guiada y transparente de "Mostrar sobre otras aplicaciones", permitiendo que los atajos y automatizaciones abran aplicaciones y juegos directamente en segundo plano sin bloqueos del sistema Android.
- 🔌 **Disparadores por Estado de Hardware**: Activación automática al conectar/desconectar el cargador o ante niveles críticos de batería.
- ⚙️ **Apartado de Ajustes y Visor Legal Integrado**: Pestaña dedicada de Configuración para consultar los Términos y Condiciones de Uso y la Política de Privacidad directamente dentro de la app (mediante un visor interactivo enriquecido) o abrir sus enlaces web oficiales en Cloudflare Workers, además de gestionar la optimización de batería, superposición y permisos.
- 📦 **Galería de Plantillas**: Atajos prediseñados listos para instalar con un toque (Lanzador de Juegos, Modo SOS, Saludo Matutino, Modo Enfoque, etc.).
- 📜 **Historial de Ejecución**: Registro de tiempos de ejecución, duración de pasos y estado de éxito/error.
- 💾 **Persistencia Local con Room**: Base de datos SQLite local rápida y segura; todos tus atajos y configuraciones se guardan localmente en el dispositivo.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.2.x |
| **UI Framework** | Jetpack Compose con Material 3 |
| **Accesos Rápidos** | Android Dynamic App Shortcuts (`ShortcutManagerCompat`) |
| **Arquitectura** | MVVM (Model-View-ViewModel) con Clean Architecture |
| **Base de Datos** | AndroidX Room (SQLite) con Kotlin Symbol Processing (KSP) |
| **Concurrencia** | Kotlin Coroutines & Reactive Flow (`Dispatchers.IO`, `Dispatchers.Main`) |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`) |
| **CI / CD & Auditoría** | GitHub Actions (Compilación de APK, Simulación de Hardware en Emulador Real, Limpieza de archivos zip, Informe de peso del repositorio, Purgado de historial de Actions) |

---

## 🤖 Automatización y Workflows de GitHub Actions

El repositorio cuenta con 7 flujos de trabajo optimizados:
1. **`Build Beta Release APK`** (`build-release-beta.yml`): Se activa estrictamente al publicar un **Pre-release** con tag Beta (ej. `v0.1.0-beta`, `v0.1.0-B`). Valida los secretos de firma en GitHub Secrets, compila el APK Release optimizado con ProGuard/R8 y lo adjunta automáticamente a los Assets del Pre-release.
2. **`Update Beta Release Notes from Changelog`** (`update-release-notes-beta.yml`): Sincroniza automáticamente la descripción y notas del Pre-release en GitHub utilizando el contenido de `Chanelog-beta.md`.
3. **`Build Android Debug APK`** (`build-apk.yml`): Compila y firma automáticamente el APK de depuración y lo envía directamente a tu Telegram mediante bot privado (sin consumir almacenamiento de artifacts en GitHub).
4. **`Android Device Hardware Simulation & Test`** (`android-device-simulation.yml`): Simulación completa en emulador oficial de Android (KVM / API 34) bajo demanda (`workflow_dispatch`) con caché acelerado de AVD y Gradle; inyecta eventos de hardware reales (conexión de cargador, desenchufe, niveles de batería al 15%, 80%, 100%) e inspecciona logs de ejecución para detectar fallos o excepciones en segundo plano.
5. **`Sync Code from Zip Archive`** (`sync-from-zip.yml`): Se activa automáticamente al subir cualquier archivo comprimido (`.zip`, `.7z`, `.tar.gz`, etc.) a la carpeta `zip/`, descomprime, sincroniza cambios preservando el control de versiones y **elimina automáticamente los archivos comprimidos** procesados para mantener el repositorio limpio sin basura.
6. **`Repository Size & Metrics Report`** (`repo-size-report.yml`): Calcula métricas de almacenamiento, peso de `.git`, líneas de código y genera reportes detallados en `REPO_SIZE_REPORT.md` y en el Step Summary de GitHub Actions.
7. **`Purge Actions History`** (`clean-workflow-runs.yml`): Flujo manual de seguridad y privacidad (`workflow_dispatch`) que elimina todas las ejecuciones previas de GitHub Actions en el repositorio, buscando tokens prioritarios en GitHub Secrets (`PAT_TOKEN`, `GH_TOKEN`, `GITHUB_TOKEN`) y purgando logs para evitar filtraciones y mantener limpia la pestaña de Actions.

---

## 🚀 Compilación y Descarga del APK

### Opción 1: Envío Directo a tu Telegram mediante GitHub Actions (Recomendada)
Cada ejecución manual o automática del flujo de CI compila, firma y envía el APK directamente a tu chat privado o canal de Telegram:
1. Configura tus secrets en GitHub (`TELEGRAM_BOT_TOKEN` y `TELEGRAM_CHAT_ID`).
2. Ve a la pestaña **Actions** en tu repositorio de GitHub.
3. Ejecuta el workflow **Build Android Debug APK**.
4. Recibirás el archivo `.apk` directamente en tu Telegram con el resumen de la compilación listo para instalar.

### Opción 2: Compilación Local con Gradle
Requisitos previos: **JDK 17** o superior y Android SDK configurado.

```bash
# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>
cd <CARPETA_DEL_PROYECTO>

# Dar permisos de ejecución al wrapper de Gradle
chmod +x ./gradlew

# Compilar el APK de depuración
./gradlew assembleDebug

# El APK generado se encontrará en:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔐 Permisos de Android Utilizados

| Permiso | Propósito |
| :--- | :--- |
| `CAMERA` / `FLASHLIGHT` | Control del flash/linterna y apertura de la cámara nativa para fotos, selfies y vídeos sin almacenar datos. |
| `VIBRATE` | Retroalimentación háptica y pulsos vibratorios avanzados. |
| `POST_NOTIFICATIONS` | Emisión de notificaciones locales de sistema con estados de atajos. |
| `WRITE_SETTINGS` | Modificación segura del nivel de brillo de pantalla del sistema. |
| `QUERY_ALL_PACKAGES` | Escaneo y listado de juegos/apps instaladas para la acción de inicio rápido. |
| `INTERNET` | Apertura de URLs y consultas de búsqueda web. |

---

## 📂 Estructura del Proyecto

```
├── .github/workflows/       # Workflows de CI/CD (Compilación de APK)
├── app/                     # Módulo Android (Kotlin + Jetpack Compose + Room)
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Punto de entrada, launcher shortcuts y navegación Compose
│   │   │   ├── data/                  # Capa de datos (Room DB, DAOs, Entidades, Repositorio)
│   │   │   ├── engine/                # Motor de ejecución, variables dinámicas y handlers modulares
│   │   │   └── ui/                    # Capa de presentación (Pantallas, ViewModel, Theme, Componentes)
│   │   └── res/                       # Recursos XML, drawables, strings, iconos adaptativos
│   └── build.gradle.kts               # Configuración de dependencias de la app
├── website/                 # Portal Web & Legal para Cloudflare Pages (Astro + Tailwind CSS)
│   ├── src/pages/           # Landing page, /legal (Términos, Privacidad) y /docs
│   ├── src/layouts/         # Layouts modulares y contenedores legales
│   ├── astro.config.mjs     # Configuración de compilación estática para Cloudflare
│   └── package.json         # Dependencias web (Astro v4 + Tailwind CSS)
├── metadata.json            # Metadatos de la plataforma
└── settings.gradle.kts      # Configuración de módulos del proyecto
```

---

## 🌐 Sitio Web y Documentos Legales (Cloudflare Workers)

El proyecto incluye el sitio web oficial en la carpeta `/website`, construido con **Astro + Tailwind CSS** y desplegado en Cloudflare:
- 🏠 **Landing Page:** [https://atajos-pagina.luisalejandrososacamacho9.workers.dev/](https://atajos-pagina.luisalejandrososacamacho9.workers.dev/)
- 📜 **Términos y Condiciones:** [https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/terminos/](https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/terminos/)
- 🛡️ **Política de Privacidad:** [https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/privacidad/](https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/privacidad/)
- 📚 **Documentación:** [https://atajos-pagina.luisalejandrososacamacho9.workers.dev/docs/](https://atajos-pagina.luisalejandrososacamacho9.workers.dev/docs/)
- 🐙 **Repositorio GitHub:** [https://github.com/LuisAlejandro544/Flurix](https://github.com/LuisAlejandro544/Flurix)

Para desarrollo local del sitio web:
```bash
cd website
npm install
npm run dev
```

Para compilar para Cloudflare Pages:
```bash
npm run build # Genera la carpeta /dist lista para Cloudflare Pages
```

---

## 📄 Licencia

Este proyecto está actualmente bajo la licencia temporal **PolyForm Noncommercial License 1.0.0** (modelo *Source-Available*). 

> 📢 **Aviso de Licenciamiento:** La licencia actual es **temporal**. Próximamente el proyecto migrará a una **licencia de código abierto oficial (Open Source)** para abrir la colaboración comunitaria completa.
>
> **`Required Notice:`** Copyright (c) 2026 Luis Alejandro Sosa Camacho. Texto oficial disponible en [https://polyformproject.org/licenses/noncommercial/1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0).

- Permite a los usuarios estudiar, inspeccionar y compilar el código fuente en sus propios dispositivos para fines privados y no comerciales.
- **Prohíbe temporalmente a terceros la redistribución no autorizada o comercialización de paquetes APK** en tiendas de aplicaciones o sitios web sin el consentimiento expreso del autor.
- La distribución oficial y publicación en tiendas de terceros es gestionada por el autor del proyecto.

