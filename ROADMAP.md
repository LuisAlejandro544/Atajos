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
- [x] Puente JNI de APIs de Android expuestas a Lua (`flashlight`, `copy`, `open_url`, `map`, `timer`, `message`, `sound_settings`, `share`, `speak`, `get_hour`, `print`).
- [x] Motor de síntesis de voz (Texto a Voz / TTS) nativo del sistema operativo Android integrado como bloque visual (`SPEAK`) y como función nativa en Lua (`speak(texto)`).
- [x] Herramientas de depuración móvil integradas: LeakCanary 2.14 (app 'Leaks' para análisis de memoria), Chucker 4.1.0 (inspector de red HTTP en pantalla) y Lua Debug Library 5.4.7 (`debug.traceback`).
- [x] Scripts modulares de preparación (`setup_leakcanary.sh`, `setup_chucker.sh`, `setup_lua_debug.sh`) integrados en el pipeline CI/CD de GitHub Actions.
- [x] Interfaz Ultra HD e inspiración iOS: gradientes multicapa, sombras volumétricas con tinte ambiental, bordes con brillo especular y badges con Glassmorphism.
- [x] Rediseño ergonómico de bloques de acción: tarjetas con acento cromático temático y selector desplegable compacto tipo píldora (Dropdown Picker) sustituyendo el carrusel horizontal.
- [x] Editor de código multi-línea con fuente monoespaciada para scripts en el modal de edición.
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
