package com.example.ui.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.IconPickerRow
import com.example.ui.viewmodel.EditorState

/**
 * Sección modular del editor para configurar título, descripción, categoría, color e icono del atajo.
 */
@Composable
fun ShortcutMetadataSection(
    state: EditorState,
    categories: List<String>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título y Descripción
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Nombre del Atajo") },
                placeholder = { Text("Ej: Modo Enfoque, Saludo Matutino") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("¿Qué hace este atajo?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_desc_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Selector de Categoría
        Column {
            Text(
                text = "Categoría",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category) }
                    )
                }
            }
        }

        // Selector de Color
        ColorPickerRow(
            selectedColorHex = state.colorHex,
            onColorSelected = onColorChange
        )

        // Selector de Icono
        IconPickerRow(
            selectedIconKey = state.iconKey,
            selectedColorHex = state.colorHex,
            onIconSelected = onIconChange
        )
    }
}
