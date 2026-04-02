package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.StreakOrange
import dev.sagi.monotask.ui.theme.customColors

enum class StreakChipSize { Normal, Small }

@Composable
fun StreakChip(
    currentStreak: Int,
    modifier: Modifier = Modifier,
    size: StreakChipSize = StreakChipSize.Normal
) {
    val streakLabel = if (currentStreak == 1) "day streak" else "days streak"
    val iconSize    = if (size == StreakChipSize.Normal) 17.dp else 15.dp
    val textStyle   = if (size == StreakChipSize.Normal)
        MaterialTheme.typography.labelLarge
    else
        MaterialTheme.typography.labelMedium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fire),
            contentDescription = null,
            tint = MaterialTheme.customColors.streak,
            modifier = Modifier
                .size(iconSize)
                .padding(bottom = 1.5.dp)
        )

        Text(
            text = currentStreak.toString(),
            fontWeight = FontWeight.SemiBold,
//            fontFamily = googleSans,
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle
        )

        Spacer(Modifier.width(0.5.dp)) // optical correction

        Text(
            text = streakLabel,
            fontWeight = FontWeight.Normal,
            style = textStyle,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true, name = "StreakChip — Normal")
@Composable
private fun StreakChipNormalPreview() {
    MonoTaskTheme {
        StreakChip(currentStreak = 7)
    }
}

@Preview(showBackground = true, name = "StreakChip — Small")
@Composable
private fun StreakChipSmallPreview() {
    MonoTaskTheme {
        StreakChip(currentStreak = 1, size = StreakChipSize.Small)
    }
}

