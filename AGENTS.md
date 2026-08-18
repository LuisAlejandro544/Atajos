# 🤖 Guía de Operación para Agentes de IA — Proyecto Flurix

Este archivo contiene las directivas, roles y protocolos de razonamiento que cualquier agente de IA o asistente de desarrollo debe seguir al modificar o expandir esta base de código.

---

## 🎯 Directrices Fundamentales

1. **Razonamiento Previo Obligatorio**:
   - Antes de escribir o modificar código, analiza la causa raíz del problema o los requisitos de la nueva funcionalidad.
   - Identifica qué herramientas y archivos específicos se van a utilizar.
   - No apliques cambios a ciegas.

2. **Distribución Independiente (No Google Play)**:
   - La aplicación está orientada a **distribución directa mediante APK y tiendas de terceros (como Uptodown, F-Droid o APKPure)**.
   - No asumas la presencia de Google Play Services como dependencia estricta; prioriza APIs nativas de Android y manejo seguro de fallbacks.

3. **Inmutabilidad de lo Funcional**:
   - No reviertas cambios previamente aprobados por el usuario.
   - Mantén la persistencia con Room y el flujo reactivo con `StateFlow` y `Flow`.

4. **Esquema de Canales, Prefijos y Package IDs**:
   - **`-E` (Estable)**: `com.flurix.app` — Etiqueta en Launcher: `Flurix`. Versiones verificadas para producción y uso diario.
   - **`-DEV` / `-D` (Desarrollo)**: `com.flurix.app.dev` — Etiqueta en Launcher: `Flurix Dev`. Funciones puras en desarrollo con cambios volátiles.
   - **`-B` / `-BETA` (Beta / Testing)**: `com.flurix.app.beta` — Etiqueta en Launcher: `Flurix Beta`. Funciones pre-estable en validación.
   - **`-CANARY` / `-CREATOR`**: `com.flurix.app.canary` — Etiqueta en Launcher: `Flurix Canary`. Compilaciones de vanguardia para el creador.

---

## 🎭 Roles y Fases del Ciclo de Desarrollo

### 1. 🏛️ El Arquitecto (Diseño y Planificación)
- Evalúa el impacto de nuevos módulos o dependencias antes de introducirlos.
- Mantén la separación estricta de capas: `data/` (Room y Repositorio), `engine/` (Ejecución de hardware), `ui/` (Compose y ViewModel).

### 2. 🔨 El Constructor (Generación de Código)
- Escribe código Kotlin limpio, modular y con manejo exhaustivo de excepciones (`try-catch` en llamadas al sistema).
- Utiliza Jetpack Compose con Material Design 3 y `Modifier.testTag` en componentes interactivos clave.

### 3. 🔍 El Detective (Debugging y Resolución de Errores)
- Sigue el razonamiento metódico:
  1. Formular hipótesis inicial.
  2. Análisis línea por línea.
  3. Identificar la causa raíz con precisión.
  4. Aplicar solución focalizada sin efectos secundarios.

### 4. 🧐 El Crítico (Revisión de Código)
- Verifica que no existan memory leaks (por ejemplo, liberando adecuadamente recursos como `TextToSpeech` en `onCleared()` o `onDestroy()`).
- Asegura que los textos sean legibles y accesibles con contraste adecuado.

### 5. ⚡ El Optimizador (Refactoring)
- Mejora la legibilidad y rendimiento sin alterar el comportamiento observable del usuario.
- Evita recomposiciones innecesarias en Compose mediante `key`, `remember` y `derivedStateOf`.

### 6. 🛡️ El Escudo (Testing y Verificación)
- Ejecuta compilaciones (`compile_applet`) y tests unitarios locales con Robolectric (`gradle :app:testDebugUnitTest`) tras cambios estructurales.
- Utiliza la GitHub Action de simulación de dispositivo (`android-device-simulation.yml`) para verificar la respuesta del sistema ante eventos de hardware (energía, batería) en un entorno emulado real sin crashes.

### 7. 📖 El Narrador (Documentación)
- Mantén actualizados los archivos `README.md`, `ROADMAP.md`, `STRUCTURE.md`, `AI_CONTEXT.md` y este archivo `AGENTS.md` cuando la arquitectura o funcionalidades clave evolucionen.
