package dev.sagi.monotask.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.GlassConfirmDialog
import dev.sagi.monotask.ui.component.core.GlassDropdownActionItem
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.GlassTooltip
import dev.sagi.monotask.ui.component.core.TextInputDialog
import dev.sagi.monotask.ui.component.display.SectionTitle
import dev.sagi.monotask.ui.component.workspace.CreateWorkspaceDialog
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.penaltyRed
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING


// ==========================================
// Generic section container
// ==========================================

@Composable
internal fun SettingsSection(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    SectionTitle(
        text     = title,
//        modifier = Modifier.padding(bottom = 8.dp)
    )
    GlassSurface(
        blurred  = false,
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        baseColor = MaterialTheme.colorScheme.surfaceContainer // make it a bit brighter
//        accentColor = MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier            = Modifier.padding(vertical = 12.dp),
            content             = content
        )
    }
}


// ==========================================
// Generic row container
// ==========================================

@Composable
internal fun SettingsRow(
    modifier        : Modifier = Modifier,
    leadingIcon     : (@Composable () -> Unit)? = null,
    trailingContent : (@Composable () -> Unit)? = null,
    onClick         : (() -> Unit)? = null,
    verticalPadding : Dp = 0.dp,
    content         : @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null)
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        leadingIcon?. let {
            it()
            Spacer(Modifier.width(12.dp))
        }
        Box(Modifier.weight(1f)) { content() }
        trailingContent?.invoke()
    }
}


// ==========================================
// Info icon button with tooltip (shared)
// ==========================================

@Composable
private fun InfoIconButton(text: String, size: Dp = 20.dp) {
    var showTooltip by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick  = { showTooltip = true },
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(size + 2.dp)
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_info_circle),
                contentDescription = "More info",
                modifier           = Modifier.size(size),
                tint               = MaterialTheme.colorScheme.outlineVariant
            )
        }
        GlassTooltip(
            expanded  = showTooltip,
            onDismiss = { showTooltip = false }
        ) {
            Text(text)
        }
    }
}


// ==========================================
// Slider colors helper
// ==========================================

@Composable
private fun settingsSliderColors() = SliderDefaults.colors().copy(
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    activeTickColor    = MaterialTheme.colorScheme.surfaceContainerHighest,
)


// ==========================================
// Sections
// ==========================================

