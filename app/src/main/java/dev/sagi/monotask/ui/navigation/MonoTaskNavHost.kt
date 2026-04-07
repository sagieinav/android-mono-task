package dev.sagi.monotask.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import dev.sagi.monotask.designsystem.animation.MonoAnimations
import dev.sagi.monotask.designsystem.animation.tabSlideIn
import dev.sagi.monotask.designsystem.animation.tabSlideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import dev.sagi.monotask.designsystem.components.MonoLoadingIndicator
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.auth.AuthUiState
import dev.sagi.monotask.ui.auth.authGraph
import dev.sagi.monotask.ui.brief.briefGraph
import dev.sagi.monotask.ui.focus.focusGraph
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.kanban.kanbanGraph
import dev.sagi.monotask.ui.profile.profileGraph
import dev.sagi.monotask.ui.settings.SettingsUiState
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.settings.settingsGraph
import dev.sagi.monotask.ui.statistics.statisticsGraph
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel

@Composable
fun MonoTaskNavHost(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel,
    kanbanVM: KanbanViewModel
) {
    val settingsState by settingsVM.uiState.collectAsStateWithLifecycle()
    val authState by authVM.uiState.collectAsStateWithLifecycle()

    val hyperfocusMode = (settingsState as? SettingsUiState.Ready)?.hyperfocusModeEnabled ?: false
    val isLoading = authState is AuthUiState.Loading || settingsState is SettingsUiState.Loading

    var initialStartDestination by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(authState, settingsState) {
        if (initialStartDestination == null && !isLoading) {
            initialStartDestination = if (authState is AuthUiState.SignedIn) MainRoute else AuthRoute
        }
    }

    val startDestination = initialStartDestination

    if (startDestination == null) {
        MonoLoadingIndicator()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(authState) {
            if (authState is AuthUiState.SignedOut) {
                navController.navigate(AuthRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                if (targetState.destination.hasRoute<SettingsRoute>())
                    slideInVertically(tween(MonoAnimations.TAB_TRANSITION_MS, easing = FastOutSlowInEasing)) { it }
                else
                    tabSlideIn(isForward(initialState.destination.route, targetState.destination.route))
            },
            exitTransition = {
                if (targetState.destination.hasRoute<SettingsRoute>())
                    fadeOut(tween(MonoAnimations.TAB_TRANSITION_MS))
                else
                    tabSlideOut(isForward(initialState.destination.route, targetState.destination.route))
            },
            popEnterTransition = {
                if (initialState.destination.hasRoute<SettingsRoute>())
                    fadeIn(tween(MonoAnimations.TAB_TRANSITION_MS))
                else
                    tabSlideIn(isForward(initialState.destination.route, targetState.destination.route))
            },
            popExitTransition = {
                if (initialState.destination.hasRoute<SettingsRoute>())
                    slideOutVertically(tween(MonoAnimations.TAB_TRANSITION_MS, easing = FastOutSlowInEasing)) { it }
                else
                    tabSlideOut(isForward(initialState.destination.route, targetState.destination.route))
            }
        ) {
            authGraph(navController, authVM)

            navigation<MainRoute>(startDestination = FocusRoute) {
                focusGraph(workspaceVM, userSessionVM)
                kanbanGraph(navController, kanbanVM, workspaceVM, userSessionVM, hyperfocusMode)
                profileGraph(userSessionVM)
                statisticsGraph()
                briefGraph(userSessionVM)
                settingsGraph(settingsVM)
            }
        }

        if (isLoading) {
            MonoLoadingIndicator()
        }
    }
}
