package dev.sagi.monotask.ui.component.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.*


// When clicking `Add Workspace`
@Composable
fun CreateWorkspaceDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    TextInputDialog(
        title = "New Workspace",
        placeholder = "Workspace name",
        confirmLabel = "Create",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}


@Composable
fun WorkspaceDropdownGlass(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
    previewExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(previewExpanded) }

    Box(modifier = modifier) {
        DropdownTriggerPill(
            text = selectedWorkspace?.name ?: "Workspace",
            expanded = expanded,
            onClick = { expanded = !expanded }
        )

        MonoDropdownMenuGlass(
            expanded = expanded,
            onDismiss = { expanded = false }
        ) {
            workspaces.forEach { workspace ->
                val isSelected = workspace.id == selectedWorkspace?.id
                MonoDropdownItem(
                    label = workspace.name,
                    selected = isSelected,
                    onClick = { onWorkspaceSelected(workspace); expanded = false },
                    trailingContent = if (isSelected) ({
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }) else null
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            MonoDropdownActionItem(
                label = "New Workspace",
                iconRes = R.drawable.ic_add,
                onClick = { onAddWorkspace(); expanded = false }
            )
        }
    }
}