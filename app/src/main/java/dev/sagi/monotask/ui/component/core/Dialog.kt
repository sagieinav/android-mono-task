package dev.sagi.monotask.ui.component.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // for consistent horizontal padding from screen edges
        )    ) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .widthIn(min = 280.dp, max = 560.dp) // M3 dialog spec bounds
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            blurred = false,
            baseColor = MaterialTheme.colorScheme.surfaceDim
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .padding(top = 24.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                // Content slot
                content()
                // Buttons
                Row(
                    // SpaceBetween + horizontal padding, for nicely sided buttons
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    content = buttons
                )
            }
        }
    }
}

@Composable
fun GlassConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    onConfirm: () -> Unit
) {
    GlassDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Thin,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    dismissLabel,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(
                    confirmLabel,
                    color = confirmColor,
                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
fun TextInputDialog(
    title: String,
    placeholder: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val canConfirm = input.isNotBlank()

    GlassDialog(
        onDismissRequest = onDismiss,
        title = title,
        content = {
            MonoTextField(
                value = input,
                onValueChange = { input = it },
                label = placeholder,
                modifier = Modifier.fillMaxWidth(),
                autoFocus = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = capitalization,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (canConfirm) onConfirm(input) }
                )
            )
        },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(
                    dismissLabel,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = { onConfirm(input) },
                enabled = canConfirm
            ) {
                Text(
                    confirmLabel,
                    color = if (canConfirm) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}


// ========== Previews ==========

@Preview(showBackground = true, name = "GlassConfirmDialog")
@Composable
private fun GlassConfirmDialogPreview() {
    MonoTaskTheme {
        GlassConfirmDialog(
            onDismissRequest = {},
            title            = "Delete Task",
            message          = "Are you sure you want to delete this task? This action cannot be undone.",
            confirmLabel     = "Delete",
            onConfirm        = {}
        )
    }
}

@Preview(showBackground = true, name = "TextInputDialog")
@Composable
private fun TextInputDialogPreview() {
    MonoTaskTheme {
        TextInputDialog(
            title        = "New Workspace",
            placeholder  = "Workspace name",
            confirmLabel = "Create",
            onConfirm    = {},
            onDismiss    = {}
        )
    }
}