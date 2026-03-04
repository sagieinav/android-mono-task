package dev.sagi.monotask.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.sagi.monotask.ui.component.core.EmptyState
import dev.sagi.monotask.ui.component.core.HeroGreeting
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding

@Composable
fun FocusScreen(
    navController: NavHostController,
    viewModel: FocusViewModel,
    sharedWorkspaceVM: WorkspaceViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.startObservingTasks(sharedWorkspaceVM.selectedWorkspace)
    }

    val uiState by viewModel.uiState.collectAsState()
    val showSnoozeSheet by viewModel.showSnoozeSheet.collectAsState()
    val hazeState = remember { HazeState() }

    FocusScreenContent(
        uiState = uiState,
        hazeState = hazeState,
        showSnoozeSheet = showSnoozeSheet,
        onCompleteTask = { viewModel.completeTask() },
        onOpenSnooze = { viewModel.openSnoozeSheet() },
        onDismissSnooze = { viewModel.dismissSnoozeSheet() },
        onSnooze = { penalty -> viewModel.snoozeTask(penalty) }
    )
}

@Composable
fun FocusScreenContent(
    uiState: FocusUiState,
    hazeState: HazeState,
    showSnoozeSheet: Boolean = false,
    onCompleteTask: () -> Unit = {},
    onOpenSnooze: () -> Unit = {},
    onDismissSnooze: () -> Unit = {},
    onSnooze: (Int) -> Unit = {}
) {
    val innerPadding = LocalScaffoldPadding.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .haze(hazeState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding())
        ) {
            HeroGreeting(userName = "Sagi", hazeState = hazeState)

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is FocusUiState.Loading -> LoadingSpinner()
                    is FocusUiState.Empty -> EmptyState()
                    is FocusUiState.Active -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FocusCardSwipeable(
                                task = uiState.focusTask,
                                onSwipeRight = onCompleteTask,
                                onSwipeLeft = onOpenSnooze,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (showSnoozeSheet) {
                SnoozeBottomSheet(
                    onDismissRequest = onDismissSnooze,
                    onSnooze = onSnooze
                )
            }
        }
    }
}
