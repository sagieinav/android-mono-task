package dev.sagi.monotask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.auth.LoginScreen
import dev.sagi.monotask.ui.auth.OnboardingScreen
import dev.sagi.monotask.ui.component.LoadingSpinner
import dev.sagi.monotask.ui.focus.FocusScreen
import dev.sagi.monotask.ui.focus.FocusViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    // ========== State Observation ==========
    val settingsState by settingsViewModel.uiState.collectAsState()
    // If still loading, show the spinner and STOP here. To prevent bypassing hardcore mode
    if (settingsState.loading) {
        LoadingSpinner()
        return // The rest of the function won't run (until recomposition)
    }

    val hardcoreMode = settingsState.hardcoreModeEnabled

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        // ========== Authentication Flow ==========
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
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
                    authViewModel = authViewModel,
                    onFinish = {
                    navController.navigate(Screen.Main.route) {
                        // Close all screens up to auth route, including auth:
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            }
        }

        // ========== Main App Flow ==========
        navigation(startDestination = Screen.Focus.route, route = Screen.Main.route) {
            composable(Screen.Focus.route) {
                val focusVM: FocusViewModel = viewModel()
                FocusScreen(
                    navController, focusVM)
            }

//            composable(Screen.Kanban.route) {
//            val kanbanVM: KanbanViewModel = viewModel()
//                // Hardcore Mode Guard
//                if (isHardcoreMode) {
//                    LaunchedEffect(Unit) { navController.popBackStack() }
//                } else {
//                    KanbanScreen(navController = navController)
//                }
//            }
//
//            composable(Screen.Profile.route) {
//            val profileVM: ProfileViewModel = viewModel()
//                ProfileScreen(navController = navController)
//            }
//
//            composable(Screen.Settings.route) {
//                SettingsScreen(navController = navController)
//            }
        }
    }
}