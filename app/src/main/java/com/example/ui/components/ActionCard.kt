package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.ShortcutAction
import com.example.ui.components.actioninputs.AppLauncherInputSection
import com.example.ui.components.actioninputs.FlashlightInputSection
import com.example.ui.components.actioninputs.MessagingInputSection
import com.example.ui.components.actioninputs.NotificationInputSection
import com.example.ui.components.actioninputs.TtsInputSection
import com.example.ui.components.actioninputs.UtilitiesInputSection
import com.example.ui.components.actioninputs.VibrationInputSection
import com.example.ui.components.actioninputs.WebUrlInputSection
import com.example.ui.theme.IndigoPrimary

/**
 * Tarjeta interactiva para representar y configurar un paso de acción dentro del editor de atajos.
 * Incluye controles para reordenar, eliminar y delegar la parametrización a submódulos dedicados.
 */
@Composable
fun ActionCard(
    stepIndex: Int,
    action: ShortcutAction,
    totalSteps: Int,
    onUpdate: (ShortcutAction) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_card_$stepIndex"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Fila superior: Contador de paso, Nombre de la acción, Reordenamiento y Borrado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stepIndex + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = action.type.displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = action.type.categoryName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stepIndex > 0) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Subir acción",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (stepIndex < totalSteps - 1) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Bajar acción",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_action_$stepIndex")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar acción",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submódulos de configuración según el tipo de acción
            when (action.type) {
                ActionType.SPEAK_TEXT -> {
                    TtsInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.TOGGLE_FLASHLIGHT -> {
                    FlashlightInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.VIBRATE -> {
                    VibrationInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.SHOW_NOTIFICATION -> {
                    NotificationInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.OPEN_URL,
                ActionType.SEARCH_WEB -> {
                    WebUrlInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.COPY_CLIPBOARD,
                ActionType.SEND_WHATSAPP,
                ActionType.SEND_SMS,
                ActionType.SHARE_TEXT -> {
                    MessagingInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.SET_TIMER,
                ActionType.WAIT_DELAY,
                ActionType.QUICK_CALCULATOR -> {
                    UtilitiesInputSection(action = action, onUpdate = onUpdate)
                }

                ActionType.LAUNCH_APP -> {
                    AppLauncherInputSection(action = action, onUpdate = onUpdate)
                }
            }
        }
    }
}
