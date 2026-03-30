package dev.sagi.monotask.ui.component.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.*
import dev.sagi.monotask.ui.theme.MonoTaskTheme


// When clicking `Add Workspace`
@Composable
fun CreateWorkspaceDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    MonoTextInputDialog(
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
        MonoDropdownTriggerPill(
            text = selectedWorkspace?.name ?: "Workspace",
            expanded = expanded,
            onClick = { expanded = !expanded }
        )

        MonoDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false }
        ) {
            workspaces.forEach { workspace ->
                val isSelected = workspace.id == selectedWorkspace?.id
                MonoDropdownItem(
                    label = workspace.name,
                    isSelected = isSelected,
                    onClick = { onWorkspaceSelected(workspace); expanded = false },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            MonoDropdownActionItem(
                label = "New workspace",
                iconRes = R.drawable.ic_add_m3,
                onClick = { onAddWorkspace(); expanded = false }
            )
        }
    }
}



// ========== Previews ==========
private val previewWorkspaces = listOf(
    Workspace(id = "1", name = "Personal"),
    Workspace(id = "2", name = "Work"),
    Workspace(id = "3", name = "Side Projects")
)

@Preview(showBackground = true, name = "WorkspaceDropdown, Collapsed")
@Composable
private fun WorkspaceDropdownCollapsedPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WorkspaceDropdownGlass(
                workspaces        = previewWorkspaces,
                selectedWorkspace = previewWorkspaces.first(),
                onWorkspaceSelected = {},
                onAddWorkspace    = {},
                previewExpanded   = false
            )
        }
    }
}

@Preview(showBackground = true, name = "WorkspaceDropdown, Expanded")
@Composable
private fun WorkspaceDropdownExpandedPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WorkspaceDropdownGlass(
                workspaces        = previewWorkspaces,
                selectedWorkspace = previewWorkspaces.first(),
                onWorkspaceSelected = {},
                onAddWorkspace    = {},
                previewExpanded   = true
            )
        }
    }
}