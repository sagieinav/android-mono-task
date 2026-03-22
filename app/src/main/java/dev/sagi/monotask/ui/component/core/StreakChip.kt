package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.fireIconGradient
import dev.sagi.monotask.ui.theme.googleSans

enum class StreakChipSize { NORMAL, SMALL }

@Composable
fun StreakChip(
    currentStreak : Int,
    size          : StreakChipSize = StreakChipSize.NORMAL,
    modifier      : Modifier = Modifier
) {
    val streakLabel = if (currentStreak == 1) "day streak" else "days streak"
    val iconSize    = if (size == StreakChipSize.NORMAL) 18.dp else 15.dp
    val textStyle   = if (size == StreakChipSize.NORMAL)
        MaterialTheme.typography.labelLarge
    else
        MaterialTheme.typography.labelMedium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = modifier
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_fire),
            contentDescription = null,
            modifier           = Modifier
                .size(iconSize)
                .fireIconGradient()
                .padding(bottom = 1.5.dp) // optical correction
        )
        Spacer(Modifier.padding(horizontal = if (size == StreakChipSize.NORMAL) 1.dp else 1.dp))
        Text(
            text       = "$currentStreak ",
            fontWeight = FontWeight.Black,
            fontFamily = googleSans,
            color      = MaterialTheme.colorScheme.onSurface,
            style      = textStyle
        )
        Text(
            text       = streakLabel,
            fontWeight = FontWeight.Thin,
            fontFamily = googleSans,
            style      = textStyle,
            color      = MaterialTheme.colorScheme.outlineVariant
        )
    }
}


// ========== Previews ==========

@Preview(showBackground = true, name = "StreakChip, Normal")
@Composable
private fun StreakChipPreview() {
    MonoTaskTheme {
        Column {
            StreakChip(currentStreak = 7)
            StreakChip(currentStreak = 1, size = StreakChipSize.SMALL)
        }
    }
}
