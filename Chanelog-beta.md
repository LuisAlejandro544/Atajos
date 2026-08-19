# ⚡ Flurix Beta (Canal Beta — Versión v0.1.0 / 0.1.0-B)

> **Automatización de tareas en Android sin dependencias obligatorias ni telemetría.**
> **Versión:** `v0.1.0` (`0.1.0-B`)

Bienvenido a la versión **Beta Oficial v0.1.0** de **Flurix para Android**. Esta entrega incluye el motor completo de automatización de acciones, disparadores de hardware en segundo plano con máxima precisión (`setAlarmClock`), soporte para superposición sobre otras aplicaciones (`SYSTEM_ALERT_WINDOW`), ajustes rápidos y optimización de compilación con ProGuard/R8.

---

## 🚀 ¿Qué incluye esta versión Beta?

### 🎛️ 1. Motor de Ejecución de Acciones
- **Lanzador de Apps y Juegos:** Escaneo asíncrono en segundo plano (`Dispatchers.IO`) con selector visual y barra de búsqueda.
- **Peticiones Web y Webhooks (Obtener contenido de URL):** Métodos `GET`, `POST`, `PUT`, `DELETE` con inserción de datos dinámicos y almacenamiento de respuesta en `{RESPUESTA_WEB}` y `{HTTP_STATUS}`.
- **Apertura Directa de Cámara:** Acceso instantáneo a Foto (trasera), Selfie (frontal) o Grabación de vídeo.
- **Control de Brillo de Pantalla:** Ajuste dinámico (`+20%`, `-20%`, presets fijos y slider de porcentaje).
- **Gestión de Sonido y Volumen:** Control independiente para Multimedia, Tono de llamada, Notificaciones y Alarmas (subir, bajar, silenciar o nivel exacto).
- **Síntesis de Voz (Text-to-Speech):** Lectura inteligente de texto y notificaciones previas (`{ULTIMA_NOTIFICACION}`).
- **Vibración Háptica Avanzada:** Patrones (Ligero, Fuerte, Doble, Triple, Latido, Código SOS y duración libre en ms).
- **Control de Linterna:** Encendido, apagado o alternancia rápida.
- **Notificaciones y Portapapeles:** Creación de alertas de sistema y copiado dinámico.
- **Cancelación Interactiva en Tiempo Real:** Aborto instantáneo de atajos desde el banner superior liberando inmediatamente TTS y vibración.

---

### 🔌 2. Disparadores Nativos en Segundo Plano
- **Alimentación y Batería (`BroadcastReceivers`):**
  - Conexión a la corriente (`ACTION_POWER_CONNECTED`).
  - Desconexión del cargador (`ACTION_POWER_DISCONNECTED`).
  - Estado combinado (Ambos).
  - Batería baja crítica (<15% `ACTION_BATTERY_LOW`).
  - Batería restablecida (`ACTION_BATTERY_OKAY`).
  - Recarga completada al 100% (`BATTERY_FULL`).
- **Disparador Horario con AlarmManager de Máxima Precisión:** Ejecución a hora exacta sin retrasos mediante `setAlarmClock` (`AlarmClockInfo`), inmune a Doze Mode y optimizaciones de juegos, con reprogramación automática tras reinicio (`BootReceiver`).
- **Permiso de Superposición en Segundo Plano (`SYSTEM_ALERT_WINDOW`):** Diálogo guiado para conceder "Mostrar sobre otras aplicaciones", permitiendo que los atajos y automatizaciones abran juegos y apps en segundo plano sin bloqueos de Android.
- **Mosaico de Ajustes Rápidos (`Quick Settings Tile`):** Ejecución con 1 toque desde la cortina de notificaciones de Android.

---

### 🏷️ 3. Variables Dinámicas del Sistema
Inserta datos en tiempo real con chips visuales autocompletables:
- `{HORA}` — Hora actual en formato local.
- `{FECHA}` — Fecha formateada.
- `{DIA_SEMANA}` — Día de la semana actual.
- `{BATERIA}` — Porcentaje de batería en tiempo real.
- `{ESTADO_BATERIA}` — Estado de carga (Cargando / Descargando).
- `{PORTAPAPELES}` — Contenido actual del portapapeles.
- `{DISPOSITIVO}` — Modelo del teléfono.
- `{ULTIMA_NOTIFICACION}` / `{NOTIFICACION_TITULO}` — Contenido de notificaciones previas.
- `{RESPUESTA_WEB}` / `{HTTP_STATUS}` — Resultado de peticiones HTTP previas.

---

### 🛡️ 4. Privacidad, Rendimiento y Compilación
- **100% Offline-First:** Sin rastreo, sin telemetría y sin dependencias obligatorias de Google Play Services.
- **Persistencia Local con Room SQLite:** Todos tus atajos y registros se almacenan exclusivamente en tu dispositivo.
- **Optimizado con ProGuard / R8:** Código ofuscado y recursos reducidos para un tamaño de APK ligero y máximo rendimiento.
- **Coexistencia de Versiones:** Identificador `com.flurix.app.beta` con etiqueta *Flurix Beta* en la pantalla de inicio.

---

### 📦 Instalación
1. Descarga el archivo `Flurix-Beta-*.apk` adjunto abajo en los Assets.
2. Abre el archivo en tu teléfono Android y autoriza la instalación de fuentes desconocidas si es necesario.
3. ¡Comienza a crear y automatizar tus propios atajos!
