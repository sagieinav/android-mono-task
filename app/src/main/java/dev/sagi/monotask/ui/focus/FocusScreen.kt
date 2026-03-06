package dev.sagi.monotask.ui.focus

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

    FocusScreenContent(
        uiState = uiState,
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            HeroGreeting(userName = "Sagi")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is FocusUiState.Loading -> LoadingSpinner()
                    is FocusUiState.Empty -> EmptyState()
                    is FocusUiState.Active -> ActiveFocusCard(
                        uiState = uiState,
                        onCompleteTask = onCompleteTask,
                        onOpenSnooze = onOpenSnooze
                    )
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

// ========== Active state: currently active FocusCard ==========

@Composable
private fun ActiveFocusCard(
    uiState: FocusUiState.Active,
    onCompleteTask: () -> Unit,
    onOpenSnooze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
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
