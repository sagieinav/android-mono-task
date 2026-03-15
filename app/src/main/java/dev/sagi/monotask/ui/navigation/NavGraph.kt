package dev.sagi.monotask.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.auth.AuthScreen
import dev.sagi.monotask.ui.auth.AuthUiState
import dev.sagi.monotask.ui.auth.OnboardingScreen
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.focus.FocusScreen
import dev.sagi.monotask.ui.focus.FocusViewModel
import dev.sagi.monotask.ui.kanban.KanbanScreen
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.profile.ProfileScreen
import dev.sagi.monotask.ui.profile.ProfileViewModel
import dev.sagi.monotask.ui.settings.SettingsUiState
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel

private val TAB_ORDER = listOf(
    Screen.Kanban.route,
    Screen.Focus.route,
    Screen.Profile.route
)

private const val navAnimationDuration = 300

// ========== Animation Helpers ==========
private fun isForward(from: String?, to: String?): Boolean {
    val fromIndex = TAB_ORDER.indexOf(from)
    val toIndex = TAB_ORDER.indexOf(to)
    if (fromIndex == -1 || toIndex == -1) return true
    return toIndex > fromIndex
}

fun tabSlideIn(from: String?, to: String?): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { if (isForward(from, to)) it else -it },
        animationSpec = tween(navAnimationDuration, easing = FastOutSlowInEasing)
    )

fun tabSlideOut(from: String?, to: String?): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { if (isForward(from, to)) -it else it },
        animationSpec = tween(navAnimationDuration, easing = FastOutSlowInEasing)
    )


// ========== Navigation Graph ==========
@Composable
fun NavGraph(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel
) {
    val settingsState by settingsVM.uiState.collectAsStateWithLifecycle()
    val authState by authVM.uiState.collectAsStateWithLifecycle()

    val isLoading = authState is AuthUiState.Loading || settingsState is SettingsUiState.Loading
    if (isLoading) {
        LoadingSpinner()
        return
    }

    val startDestination = if (authState is AuthUiState.SignedIn)
        Screen.Main.route else Screen.Auth.route

    LaunchedEffect(authState) {
        if (authState is AuthUiState.SignedOut) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val hardcoreMode = (settingsState as? SettingsUiState.Ready)?.hardcoreModeEnabled ?: false

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { tabSlideIn(initialState.destination.route, targetState.destination.route) },
        exitTransition = { tabSlideOut(initialState.destination.route, targetState.destination.route) },
        popEnterTransition = { tabSlideIn(initialState.destination.route, targetState.destination.route) },
        popExitTransition = { tabSlideOut(initialState.destination.route, targetState.destination.route) }
    ) {
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.Auth.route
        ) {
            composable(Screen.Login.route) {
                AuthScreen(
                    authViewModel = authVM,
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToMain = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    authViewModel = authVM,
                    onFinish = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        navigation(startDestination = Screen.Focus.route, route = Screen.Main.route) {
            composable(Screen.Focus.route) {
                val focusVM: FocusViewModel = viewModel()
                // Wire workspace source once. Observation starts automatically in init
                focusVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
                FocusScreen(
                    navController = navController,
                    focusVM       = focusVM,
//                    userSessionVM = userSessionVM
                )
            }
            composable(Screen.Kanban.route) {
                val kanbanVM: KanbanViewModel = viewModel()
                kanbanVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
                if (hardcoreMode) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    KanbanScreen(
                        navController = navController,
                        kanbanVM      = kanbanVM
                    )
                }
            }
            composable(Screen.Profile.route) {
                val profileVM: ProfileViewModel = viewModel()
                LaunchedEffect(Unit) {
                    profileVM.startObserving(userSessionVM.currentUser)
                }
                ProfileScreen(
                    navController = navController,
                    profileVM     = profileVM
                )
            }
        }
    }
}
