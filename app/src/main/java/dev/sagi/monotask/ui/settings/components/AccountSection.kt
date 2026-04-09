package dev.sagi.monotask.ui.settings.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.components.MonoConfirmDialog
import dev.sagi.monotask.designsystem.components.MonoTextInputDialog
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING


@Composable
internal fun SettingsAccountSection(
    displayName : String,
    email : String,
    onUpdateDisplayName: (String) -> Unit,
    onClearArchive: () -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showClearArchiveDialog by remember { mutableStateOf(false) }

    SettingsSection("Account") {
        SettingsAccountRow(
            label = "Name",
            value = displayName.ifEmpty { "—" },
            leadingIcon = {
                SettingsRowIcon(
                    IconPack.Id,
                    color = SettingsIconColors.accountName
                )
            },
            onEdit = { showEditNameDialog = true }
        )

        SettingsDivider()

        SettingsAccountRow(
            label = "Email",
            value = email.ifEmpty { "—" },
            leadingIcon = {
                SettingsRowIcon(
                    IconPack.Email,
                    color = SettingsIconColors.email
                )
            }
        )

        SettingsDivider()

        ClearArchiveRow(onClick = { showClearArchiveDialog = true })
    }

    if (showClearArchiveDialog) {
        MonoConfirmDialog(
            onDismissRequest = { showClearArchiveDialog = false },
            title = "Clear archive?",
            message = "All completed tasks across all workspaces will be permanently deleted. This operation can't be undone.",
            confirmLabel = "Confirm",
            dismissLabel = "Cancel",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onClearArchive(); showClearArchiveDialog = false }
        )
    }

    if (showEditNameDialog) {
        MonoTextInputDialog(
            title = "Change Name",
            placeholder = "Display name",
            confirmLabel = "Save",
            onConfirm = { onUpdateDisplayName(it); showEditNameDialog = false },
            onDismiss = { showEditNameDialog = false }
        )
    }
}


// ==========================================
// Private row helpers
// ==========================================

@Composable
private fun ClearArchiveRow(onClick: () -> Unit) {
    val errorColor = MaterialTheme.colorScheme.error
    SettingsRow(
        leadingIcon = { SettingsRowIcon(IconPack.ArchiveDelete, color = errorColor) },
        verticalPadding = 16.dp,
        onClick = onClick,
        trailingContent = {
            Icon(
                painter = painterResource(IconPack.ChevronRight),
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(20.dp)
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Clear task archive",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal,
                color = errorColor
            )
        }
    }
}

@Composable
private fun SettingsAccountRow(
    label : String,
    value : String,
    leadingIcon : (@Composable () -> Unit)? = null,
    onEdit : (() -> Unit)? = null
) {
    SettingsRow(
        leadingIcon = leadingIcon,
        verticalPadding = 16.dp,
        trailingContent = if (onEdit != null) {
            {
                SettingsActionIconButton(
                    iconRes = IconPack.Edit,
                    contentDescription = "Edit $label",
                    onClick = onEdit
                )
            }
        } else null
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}


// ==========================================
// Preview
// ==========================================

@Preview(showBackground = true, name = "Account Section")
@Composable
private fun SettingsAccountSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                SettingsAccountSection(
                    displayName         = "Sagi Einav",
                    email               = "sagii9021@gmail.com",
                    onUpdateDisplayName = {},
                    onClearArchive      = {}
                )
            }
        }
    }
}
