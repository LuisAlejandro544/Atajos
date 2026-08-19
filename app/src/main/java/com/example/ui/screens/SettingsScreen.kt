package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.engine.updates.AppReleaseInfo
import com.example.engine.updates.UpdateCheckStatus
import com.example.engine.updates.UpdateCheckerHelper
import com.example.ui.components.BatteryOptimizationHelper
import com.example.ui.components.updates.AllChannelsDialog
import com.example.ui.components.updates.UpdateDialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.launch

private const val URL_TERMINOS = "https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/terminos/"
private const val URL_PRIVACIDAD = "https://atajos-pagina.luisalejandrososacamacho9.workers.dev/legal/privacidad/"
private const val URL_DOCUMENTACION = "https://atajos-pagina.luisalejandrososacamacho9.workers.dev/docs/"
private const val URL_GITHUB = "https://github.com/LuisAlejandro544/Atajos"

/**
 * Pantalla modular de Ajustes y Configuración de Flurix.
 * Proporciona acceso interactivo y enlaces externos a Términos y Condiciones,
 * Política de Privacidad, optimización de batería, permisos y documentación técnica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenBatteryDialog: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLegalDocument by remember { mutableStateOf<LegalDocumentType?>(null) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var showAllChannelsDialog by remember { mutableStateOf(false) }
    var channelReleasesMap by remember { mutableStateOf<Map<String, AppReleaseInfo>>(emptyMap()) }
    var isLoadingChannels by remember { mutableStateOf(false) }
    val isBatteryOptimized = remember { !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsHeaderCard()
        }

        // Sección Actualizaciones de la App
        item {
            SettingsSectionTitle(
                title = "Actualizaciones de la App",
                icon = Icons.Default.SystemUpdate
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_updates_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Buscador de Actualizaciones",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Canal ${BuildConfig.APP_CHANNEL} • v${BuildConfig.VERSION_NAME}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Consulta si existe un nuevo release en GitHub para tu canal de distribución (Beta, Dev o Estable) y descarga o instala el APK directamente.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isCheckingUpdates) {
                                    isCheckingUpdates = true
                                    coroutineScope.launch {
                                        when (val status = UpdateCheckerHelper.checkForUpdates(
                                            currentVersionName = BuildConfig.VERSION_NAME,
                                            currentChannel = BuildConfig.APP_CHANNEL
                                        )) {
                                            is UpdateCheckStatus.UpdateAvailable -> {
                                                isCheckingUpdates = false
                                                availableUpdate = status.release
                                            }
                                            is UpdateCheckStatus.UpToDate -> {
                                                isCheckingUpdates = false
                                                Toast.makeText(context, "¡Estás en la última versión de Flurix (v${BuildConfig.VERSION_NAME})!", Toast.LENGTH_SHORT).show()
                                            }
                                            is UpdateCheckStatus.Error -> {
                                                isCheckingUpdates = false
                                                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                                            }
                                            else -> {
                                                isCheckingUpdates = false
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isCheckingUpdates,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_check_updates")
                        ) {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buscando...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buscar ahora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { openBrowserUrl(context, "https://github.com/LuisAlejandro544/Flurix/releases") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_all_releases")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Releases Web", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            if (!isLoadingChannels) {
                                isLoadingChannels = true
                                coroutineScope.launch {
                                    val releasesMap = UpdateCheckerHelper.fetchChannelReleases(BuildConfig.VERSION_NAME)
                                    channelReleasesMap = releasesMap
                                    isLoadingChannels = false
                                    showAllChannelsDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_explore_all_channels")
                    ) {
                        if (isLoadingChannels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = IndigoPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cargando canales...", fontSize = 12.sp)
                        } else {
                            Text(
                                "⚡ Ver todos los canales (Estable / Beta / Dev)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IndigoPrimary
                            )
                        }
                    }
                }
            }
        }

        // Sección Legal y Transparencia
        item {
            SettingsSectionTitle(
                title = "Legal y Privacidad",
                icon = Icons.Default.Gavel
            )
        }

        item {
            LegalItemCard(
                title = "Términos y Condiciones de Uso",
                description = "Licencia PolyForm Noncommercial 1.0.0, responsabilidad sobre automatizaciones y normas de uso.",
                icon = Icons.Default.Description,
                badgeText = "PolyForm 1.0.0",
                onReadInApp = { selectedLegalDocument = LegalDocumentType.TERMINOS },
                onOpenBrowser = { openBrowserUrl(context, URL_TERMINOS) },
                testTagPrefix = "terms"
            )
        }

        item {
            LegalItemCard(
                title = "Política de Privacidad",
                description = "Arquitectura 100% Offline-First, cero telemetría y almacenamiento local exclusivo en Room SQLite.",
                icon = Icons.Default.Security,
                badgeText = "Offline-First",
                badgeColor = Color(0xFF10B981),
                onReadInApp = { selectedLegalDocument = LegalDocumentType.PRIVACIDAD },
                onOpenBrowser = { openBrowserUrl(context, URL_PRIVACIDAD) },
                testTagPrefix = "privacy"
            )
        }

        // Sección Sistema y Segundo Plano
        item {
            SettingsSectionTitle(
                title = "Sistema y Segundo Plano",
                icon = Icons.Default.Tune
            )
        }

        item {
            val hasOverlayPermission = com.example.ui.components.OverlayPermissionHelper.hasOverlayPermission(context)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_overlay_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasOverlayPermission) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mostrar sobre otras apps",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (hasOverlayPermission) "Permiso concedido (Apertura libre en juegos)" else "Sin permiso (Android puede bloquear aperturas)",
                                fontSize = 12.sp,
                                color = if (hasOverlayPermission) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Permite a Flurix abrir juegos o aplicaciones de tus atajos programados mientras estás jugando o usando otra pantalla.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { com.example.ui.components.OverlayPermissionHelper.requestOverlayPermission(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_configure_overlay")
                        ) {
                            Text(if (hasOverlayPermission) "Revisar Ajustes" else "Conceder Permiso", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_battery_settings")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBatteryOptimized) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else Color(0xFF10B981).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = if (isBatteryOptimized) MaterialTheme.colorScheme.tertiary else Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Optimización de Batería",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBatteryOptimized) "Optimizado por el sistema (puede haber retrasos)" else "Segundo plano sin restricciones",
                                fontSize = 12.sp,
                                color = if (isBatteryOptimized) MaterialTheme.colorScheme.tertiary else Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Para que los disparadores por horario y eventos de hardware respondan al instante con la pantalla apagada, desactiva la optimización.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onOpenBatteryDialog,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_configure_battery")
                        ) {
                            Text("Configurar Batería", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sección Documentación y Proyecto
        item {
            SettingsSectionTitle(
                title = "Documentación y Proyecto",
                icon = Icons.Default.Info
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProjectLinkRow(
                        title = "Guía de Documentación Web",
                        subtitle = "Variables dinámicas, acciones y disparadores",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { openBrowserUrl(context, URL_DOCUMENTACION) },
                        testTag = "btn_open_docs_link"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    ProjectLinkRow(
                        title = "Repositorio en GitHub",
                        subtitle = "Código fuente y reporte de issues",
                        icon = Icons.Default.Code,
                        onClick = { openBrowserUrl(context, URL_GITHUB) },
                        testTag = "btn_open_github_link"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Flurix • Autor: Luis Alejandro Sosa Camacho\nPolyForm Noncommercial License 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
    }

    // Modal BottomSheet con el visor del texto legal dentro de la app
    selectedLegalDocument?.let { docType ->
        LegalViewerBottomSheet(
            documentType = docType,
            onDismiss = { selectedLegalDocument = null },
            onOpenInBrowser = {
                val url = when (docType) {
                    LegalDocumentType.TERMINOS -> URL_TERMINOS
                    LegalDocumentType.PRIVACIDAD -> URL_PRIVACIDAD
                }
                openBrowserUrl(context, url)
            }
        )
    }

    // Diálogo interactivo de actualización de la app (In-App y Releases Web)
    availableUpdate?.let { release ->
        UpdateDialog(
            releaseInfo = release,
            onDismiss = { availableUpdate = null }
        )
    }

    // Diálogo explorador de todos los canales (Estable, Beta, Dev)
    if (showAllChannelsDialog) {
        AllChannelsDialog(
            channelReleases = channelReleasesMap,
            currentChannel = BuildConfig.APP_CHANNEL,
            onSelectReleaseToDownload = { release ->
                availableUpdate = release
            },
            onDismiss = { showAllChannelsDialog = false }
        )
    }
}

@Composable
private fun SettingsHeaderCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
            .testTag("settings_header_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(IndigoPrimary, CyanAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Logo Flurix",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Flurix para Android",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Versión 0.1.0-B (Canal Beta)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "100% Offline-First",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Automatización rápida, ligera y potente de tareas en Android sin rastreo ni dependencias obligatorias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IndigoPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegalItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color = IndigoPrimary,
    onReadInApp: () -> Unit,
    onOpenBrowser: () -> Unit,
    testTagPrefix: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_$testTagPrefix")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReadInApp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_${testTagPrefix}_read_app")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Leer en la App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenBrowser,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_${testTagPrefix}_open_web")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir enlace", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ProjectLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IndigoPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Tipos de documentos legales disponibles para visualización en la app.
 */
