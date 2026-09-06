# Roadmap del Proyecto Atajos Android

Plan de evolución técnica y funcional para la aplicación de atajos y automatización con scripting nativo en Lua 5.4.7.

---

## 📍 Fase 1: Fundamentos y Validación (Completada ✅)
- [x] Modelo de datos reactivo con Room Database.
- [x] Ejecución de atajos individuales (Linterna, Navegación, Portapapeles, URL, Temporizador, Mensaje, Ajustes de Audio, Compartir).
- [x] Interfaz gráfica fluida estilo tarjetas táctiles y banner flotante de notificación.
- [x] Filtrado por categorías y búsqueda por texto.

## 📍 Fase 2: Secuencias y Scripting Nativo Puro (Completada ✅)
- [x] Soporte para múltiples bloques (`ActionBlock`) por atajo.
- [x] Temporización fija y calibrada de `1003 ms` entre pasos consecutivos por defecto.
- [x] Nuevo bloque de acción configurable **"Esperar (Pausa)" (`WAIT`)** para personalizar los retardos entre bloques si un atajo específico lo requiere.
- [x] Ordenamiento de atajos estable en la interfaz (los atajos ya no se mueven hacia arriba al ejecutarse).
- [x] Integración de **Lua 5.4.7 puro** en código C oficial compilado con Android NDK 27 y CMake 3.22.1 (`libnative-lua.so`), sin intérpretes ni wrappers en Java.
- [x] Soporte para características avanzadas de Lua 5.4 (`<const>`, `<close>`, enteros nativos de 64 bits, recolección generacional).
- [x] Puente JNI de APIs de Android expuestas a Lua (`open_app`, `set_volume`, `flashlight`, `copy`, `open_url`, `map`, `timer`, `message`, `sound_settings`, `share`, `speak`, `get_hour`, `print`).
- [x] Bloque de acción **"Abrir Aplicación" (`OPEN_APP`)**: selector visual de aplicaciones instaladas en el dispositivo con buscador en vivo y resolución de actividades de lanzamiento.
- [x] Bloque de acción **"Ajustar Volumen" (`SET_VOLUME`)**: control deslizable táctil (*Slider*) con escala porcentual (0-100%), feedback dinámico y accesos rápidos ergonómicos para teléfonos móviles.
- [x] Bloque de acción **"Abrir Sitio Web" (`OPEN_URL`)**: apertura directa de enlaces en el navegador predeterminado.
- [x] Motor de síntesis de voz (Texto a Voz / TTS) nativo del sistema operativo Android integrado como bloque visual (`SPEAK`) y como función nativa en Lua (`speak(texto)`).
- [x] Bloque de acción **"Mostrar Notificación" (`NOTIFICATION`)**: notificaciones nativas de alta prioridad con bypass de modo No Molestar (`setBypassDnd(true)`).
- [x] Selector de **Sonido de Notificación**: opción entre el sonido original del dispositivo o el sonido 'Pop Notification' (CC0 de GabrielAraujo), preescucha interactiva en tiempo real y script de conversión automatizado a Ogg Vorbis sin delay (`convert_audio.sh`).
- [x] Soporte de **Variables Dinámicas en Tiempo Real** (`VariableResolver`) para `{hora}`, `{hora_segundos}`, `{fecha}`, `{dia}`, `{bateria}` y `{portapapeles}` en notificaciones y texto a voz con chips de inserción rápida en el editor.
- [x] Bloque de acción **"Brillo de Pantalla" (`SET_BRIGHTNESS`)**: control táctil con slider manual (0-100%), visualización en tiempo real y presets rápidos.
- [x] Conmutador directo de favoritos en tarjetas (1-click) sin requerir apertura de menús secundarios.
- [x] Persistencia garantizada de atajos personalizados creados por el usuario con campo `isCustom` en Room Database.
- [x] Bloque de acción **"Interacción con el Usuario" (`USER_INTERACTION`)**: pausar la ejecución de atajos para solicitar confirmación del usuario antes de continuar ("Preguntar antes de continuar"). Dispone de 2 diseños ergonómicos seleccionables:
  * **Modal emergente (`UserPromptDialog`)**: ventana en pantalla con logotipo, título y color temático del atajo, mensaje personalizable, campo de validación de palabra clave (ej. "Si", "No") y 2 botones de acción configurables.
  * **Notificación interactiva (`UserInteractionNotificationHelper`)**: notificación en la barra del sistema con acción de respuesta directa para ingresar la palabra clave requerida o continuar.
