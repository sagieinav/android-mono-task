package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ValueLabel(
    value: String,
    unit: String? = null,
//    accentColor: Color? = null,
) {
    val initialFontSize = MaterialTheme.typography.headlineMedium.fontSize
    var fontSize by remember(value) { mutableStateOf(initialFontSize) }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text     = value,
            style    = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
            onTextLayout = { result ->
                if (result.hasVisualOverflow && fontSize > 18.sp) fontSize *= 0.9f
            }
        )
        unit?. let {
            Text(
                text       = unit,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Thin,
                color      = MaterialTheme.colorScheme.outlineVariant,
                modifier   = Modifier.alignByBaseline()
            )
        }
    }
}