package dev.sagi.monotask.ui.settings.component

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.designsystem.component.MonoConfirmDialog
import dev.sagi.monotask.designsystem.component.GlassSurface
import dev.sagi.monotask.designsystem.component.SwipeRevealAction
import dev.sagi.monotask.designsystem.component.SwipeRevealRow
import dev.sagi.monotask.designsystem.component.MonoTextInputDialog
import dev.sagi.monotask.designsystem.component.SectionTitle
import dev.sagi.monotask.ui.common.CreateWorkspaceDialog
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.penaltyRed
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.designsystem.util.Constants.Theme.TRAILING_BUTTON_SIZE

@Composable
internal fun WorkspacesSection(
    workspaces       : List<Workspace>,
    onCreateWorkspace: (String) -> Unit,
    onRenameWorkspace: (Workspace, String) -> Unit,
    onDeleteWorkspace: (Workspace) -> Unit
) {
    var expanded         by remember { mutableStateOf(false) }
    var renameTarget     by remember { mutableStateOf<Workspace?>(null) }
    var deleteTarget     by remember { mutableStateOf<Workspace?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label       = "workspaces_chevron"
    )

    val shape = MaterialTheme.shapes.large

    SectionTitle("Workspaces")

    GlassSurface(
        blurred  = false,
        modifier = Modifier.fillMaxWidth(),
        shape    = shape
    ) {
        Column {
            // Header row
            SettingsRow(
                leadingIcon = {
                    SettingsRowIcon(
                        IconPack.Workspace,
                        color = SettingsIconColors.workspace
                    )
                },
                onClick = { expanded = !expanded },
                trailingContent = {
                    Icon(
                        painter = painterResource(IconPack.Chevron),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(TRAILING_BUTTON_SIZE)
                            .rotate(chevronRotation)
                    )
                }
            ) {
                // Header text content with expandable subtitle
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Manage workspaces",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Text(
                            text = "Swipe right to edit, left to delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsDivider(startPadding = SCREEN_PADDING)

                    Spacer(Modifier.height(8.dp))

                    workspaces.forEach { workspace ->
                        SwipeRevealRow(
                            modifier = Modifier.padding(horizontal = SCREEN_PADDING),
                            startAction = SwipeRevealAction(
                                color       = MaterialTheme.colorScheme.primary,
                                icon        = IconPack.Edit,
                                label       = "Edit",
                                onTriggered = { renameTarget = workspace }
                            ),
                            endAction = if (workspaces.size > 1) SwipeRevealAction(
                                color       = penaltyRed,
                                icon        = IconPack.Delete,
                                label       = "Delete",
                                onTriggered = { deleteTarget = workspace }
                            ) else null
                        ) {
                            SettingsRow(verticalPadding = 8.dp) {
                                Text(
                                    text = workspace.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    SettingsDivider(startPadding = SCREEN_PADDING)

                    // "New workspace" button
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateDialog = true }
                            .padding(horizontal = 16.dp, vertical = 22.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter            = painterResource(IconPack.AddM3),
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text       = "New workspace",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    renameTarget?.let { workspace ->
        MonoTextInputDialog(
            title        = "Rename Workspace",
            placeholder  = workspace.name,
            confirmLabel = "Save",
            onConfirm    = { onRenameWorkspace(workspace, it); renameTarget = null },
            onDismiss    = { renameTarget = null }
        )
    }

    deleteTarget?.let { workspace ->
        MonoConfirmDialog(
            onDismissRequest = { deleteTarget = null },
            title            = "Delete '${workspace.name}'?",
            message          = "This will permanently delete the workspace and all its tasks.",
            confirmLabel     = "Delete",
            dismissLabel     = "Cancel",
            confirmColor     = penaltyRed,
            onConfirm        = { onDeleteWorkspace(workspace); deleteTarget = null }
        )
    }

    if (showCreateDialog) {
        CreateWorkspaceDialog(
            onConfirm = { onCreateWorkspace(it); showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }
}

// ==========================================
// Preview
// ==========================================

private val previewWorkspaces = listOf(
    Workspace(id = "1", name = "Health & Fitness"),
    Workspace(id = "2", name = "Learning"),
    Workspace(id = "3", name = "Personal"),
    Workspace(id = "4", name = "Side Projects"),
    Workspace(id = "5", name = "Work")
)

@Preview(showBackground = true, name = "Workspaces Section")
@Composable
private fun WorkspacesSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                WorkspacesSection(
                    workspaces        = previewWorkspaces,
                    onCreateWorkspace = {},
                    onRenameWorkspace = { _, _ -> },
                    onDeleteWorkspace = {}
                )
            }
        }
    }
}