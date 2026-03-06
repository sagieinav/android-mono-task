@file:OptIn(ExperimentalHazeMaterialsApi::class)
package dev.sagi.monotask.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.component.task.CreateTaskSheet
import dev.sagi.monotask.ui.component.workspace.CreateWorkspaceDialog
import dev.sagi.monotask.ui.profile.ProfileViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.R

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    workspaceVM: WorkspaceViewModel,
    profileVM: ProfileViewModel,
    settingsVM: SettingsViewModel
) {
    val hazeState = rememberHazeState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by profileVM.currentUser.collectAsState()

    val mainScreens = listOf(Screen.Focus.route, Screen.Kanban.route, Screen.Profile.route)
//    val showTopBar = currentRoute in listOf(Screen.Focus.route, Screen.Kanban.route)
    val showBottomBar = currentRoute in mainScreens

    val selectedTab = when (currentRoute) {
        Screen.Kanban.route -> NavTab.BOARD
        Screen.Profile.route -> NavTab.PROFILE
        else -> NavTab.FOCUS
    }

    var lastNavTime by remember { mutableLongStateOf(0L) }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }  // ← add this
    val workspaces by workspaceVM.workspaces.collectAsState()
    val selectedWorkspace by workspaceVM.selectedWorkspace.collectAsState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                when (currentRoute) {
                    Screen.Focus.route, Screen.Kanban.route ->
                        WorkspaceTopBar(
                        workspaces = workspaces,
                        selectedWorkspace = selectedWorkspace,
                        onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
                        onAddWorkspace = { showCreateWorkspaceDialog = true },
                        onAddTaskClick = { showCreateSheet = true }
                    )
                    Screen.Profile.route -> TitleTopBar(
                        title = currentUser?.displayName ?: "",
                        trailingIcon = {
                            TopBarIconButton(
                                iconRes = R.drawable.ic_settings,
                                contentDescription = "Settings",
                                onClick = { navController.navigate(Screen.Settings.route) }
                            )
                        }
                    )
                    // Screen.Settings.route -> TitleTopBar(title = "Settings")
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            val now = System.currentTimeMillis()
                            if (tab == selectedTab || now - lastNavTime < 300) return@BottomNavBar
                            lastNavTime = now
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
                    NavGraph(
                        navController,
                        authVM,
                        workspaceVM,
                        profileVM,
                        settingsVM,
                    )
                }

                if (showCreateSheet) {
                    CreateTaskSheet(
                        onDismiss = { showCreateSheet = false },
                        onAddTask = { title, desc, importance, tags, dueDate ->
                            workspaceVM.createTask(title, desc, importance, tags, dueDate)
                            showCreateSheet = false
                        }
                    )
                }

                if (showCreateWorkspaceDialog) {
                    CreateWorkspaceDialog(
                        onConfirm = { name ->
                            workspaceVM.createWorkspace(name)
                            showCreateWorkspaceDialog = false
                        },
                        onDismiss = { showCreateWorkspaceDialog = false }
                    )
                }
            }
        }
    }
}