enum class LegalDocumentType {
    TERMINOS,
    PRIVACIDAD
}

/**
 * Modal BottomSheet con el contenido textual completo y estructurado de Términos y Condiciones o Privacidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalViewerBottomSheet(
    documentType: LegalDocumentType,
    onDismiss: () -> Unit,
    onOpenInBrowser: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("legal_viewer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Cabecera del modal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (documentType == LegalDocumentType.TERMINOS) "Términos y Condiciones" else "Política de Privacidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Última actualización: 14 de Agosto de 2026",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Contenido legible con scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (documentType) {
                    LegalDocumentType.TERMINOS -> TermsContent()
                    LegalDocumentType.PRIVACIDAD -> PrivacyContent()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón inferior para abrir en el navegador web
            Button(
                onClick = onOpenInBrowser,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_modal_open_web")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver documento oficial en la Web",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TermsContent() {
    LegalSection(
        title = "1. Aceptación de los Términos",
        body = "Al descargar, instalar o utilizar la aplicación Flurix / Atajos (en adelante, \"la Aplicación\"), aceptas quedar vinculado por estos Términos y Condiciones. Si no estás de acuerdo con alguna parte de los términos, no debes utilizar la Aplicación."
    )

    LegalSection(
        title = "2. Naturaleza del Software y Licencia (PolyForm Noncommercial 1.0.0)",
        body = "Flurix es un software de automatización para Android creado por Luis Alejandro Sosa Camacho bajo el modelo Source-Available y licenciado conforme a la PolyForm Noncommercial License 1.0.0.\n\nSe concede al usuario una licencia para ejecutar, estudiar, probar y compilar el código fuente en sus propios dispositivos con fines estrictamente personales y no comerciales."
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Aviso de derechos y restricción de distribución:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "• Required Notice: Copyright (c) 2026 Luis Alejandro Sosa Camacho\n• Queda estrictamente prohibido a terceros publicar, republicar, redistribuir o comercializar paquetes compilados (APK) de la Aplicación en tiendas de aplicaciones o sitios web sin autorización expresa por escrito del autor.\n• La distribución oficial de binarios y actualizaciones APK es facultad exclusiva del titular del proyecto.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                lineHeight = 15.sp
            )
        }
    }

    LegalSection(
        title = "3. Responsabilidad sobre las Automatizaciones",
        body = "La Aplicación permite al usuario crear secuencias automatizadas de acciones de hardware y software (peticiones HTTP a servidores externos, ajuste de brillo, volumen, linterna y envío de mensajes/notificaciones).\n\n• El usuario es el único responsable de los scripts, URLs y peticiones web que configure en sus atajos.\n• El autor no se hace responsable de daños derivados del uso indebido de automatizaciones, configuraciones erróneas de hardware, llamadas a APIs de terceros o pérdida de datos."
    )

    LegalSection(
        title = "4. Exclusión de Garantías",
        body = "La Aplicación se suministra \"tal cual\" (AS IS) y \"según disponibilidad\", sin garantías de ningún tipo, ya sean expresas o implícitas. Aunque nos esforzamos por ofrecer la máxima estabilidad y compatibilidad con versiones modernas de Android, no garantizamos que el software esté libre de interrupciones o fallos."
    )

    LegalSection(
        title = "5. Modificaciones de los Términos",
        body = "Nos reservamos el derecho de actualizar estos Términos y Condiciones en cualquier momento. Los cambios sustanciales se notificarán mediante la publicación de una versión actualizada en el sitio web oficial."
    )
}

@Composable
private fun PrivacyContent() {
    LegalSection(
        title = "1. Filosofía Offline-First y Cero Telemetría",
        body = "En Flurix creemos firmemente en la privacidad y soberanía de los datos del usuario. La aplicación está diseñada desde su núcleo bajo la arquitectura Local & Offline-First."
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF10B981).copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sin servidores propios de rastreo: No recopilamos, procesamos ni almacenamos datos personales, métricas analíticas ni registros de uso en servidores externos.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 15.sp
            )
        }
    }

    LegalSection(
        title = "2. Datos Almacenados en el Dispositivo",
        body = "Toda la información creada por el usuario (incluyendo nombres de atajos, configuraciones de disparadores, historial de ejecuciones y credenciales o tokens en peticiones HTTP) se almacena exclusivamente en el almacenamiento local del dispositivo mediante SQLite (Room Database)."
    )

    LegalSection(
        title = "3. Permisos del Sistema Operativo",
        body = "Para ejecutar las acciones solicitadas por el usuario, la aplicación puede requerir acceso a ciertos permisos de Android:\n\n• Cámara y Linterna: Requerido únicamente para encender la linterna física o abrir la cámara a petición del usuario. No se capturan imágenes en segundo plano.\n• Modificación de Ajustes del Sistema (WRITE_SETTINGS): Utilizado exclusivamente para calibrar el nivel de brillo de pantalla configurado por el usuario.\n• Acceso a Internet: Utilizado únicamente para ejecutar las peticiones HTTP que el usuario haya configurado explícitamente en una acción de tipo Webhook/Petición Web."
    )

    LegalSection(
        title = "4. Contacto y Consultas",
        body = "Para cualquier duda o inquietud respecto a esta Política de Privacidad o el funcionamiento de la aplicación, puedes abrir un issue en nuestro repositorio de GitHub o comunicarte a través de los canales oficiales del proyecto."
    )
}

@Composable
private fun LegalSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )
    }
}

private fun openBrowserUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir el navegador web: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
