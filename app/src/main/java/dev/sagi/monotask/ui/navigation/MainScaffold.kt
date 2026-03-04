@file:OptIn(ExperimentalHazeMaterialsApi::class)
package dev.sagi.monotask.ui.navigation

import BottomNavBar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.component.CreateTaskSheet
import dev.sagi.monotask.ui.component.CreateWorkspaceDialog
import dev.sagi.monotask.ui.component.TopBar
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.SharedWorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel,
    sharedWorkspaceVM: SharedWorkspaceViewModel
) {
    val hazeState = rememberHazeState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(Screen.Focus.route, Screen.Kanban.route, Screen.Profile.route)
    val showTopBar = currentRoute in listOf(Screen.Focus.route, Screen.Kanban.route)
    val showBottomBar = currentRoute in mainScreens

    val selectedTab = when (currentRoute) {
        Screen.Kanban.route -> NavTab.BOARD
        Screen.Profile.route -> NavTab.PROFILE
        else -> NavTab.FOCUS
    }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }  // ← add this
    val workspaces by sharedWorkspaceVM.workspaces.collectAsState()
    val selectedWorkspace by sharedWorkspaceVM.selectedWorkspace.collectAsState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (showTopBar) {
                    TopBar(
                        workspaces = workspaces,
                        selectedWorkspace = selectedWorkspace,
                        onWorkspaceSelected = { sharedWorkspaceVM.selectWorkspace(it) },
                        onAddWorkspace = { showCreateWorkspaceDialog = true },
                        onAddTaskClick = { showCreateSheet = true }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            if (tab == selectedTab) return@BottomNavBar
                            val route = when (tab) {
                                NavTab.BOARD -> Screen.Kanban.route
                                NavTab.FOCUS -> Screen.Focus.route
                                NavTab.PROFILE -> Screen.Profile.route
                            }
                            navController.navigate(route) {
                                popUpTo(Screen.Focus.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
                    NavGraph(navController, authVM, settingsVM, sharedWorkspaceVM)
                }

                if (showCreateSheet) {
                    CreateTaskSheet(
                        onDismiss = { showCreateSheet = false },
                        onAddTask = { title, desc, importance, tags, dueDate ->
                            sharedWorkspaceVM.createTask(title, desc, importance, tags, dueDate)
                            showCreateSheet = false
                        }
                    )
                }

                if (showCreateWorkspaceDialog) {
                    CreateWorkspaceDialog(
                        onConfirm = { name ->
                            sharedWorkspaceVM.createWorkspace(name)
                            showCreateWorkspaceDialog = false
                        },
                        onDismiss = { showCreateWorkspaceDialog = false }
                    )
                }
            }
        }
    }
}
