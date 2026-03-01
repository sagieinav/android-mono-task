package dev.sagi.monotask.ui.focus

import EmptyState
import TaskCard
import TopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.LoadingSpinner
import dev.sagi.monotask.ui.theme.MonoTaskTheme
// import dev.sagi.monotask.ui.component.LoadingSpinner // (Use yours if you have it)

// STATEFUL WRAPPER (Used by NavGraph)
@Composable
fun FocusScreen(
    navController: NavHostController,
    viewModel: FocusViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

    FocusScreenContent(
        uiState = uiState,
        hazeState = hazeState,
        onProfileClick = { /* TODO: navController.navigate(Screen.Profile.route) */ }
    )
}

// STATELESS CONTENT (Used for UI & Previews)
@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    hazeState: HazeState,
    onProfileClick: () -> Unit
) {
    // Extract workspace if Active, otherwise fallback to a generic one for now
    val currentWorkspace = (uiState as? FocusUiState.Active)?.workspace
        ?: Workspace(id = "1", name = "Workspace")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .haze(hazeState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ========== Top Bar ==========
            TopBar(
                userName = "Sagi",
                profilePictureUrl = null,
                workspaces = listOf(currentWorkspace),
                selectedWorkspace = currentWorkspace,
                onWorkspaceSelected = { },
                onAddWorkspace = { },
                onProfileClick = onProfileClick,
                hazeState = hazeState
            )

            // ========== Dynamic Content Box ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
//                contentAlignment = Alignment.BottomCenter
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is FocusUiState.Loading -> {
//                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        LoadingSpinner()
                    }

                    is FocusUiState.Empty -> {
                        EmptyState()
//                        EmptyState(
//                            emoji = "\uD83E\uDDBE",
//                            title = "No open tasks",
//                            subtitle = "Damn, you're a productivity machine."
//            )
                    }

                    is FocusUiState.Active -> {
                        // The Focus Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TaskCard(
                                task = uiState.focusTask,
                                modifier = Modifier.height(420.dp)
                            )
                        }
                    }
                }
            }

            // Add padding at the bottom so the FloatingNavBar doesn't cover the card
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    Box (
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize().padding(vertical = 50.dp)
    ) {
        Text(
            text = "Just some long ass text",
            fontSize = 64.sp,
            modifier = Modifier
        )
    }

}

// ========== PREVIEW SECTION ==========
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FocusScreenPreview() {
    val hazeState = remember { HazeState() }

    // Create a dummy Active state just for preview
    val dummyState = FocusUiState.Active(
        focusTask = Task(
            id = "1",
            title = "Build Swipe-to-Complete",
            description = "Implement the gesture logic for the MonoTask Focus Screen.",
            importance = Importance.HIGH,
            tags = listOf("dev", "compose"),
            dueDate = Timestamp.now()
        ),
        queue = emptyList(),
        workspace = Workspace(id = "1", name = "MonoTask Project")
    )

    MonoTaskTheme {
        FocusScreenContent(
            uiState = dummyState,
            hazeState = hazeState,
            onProfileClick = {}
        )
    }
}