- [x] App complementaria independiente de **"Telemetría y Depuración" (`DebugActivity`)**:
  * Integrada dentro del mismo APK con icono y lanzador propio en el sistema operativo ("Telemetría Atajos") al estilo de LeakCanary.
  * Historial detallado de ejecuciones con desglose de duración paso a paso, tasa global de éxito y verificación de la cadencia fija de 1003 ms.
  * Accesos rápidos directos a los inspectores del sistema (Chucker para tráfico de red HTTP y DbInspector para SQLite/Room).
- [x] Modularización arquitectónica completa: desacoplamiento de `ShortcutEditSheet`, `ShortcutViewModel` y `ShortcutExecutor` en componentes modulares y manejadores de dominio especializados (`com.example.executor.handlers` y `com.example.ui.components.editors`).
- [x] Herramientas de depuración móvil integradas:
  * **Infinum DbInspector (6.0.0)**: explorador visual y editor interactivo de archivos de base de datos `.db` (Room/SQLite) en pantalla.
  * **Hyperion-Android (0.9.38)**: cajón lateral de depuración accesible por gesto táctil con módulos de medición de vistas y reporte de fallos.
  * **ANR-WatchDog (1.4.0)**: monitorización continua del UI Thread para capturar y registrar bloqueos antes de que se produzca un ANR.
  * **LeakCanary (2.14)**: app 'Leaks' para análisis autónomo de memoria y fugas en el dispositivo.
  * **Chucker (4.1.0)**: inspector de tráfico de red HTTP en pantalla y notificaciones.
  * **Lua Debug Library (5.4.7)**: introspección nativa en C++ con `debug.traceback`.
- [x] Scripts modulares de preparación (`setup_dbinspector.sh`, `setup_hyperion.sh`, `setup_anr_watchdog.sh`, `setup_leakcanary.sh`, `setup_chucker.sh`, `setup_lua_debug.sh`) integrados en el pipeline CI/CD de GitHub Actions.
- [x] Interfaz Ultra HD e inspiración iOS: gradientes multicapa, sombras volumétricas con tinte ambiental, bordes con brillo especular y badges con Glassmorphism.
- [x] Rediseño ergonómico de bloques de acción: tarjetas con acento cromático temático y selector desplegable compacto tipo píldora (Dropdown Picker) sustituyendo el carrusel horizontal.
- [x] Editor de código multi-línea con fuente monoespaciada para scripts en el modal de edición.
- [x] Integración de **Síntesis Neuronal Offline Piper TTS (VITS)**:
  * Inferencia local acelerada en CPU con **ONNX Runtime** (`onnxruntime-android`).
  * Modelo en español empaquetado ('es_ES-carlfm-x_low' a 16 kHz) y voces descargables ('davefx' y 'sharvard' a 22.05 kHz).
  * Diálogo de configuración global `TtsEngineSettingsDialog` con preescucha en vivo.
  * Selector por bloque de acción `SpeakBlockEditor` (Predeterminado, Piper, eSpeak, Sistema).
  * Nuevas funciones nativas de Lua: `speak(texto, motor, voz)` y `set_tts_engine(motor)`.
  * Banco de diagnóstico y telemetría de latencia en `DebugActivity`.
  * Script automatizado de verificación `scripts/tts/setup_piper.sh`.
- [x] Suite de pruebas unitarias locales para ejecución secuencial y modelos de datos.
- [x] Flujos CI/CD en GitHub Actions: compilación manual de APK Debug (`workflow_dispatch`) y reescritura de mensaje de commit desde `commit_message.txt`.

## 📍 Fase 3: Disparadores y Automatización en Segundo Plano (Próxima ⏳)
- [ ] **Disparadores por Evento (Triggers)**:
  - Al conectarse a una red Wi-Fi específica.
  - Al enchufar o desconectar el cargador / nivel de batería.
  - Horarios fijos programados mediante `WorkManager` o `AlarmManager`.
- [ ] **Manejo de Variables Globales y Estado en Lua**:
  - Persistencia de variables entre distintas ejecuciones de scripts (tabla `storage.set(k, v)` y `storage.get(k)`).
- [ ] **Acciones de Red HTTP en Lua**:
  - Función `http_get(url)` y `http_post(url, body)` para interactuar con APIs REST o webhooks (ej. Home Assistant, Discord, IFTTT).

## 📍 Fase 4: Exportación, Importación y Ecosistema (Mediano Plazo 📅)
- [ ] Exportar e importar atajos individuales y colecciones en formato `.json` o `.lua`.
- [ ] Generación de accesos directos dinámicos en la pantalla de inicio de Android (`ShortcutManager` / App Shortcuts).
- [ ] Modo de prueba rápida / consola interactiva de Lua con visualizador de variables en tiempo real.
- [ ] Paquete de distribución optimizado para tiendas de terceros (Uptodown, descarga directa de APK).
