package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ExecutionStatus
import com.example.ui.components.ExecutionStatusBanner
import com.example.ui.components.PermissionBanner
import com.example.ui.theme.IndigoPrimary

/**
 * TopAppBar modular principal de la aplicación, con insignia de marca,
 * banner de permisos y monitor de estado de ejecución en vivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsTopBar(
    showPermissionBanner: Boolean,
    executionStatus: ExecutionStatus,
    onRequestPermissions: () -> Unit,
    onDismissPermissionBanner: () -> Unit,
    onDismissExecutionStatus: () -> Unit,
    onCancelExecution: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Atajos",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Banner de permisos recomendados
        AnimatedVisibility(visible = showPermissionBanner) {
            PermissionBanner(
                onRequestPermissions = onRequestPermissions,
                onDismiss = onDismissPermissionBanner
            )
        }

        // Banner de estado de ejecución en tiempo real
        ExecutionStatusBanner(
            status = executionStatus,
            onDismiss = onDismissExecutionStatus,
            onCancel = onCancelExecution
        )
    }
}
