package dev.sagi.monotask.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.util.toRelativeDate

private val DueDateFontSizeNormal = 12.sp
private val DueDateFontSizeSmall  = 9.sp
private const val DueDateAlpha    = 0.45f

@Composable
fun DueDateLabel(
    timestamp: Timestamp,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    val relativeDate = timestamp.toRelativeDate()
    val dateColor = if (relativeDate.isOverdue)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = DueDateAlpha)

    val textStyle = MaterialTheme.typography.labelLarge.copy(
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.Both
        )
    )

    Text(
        text = relativeDate.text,
        style = textStyle,
        fontSize = if (small) DueDateFontSizeSmall else DueDateFontSizeNormal,
        fontWeight = FontWeight.Light,
        color = dateColor,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "DueDateLabel — upcoming")
@Composable
private fun DueDateLabelUpcomingPreview() {
    MonoTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 2, 0))
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 2, 0), small = true)
        }
    }
}

@Preview(showBackground = true, name = "DueDateLabel — overdue")
@Composable
private fun DueDateLabelOverduePreview() {
    MonoTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 2, 0))
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 2, 0), small = true)
        }
    }
}
