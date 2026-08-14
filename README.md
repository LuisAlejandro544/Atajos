# Atajos para Android ⚡

> **Automatización rápida y potente de tareas en Android sin complicaciones.**

Una aplicación nativa para Android construida con **Kotlin** y **Jetpack Compose (Material Design 3)** que permite crear, editar, organizar y ejecutar flujos de acciones automatizadas en el dispositivo. Diseñada para ser rápida, ligera y totalmente funcional sin depender de servicios externos obligatorios ni tiendas cerradas (optimizada para distribución directa como APK o en tiendas de terceros como Uptodown).

---

## 📱 Características Principales

- ⚡ **Motor de Ejecución de Acciones**: Encadenamiento secuencial de acciones de hardware y software (Lanzador de juegos y apps, Texto a voz, Linterna, Vibración háptica con patrones avanzados, Notificaciones, Portapapeles, Delays, URLs, Búsquedas y Mensajería).
- 🏷️ **Variables Dinámicas del Sistema**: Posibilidad de insertar datos en tiempo real (`{HORA}`, `{FECHA}`, `{DIA_SEMANA}`, `{BATERIA}`, `{ESTADO_BATERIA}`, `{PORTAPAPELES}`, `{DISPOSITIVO}`) en tus mensajes de voz, notificaciones, WhatsApp y SMS con chips autocompletables para no tener que memorizar comandos.
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
| **CI / CD** | GitHub Actions (Compilación y firma automática de Debug APK) |

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
| `CAMERA` / `FLASHLIGHT` | Control del flash/linterna del dispositivo en acciones de iluminación. |
| `VIBRATE` | Retroalimentación háptica y pulsos vibratorios avanzados. |
| `POST_NOTIFICATIONS` | Emisión de notificaciones locales de sistema con estados de atajos. |
| `QUERY_ALL_PACKAGES` | Escaneo y listado de juegos/apps instaladas para la acción de inicio rápido. |
| `INTERNET` | Apertura de URLs y consultas de búsqueda web. |

---

## 📂 Estructura del Proyecto

```
├── .github/workflows/       # Workflows de CI/CD (Compilación de APK)
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Punto de entrada, launcher shortcuts y navegación Compose
│   │   │   ├── data/                  # Capa de datos (Room DB, DAOs, Entidades, Repositorio)
│   │   │   ├── engine/                # Motor de ejecución, variables dinámicas y handlers modulares
│   │   │   └── ui/                    # Capa de presentación (Pantallas, ViewModel, Theme, Componentes)
│   │   └── res/                       # Recursos XML, drawables, strings, iconos adaptativos
│   └── build.gradle.kts               # Configuración de dependencias de la app
├── metadata.json                      # Metadatos de la plataforma
└── settings.gradle.kts                # Configuración de módulos del proyecto
```

---

## 📄 Licencia

Proyecto de código abierto disponible para uso y distribución directa.