@Composable
internal fun FocusPreferencesSection(
    hardcoreModeEnabled: Boolean,
    dueDateWeight      : Float,
    onUpdatePreferences: (hardcoreMode: Boolean?, notifications: Boolean?, dueSoon: Int?) -> Unit,
    onUpdateWeights    : (dueDateWeight: Float, importanceWeight: Float) -> Unit
) {
    var localDueDateWeight by remember(dueDateWeight) { mutableFloatStateOf(dueDateWeight) }

    SettingsSection("Focus Preferences") {
        SettingsToggleRow(
            label    = "Hyperfocus",
            checked  = hardcoreModeEnabled,
            onChange = { onUpdatePreferences(it, null, null) },
            infoText = "Prevents access to the Kanban Board, so you can only focus on one task at a time!"
        )

        SettingsDivider()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text       = "Task priority calculation",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal
                    )
                    InfoIconButton(
                        text = "Weight ratio of the tasks' due date VS importance. Controls how tasks are prioritized."
                    )
                }
            }
            Slider(
                value                 = localDueDateWeight,
                onValueChange         = { localDueDateWeight = it },
                onValueChangeFinished = { onUpdateWeights(localDueDateWeight, 1f - localDueDateWeight) },
                valueRange            = 0f..1f,
                colors                = settingsSliderColors()
            )
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Due Date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
                Text(
                    text  = "Importance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    }
}

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
    val iconSize = 22.dp

    SectionTitle("Workspaces")

    GlassSurface(
        blurred  = false,
        modifier = Modifier.fillMaxWidth(),
        shape    = shape
    ) {
        Column {
            // Header row (always visible)
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Manage workspaces",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal
                )
                Icon(
                    painter            = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier
                        .size(22.dp)
                        .rotate(chevronRotation)
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .padding(bottom = SCREEN_PADDING)
//                        .padding(horizontal = SCREEN_PADDING)
                    ,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsDivider()

                    workspaces.forEach { workspace ->
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .padding(horizontal = SCREEN_PADDING)
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment     = Alignment.CenterVertically
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text       = workspace.name,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Normal,
                                modifier   = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { renameTarget = workspace },
                                modifier = Modifier.size(iconSize + 8.dp)
                            ) {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_edit),
                                    contentDescription = "Rename ${workspace.name}",
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick  = { deleteTarget = workspace },
                                enabled  = workspaces.size > 1,
                                modifier = Modifier.size(iconSize + 8.dp)
                            ) {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_delete),
                                    contentDescription = "Delete ${workspace.name}",
                                    modifier           = Modifier.size(18.dp),
                                    tint               = if (workspaces.size > 1)
                                        penaltyRed.copy(alpha = 0.8f)
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    Column {
                        SettingsDivider()

                        // "New workspace" button
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
//                            .padding(top = 8.dp, bottom = 16.dp)
//                                .clip(MaterialTheme.shapes.large)
                                .clickable { showCreateDialog = true }
                                .padding(horizontal = 16.dp)
                                .padding(top = 14.dp, bottom = 18.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_add_m3),
                                contentDescription = null,
                                modifier           = Modifier.size(16.dp),
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text       = "New Workspace",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                }
            }
        }
    }

    // Dialogs
    renameTarget?.let { workspace ->
        TextInputDialog(
            title        = "Rename Workspace",
            placeholder  = workspace.name,
            confirmLabel = "Save",
            onConfirm    = { onRenameWorkspace(workspace, it); renameTarget = null },
            onDismiss    = { renameTarget = null }
        )
    }

    deleteTarget?.let { workspace ->
        GlassConfirmDialog(
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

@Composable
internal fun AccountSection(
    displayName        : String,
    email              : String,
    onUpdateDisplayName: (String) -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }

    SettingsSection("Account") {
        SettingsInfoRow(
            label  = "Name",
            value  = displayName.ifEmpty { "—" },
            onEdit = { showEditNameDialog = true }
        )

        SettingsDivider()

        SettingsInfoRow(
            label = "Email",
            value = email.ifEmpty { "—" }
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

@Composable
internal fun AboutSection() {
    SettingsSection("About") {
        AboutRow(label = "Help / FAQ")
        SettingsDivider()
        AboutRow(label = "Send Feedback", showLinkIcon = true)
        SettingsDivider()
        AboutRow(label = "Rate the app", showLinkIcon = true)
        SettingsDivider()
        AboutRow(label = "Developer", showLinkIcon = true)
    }
}

@Composable
internal fun SignOutButton(onSignOut: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    ActionButton(
        onClick  = { showConfirm = true },
        color    = penaltyRed,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text       = "Sign Out",
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (showConfirm) {
        GlassConfirmDialog(
            onDismissRequest = { showConfirm = false },
            title            = "Sign Out",
            message          = "Are you sure you want to sign out?",
            confirmLabel     = "Sign Out",
            dismissLabel     = "Cancel",
            confirmColor     = penaltyRed,
            onConfirm        = onSignOut
        )
    }
}


// ==========================================
// Row helpers
// ==========================================

@Composable
private fun SettingsToggleRow(
    label   : String,
    checked : Boolean,
    onChange: (Boolean) -> Unit,
    infoText: String? = null
) {
    SettingsRow(
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal
            )
            infoText?.let { InfoIconButton(it) }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    label       : String,
    value       : String,
    leadingIcon : (@Composable () -> Unit)? = null,
    onEdit      : (() -> Unit)? = null
) {
    SettingsRow(
        leadingIcon     = leadingIcon,
        trailingContent = if (onEdit != null) {{
            IconButton(onClick = onEdit) {
                Icon(
                    painter            = painterResource(R.drawable.ic_edit),
                    contentDescription = "Edit $label",
                    modifier           = Modifier.size(20.dp)
                )
            }
        }} else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
//                fontWeight = FontWeight.Thin,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun AboutRow(
    label       : String,
    showLinkIcon: Boolean = false,
    onClick     : () -> Unit = {}
) {
    SettingsRow(
        onClick         = onClick,
        verticalPadding = 6.dp,
        trailingContent = if (showLinkIcon) {{
            Icon(
                painter            = painterResource(R.drawable.ic_external_link),
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }} else null
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
internal fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(horizontal = SCREEN_PADDING)
    )
}


// ==========================================
// Previews
// ==========================================

private val previewWorkspaces = listOf(
    Workspace(id = "1", name = "Personal"),
    Workspace(id = "2", name = "University"),
    Workspace(id = "3", name = "Side Projects")
)

@Preview(showBackground = true, name = "FocusPreferences Section")
@Composable
private fun FocusPreferencesSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                FocusPreferencesSection(
                    hardcoreModeEnabled = false,
                    dueDateWeight       = 0.6f,
                    onUpdatePreferences = { _, _, _ -> },
                    onUpdateWeights     = { _, _ -> }
                )
            }
        }
    }
}

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

@Preview(showBackground = true, name = "Account Section")
@Composable
private fun AccountSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                AccountSection(
                    displayName         = "Sagi Einav",
                    email               = "sagi@example.com",
                    onUpdateDisplayName = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "About Section")
@Composable
private fun AboutSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                AboutSection()
            }
        }
    }
}

@Preview(showBackground = true, name = "Sign Out Button")
@Composable
private fun SignOutButtonPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                SignOutButton(onSignOut = {})
            }
        }
    }
}
