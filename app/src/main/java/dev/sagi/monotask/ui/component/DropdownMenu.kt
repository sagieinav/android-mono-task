import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask

@Composable
fun WorkspaceDropdown(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
    previewExpanded: Boolean = false // Added for the preview!
) {
    var expanded by remember { mutableStateOf(previewExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    Box(modifier = modifier) {

// ========== Trigger pill ==========
        Row(
            modifier = Modifier
                .basicMonoTask(CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceBright)
                .clickable { expanded = !expanded }
                .padding(
                    start = 16.dp, end = 10.dp,
                    top = 8.dp, bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedWorkspace?.name ?: "Workspace",
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

        // ========== Open Menu ==========
        MaterialTheme() {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                shape = MaterialTheme.shapes.medium
            ) {
                // Workspace items
                workspaces.forEach { workspace ->
                    val isSelected = workspace.id == selectedWorkspace?.id

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = workspace.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(18.dp),
//                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onWorkspaceSelected(workspace)
                            expanded = false
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp) // has 8dp vertical by default
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else Color.Transparent
                            )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // "Add workspace" item
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "New Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "Add Workspace",
                            modifier = Modifier.size(18.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onAddWorkspace()
                        expanded = false
                    },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

// ========== Previews ==========

@Preview(showBackground = true, name = "1. Closed State")
@Composable
fun WorkspaceDropdownClosedPreview() {
    val hazeState = remember { HazeState() }
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WorkspaceDropdown(
                workspaces = listOf(Workspace(id = "1", name = "University")),
                selectedWorkspace = Workspace(id = "1", name = "University"),
                onWorkspaceSelected = {}, onAddWorkspace = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Open State")
@Composable
fun WorkspaceDropdownOpenPreview() {
    val workspaces = listOf(
        Workspace(id = "1", name = "MonoTask Project"),
        Workspace(id = "2", name = "Education"),
        Workspace(id = "3", name = "Work"),
    )
    val hazeState = remember { HazeState() }

    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
//                .padding(16.dp)
        ) {
            WorkspaceDropdown(
                workspaces = workspaces,
                selectedWorkspace = workspaces[0],
                onWorkspaceSelected = {}, onAddWorkspace = {},
                previewExpanded = true,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}