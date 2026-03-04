package dev.sagi.monotask.ui.navigation

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
import dev.sagi.monotask.ui.shared.WorkspaceViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authVM: AuthViewModel,
    workspaceVM: WorkspaceViewModel,
    profileVM: ProfileViewModel,
    settingsVM: SettingsViewModel
) {
    val settingsState by settingsVM.uiState.collectAsState()
    val authState by authVM.uiState.collectAsState()

    // Wait for both settings AND auth to resolve before rendering NavHost
    if (settingsState.loading || authState is AuthUiState.Loading) {
        LoadingSpinner()
        return
    }

    // Auth is resolved. Pick the correct start destination
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
        startDestination = startDestination
    ) {

        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
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
                    viewModel = focusVM,
                    sharedWorkspaceVM = workspaceVM   // ← added
                )
            }

            composable(Screen.Kanban.route) {
                val kanbanVM: KanbanViewModel = viewModel()
                if (hardcoreMode) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    KanbanScreen(
                        navController = navController,
                        sharedWorkspaceVM = workspaceVM,  // ← added
                        viewModel = kanbanVM
                    )
                }
            }
        }
    }
}
