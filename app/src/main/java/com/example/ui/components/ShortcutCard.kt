package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ShortcutEntity
import com.example.data.model.TriggerType

fun parseColorSafe(hex: String, defaultColor: Color = Color(0xFF007AFF)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else if (cleanHex.length == 8) {
            Color(colorInt)
        } else {
            defaultColor
        }
    } catch (_: Exception) {
        defaultColor
    }
}

@Composable
fun ShortcutCard(
    shortcut: ShortcutEntity,
    isExecuting: Boolean,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val baseColor = parseColorSafe(shortcut.colorHex)

    val gradient = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(
                red = (baseColor.red * 1.12f).coerceIn(0f, 1f),
                green = (baseColor.green * 1.12f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 1.12f).coerceIn(0f, 1f)
            ),
            baseColor,
            baseColor.copy(
                red = (baseColor.red * 0.76f).coerceIn(0f, 1f),
                green = (baseColor.green * 0.76f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 0.76f).coerceIn(0f, 1f)
            )
        )
    )

    val scale by animateFloatAsState(
        targetValue = if (isExecuting) 0.96f else 1f,
        label = "scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .height(162.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = baseColor.copy(alpha = 0.45f),
                spotColor = baseColor.copy(alpha = 0.70f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onExecute() }
            .testTag("shortcut_card_${shortcut.id}"),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo: Foto personalizada o Gradiente vibrante
            if (!shortcut.backgroundImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(shortcut.backgroundImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // Capa oscura de contraste sobre la foto
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.30f),
                                    Color.Black.copy(alpha = 0.58f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(gradient)
                )
            }

            // Contenido de la tarjeta
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(13.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Ícono + Badge de Disparador | Cápsula única de acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge de ícono
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(
                                    width = 0.75.dp,
                                    color = Color.White.copy(alpha = 0.30f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ShortcutIconHelper.getIcon(shortcut.iconKey),
                                contentDescription = shortcut.title,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Badge de Disparador (si está activado)
                        if (shortcut.triggerType == TriggerType.CHARGER_CONNECTED.name) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "⚡ Cargador",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFFD60A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        } else if (shortcut.triggerType == TriggerType.CHARGER_DISCONNECTED.name) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🔋 Batería",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFF9500),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    // Cápsula Unificada de Cristal (Favorito + Menú Opciones)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.28f))
                            .border(
                                width = 0.75.dp,
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        // Botón Favorito
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onToggleFavorite
                                )
                                .testTag("toggle_favorite_${shortcut.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (shortcut.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (shortcut.isFavorite) "Quitar de favoritos" else "Marcar favorito",
                                tint = if (shortcut.isFavorite) Color(0xFFFFD60A) else Color.White.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Divisor sutil
                        Box(
                            modifier = Modifier
                                .width(0.75.dp)
                                .height(14.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )

                        // Botón Opciones (3 puntos)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { showMenu = true }
                                )
                                .testTag("shortcut_options_${shortcut.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color.White.copy(alpha = 0.90f),
                                modifier = Modifier.size(16.dp)
                            )

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar atajo") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Edit, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(if (shortcut.isFavorite) "Quitar de favoritos" else "Marcar favorito")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (shortcut.isFavorite) Icons.Outlined.StarBorder else Icons.Filled.Star,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onToggleFavorite()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Título en ancho completo (evita cortes de palabras antiestéticos)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = shortcut.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Fila Inferior: Subtítulo / pasos a la izquierda + Micro indicador de play
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subtitleText = shortcut.description.ifBlank {
                            if (shortcut.actions.size > 1) "${shortcut.actions.size} pasos" else "1 acción"
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        )

                        if (isExecuting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.20f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Ejecutar atajo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
