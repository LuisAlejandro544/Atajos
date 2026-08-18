package com.example.ui.components.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.updates.AppReleaseInfo
import com.example.ui.theme.IndigoPrimary

/**
 * Diálogo interactivo para explorar y descargar APKs de todos los canales de Flurix (Estable, Beta, Dev).
 */
@Composable
fun AllChannelsDialog(
    channelReleases: Map<String, AppReleaseInfo>,
    currentChannel: String,
    onSelectReleaseToDownload: (AppReleaseInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("all_channels_dialog_content")
            ) {
                // Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Canales de Distribución",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Estable • Beta • Dev",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Puedes cambiar de canal descargando e instalando el APK correspondiente directamente:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Canal Estable
                val stableRelease = channelReleases["STABLE"]
                ChannelCardItem(
                    channelName = "Flurix Estable",
                    tagCode = "-E",
                    badgeColor = Color(0xFF10B981),
                    badgeText = "Recomendada",
                    description = "Versión verificada para uso diario. Máxima estabilidad.",
                    releaseInfo = stableRelease,
                    isCurrent = currentChannel.equals("STABLE", ignoreCase = true) || currentChannel.equals("E", ignoreCase = true),
                    onDownload = {
                        stableRelease?.let {
                            onDismiss()
                            onSelectReleaseToDownload(it)
                        }
                    },
                    onOpenWeb = {
                        openWebUrl(context, stableRelease?.htmlUrl ?: "https://github.com/LuisAlejandro544/Flurix/releases")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Canal Beta
                val betaRelease = channelReleases["BETA"]
                ChannelCardItem(
                    channelName = "Flurix Beta",
                    tagCode = "-B",
                    badgeColor = IndigoPrimary,
                    badgeText = "Novedades",
                    description = "Acceso anticipado a nuevas funciones antes de que lleguen a estable.",
                    releaseInfo = betaRelease,
                    isCurrent = currentChannel.equals("BETA", ignoreCase = true) || currentChannel.equals("B", ignoreCase = true),
                    onDownload = {
                        betaRelease?.let {
                            onDismiss()
                            onSelectReleaseToDownload(it)
                        }
                    },
                    onOpenWeb = {
                        openWebUrl(context, betaRelease?.htmlUrl ?: "https://github.com/LuisAlejandro544/Flurix/releases")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Canal Dev
                val devRelease = channelReleases["DEV"] ?: channelReleases["CANARY"]
                ChannelCardItem(
                    channelName = "Flurix Dev",
                    tagCode = "-DEV",
                    badgeColor = Color(0xFFF59E0B),
                    badgeText = "Experimental",
                    description = "Compilaciones directas de desarrollo con cambios continuos.",
                    releaseInfo = devRelease,
                    isCurrent = currentChannel.equals("DEV", ignoreCase = true) || currentChannel.equals("D", ignoreCase = true),
                    onDownload = {
                        devRelease?.let {
                            onDismiss()
                            onSelectReleaseToDownload(it)
                        }
                    },
                    onOpenWeb = {
                        openWebUrl(context, devRelease?.htmlUrl ?: "https://github.com/LuisAlejandro544/Flurix/releases")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = null
    )
}

@Composable
private fun ChannelCardItem(
    channelName: String,
    tagCode: String,
    badgeColor: Color,
    badgeText: String,
    description: String,
    releaseInfo: AppReleaseInfo?,
    isCurrent: Boolean,
    onDownload: () -> Unit,
    onOpenWeb: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.border(1.5.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = channelName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isCurrent) "Canal Actual" else badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = releaseInfo?.tagName ?: "Disponible en GitHub ($tagCode)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (releaseInfo != null && releaseInfo.apkSizeInBytes > 0) {
                    val sizeMb = String.format(java.util.Locale.US, "%.1f MB", releaseInfo.apkSizeInBytes / (1024.0 * 1024.0))
                    Text(
                        text = sizeMb,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        if (releaseInfo?.apkDownloadUrl?.isNotBlank() == true) {
                            onDownload()
                        } else {
                            onOpenWeb()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (releaseInfo?.apkDownloadUrl?.isNotBlank() == true) "Descargar" else "Ver Release",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenWeb,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.7f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Web", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun openWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
