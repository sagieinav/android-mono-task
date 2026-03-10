//package dev.sagi.monotask.ui.component.core
//
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardCapitalization
//import androidx.compose.ui.tooling.preview.Preview
//import dev.sagi.monotask.ui.theme.MonoTaskTheme
//
//@Composable
//fun TextInputDialog(
//    title: String,
//    placeholder: String,
//    confirmLabel: String = "Confirm",
//    dismissLabel: String = "Cancel",
//    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
//    onConfirm: (String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    var input by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
//        text = {
//            MonoTextField(
//                value = input,
//                onValueChange = { input = it },
//                label = placeholder,
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(
//                    capitalization = capitalization,
//                    imeAction = ImeAction.Done
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = { if (input.isNotBlank()) onConfirm(input) }
//                )
//            )
//        },
//        confirmButton = {
//            TextButton(
//                onClick = { onConfirm(input) },
//                enabled = input.isNotBlank()
//            ) { Text(confirmLabel) }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text(dismissLabel) }
//        }
//    )
//}
//
//
//// ─────────────────────────────────────────
//// Previews
//// ─────────────────────────────────────────
//@Preview(showBackground = true)
//@Composable
//private fun TextInputDialogPreview() {
//    MonoTaskTheme {
//        TextInputDialog(
//            title = "New Workspace",
//            placeholder = "Workspace name",
//            confirmLabel = "Create",
//            onConfirm = {},
//            onDismiss = {}
//        )
//    }
//}
