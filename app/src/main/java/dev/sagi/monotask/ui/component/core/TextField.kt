package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.monoBorder
import kotlinx.coroutines.delay

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

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(200L)
            focusRequester.requestFocus()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            if (required) {
                val text = buildAnnotatedString {
                    append(label)
                    append(" ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))) {
                        append("*")
                    }
                }
                Text(text)
            } else {
                Text(label)
            }
        },
        supportingText = supportingText?.let {
            { Text(
                it,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)) }
        },
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
