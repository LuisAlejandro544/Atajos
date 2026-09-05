# Directivas y Reglas para Agentes (AGENTS.md)

Este archivo define las instrucciones y restricciones persistentes para cualquier agente de inteligencia artificial que colabore en este repositorio.

---

## 🛑 Reglas Absolutas e Invariables

1. **Acción Directa sobre Conversación**:
   - No des largas explicaciones teóricas ni pidas confirmación innecesaria cuando el usuario solicite un cambio o función concreta.
   - Ejecuta directamente las herramientas (`create_file`, `edit_file`, `compile_applet`) para entregar código funcional.

2. **Perfil y Plataforma del Usuario**:
   - El usuario interactúa desde un teléfono móvil (no desde una computadora de escritorio).
   - El APK resultante está destinado a tiendas de terceros y **Uptodown**, no necesariamente a Google Play Store.

3. **Uso de Dependencias Reales**:
   - Al usuario no le preocupa el peso final del APK, siempre que las librerías sean 100% funcionales y estables.
   - **Evita** reinventar la rueda con soluciones caseras sin dependencias. Prioriza dependencias oficiales o estándar de la industria (Maven Central).

4. **Reglas para Lenguajes Nativos / Compilación**:
   - Si se incorporan lenguajes como C++, Rust, Python o Lua, la configuración de compilación correspondiente debe estar plenamente integrada en los scripts de Gradle (`build.gradle.kts`), sin omitir pasos ni colocar fallbacks ficticios si la funcionalidad requiere el lenguaje específico.

5. **Protección de Derechos de Autor**:
   - Evita explícitamente nombrar archivos, paquetes o identificadores con marcas registradas protegidas por derechos de autor que puedan comprometer al usuario.

6. **Idiomas y Archivos de Control**:
   - Toda interacción con el usuario y documentación debe ser en español.
   - Si existe un archivo `commit_message.txt`, asegúrate de que su contenido esté en español y no lo modifiques salvo petición expresa del usuario.

7. **Consistencia de la Arquitectura de Atajos**:
   - El retardo por defecto entre bloques en secuencias multi-acción se mantiene siempre en **1003 ms** (`ShortcutExecutor.STEP_DELAY_MS`), con la posibilidad de configurarse o ajustarse específicamente mediante el bloque de acción de espera (`WAIT`).
   - La lista de atajos mantiene un orden de visualización estable (no debe desplazarse hacia arriba al ser ejecutado un atajo).
   - El motor de Lua integrado debe mantenerse en el estándar original con sus bindings a las capacidades del hardware Android (`flashlight`, `copy`, `map`, `timer`, `message`, `share`, `speak`, `get_hour`, etc.).
   - La síntesis de voz (Texto a Voz / TTS) utiliza el motor configurado en el sistema operativo Android (`android.speech.tts.TextToSpeech`), disponible como bloque nativo (`SPEAK`) y como función nativa en Lua (`speak(texto)`).
   - Las herramientas de depuración para desarrollo móvil (Infinum DbInspector, Hyperion-Android, ANR-WatchDog, LeakCanary y Chucker) y la introspección con Lua Debug Library (`debug.traceback`) deben mantenerse activas y funcionales en la variante Debug, con sus respectivos scripts modulares de descarga en el flujo de GitHub Actions.
