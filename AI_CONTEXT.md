# Contexto del Proyecto para Modelos de IA (AI Context)

Este documento resume el contexto de negocio, arquitectura de software, restricciones del entorno y decisiones técnicas clave para cualquier modelo o asistente de IA que trabaje en esta base de código.

---

## 🎯 Propósito del Proyecto
Construir una aplicación nativa para Android en Kotlin y Jetpack Compose que emula la versatilidad de la app de atajos de iOS, permitiendo a los usuarios ejecutar secuencias de tareas automatizadas en su dispositivo mediante bloques gráficos o scripts en **Lua 5.4.7 puro** (compilado en C nativo mediante Android NDK).

---

## 📱 Contexto de Usuario y Entorno
- **Perfil de Usuario**:
  - Opera y prueba principalmente desde un teléfono móvil.
  - El APK final se distribuirá a través de **Uptodown** o descarga directa de terceros (no depende de las políticas restrictivas de Google Play Store).
  - Prioriza funcionalidad real y dependencias 100% estables sobre la optimización extrema del peso del APK.
- **Entorno de Ejecución del Agente**:
  - Contenedor Linux headless sin emulador local ni ADB conectado.
  - Verificación mediante `compile_applet` y pruebas unitarias JVM en `./gradlew :app:testDebugUnitTest`.
  - Sin herramientas como `local.properties`; los secretos y configuraciones se manejan por variables de entorno y `BuildConfig`.

---

## ⚙️ Decisiones Técnicas Críticas

1. **Retardo entre Bloques (1003 ms por defecto y bloque WAIT personalizado)**:
   - La ejecución secuencial de bloques respeta por defecto `ShortcutExecutor.STEP_DELAY_MS = 1003L`.
   - Se incluye el bloque de acción `WAIT` ("Esperar (Pausa)") que permite a los usuarios personalizar o alterar este retardo según las necesidades de cada atajo.
   - Justificación: Evita que el `ActivityTaskManager` de Android descarte Intents consecutivos o cause parpadeos de ventana.

2. **Orden Estable de Atajos en Pantalla**:
   - La consulta de Room ordena los atajos por `isFavorite DESC, id ASC`, garantizando que la posición de cada atajo se mantenga fija y no se altere bruscamente hacia arriba al ser ejecutado.

3. **Motor de Lua 5.4.7 Puro (Nativo en C)**:
   - Compilación e integración real con **Android NDK 27** y **CMake 3.22.1** (`app/src/main/cpp/CMakeLists.txt` y `app/src/main/cpp/lua/`).
   - Sin emuladores, intérpretes en Java ni wrappers intermediarios. El código C oficial de Lua 5.4.7 compila directamente a librerías compartidas (`libnative-lua.so`) para `arm64-v8a`, `armeabi-v7a`, `x86` y `x86_64`.
   - Puente JNI bidireccional (`native-lua.cpp` <-> `LuaShortcutEngine.kt`):
     - Expone a los scripts Lua el control de hardware y APIs del sistema (`flashlight`, `copy`, `open_url`, `map`, `timer`, `message`, `sound_settings`, `share`, `speak`, `get_hour`, `print`).
     - Respeta la sintaxis nativa de Lua 5.4 (variables locales `<const>`, variables `<close>`, operadores bitwise, recolección generacional).
     - Los mensajes y salidas impresas se capturan y devuelven hacia la UI de Compose en el banner de estado.

4. **Síntesis de Voz Nativa del Sistema (Text-to-Speech)**:
   - Implementada con `android.speech.tts.TextToSpeech`, aprovechando los paquetes de idiomas y motores de voz que el usuario ya tenga configurados en su dispositivo Android (Google TTS, Samsung TTS, etc.).
   - Disponible como bloque `SPEAK` ("Texto a Voz") y expuesta en Lua mediante `speak("texto")`.

5. **Herramientas de Depuración Integradas (Mobile-First Debugging)**:
   - **LeakCanary (2.14)**: Detección automática de fugas de memoria, generando su propia aplicación e interfaz "Leaks" en el launcher del dispositivo.
   - **Chucker (4.1.0)**: Auditor e inspector de red con interfaz móvil propia y acceso rápido desde el menú contextual superior de la app.
   - **Lua Debug Library (5.4.7)**: Manejador nativo con `luaL_traceback` en C++ (`native-lua.cpp`) para diagnóstico exacto de archivos y líneas en scripts erróneos.
   - Scripts modulares de preparación (`scripts/setup_leakcanary.sh`, `scripts/setup_chucker.sh`, `scripts/setup_lua_debug.sh`) integrados en el pipeline CI/CD.

6. **Almacenamiento Local con Room Database**:
   - Tablas `shortcuts` persistidas localmente con `TypeConverters` para listas JSON de `ActionBlock`.
   - Migraciones con `fallbackToDestructiveMigration()` para simplificar pruebas y despliegues rápidos en desarrollo.

7. **Reglas de Buenas Prácticas y CI/CD**:
   - Compilación de APK Debug configurada para disparo manual (`workflow_dispatch`).
   - Automatización de mensajes de commit controlada vía `override_commit_message.yml` y `commit_message.txt`.
   - No hardcodear API keys o cadenas sensibles en archivos fuente.
   - No utilizar nombres protegidos por derechos de autor que expongan al usuario a riesgos legales.
   - Evitar soluciones sin dependencias si existen librerías estándar maduras que resuelvan el problema de forma robusta.
   - Si se genera o modifica `commit_message.txt`, su contenido debe estar redactado en español.
