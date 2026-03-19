package dev.sagi.monotask.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.component.task.CreateTaskSheet
import dev.sagi.monotask.ui.component.workspace.CreateWorkspaceDialog
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassSnackbarDismissable
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.theme.LocalProfileTabState
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel
) {
    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(Screen.Focus.route, Screen.Kanban.route, Screen.Profile.route)
    val showBottomBar = currentRoute in mainScreens

    val selectedTab = when (currentRoute) {
        Screen.Kanban.route -> NavTab.BOARD
        Screen.Profile.route -> NavTab.PROFILE
        else -> NavTab.FOCUS
    }

    var lastNavTime by remember { mutableLongStateOf(0L) }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }
    val workspaces by workspaceVM.workspaces.collectAsStateWithLifecycle()
    val selectedWorkspace by workspaceVM.selectedWorkspace.collectAsStateWithLifecycle()


    val profileTabState = remember { mutableIntStateOf(0) }
    val profileTabs     = listOf("Profile", "Statistics", "Social")

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalSnackbarHostState provides snackbarHostState,
        LocalProfileTabState    provides profileTabState
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    GlassSnackbarDismissable(snackbarData)
                }
            },
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
                        tabs          = profileTabs,
                        selectedTab   = profileTabState.value,
                        onTabSelected = { profileTabState.value = it },
                        trailingIcon  = {
                            TopBarIconButton(
                                iconRes            = R.drawable.ic_settings,
                                contentDescription = "Settings",
                                onClick            = { navController.navigate(Screen.Settings.route) }
                            )
                        }
                    )
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
                        navController = navController,
                        authVM        = authVM,
                        settingsVM    = settingsVM,
                        workspaceVM   = workspaceVM,
                        userSessionVM = userSessionVM
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
