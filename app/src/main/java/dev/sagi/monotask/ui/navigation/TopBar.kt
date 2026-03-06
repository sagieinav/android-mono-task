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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.workspace.WorkspaceDropdownGlass
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask
import dev.sagi.monotask.ui.theme.monoShadow


// ─────────────────────────────────────────
// Generic core — not used directly outside navigation/
// ─────────────────────────────────────────
@Composable
private fun TopBarScaffold(
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        trailing()
    }
}


// ─────────────────────────────────────────
// Workspace TopBar (Focus + Kanban)
// ─────────────────────────────────────────
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


// ─────────────────────────────────────────
// Title TopBar (Profile + Settings)
// ─────────────────────────────────────────
@Composable
fun TitleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    TopBarScaffold(
        modifier = modifier,
        leading = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        trailing = { trailingIcon?.invoke() }
    )
}


// ─────────────────────────────────────────
// Shared icon button pill
// ─────────────────────────────────────────
@Composable
fun TopBarIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        blurred = true,
        shape = CircleShape,
        modifier = modifier
            .monoShadow(CircleShape)
            .size(40.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add task",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp)  // ← padding sizes the card, no Box needed
        )
    }

}


// ─────────────────────────────────────────
// Previews
// ─────────────────────────────────────────
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
