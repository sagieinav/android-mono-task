package dev.sagi.monotask.ui.navigation

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sagi.monotask.designsystem.component.MonoDropdownItem
import dev.sagi.monotask.designsystem.component.MonoDropdownMenu
import dev.sagi.monotask.ui.kanban.SortOrder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.designsystem.component.GlassSurface
import dev.sagi.monotask.ui.common.WorkspaceDropdownGlass
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.monoShadow
import dev.sagi.monotask.designsystem.util.Constants
import dev.sagi.monotask.designsystem.util.Constants.Theme.TOP_BAR_ITEM_HEIGHT


// ==========================================
// Generic core. Not used directly outside 'navigation/'
// ==========================================
@Composable
internal fun TopBarScaffold(
    leading: @Composable RowScope.() -> Unit,
    trailing: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = Constants.Theme.SCREEN_PADDING,
                end = Constants.Theme.SCREEN_PADDING,
                top = 8.dp
            )
            .heightIn(min = 48.dp, max = 96.dp)
            .background(Color.Transparent)
        ,
//        horizontalArrangement = Arrangement.SpaceBetween,
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        leading()
        trailing()
    }
}


// ==========================================
// Workspace TopBar (Focus)
// ==========================================
@Composable
fun WorkspaceTopBar(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarScaffold(
        modifier = modifier,
        leading = {
            WorkspaceDropdownGlass(
                workspaces = workspaces,
                selectedWorkspace = selectedWorkspace,
                onWorkspaceSelected = onWorkspaceSelected,
                onAddWorkspace = onAddWorkspace
            )
            Spacer(Modifier.weight(1f))
        },
        trailing = {
            TopBarIconButton(
                iconRes = IconPack.Add,
                contentDescription = "Create new task",
                onClick = onAddTaskClick
            )
        }
    )
}


// ==========================================
// Kanban TopBar (Workspace + Sort)
// ==========================================
@Composable
fun KanbanTopBar(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    onAddTaskClick: () -> Unit,
    sortOrder: SortOrder?,
    onSortOrderChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortDropdown by remember { mutableStateOf(false) }
    // Cache last known order so the button never disappears during state transitions
    var lastSortOrder by remember { mutableStateOf(SortOrder.CREATED_DESC) }
    if (sortOrder != null) lastSortOrder = sortOrder

    TopBarScaffold(
        modifier = modifier,
        leading = {
            WorkspaceDropdownGlass(
                workspaces = workspaces,
                selectedWorkspace = selectedWorkspace,
                onWorkspaceSelected = onWorkspaceSelected,
                onAddWorkspace = onAddWorkspace
            )
            Spacer(Modifier.weight(1f))
        },
        trailing = {
            Box {
                TopBarIconButton(
                    iconRes = IconPack.Sort,
                    contentDescription = "Sort tasks",
                    onClick = { showSortDropdown = true }
                )
                MonoDropdownMenu(
                    expanded = showSortDropdown,
                    onDismiss = { showSortDropdown = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        val label = when (order) {
                            SortOrder.DUE_ASC, SortOrder.DUE_DESC -> "Due date"
                            SortOrder.CREATED_ASC, SortOrder.CREATED_DESC -> "Created"
                        }
                        val arrowIcon = when (order) {
                            SortOrder.DUE_ASC, SortOrder.CREATED_ASC -> IconPack.ArrowNarrowUp
                            SortOrder.DUE_DESC, SortOrder.CREATED_DESC -> IconPack.ArrowNarrowDown
                        }
                        MonoDropdownItem(
                            label = label,
                            trailingIconRes = arrowIcon,
                            isSelected = lastSortOrder == order,
                            onClick = {
                                onSortOrderChanged(order)
                                showSortDropdown = false
                            },
                            textStyle = MaterialTheme.typography.titleSmall,
                            showSelectedIcon = true
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            TopBarIconButton(
                iconRes = IconPack.Add,
                contentDescription = "Create new task",
                onClick = onAddTaskClick
            )
        }
    )
}



// ==========================================
// Simple title TopBar (Profile)
// ==========================================
@Composable
fun TitleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val color = MaterialTheme.colorScheme.background
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to color,
            0.86f to color,
            0.9f to color.copy(alpha = 0.8f),
            1.0f to Color.Transparent,
        )
    )
    TopBarScaffold(
        modifier = modifier
            .background(brush = gradient),
        leading = {
            leadingIcon?. let {
                it()
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        },
        trailing = { trailingIcon?.invoke() }
    )
}


// ==========================================
// Shared icon button pill
// ==========================================
@Composable
fun TopBarIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false
) {
    val bgAccentColor = if (isPressed) MaterialTheme.colorScheme.onSurface
                        else null

    GlassSurface(
        shape = CircleShape,
        modifier = modifier
            .height(TOP_BAR_ITEM_HEIGHT)
            // "remove" shadow when button is pressed:
            .then(
                if (isPressed) Modifier
                else Modifier.monoShadow(CircleShape)
            )
            // add border color if pressed:
            .then(
                if (isPressed)
                    Modifier.glassBorder(CircleShape, MaterialTheme.colorScheme.onSurface)
                else Modifier
            )
            .clip(CircleShape)
            .clickable { onClick() },
        accentColor = bgAccentColor,
        baseColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .size(TOP_BAR_ITEM_HEIGHT)
                .padding(8.dp)
        )
    }
}


// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Workspace TopBar")
@Composable
private fun WorkspaceTopBarPreview() {
    MonoTaskTheme {
        WorkspaceTopBar(
            workspaces = listOf(
                Workspace(id = "1", name = "University"),
                Workspace(id = "2", name = "Personal")
            ),
            selectedWorkspace = Workspace(id = "1", name = "University"),
            onWorkspaceSelected = {}, onAddWorkspace = {}, onAddTaskClick = {}
        )
    }
}

@Preview(showBackground = false, name = "Title TopBar")
@Composable
private fun TitleTopBarPreview() {
    MonoTaskTheme {
        TitleTopBar("MonoTask")
    }
}
