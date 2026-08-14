# Atajos para Android ⚡

> **Automatización rápida y potente de tareas en Android sin complicaciones.**

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
- ⏰ **Automatizaciones Programadas**: Configuración de disparadores por horario (mañana, noche) o estado de hardware.
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
| **CI / CD & Auditoría** | GitHub Actions (Compilación de APK, Limpieza de archivos zip, Informe de peso del repositorio) |

---

## 🤖 Automatización y Workflows de GitHub Actions

El repositorio cuenta con 3 flujos de trabajo optimizados:
1. **`Build Android Debug APK`** (`build-apk.yml`): Compila y firma automáticamente el APK listo para descargar e instalar en dispositivos o emuladores.
2. **`Sync Code from Zip Archive`** (`sync-from-zip.yml`): Se activa automáticamente al subir cualquier archivo comprimido (`.zip`, `.7z`, `.tar.gz`, etc.) a la carpeta `zip/`, descomprime, sincroniza cambios preservando el control de versiones y **elimina automáticamente los archivos comprimidos** procesados para mantener el repositorio limpio sin basura.
3. **`Repository Size & Metrics Report`** (`repo-size-report.yml`): Calcula métricas de almacenamiento, peso de `.git`, líneas de código y genera reportes detallados en `REPO_SIZE_REPORT.md` y en el Step Summary de GitHub Actions.

---

## 🚀 Compilación y Descarga del APK

### Opción 1: Descarga Directa desde GitHub Actions (Recomendada)
Cada commit y pull request ejecuta automáticamente un flujo de CI en GitHub Actions que compila y firma el APK:
1. Ve a la pestaña **Actions** en tu repositorio de GitHub.
2. Selecciona la última ejecución del workflow **Build Android Debug APK**.
3. En la sección **Artifacts** al final de la página, descarga el archivo `Atajos-Debug-APK.zip`.
4. Descomprime e instala el archivo `.apk` directamente en tu dispositivo Android o emulador.

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

## 🌐 Sitio Web y Documentos Legales (Cloudflare Pages)

El proyecto incluye el sitio web oficial en la carpeta `/website`, construido con **Astro + Tailwind CSS** y optimizado para dispositivos móviles:
- 🏠 **Landing Page (`/`):** Presentación del proyecto, sección de preguntas frecuentes (FAQ) y descarga directa del APK.
- 📜 **Términos y Condiciones (`/legal/terminos`):** Marco legal para la ejecución de atajos locales.
- 🛡️ **Política de Privacidad (`/legal/privacidad`):** Declaración de arquitectura 100% offline-first y sin rastreo.
- 📚 **Documentación (`/docs`):** Guía completa de variables dinámicas, acciones soportadas y disparadores.
- 🐙 **Repositorio GitHub:** [https://github.com/LuisAlejandro544/Atajos](https://github.com/LuisAlejandro544/Atajos)

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

Este proyecto está protegido bajo la **PolyForm Noncommercial License 1.0.0** (modelo *Source-Available*).

> **`Required Notice:`** Copyright (c) 2026 Luis Alejandro Sosa Camacho. Texto oficial disponible en [https://polyformproject.org/licenses/noncommercial/1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0).

- Permite a los usuarios estudiar, inspeccionar y compilar el código fuente en sus propios dispositivos para fines privados y no comerciales.
- **Prohíbe a terceros la redistribución, republicación o comercialización de paquetes APK** en tiendas de aplicaciones o sitios web sin el consentimiento expreso y por escrito del autor.
- La distribución oficial y publicación en tiendas de terceros es facultad exclusiva del titular del proyecto.

