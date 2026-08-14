# Atajos para Android ⚡

> **Automatización rápida y potente de tareas en Android sin complicaciones.**

Una aplicación nativa para Android construida con **Kotlin** y **Jetpack Compose (Material Design 3)** que permite crear, editar, organizar y ejecutar flujos de acciones automatizadas en el dispositivo. Diseñada para ser rápida, ligera y totalmente funcional sin depender de servicios externos obligatorios ni tiendas cerradas (optimizada para distribución directa como APK o en tiendas de terceros como Uptodown).

---

## 📱 Características Principales

- ⚡ **Motor de Ejecución de Acciones**: Encadenamiento secuencial de acciones de hardware y software (Texto a voz, Linterna, Vibración háptica, Notificaciones, Portapapeles, Delays, URLs, Búsquedas y Mensajería).
- 🎨 **Editor Visual de Atajos**: Añade, reordena y configura pasos de ejecución, asigna colores en degradado, nombres personalizados e iconos temáticos.
- ⏰ **Automatizaciones Programadas**: Configuración de disparadores por horario (mañana, noche) o estado de hardware.
- 📦 **Galería de Plantillas**: Atajos prediseñados listos para instalar con un toque (Modo Noche, Saludo Matutino, Modo Enfoque, etc.).
- 📜 **Historial de Ejecución**: Registro de tiempos de ejecución, duración de pasos y estado de éxito/error.
- 💾 **Persistencia Local con Room**: Base de datos SQLite local rápida y segura; todos tus atajos y configuraciones se guardan localmente en el dispositivo.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.2.x |
| **UI Framework** | Jetpack Compose con Material 3 |
| **Arquitectura** | MVVM (Model-View-ViewModel) con Clean Architecture |
| **Base de Datos** | AndroidX Room (SQLite) con Kotlin Symbol Processing (KSP) |
| **Concurrencia** | Kotlin Coroutines & Reactive Flow |
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
| `VIBRATE` | Retroalimentación háptica y pulsos vibratorios personalizados. |
| `POST_NOTIFICATIONS` | Emisión de notificaciones locales de sistema con estados de atajos. |
| `INTERNET` | Apertura de URLs y consultas de búsqueda web. |

---

## 📂 Estructura del Proyecto

```
├── .github/workflows/       # Workflows de CI/CD (Compilación de APK)
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Punto de entrada y navegación Compose
│   │   │   ├── data/                  # Capa de datos (Room DB, DAOs, Entidades, Repositorio)
│   │   │   ├── engine/                # Motor de ejecución de acciones del sistema
│   │   │   └── ui/                    # Capa de presentación (Pantallas, ViewModel, Theme, Componentes)
│   │   └── res/                       # Recursos XML, drawables, strings, iconos adaptativos
│   └── build.gradle.kts               # Configuración de dependencias de la app
├── metadata.json                      # Metadatos de la plataforma
└── settings.gradle.kts                # Configuración de módulos del proyecto
```

---

## 📄 Licencia

Proyecto de código abierto disponible para uso y distribución directa.
