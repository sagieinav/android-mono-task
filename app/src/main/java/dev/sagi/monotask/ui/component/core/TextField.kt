package dev.sagi.monotask.ui.component.core

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun MonoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 3,
    shape: Shape = MaterialTheme.shapes.medium,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        // Enforce fillMaxWidth as a default behavior, but allow overriding
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.scrim,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.scrim,
            unfocusedLabelColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            cursorColor = MaterialTheme.colorScheme.scrim
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
