package com.example.ui.components.editors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LuaScriptBlockEditor(
    parameter: String,
    onParameterChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = parameter,
            onValueChange = onParameterChange,
            label = { Text("Script en Lua (retorna texto opcional)", fontSize = 12.sp) },
            singleLine = false,
            minLines = 4,
            maxLines = 10,
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
