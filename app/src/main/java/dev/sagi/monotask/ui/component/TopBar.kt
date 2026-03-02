package dev.sagi.monotask.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask

@Composable
fun TopBar(
    workspaces: List<Workspace>,
    selectedWorkspace: Workspace?,
    onWorkspaceSelected: (Workspace) -> Unit,
    onAddWorkspace: () -> Unit,
    onAddTaskClick: () -> Unit, // NEW: Trigger for task creation
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
        // ========== Left: Workspace Dropdown ==========
        WorkspaceDropdownGlass(
            workspaces = workspaces,
            selectedWorkspace = selectedWorkspace,
            onWorkspaceSelected = onWorkspaceSelected,
            onAddWorkspace = onAddWorkspace
        )

        // ========== Right: Add Task Button ==========
        Box(
            modifier = Modifier
                .basicMonoTask(CircleShape)
                .size(42.dp) // Perfectly matches the height of your Workspace pill
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceBright)
                .clickable { onAddTaskClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Create new task",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    MonoTaskTheme {
        TopBar(
            workspaces = listOf(
                Workspace(id = "1", name = "University"),
                Workspace(id = "2", name = "Personal"),
            ),
            selectedWorkspace = Workspace(id = "1", name = "University"),
            onWorkspaceSelected = {},
            onAddWorkspace = {},
            onAddTaskClick = {}
        )
    }
}
