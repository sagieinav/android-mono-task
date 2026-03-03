package dev.sagi.monotask.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimePicker(
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // ========== Step 1: Date ==========
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showTimePicker by remember { mutableStateOf(false) }

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { showTimePicker = true },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ========== Step 2: Time ==========
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Combine date + time into a single millis value
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = datePickerState.selectedDateMillis!!
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDateTimeSelected(calendar.timeInMillis)
                    onDismiss()
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Back") }
            },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskDateTimePickerPreview() {
    MonoTaskTheme {
        // Toggle these to preview each step independently
        val previewStep = "date" // "date" or "time"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (previewStep == "date") {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(onClick = {}) { Text("Next") }
                    },
                    dismissButton = {
                        TextButton(onClick = {}) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (previewStep == "time") {
                val timePickerState = rememberTimePickerState(
                    initialHour = 14,
                    initialMinute = 30,
                    is24Hour = false
                )
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(onClick = {}) { Text("Done") }
                    },
                    dismissButton = {
                        TextButton(onClick = {}) { Text("Back") }
                    },
                    title = { Text("Select Time") },
                    text = { TimePicker(state = timePickerState) }
                )
            }
        }
    }
}
