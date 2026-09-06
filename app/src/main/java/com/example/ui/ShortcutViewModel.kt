package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.InstalledAppItem
import com.example.data.model.ShortcutEntity
import com.example.data.repository.ShortcutRepository
import com.example.executor.ShortcutExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BannerInfo(
    val message: String,
    val isSuccess: Boolean,
    val shortcutTitle: String,
    val currentBlockIndex: Int = 1,
    val totalBlocks: Int = 1,
    val isExecuting: Boolean = false
)

data class ShortcutUiState(
    val shortcuts: List<ShortcutEntity> = emptyList(),
    val filteredShortcuts: List<ShortcutEntity> = emptyList(),
    val selectedCategory: String = "Todos",
    val searchQuery: String = "",
    val executingShortcutId: Long? = null,
    val banner: BannerInfo? = null,
    val editingShortcut: ShortcutEntity? = null,
    val isSheetOpen: Boolean = false
)

class ShortcutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortcutRepository
    private val executor: ShortcutExecutor = ShortcutExecutor(application.applicationContext)

    private val _selectedCategory = MutableStateFlow("Todos")
    private val _searchQuery = MutableStateFlow("")
    private val _executingShortcutId = MutableStateFlow<Long?>(null)
    private val _banner = MutableStateFlow<BannerInfo?>(null)
    private val _editingShortcut = MutableStateFlow<ShortcutEntity?>(null)
    private val _isSheetOpen = MutableStateFlow(false)

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private var executionJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShortcutRepository(database.shortcutDao())
        viewModelScope.launch(Dispatchers.IO) {
            loadInstalledApps()
        }
    }

    private data class FilterState(
        val category: String,
        val query: String,
        val executingId: Long?,
        val banner: BannerInfo?
    )

    private data class SheetState(
        val editingShortcut: ShortcutEntity?,
        val isSheetOpen: Boolean
    )

    private val filterFlow = combine(
        _selectedCategory,
        _searchQuery,
        _executingShortcutId,
        _banner
    ) { category, query, executingId, banner ->
        FilterState(category, query, executingId, banner)
    }

    private val sheetFlow = combine(
        _editingShortcut,
        _isSheetOpen
    ) { editing, isOpen ->
        SheetState(editing, isOpen)
    }

    val uiState: StateFlow<ShortcutUiState> = combine(
        repository.allShortcuts,
        filterFlow,
        sheetFlow
    ) { all, filter, sheet ->
        val filtered = all.filter { shortcut ->
            val matchesCategory = when (filter.category) {
                "Todos" -> true
                "Favoritos" -> shortcut.isFavorite
                else -> shortcut.category.equals(filter.category, ignoreCase = true)
            }
            val matchesQuery = filter.query.isBlank() ||
                shortcut.title.contains(filter.query, ignoreCase = true) ||
                shortcut.description.contains(filter.query, ignoreCase = true)

            matchesCategory && matchesQuery
        }

        ShortcutUiState(
            shortcuts = all,
            filteredShortcuts = filtered,
            selectedCategory = filter.category,
            searchQuery = filter.query,
            executingShortcutId = filter.executingId,
            banner = filter.banner,
            editingShortcut = sheet.editingShortcut,
            isSheetOpen = sheet.isSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShortcutUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun executeShortcut(shortcut: ShortcutEntity) {
        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            _executingShortcutId.value = shortcut.id
            repository.incrementExecution(shortcut.id)

            val blocks = executor.resolveBlocks(shortcut)
            val totalBlocks = blocks.size
            val executedResults = mutableListOf<String>()
            var allSuccess = true

            for (index in blocks.indices) {
                val block = blocks[index]
                val currentStep = index + 1
                val isWaitBlock = block.actionType == ActionType.WAIT.name

                val stepLabel = if (block.customLabel.isNotBlank()) block.customLabel
                else ActionType.values().firstOrNull { it.name == block.actionType }?.label ?: block.actionType

                val waitDurationMs = if (isWaitBlock) {
                    block.parameter.trim().toLongOrNull()?.coerceAtLeast(0L) ?: ShortcutExecutor.STEP_DELAY_MS
                } else 0L

                _banner.value = BannerInfo(
                    message = if (isWaitBlock) "Paso $currentStep de $totalBlocks: Esperando ${waitDurationMs} ms..."
                    else "Paso $currentStep de $totalBlocks: $stepLabel",
                    isSuccess = true,
                    shortcutTitle = shortcut.title,
                    currentBlockIndex = currentStep,
                    totalBlocks = totalBlocks,
                    isExecuting = true
                )

                if (isWaitBlock) {
                    delay(waitDurationMs)
                    executedResults.add("Espera de ${waitDurationMs} ms")
                } else {
                    // Ejecutar el bloque actual
                    val result = executor.executeSingleBlock(block.actionType, block.parameter)
                    executedResults.add(result.message)
                    if (!result.success) {
                        allSuccess = false
                    }
                }

                // Pausa entre bloques:
                // Si el bloque actual no es WAIT y el siguiente tampoco es WAIT,
                // se aplica el retardo por defecto de 1 segundo con 3 milisegundos (1003 ms).
                if (index < blocks.lastIndex) {
                    val nextBlock = blocks[index + 1]
                    if (!isWaitBlock && nextBlock.actionType != ActionType.WAIT.name) {
                        delay(ShortcutExecutor.STEP_DELAY_MS)
                    }
                }
            }

            val finalMessage = if (totalBlocks > 1) {
                if (allSuccess) "¡$totalBlocks bloques completados con éxito!"
                else "Completado con advertencias: ${executedResults.lastOrNull()}"
            } else {
                executedResults.firstOrNull() ?: "Atajo ejecutado"
            }

            _banner.value = BannerInfo(
                message = finalMessage,
                isSuccess = allSuccess,
                shortcutTitle = shortcut.title,
                currentBlockIndex = totalBlocks,
                totalBlocks = totalBlocks,
                isExecuting = false
            )

            delay(350)
            _executingShortcutId.value = null

            // Auto-dismiss banner después de 3.5 segundos
            delay(3500)
            if (_banner.value?.shortcutTitle == shortcut.title && !_banner.value!!.isExecuting) {
                _banner.value = null
            }
        }
    }

    fun toggleFavorite(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(shortcut.id, !shortcut.isFavorite)
        }
    }

    fun openCreateSheet() {
        _editingShortcut.value = null
        _isSheetOpen.value = true
    }

    fun openEditSheet(shortcut: ShortcutEntity) {
        _editingShortcut.value = shortcut
        _isSheetOpen.value = true
    }

    fun closeSheet() {
        _isSheetOpen.value = false
        _editingShortcut.value = null
    }

    fun saveShortcut(
        title: String,
        description: String,
        colorHex: String,
        iconKey: String,
        actions: List<ActionBlock>,
        category: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val primaryAction = actions.firstOrNull()?.actionType ?: ActionType.OPEN_URL.name
            val primaryParam = actions.firstOrNull()?.parameter ?: ""

            val current = _editingShortcut.value
            if (current != null) {
                repository.update(
                    current.copy(
                        title = title.trim().ifEmpty { "Mi Atajo" },
                        description = description.trim().ifEmpty {
                            if (actions.size > 1) "${actions.size} acciones encadenadas" else "Acción rápida"
                        },
                        colorHex = colorHex,
                        iconKey = iconKey,
                        actionType = primaryAction,
                        actions = actions,
                        parameter = primaryParam,
                        category = category,
                        isFavorite = isFavorite,
                        isCustom = true
                    )
                )
            } else {
                repository.insert(
                    ShortcutEntity(
                        title = title.trim().ifEmpty { "Nuevo Atajo" },
                        description = description.trim().ifEmpty {
                            if (actions.size > 1) "${actions.size} acciones encadenadas" else "Acción rápida"
                        },
                        colorHex = colorHex,
                        iconKey = iconKey,
                        actionType = primaryAction,
                        actions = actions,
                        parameter = primaryParam,
                        category = category,
                        isFavorite = isFavorite,
                        isCustom = true
                    )
                )
            }
            closeSheet()
        }
    }

    fun deleteShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.delete(shortcut)
            if (_editingShortcut.value?.id == shortcut.id) {
                closeSheet()
            }
        }
    }

    fun dismissBanner() {
        _banner.value = null
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            val defaults = listOf(
                ShortcutEntity(
                    title = "Linterna Rápida",
                    description = "Flash y registro horario",
                    colorHex = "#FF9500",
                    iconKey = "FLASH",
                    actionType = ActionType.FLASHLIGHT.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.FLASHLIGHT.name, parameter = "", customLabel = "Encender/Apagar Flash"),
                        ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "Linterna activada", customLabel = "Registrar estado")
                    ),
                    parameter = "",
                    isFavorite = true,
                    category = "Utilidades"
                ),
                ShortcutEntity(
                    title = "Ruta a Casa",
                    description = "Aviso y navegación en Maps",
                    colorHex = "#007AFF",
                    iconKey = "MAP",
                    actionType = ActionType.MAP_NAV.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "¡Voy en camino a casa!", customLabel = "Copiar aviso"),
                        ActionBlock(actionType = ActionType.MAP_NAV.name, parameter = "Casa", customLabel = "Abrir Maps hacia Casa")
                    ),
                    parameter = "Casa",
                    isFavorite = true,
                    category = "Viajes"
                ),
                ShortcutEntity(
                    title = "Temporizador 5m",
                    description = "Temporizador y volumen",
                    colorHex = "#FF2D55",
                    iconKey = "TIMER",
                    actionType = ActionType.SET_TIMER.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.SET_TIMER.name, parameter = "5", customLabel = "Iniciar 5 minutos"),
                        ActionBlock(actionType = ActionType.SOUND_SETTINGS.name, parameter = "", customLabel = "Verificar sonido")
                    ),
                    parameter = "5",
                    isFavorite = true,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Copiar Mi Correo",
                    description = "Portapapeles y compartir",
                    colorHex = "#34C759",
                    iconKey = "COPY",
                    actionType = ActionType.COPY_TEXT.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "contacto@ejemplo.com", customLabel = "Copiar correo"),
                        ActionBlock(actionType = ActionType.SHARE_TEXT.name, parameter = "contacto@ejemplo.com", customLabel = "Compartir correo")
                    ),
                    parameter = "contacto@ejemplo.com",
                    isFavorite = false,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Buscar en Web",
                    description = "Abre Google en navegador",
                    colorHex = "#5856D6",
                    iconKey = "WEB",
                    actionType = ActionType.OPEN_URL.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.OPEN_URL.name, parameter = "https://www.google.com", customLabel = "Abrir Google")
                    ),
                    parameter = "https://www.google.com",
                    isFavorite = false,
                    category = "Navegación"
                ),
                ShortcutEntity(
                    title = "Ajustar Volumen",
                    description = "Fija volumen multimedia al 70%",
                    colorHex = "#34C759",
                    iconKey = "VOLUME",
                    actionType = ActionType.SET_VOLUME.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.SET_VOLUME.name, parameter = "70", customLabel = "Volumen al 70%")
                    ),
                    parameter = "70",
                    isFavorite = false,
                    category = "Utilidades"
                ),
                ShortcutEntity(
                    title = "Mensaje Rápido",
                    description = "Copia y abre envío",
                    colorHex = "#00C7BE",
                    iconKey = "MESSAGE",
                    actionType = ActionType.SEND_MESSAGE.name,
                    actions = listOf(
                        ActionBlock(actionType = ActionType.COPY_TEXT.name, parameter = "¡Llego en 5 minutos!", customLabel = "Copiar mensaje"),
                        ActionBlock(actionType = ActionType.SEND_MESSAGE.name, parameter = "¡Llego en 5 minutos!", customLabel = "Enviar mensaje")
                    ),
                    parameter = "¡Llego en 5 minutos!",
                    isFavorite = false,
                    category = "Comunicación"
                ),
                ShortcutEntity(
                    title = "Script Inteligente Lua",
                    description = "Lógica y condición horaria",
                    colorHex = "#5856D6",
                    iconKey = "CODE",
                    actionType = ActionType.LUA_SCRIPT.name,
                    actions = listOf(
                        ActionBlock(
                            actionType = ActionType.LUA_SCRIPT.name,
                            parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
                            customLabel = "Ejecutar lógica condicional Lua"
                        )
                    ),
                    parameter = "local hora = get_hour()\nif hora >= 19 or hora < 7 then\n  flashlight()\n  return 'Noche (hora ' .. hora .. '): linterna'\nelse\n  copy('¡Buen día desde script Lua!')\n  return 'Día (hora ' .. hora .. '): saludo copiado'\nend",
                    isFavorite = true,
                    category = "Productividad"
                ),
                ShortcutEntity(
                    title = "Aviso de Voz",
                    description = "Lee un mensaje con la voz del sistema",
                    colorHex = "#FF2D55",
                    iconKey = "SPEAK",
                    actionType = ActionType.SPEAK.name,
                    parameter = "Atajo ejecutado a las {hora}. Batería al {bateria} por ciento.",
                    isFavorite = true,
                    category = "Utilidades"
                ),
                ShortcutEntity(
                    title = "Notificación de Estado",
                    description = "Aviso prioritario con hora y batería",
                    colorHex = "#FF9500",
                    iconKey = "NOTIFICATION",
                    actionType = ActionType.NOTIFICATION.name,
                    actions = listOf(
                        ActionBlock(
                            actionType = ActionType.NOTIFICATION.name,
                            parameter = "sound:pop|¡Atención! Son las {hora} ({dia}) y tu batería está al {bateria}%.",
                            customLabel = "Lanzar Notificación con Pop y Variables"
                        ),
                        ActionBlock(
                            actionType = ActionType.SPEAK.name,
                            parameter = "Aviso recibido a las {hora}",
                            customLabel = "Confirmación por voz"
                        )
                    ),
                    parameter = "sound:pop|¡Atención! Son las {hora} ({dia}) y tu batería está al {bateria}%.",
                    isFavorite = true,
                    category = "Utilidades"
                ),
                ShortcutEntity(
                    title = "Brillo Óptimo",
                    description = "Fija el brillo de pantalla al 80%",
                    colorHex = "#007AFF",
                    iconKey = "BRIGHTNESS",
                    actionType = ActionType.SET_BRIGHTNESS.name,
                    actions = listOf(
                        ActionBlock(
                            actionType = ActionType.SET_BRIGHTNESS.name,
                            parameter = "80",
                            customLabel = "Ajustar brillo al 80%"
                        )
                    ),
                    parameter = "80",
                    isFavorite = false,
                    category = "Ajustes"
                )
            )
            repository.resetDefaults(defaults)
        }
    }

    private suspend fun loadInstalledApps() {
        withContext(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val launcherApps = pm.queryIntentActivities(mainIntent, 0)
                val list = launcherApps.mapNotNull { resolveInfo ->
                    try {
                        val pkg = resolveInfo.activityInfo.packageName
                        val label = resolveInfo.loadLabel(pm).toString().trim()
                        if (label.isNotBlank()) InstalledAppItem(name = label, packageName = pkg) else null
                    } catch (_: Exception) {
                        null
                    }
                }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }
                _installedApps.value = list
            } catch (_: Exception) {
                _installedApps.value = emptyList()
            }
        }
    }
}
