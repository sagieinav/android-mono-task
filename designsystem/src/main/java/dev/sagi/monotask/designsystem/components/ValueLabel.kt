package dev.sagi.monotask.designsystem.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme

private val MinValueLabelSize = 18.sp

@Composable
fun ValueLabel(
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null
) {
    val initialFontSize = MaterialTheme.typography.headlineMedium.fontSize
    var fontSize by remember(value) { mutableStateOf(initialFontSize) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
            onTextLayout = { result ->
                if (result.hasVisualOverflow && fontSize > MinValueLabelSize) fontSize *= 0.9f
            }
        )
        unit?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

@Preview(showBackground = true, name = "ValueLabel — with unit")
@Composable
private fun ValueLabelPreview() {
    MonoTaskTheme {
        ValueLabel(value = "72,340", unit = "xp")
    }
}

@Preview(showBackground = true, name = "ValueLabel — long value")
@Composable
private fun ValueLabelLongPreview() {
    MonoTaskTheme {
        ValueLabel(value = "1,000,000", unit = "tasks")
    }
}
