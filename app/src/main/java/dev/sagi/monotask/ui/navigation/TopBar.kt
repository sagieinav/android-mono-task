package dev.sagi.monotask.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.GlassTabRow
import dev.sagi.monotask.ui.component.core.SegmentedToggle
import dev.sagi.monotask.ui.component.workspace.WorkspaceDropdownGlass
import dev.sagi.monotask.ui.kanban.KanbanEvent
import dev.sagi.monotask.ui.kanban.KanbanUiState
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.util.Constants
import dev.sagi.monotask.util.Constants.Theme.TOP_BAR_ITEM_HEIGHT


// ==========================================
// Generic core. Not used directly outside 'navigation/'
// ==========================================
@Composable
private fun TopBarScaffold(
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
//                top = 8.dp
            )
            .heightIn(min = 54.dp, max = 96.dp)
            .background(Color.Transparent)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        trailing()
    }
}


// ==========================================
// Workspace TopBar (Focus + Kanban)
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
        },
        trailing = {
            TopBarIconButton(
                iconRes = R.drawable.ic_add,
                contentDescription = "Create new task",
                onClick = onAddTaskClick
            )
        }
    )
}

@Composable
fun KanbanTopBar(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    isArchive: Boolean,
    onToggleArchive: () -> Unit,
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
        },
        trailing = {
            TopBarIconButton(
                iconRes = R.drawable.ic_archive,
                contentDescription = "Toggle Archive",
                isPressed = isArchive,
                onClick = onToggleArchive
            )
        }
    )
}


// ==========================================
// Tab TopBar
// ==========================================
@Composable
fun TabbedTopBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    TopBarScaffold(
        modifier = modifier,
        leading = {
            GlassTabRow(
                tabs          = tabs,
                selectedIndex = selectedTab,
                onTabSelected = onTabSelected,
                modifier      = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .height(TOP_BAR_ITEM_HEIGHT)
                    .padding(end = 12.dp)
            )
        },
        trailing = { trailingIcon?.invoke() }
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
                fontWeight = FontWeight.Bold,
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
            .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
//            .monoShadowWorkaround(CircleShape)
            // "remove" shadow when button is pressed:
            .then(
                if (isPressed) Modifier
                else Modifier.monoShadowWorkaround(CircleShape)
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
        baseColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .size(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
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
