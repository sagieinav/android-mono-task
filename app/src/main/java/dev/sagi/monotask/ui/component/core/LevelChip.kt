package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder

@Composable
fun LevelChip(
    level    : Int,
    modifier : Modifier = Modifier
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .clip(shape)
            .glassBackground(MaterialTheme.colorScheme.outline)
            .glassBorder(shape, color = MaterialTheme.colorScheme.outline)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text       = "Lv. $level",
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// ========== Preview ==========

@Preview(showBackground = true)
@Composable
private fun LevelChipPreview() {
    MonoTaskTheme {
        LevelChip(level = 12)
    }
}
