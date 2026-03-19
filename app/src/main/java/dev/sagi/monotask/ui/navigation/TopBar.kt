package dev.sagi.monotask.ui.navigation

import android.R.id.tabs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.core.GlassTabRow
import dev.sagi.monotask.ui.component.workspace.WorkspaceDropdownGlass
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.util.Constants


// ==========================================
// Generic core. Not used directly outside 'navigation/'
// ==========================================
@Composable
private fun TopBarScaffold(
//    leading: @Composable () -> Unit,
//    trailing: @Composable () -> Unit,
    leading: @Composable RowScope.() -> Unit,
    trailing: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // standard for top/bottom bar, to avoid notch, etc...
            .statusBarsPadding()
            .padding(
                // horizontal: normal
                start = Constants.Theme.SCREEN_PADDING,
                end = Constants.Theme.SCREEN_PADDING,
                // bottom: add a little bit on top of statusBarsPadding
//                bottom = Constants.Theme.SCREEN_PADDING / 2
            )
            .heightIn(min = 64.dp, max = 96.dp)
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


// ==========================================
// Title TopBar (Profile + Settings)
// ==========================================
@Composable
fun TitleTopBar(
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    TopBarScaffold(
        modifier = modifier,
        leading = {
            GlassSurface(
//                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                )
            }
        },
        trailing = { trailingIcon?.invoke() }
    )
}

@Composable
fun TitleTopBarV2(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val localHaze       = rememberHazeState()
    TopBarScaffold(
        modifier = modifier,
        leading = {
            CompositionLocalProvider(LocalHazeState provides localHaze) {

                GlassTabRow(
                    tabs          = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = onTabSelected,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .weight(1f)
//                        .padding(vertical = 4.dp, horizontal = 12.dp)
                        .padding(end = 12.dp)
                )
            }
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
    modifier: Modifier = Modifier
) {
    GlassSurface(
//        blurred = false,
        shape = CircleShape,
//        baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
            .monoShadowWorkaround(CircleShape)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
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

@Preview(showBackground = true, name = "Title TopBar — with icon")
@Composable
private fun TitleTopBarWithIconPreview() {
    MonoTaskTheme {
        TitleTopBar(
            title = "Sagi Einav",
            trailingIcon = {
                TopBarIconButton(
                    iconRes = R.drawable.ic_settings,
                    contentDescription = "Settings",
                    onClick = {}
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "Title TopBar — no icon")
@Composable
private fun TitleTopBarNoIconPreview() {
    MonoTaskTheme {
        TitleTopBar(title = "Settings")
    }
}
