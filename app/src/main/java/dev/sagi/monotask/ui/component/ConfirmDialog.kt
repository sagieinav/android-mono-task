import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme

@Composable
fun ConfirmDialog(
    title: String,
    message: String? = null,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmColor = if (isDestructive)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.secondary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = if (message != null) {{
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }} else null,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = confirmColor)
            ) {
                Text(confirmLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}




// ========== Previews ==========
@Preview
@Composable
fun ConfirmDialogPreview() {
    MonoTaskTheme {
        val hazeState = HazeState()
        ConfirmDialog(
            title = "Complete Task?",
            message = "This will mark the task as done and award you XP.",
            confirmLabel = "Complete",
            dismissLabel = "Not yet",
            isDestructive = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun ConfirmDialogDestructivePreview() {
    MonoTaskTheme {
        val hazeState = HazeState()
        ConfirmDialog(
            title = "Delete Task?",
            message = "This cannot be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
