package dev.sagi.monotask.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.auth.AuthScreen
import dev.sagi.monotask.ui.auth.AuthUiState
import dev.sagi.monotask.ui.auth.OnboardingScreen
import dev.sagi.monotask.ui.brief.BriefScreen
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.focus.FocusScreen
import dev.sagi.monotask.ui.focus.FocusViewModel
import dev.sagi.monotask.ui.kanban.KanbanScreen
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.profile.ProfileScreen
import dev.sagi.monotask.ui.profile.ProfileViewModel
import dev.sagi.monotask.ui.settings.SettingsScreen
import dev.sagi.monotask.ui.settings.SettingsUiState
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.statistics.StatisticsScreen

val TAB_ORDER = listOf(
    Screen.Kanban.route,
    Screen.Brief.route,
    Screen.Focus.route,
    Screen.Statistics.route,
    Screen.Profile.route
)

const val navAnimationDuration = 300

// ========== Animation Helpers ==========
fun isForward(from: String?, to: String?): Boolean {
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
    userSessionVM: UserSessionViewModel,
    kanbanVM: KanbanViewModel
) {
    val settingsState by settingsVM.uiState.collectAsStateWithLifecycle()
    val authState by authVM.uiState.collectAsStateWithLifecycle()

    val hardcoreMode = (settingsState as? SettingsUiState.Ready)?.hardcoreModeEnabled ?: false
    val isLoading = authState is AuthUiState.Loading || settingsState is SettingsUiState.Loading

    var initialStartDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState, settingsState) {
        if (initialStartDestination == null && !isLoading) {
            initialStartDestination = if (authState is AuthUiState.SignedIn) Screen.Main.route else Screen.Auth.route
        }
    }

    val startDestination = initialStartDestination

    if (startDestination == null) {
        LoadingSpinner()
        return
    }


    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(authState) {
            if (authState is AuthUiState.SignedOut) {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        NavHost(
            navController    = navController,
            startDestination = startDestination,
            enterTransition  = {
                if (targetState.destination.route == Screen.Settings.route)
                    slideInVertically(tween(navAnimationDuration, easing = FastOutSlowInEasing)) { it }
                else
                    tabSlideIn(initialState.destination.route, targetState.destination.route)
            },
            exitTransition   = {
                if (targetState.destination.route == Screen.Settings.route)
                    fadeOut(tween(navAnimationDuration))
                else
                    tabSlideOut(initialState.destination.route, targetState.destination.route)
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.Settings.route)
                    fadeIn(tween(navAnimationDuration))
                else
                    tabSlideIn(initialState.destination.route, targetState.destination.route)
            },
            popExitTransition  = {
                if (initialState.destination.route == Screen.Settings.route)
                    slideOutVertically(tween(navAnimationDuration, easing = FastOutSlowInEasing)) { it }
                else
                    tabSlideOut(initialState.destination.route, targetState.destination.route)
            }
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
                    val focusVM: FocusViewModel = hiltViewModel()
                    focusVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
                    focusVM.setUserSource(userSessionVM.currentUser)
                    FocusScreen(focusVM = focusVM)
                }
                composable(Screen.Kanban.route) {
//                    val kanbanVM: KanbanViewModel = hiltViewModel()
                    kanbanVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
                    kanbanVM.setUserSource(userSessionVM.currentUser)
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
                    val profileVM: ProfileViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        profileVM.startObserving(userSessionVM.currentUser)
                    }
                    ProfileScreen(
                        navController = navController,
                        profileVM     = profileVM
                    )
                }
                composable(Screen.Statistics.route) {
                    val profileVM: ProfileViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        profileVM.startObserving(userSessionVM.currentUser)
                    }
                    StatisticsScreen(
                        profileVM = profileVM
                    )
                }
                composable(Screen.Brief.route) {
                    BriefScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(settingsVM = settingsVM)
                }
            }
        }
        if (isLoading) {
            LoadingSpinner()
        }
    }
}
