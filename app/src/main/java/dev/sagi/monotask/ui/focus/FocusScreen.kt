package dev.sagi.monotask.ui.focus

import dev.sagi.monotask.ui.component.EmptyState
import dev.sagi.monotask.ui.component.TaskCard
import dev.sagi.monotask.ui.component.TopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.HeroGreeting
import dev.sagi.monotask.ui.component.LoadingSpinner
import dev.sagi.monotask.ui.navigation.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme
// import dev.sagi.monotask.ui.component.LoadingSpinner // (Use yours if you have it)

// STATEFUL WRAPPER (Used by NavGraph)
@Composable
fun FocusScreen(
    navController: NavHostController,
    viewModel: FocusViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

    FocusScreenContent(
        uiState = uiState,
        hazeState = hazeState,
        onProfileClick = { /* TODO */ }
    )
}

// STATELESS CONTENT (Used for UI & Previews)
@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    hazeState: HazeState,
    onProfileClick: () -> Unit
) {
    // Grab the exact height of the app's bottom bar
    val innerPadding = LocalScaffoldPadding.current

    val currentWorkspace = (uiState as? FocusUiState.Active)?.workspace
        ?: Workspace(id = "1", name = "Workspace")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .haze(hazeState)
    ) {
        // Apply the scaffold padding exactly here!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // ========== Top Bar + User Greeting ==========
            TopBar(
                workspaces = listOf(currentWorkspace),
                selectedWorkspace = currentWorkspace,
                onWorkspaceSelected = { },
                onAddWorkspace = { },
                onAddTaskClick = {}
            )

            HeroGreeting(
                userName = "Sagi",
                hazeState = hazeState
            )

            // ========== FocusCard / EmptyState ==========
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
        }
    }

    // Dummy text to test bottom nav bar's bg blur
//    Box (
//        contentAlignment = Alignment.BottomCenter,
//        modifier = Modifier.fillMaxSize().padding(vertical = 50.dp)
//    ) {
//        Text(
//            text = "Just some long ass text",
//            fontSize = 64.sp,
//            modifier = Modifier
//        )
//    }

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