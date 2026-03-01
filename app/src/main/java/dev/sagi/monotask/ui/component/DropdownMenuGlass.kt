import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.GlassSurface
import dev.sagi.monotask.ui.theme.basicMonoTask

// ==========================================
// 1. MAIN COMPONENT (State & Structure)
// ==========================================
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
        // Trigger Pill
        WorkspaceDropdownTrigger(
            workspaceName = selectedWorkspace?.name ?: "Workspace",
            expanded = expanded,
            onClick = { expanded = !expanded }
        )

        // Glass Dropdown Menu
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 130),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                GlassSurface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .widthIn(min = 100.dp, max = 220.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        // Items List
                        workspaces.forEach { workspace ->
                            WorkspaceMenuItem(
                                workspace = workspace,
                                isSelected = workspace.id == selectedWorkspace?.id,
                                onClick = {
                                    onWorkspaceSelected(workspace)
                                    expanded = false
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Action Item
                        AddWorkspaceMenuItem(
                            onClick = {
                                onAddWorkspace()
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. MODULAR SUB-COMPONENTS
// ==========================================

@Composable
private fun WorkspaceDropdownTrigger(
    workspaceName: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    // Moved the animation logic inside the trigger so the main component stays clean!
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    Row(
        modifier = Modifier
            .basicMonoTask(CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceBright)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = workspaceName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 100.dp)
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_down),
            contentDescription = "Toggle workspace menu",
            modifier = Modifier.rotate(chevronRotation),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WorkspaceMenuItem(
    workspace: Workspace,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f)
                else Color.Transparent
            )
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = workspace.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = "Selected",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddWorkspaceMenuItem(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add Workspace",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "New Workspace",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}