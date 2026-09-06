package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultShortcuts
import com.example.data.db.AppDatabase
import com.example.data.model.ActionBlock
import com.example.data.model.ActionType
import com.example.data.model.InstalledAppItem
import com.example.data.model.ShortcutEntity
import com.example.data.model.UserInteractionConfig
import com.example.data.repository.InstalledAppsRepository
import com.example.data.repository.ShortcutRepository
import com.example.debug.telemetry.ExecutionTelemetry
import com.example.debug.telemetry.StepTelemetry
import com.example.debug.telemetry.TelemetryManager
import com.example.executor.ShortcutExecutor
import com.example.executor.handlers.UserInteractionNotificationHelper
import kotlinx.coroutines.CompletableDeferred
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

data class BannerInfo(
    val message: String,
    val isSuccess: Boolean,
    val shortcutTitle: String,
    val currentBlockIndex: Int = 1,
    val totalBlocks: Int = 1,
    val isExecuting: Boolean = false
)

data class UserPromptState(
    val shortcutTitle: String,
    val iconKey: String,
    val colorHex: String,
    val config: UserInteractionConfig,
    val onResponse: (approved: Boolean) -> Unit
)

data class ShortcutUiState(
    val shortcuts: List<ShortcutEntity> = emptyList(),
    val filteredShortcuts: List<ShortcutEntity> = emptyList(),
    val selectedCategory: String = "Todos",
    val searchQuery: String = "",
    val executingShortcutId: Long? = null,
    val banner: BannerInfo? = null,
    val editingShortcut: ShortcutEntity? = null,
    val isSheetOpen: Boolean = false,
    val activePrompt: UserPromptState? = null
)

class ShortcutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShortcutRepository
    private val installedAppsRepository: InstalledAppsRepository = InstalledAppsRepository(application.applicationContext)
    private val executor: ShortcutExecutor = ShortcutExecutor(application.applicationContext)

    private val _selectedCategory = MutableStateFlow("Todos")
    private val _searchQuery = MutableStateFlow("")
    private val _executingShortcutId = MutableStateFlow<Long?>(null)
    private val _banner = MutableStateFlow<BannerInfo?>(null)
    private val _editingShortcut = MutableStateFlow<ShortcutEntity?>(null)
    private val _isSheetOpen = MutableStateFlow(false)
    private val _activePrompt = MutableStateFlow<UserPromptState?>(null)

    private val userInteractionNotificationHelper = UserInteractionNotificationHelper(application.applicationContext)
    private val telemetryManager = TelemetryManager.getInstance(application.applicationContext)

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private var executionJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShortcutRepository(database.shortcutDao())
        viewModelScope.launch(Dispatchers.IO) {
            _installedApps.value = installedAppsRepository.getInstalledLauncherApps()
        }
    }

    private data class FilterState(
        val category: String,
        val query: String,
        val executingId: Long?,
        val banner: BannerInfo?,
        val activePrompt: UserPromptState?
    )

    private data class SheetState(
        val editingShortcut: ShortcutEntity?,
        val isSheetOpen: Boolean
    )

    private val filterFlow = combine(
        _selectedCategory,
        _searchQuery,
        _executingShortcutId,
        _banner,
        _activePrompt
    ) { category, query, executingId, banner, prompt ->
        FilterState(category, query, executingId, banner, prompt)
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
            isSheetOpen = sheet.isSheetOpen,
            activePrompt = filter.activePrompt
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
            val shortcutStartTime = System.currentTimeMillis()
            _executingShortcutId.value = shortcut.id
            repository.incrementExecution(shortcut.id)

            val blocks = executor.resolveBlocks(shortcut)
            val totalBlocks = blocks.size
            val executedResults = mutableListOf<String>()
            val stepsTelemetry = mutableListOf<StepTelemetry>()
            var allSuccess = true
            var wasCancelled = false

            for (index in blocks.indices) {
                val block = blocks[index]
                val currentStep = index + 1
                val isWaitBlock = block.actionType == ActionType.WAIT.name
                val isUserInteraction = block.actionType == ActionType.USER_INTERACTION.name

                val stepLabel = if (block.customLabel.isNotBlank()) block.customLabel
                else ActionType.values().firstOrNull { it.name == block.actionType }?.label ?: block.actionType

                val waitDurationMs = if (isWaitBlock) {
                    block.parameter.trim().toLongOrNull()?.coerceAtLeast(0L) ?: ShortcutExecutor.STEP_DELAY_MS
                } else 0L

                _banner.value = BannerInfo(
                    message = if (isWaitBlock) "Paso $currentStep de $totalBlocks: Esperando ${waitDurationMs} ms..."
                    else if (isUserInteraction) "Paso $currentStep de $totalBlocks: Esperando confirmación..."
                    else "Paso $currentStep de $totalBlocks: $stepLabel",
                    isSuccess = true,
                    shortcutTitle = shortcut.title,
                    currentBlockIndex = currentStep,
                    totalBlocks = totalBlocks,
                    isExecuting = true
                )

                val stepStart = System.currentTimeMillis()
                var stepSuccess = true
                var stepResultMessage = ""

                if (isWaitBlock) {
                    delay(waitDurationMs)
                    stepResultMessage = "Espera de ${waitDurationMs} ms"
                    executedResults.add(stepResultMessage)
                } else if (isUserInteraction) {
                    val config = UserInteractionConfig.fromJson(block.parameter)
                    val approved: Boolean = if (config.designType == UserInteractionConfig.DESIGN_NOTIFICATION) {
                        userInteractionNotificationHelper.showInteractionNotification(shortcut.title, config)
                    } else {
                        val deferred = CompletableDeferred<Boolean>()
                        _activePrompt.value = UserPromptState(
                            shortcutTitle = shortcut.title,
                            iconKey = shortcut.iconKey,
                            colorHex = shortcut.colorHex,
                            config = config,
                            onResponse = { result ->
                                _activePrompt.value = null
                                deferred.complete(result)
                            }
                        )
                        deferred.await()
                    }

                    if (approved) {
                        stepResultMessage = "Interacción confirmada"
                        executedResults.add(stepResultMessage)
                    } else {
                        stepSuccess = false
                        allSuccess = false
                        wasCancelled = true
                        stepResultMessage = "Cancelado por el usuario o palabra clave no coincidente"
                        executedResults.add(stepResultMessage)
                    }
                } else {
                    val result = executor.executeSingleBlock(block.actionType, block.parameter)
                    stepSuccess = result.success
                    stepResultMessage = result.message
                    executedResults.add(result.message)
                    if (!result.success) {
                        allSuccess = false
                    }
                }

                val stepDuration = System.currentTimeMillis() - stepStart
                stepsTelemetry.add(
                    StepTelemetry(
                        stepIndex = currentStep,
                        actionType = block.actionType,
                        stepLabel = stepLabel,
                        durationMs = stepDuration,
                        resultMessage = stepResultMessage,
                        success = stepSuccess
                    )
                )

                if (wasCancelled) {
                    break
                }

                // Pausa entre bloques: 1003 ms estándar
                if (index < blocks.lastIndex) {
                    val nextBlock = blocks[index + 1]
                    if (!isWaitBlock && nextBlock.actionType != ActionType.WAIT.name) {
                        delay(ShortcutExecutor.STEP_DELAY_MS)
                    }
                }
            }

            val totalDurationMs = System.currentTimeMillis() - shortcutStartTime
            val finalStatus = if (wasCancelled) "CANCELADO"
            else if (allSuccess) "EXITOSO"
            else "ADVERTENCIA"

            val finalMessage = if (wasCancelled) {
                "Atajo cancelado por el usuario"
            } else if (totalBlocks > 1) {
                if (allSuccess) "¡$totalBlocks bloques completados con éxito!"
                else "Completado con advertencias: ${executedResults.lastOrNull()}"
            } else {
                executedResults.firstOrNull() ?: "Atajo ejecutado"
            }

            // Registrar telemetría completa
            telemetryManager.recordExecution(
                ExecutionTelemetry(
                    shortcutId = shortcut.id,
                    shortcutTitle = shortcut.title,
                    totalDurationMs = totalDurationMs,
                    status = finalStatus,
                    finalMessage = finalMessage,
                    steps = stepsTelemetry
                )
            )

            _banner.value = BannerInfo(
                message = finalMessage,
                isSuccess = !wasCancelled && allSuccess,
                shortcutTitle = shortcut.title,
                currentBlockIndex = if (wasCancelled) stepsTelemetry.size else totalBlocks,
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
            repository.resetDefaults(DefaultShortcuts.getDefaultShortcuts())
        }
    }
}
