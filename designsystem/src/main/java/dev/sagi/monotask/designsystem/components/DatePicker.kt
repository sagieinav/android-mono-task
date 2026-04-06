package dev.sagi.monotask.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.WheelDatePicker
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun MonoDatePicker(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialDateMillis: Long? = null
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(timeZone).date }
    val initialDate = remember(initialDateMillis) {
        initialDateMillis
            ?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date }
            ?: today
    }

    val glassBorder = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.4f)
            )
        )
    }

    var selectedDate by remember { mutableStateOf(initialDate) }

    MonoDialog(
        onDismissRequest = onDismiss,
        title = "Due Date",
        modifier = modifier,
        content = {
            WheelDatePicker(
                modifier = Modifier.fillMaxWidth(),
                startDate = initialDate,
                minDate = today,
                textStyle = MaterialTheme.typography.titleSmall,
                textColor = MaterialTheme.colorScheme.onSurface,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    color = Color.White.copy(alpha = 0.4f),
                    border = BorderStroke(
                        0.5.dp,
                        brush = glassBorder
                    ),
                    shape = CircleShape,
                ),
            ) { snappedDate -> selectedDate = snappedDate }
        },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            TextButton(onClick = {
                val millis = selectedDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
                onDateSelected(millis)
                onDismiss()
            }) {
                Text("Done")
            }
        }
    )

}




@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MonoDatePickerPreview() {
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            MonoDatePicker(
                initialDateMillis = System.currentTimeMillis(),
                onDateSelected = {},
                onDismiss = {}
            )
        }
    }
}
