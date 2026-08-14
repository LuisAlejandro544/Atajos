# 🗺️ Roadmap del Proyecto — Atajos para Android

Plan de evolución técnica y funcional para el desarrollo progresivo de la aplicación.

---

## 📍 Fase 1: Fundamentos y MVP Funcional (✅ Completada)
- [x] Arquitectura base MVVM con Jetpack Compose y Material 3.
- [x] Motor de ejecución modular (`ActionExecutor` con patrón Strategy y handlers dedicados).
- [x] Acciones nativas implementadas:
  - **Lanzador de Juegos y Apps**: Escaneo asíncrono (`Dispatchers.IO`) con estados de carga y selector visual.
  - **Vibración Háptica Avanzada**: Patrones enriquecidos (Ligero, Fuerte, Doble, Triple, Latido, SOS y duración libre).
  - **Variables Dinámicas del Sistema**: Interpolación en vivo de `{HORA}`, `{FECHA}`, `{DIA_SEMANA}`, `{BATERIA}`, `{ESTADO_BATERIA}`, `{PORTAPAPELES}`, `{DISPOSITIVO}` en Texto a Voz, Notificaciones, SMS, WhatsApp y Compartir.
  - **Selector Visual de Variables**: Chips interactivos `VariablePickerChips` para insertar datos sin memorizar sintaxis.
  - **App Shortcuts en el Icono**: Accesos directos dinámicos en el launcher del dispositivo sincronizados automáticamente con los atajos favoritos.
  - Síntesis de voz (Text-to-Speech).
  - Control de linterna (Toggle, Encender, Apagar).
  - Notificaciones de sistema con canal dedicado y verificación dinámica de permisos (`POST_NOTIFICATIONS`).
  - Copiado al portapapeles.
  - Delays/Pausas controladas.
  - Intents para compartir texto, enviar WhatsApp/SMS y búsqueda web.
- [x] Solicitud dinámica de permisos en tiempo de ejecución al iniciar la app con banner configurable.
- [x] Editor visual de atajos modularizado con selección de iconos, colores en degradado y reordenación de pasos.
- [x] Persistencia local completa con Room SQLite (Atajos, Automatizaciones, Logs de historial).
- [x] Catálogo de plantillas preconfiguradas con instalación en 1 clic (incluyendo Modo Gamer y Reporte Dinámico de Estado).
- [x] Historial de ejecuciones con tiempos y estados.
- [x] Pipeline de CI/CD en GitHub Actions para compilación y firma automática de APK Debug.

---

## 📍 Fase 2: Automatizaciones en Segundo Plano y Sensores (En Progreso / Próxima)
- [ ] **Background Execution con Android WorkManager**:
  - Ejecución de atajos programados incluso con la app cerrada.
  - Notificaciones de progreso para atajos de larga duración.
- [ ] **Disparadores de Sistema (BroadcastReceivers nativos)**:
  - Detección real de conexión/desconexión del cargador (`ACTION_POWER_CONNECTED`).
  - Detección de batería baja (`ACTION_BATTERY_LOW`) y nivel porcentual configurable.
  - Detección de cambios de red (Conexión/Desconexión de WiFi).
  - Modo No Molestar automático.

---

## 📍 Fase 3: Widgets de Pantalla de Inicio y Conectividad
- [ ] **App Widgets con Jetpack Glance**:
  - Botones rápidos en la pantalla de inicio de Android para ejecutar atajos favoritos sin abrir la app.
- [ ] **Acciones de Red y Webhooks**:
  - Realizar peticiones HTTP GET / POST personalizadas (llamadas a APIs REST, IFTTT, Home Assistant, Node-RED).
  - Parseo básico de respuestas JSON para encadenar en siguientes pasos.
- [ ] **Integración de IA Local/Gemini**:
  - Acción de resumen de texto o generación de respuestas con IA.

---

## 📍 Fase 4: Portabilidad y Distribución
- [ ] **Importación y Exportación**:
  - Exportar atajos en formato JSON para respaldos locales.
  - Compartir atajos mediante enlaces o códigos QR.
- [ ] **Distribución en Tiendas Alternativas y Terceros**:
  - Publicación y empaquetado optimizado para **Uptodown**, **F-Droid** y descarga directa de APK.
  - Verificación de firmas para actualizaciones seguras fuera de Google Play.
