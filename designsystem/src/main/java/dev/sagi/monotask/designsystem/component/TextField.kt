package dev.sagi.monotask.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import kotlinx.coroutines.delay

private const val AUTO_FOCUS_DELAY_MS = 200L

@Composable
fun MonoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    required: Boolean = false,
    autoFocus: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 3,
    shape: Shape = MaterialTheme.shapes.medium,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val focusRequester = remember { FocusRequester() }
    var tfValue by remember { mutableStateOf(TextFieldValue(value)) }

    LaunchedEffect(value) {
        if (value != tfValue.text) tfValue = tfValue.copy(text = value)
    }

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(AUTO_FOCUS_DELAY_MS)
            tfValue = tfValue.copy(selection = TextRange(tfValue.text.length))
            focusRequester.requestFocus()
        }
    }

    OutlinedTextField(
        value = tfValue,
        onValueChange = { new ->
            tfValue = new
            onValueChange(new.text)
        },
        label = {
            if (required) {
                Text(buildAnnotatedString {
                    append(label)
                    append(" ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))) {
                        append("*")
                    }
                })
            } else {
                Text(label)
            }
        },
        supportingText = supportingText?.let { {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        }},
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        shape = shape,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.scrim,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.scrim,
            unfocusedLabelColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.scrim,
            focusedLeadingIconColor = MaterialTheme.colorScheme.scrim,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}



@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun MonoTextFieldUnfocusedPreview() {
    MonoTaskTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Empty unfocused
            MonoTextField(value = "", onValueChange = {}, label = "Title", required = true)
            // With content unfocused
            MonoTextField(value = "Finish Android Project", onValueChange = {}, label = "Task Title")
            // Multiline
            MonoTextField(
                value = "Still need to do ProfileScreen and SettingsScreen",
                onValueChange = {},
                label = "Description (Optional)",
                singleLine = false
            )
        }
    }
}
