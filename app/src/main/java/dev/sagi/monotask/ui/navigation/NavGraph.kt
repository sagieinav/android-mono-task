package dev.sagi.monotask.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
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
import dev.sagi.monotask.ui.profile.ProfileViewModel
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
    // unknown route (Settings, Auth, etc.): always treat as forward
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
    profileVM: ProfileViewModel,
    settingsVM: SettingsViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel
) {
    val settingsState by settingsVM.uiState.collectAsState()
    val authState by authVM.uiState.collectAsState()

    if (settingsState.loading || authState is AuthUiState.Loading) {
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

    val hardcoreMode = settingsState.hardcoreModeEnabled

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
                FocusScreen(
                    navController = navController,
                    focusVM = focusVM,
                    workspaceVM = workspaceVM,
                    userSessionVM = userSessionVM
                )
            }
            composable(Screen.Kanban.route) {
                val kanbanVM: KanbanViewModel = viewModel()
                if (hardcoreMode) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    KanbanScreen(
                        navController = navController,
                        workspaceVM = workspaceVM,
                        kanbanVM = kanbanVM
                    )
                }
            }
//            composable(Screen.Profile.route) {
//                ProfileScreen(
//                    navController = navController,
//                    viewModel = profileVM
//                )
//            }
//            composable(Screen.Settings.route) {
//                SettingsScreen(
//                    navController = navController,
//                    viewModel = settingsVM
//                )
//            }
        }
    }
}
