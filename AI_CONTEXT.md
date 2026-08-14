# 🧠 AI Context & Development Knowledge Base

Este documento proporciona contexto técnico estructurado y directo para modelos de lenguaje (LLMs) y agentes de codificación que trabajen en este repositorio.

---

## 📌 Resumen del Proyecto

- **Nombre**: Atajos (Shortcuts) para Android
- **Propósito**: Automatización local y secuencial de tareas en el dispositivo Android mediante disparadores y acciones encadenadas.
- **Enfoque de Distribución**: Sin ataduras a Google Play. Diseñado para funcionar de manera independiente, compatible con distribución directa (APK sideloading) o tiendas de terceros (Uptodown, F-Droid).
- **Paradigma**: 100% Kotlin nativo con Jetpack Compose y Room SQLite.

---

## 🔑 Entidades Clave del Modelo de Datos

1. **`ShortcutEntity`** (`data/model/ShortcutEntity.kt`):
   - Representa un atajo configurable.
   - Campos: `id`, `name`, `description`, `iconName`, `colorHex`, `category`, `actionsJson` (lista serializada de `ShortcutAction`), `isFavorite`, `executionCount`, `lastRunTimestamp`.

2. **`ShortcutAction`** (`data/model/ActionType.kt`):
   - Paso individual dentro de un atajo.
   - Campos: `id`, `type` (`ActionType`), `param1`, `param2`, `orderIndex`.

3. **`ActionType`** (`data/model/ActionType.kt`):
   - Tipos soportados:
     - `SPEAK_TEXT`: Síntesis de voz. `param1`: texto a hablar.
     - `TOGGLE_FLASHLIGHT`: Control de linterna. `param1`: "toggle" / "on" / "off".
     - `VIBRATE`: Vibración háptica. `param1`: "short" / "double" / "sos" / "custom_ms".
     - `SHOW_NOTIFICATION`: Notificación local. `param1`: título, `param2`: mensaje.
     - `COPY_CLIPBOARD`: Copiar al portapapeles. `param1`: texto.
     - `WAIT_DELAY`: Pausa en milisegundos. `param1`: duración en ms (ej: "1500").
     - `OPEN_URL`: Abrir enlace web en navegador. `param1`: URL.
     - `SEARCH_WEB`: Búsqueda en Google. `param1`: término de búsqueda.
     - `SHARE_TEXT`: Compartir texto mediante Intent chooser. `param1`: contenido.
     - `SEND_WHATSAPP`: Enviar mensaje por WhatsApp. `param1`: teléfono, `param2`: mensaje.
     - `SEND_SMS`: Enviar mensaje por SMS vía Intent. `param1`: teléfono, `param2`: mensaje.
     - `SET_TIMER`: Iniciar temporizador de alarma. `param1`: segundos.
     - `QUICK_CALC`: Evaluación de expresión matemática simple. `param1`: operación.

4. **`AutomationEntity`** (`data/model/AutomationEntity.kt`):
   - Disparador de automatización. Campos: `id`, `name`, `triggerType` (HORA, BATERIA, CARGADOR), `triggerCondition`, `shortcutId`, `isEnabled`.

5. **`ExecutionLogEntity`** (`data/model/ExecutionLogEntity.kt`):
   - Registro histórico de ejecuciones. Campos: `id`, `shortcutId`, `shortcutName`, `timestamp`, `durationMs`, `success`, `errorMessage`.

---

## ⚙️ Reglas de Implementación para Nuevas Acciones

Cuando se añada una nueva acción al sistema:
1. **Definir el enum**: Añadir el nuevo valor a `ActionType` en `data/model/ActionType.kt` con su título, descripción, icono y parámetros esperados.
2. **Implementar en el motor**: Añadir la rama correspondiente en `ActionExecutor.executeAction()` en `engine/ActionExecutor.kt`.
3. **Editor UI**: Si requiere campos de configuración especializados, actualizar `ShortcutEditorScreen.kt` en la sección de edición de pasos.
4. **Verificación**: Comprobar que no rompa la serialización JSON ni cause excepciones no controladas.

---

## 🚫 Restricciones Críticas
- **No inventar dependencias**: Utilizar las dependencias ya declaradas en `gradle/libs.versions.toml`.
- **Compatibilidad Offline**: Las acciones principales deben funcionar sin requerir conexión a internet obligatoria.
- **Respetar el orden en UI**: Los atajos se ordenan de manera fija y predecible en la vista principal (`ORDER BY id ASC`).
