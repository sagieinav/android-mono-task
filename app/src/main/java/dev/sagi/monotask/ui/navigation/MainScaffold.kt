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
import dev.sagi.monotask.ui.auth.AuthUiState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.component.task.CreateTaskSheet
import dev.sagi.monotask.ui.component.workspace.CreateWorkspaceDialog
import dev.sagi.monotask.ui.profile.InviteSheet
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassSnackbarDismissable
import dev.sagi.monotask.ui.kanban.KanbanEvent
import dev.sagi.monotask.ui.kanban.KanbanUiState
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.theme.LocalSnackbarHostState

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel,
    kanbanVM: KanbanViewModel,
    pendingInviteUid: String? = null,
    onInviteDismissed: () -> Unit = {}
) {
    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authState by authVM.uiState.collectAsStateWithLifecycle()

    val mainScreens = listOf(
        Screen.Focus.route,
        Screen.Kanban.route,
        Screen.Profile.route,
        Screen.Statistics.route,
        Screen.Brief.route
    )
    val showBottomBar = currentRoute in mainScreens

    val selectedTab = when (currentRoute) {
        Screen.Kanban.route     -> NavTab.BOARD
        Screen.Brief.route      -> NavTab.BRIEF
        Screen.Statistics.route -> NavTab.STATISTICS
        Screen.Profile.route    -> NavTab.PROFILE
        else                    -> NavTab.FOCUS
    }

    var lastNavTime by remember { mutableLongStateOf(0L) }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }
    val workspaces by workspaceVM.workspaces.collectAsStateWithLifecycle()
    val selectedWorkspace by workspaceVM.selectedWorkspace.collectAsStateWithLifecycle()

    val kanbanUiState by kanbanVM.uiState.collectAsStateWithLifecycle()
    val isKanbanArchive = (kanbanUiState as? KanbanUiState.Ready)?.isArchive ?: false
    val onKanbanEvent: (KanbanEvent) -> Unit = remember { { kanbanVM.onEvent(it) } }

    CompositionLocalProvider(
        LocalHazeState         provides hazeState,
        LocalSnackbarHostState provides snackbarHostState,
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
//                    Screen.Focus.route, Screen.Kanban.route ->
//                        WorkspaceTopBar(
//                            workspaces          = workspaces,
//                            selectedWorkspace   = selectedWorkspace,
//                            onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
//                            onAddWorkspace      = { showCreateWorkspaceDialog = true },
//                            onAddTaskClick      = { showCreateSheet = true }
//                        )
                    Screen.Focus.route ->
                        WorkspaceTopBar(
                            workspaces          = workspaces,
                            selectedWorkspace   = selectedWorkspace,
                            onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
                            onAddWorkspace      = { showCreateWorkspaceDialog = true },
                            onAddTaskClick      = { showCreateSheet = true }
                        )

                    Screen.Kanban.route ->
                        KanbanTopBar(
                            workspaces          = workspaces,
                            selectedWorkspace   = selectedWorkspace,
                            onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
                            onAddWorkspace      = { showCreateWorkspaceDialog = true },
                            isArchive           = isKanbanArchive,
                            onToggleArchive     = { onKanbanEvent(KanbanEvent.ToggleArchive) }
                        )

                    Screen.Statistics.route ->
                        TitleTopBar(title = NavTab.STATISTICS.label)

                    Screen.Profile.route ->
                        TitleTopBar(
                            title = NavTab.PROFILE.label,
                            trailingIcon = {
                                TopBarIconButton(
                                    iconRes            = R.drawable.ic_settings,
                                    contentDescription = "Settings",
                                    onClick            = { navController.navigate(Screen.Settings.route) }
                                )
                            }
                        )

                    Screen.Brief.route ->
                        TitleTopBar(title = NavTab.BRIEF.label)
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
                                NavTab.BOARD      -> Screen.Kanban.route
                                NavTab.BRIEF      -> Screen.Brief.route
                                NavTab.FOCUS      -> Screen.Focus.route
                                NavTab.STATISTICS -> Screen.Statistics.route
                                NavTab.PROFILE    -> Screen.Profile.route
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
                        userSessionVM = userSessionVM,
                        kanbanVM      = kanbanVM
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

                if (pendingInviteUid != null && authState is AuthUiState.SignedIn) {
                    InviteSheet(
                        senderUid = pendingInviteUid,
                        onDismiss = onInviteDismissed
                    )
                }
            }
        }
    }
}
