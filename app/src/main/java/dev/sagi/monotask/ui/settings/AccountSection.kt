package dev.sagi.monotask.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.TextInputDialog
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING


@Composable
internal fun SettingsAccountSection(
    displayName        : String,
    email              : String,
    onUpdateDisplayName: (String) -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }

    SettingsSection("Account") {
        SettingsAccountRow(
            label       = "Name",
            value       = displayName.ifEmpty { "—" },
            leadingIcon = { SettingsRowIcon(R.drawable.ic_id, color = SettingsIconColors.accountName) },
            onEdit      = { showEditNameDialog = true }
        )

        SettingsDivider()

        SettingsAccountRow(
            label       = "Email",
            value       = email.ifEmpty { "—" },
            leadingIcon = { SettingsRowIcon(R.drawable.ic_email, color = SettingsIconColors.email) }
        )
    }

    if (showEditNameDialog) {
        TextInputDialog(
            title        = "Change Name",
            placeholder  = "Display name",
            confirmLabel = "Save",
            onConfirm    = { onUpdateDisplayName(it); showEditNameDialog = false },
            onDismiss    = { showEditNameDialog = false }
        )
    }
}


// ==========================================
// Private row helpers
// ==========================================

@Composable
private fun SettingsAccountRow(
    label       : String,
    value       : String,
    leadingIcon : (@Composable () -> Unit)? = null,
    onEdit      : (() -> Unit)? = null
) {
    SettingsRow(
        leadingIcon     = leadingIcon,
        verticalPadding = 16.dp,
        trailingContent = if (onEdit != null) {
            {
                SettingsActionIconButton(
                    iconRes            = R.drawable.ic_edit,
                    contentDescription = "Edit $label",
                    onClick            = onEdit
                )
            }
        } else null
    ) {
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.titleSmall
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
                    onUpdateDisplayName = {}
                )
            }
        }
    }
}
