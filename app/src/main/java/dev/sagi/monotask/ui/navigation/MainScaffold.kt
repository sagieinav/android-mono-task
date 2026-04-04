package dev.sagi.monotask.ui.navigation

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import dev.sagi.monotask.ui.common.CreateTaskSheet
import dev.sagi.monotask.ui.common.CreateWorkspaceDialog
import dev.sagi.monotask.ui.profile.component.InviteSheet
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel
import dev.sagi.monotask.designsystem.theme.LocalHazeState
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.component.MonoSnackbarDismissible
import dev.sagi.monotask.designsystem.component.MonoSnackbarVisuals
import dev.sagi.monotask.ui.common.CreateSheetDraft
import dev.sagi.monotask.ui.kanban.KanbanUiState
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.designsystem.theme.LocalSnackbarHostState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalLocale

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
    val scope = rememberCoroutineScope()
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

    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }
    val showCreateSheet  by workspaceVM.createSheetVisible.collectAsStateWithLifecycle()
    val createDraft      by workspaceVM.createDraft.collectAsStateWithLifecycle()
    val workspaces by workspaceVM.workspaces.collectAsStateWithLifecycle()
    val selectedWorkspace by workspaceVM.selectedWorkspace.collectAsStateWithLifecycle()

    val kanbanUiState by kanbanVM.uiState.collectAsStateWithLifecycle()

    // Eagerly initialize KanbanViewModel so uiState is Ready before the user navigates to Kanban.
    // setWorkspaceSource / setUserSource are no-ops if already set, so NavGraph calls are harmless.
    LaunchedEffect(Unit) {
        kanbanVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
        kanbanVM.setUserSource(userSessionVM.currentUser)
    }

    CompositionLocalProvider(
        LocalHazeState         provides hazeState,
        LocalSnackbarHostState provides snackbarHostState,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    MonoSnackbarDismissible(snackbarData)
                }
            },
            topBar = {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = {
                        if (initialState == Screen.Settings.route || targetState == Screen.Settings.route)
                            fadeIn(tween(navAnimationDuration)) togetherWith fadeOut(tween(navAnimationDuration))
                        else
                            tabSlideIn(initialState, targetState) togetherWith tabSlideOut(initialState, targetState)
                    },
                    label = "TopBarTransition"
                ) { route ->
                when (route) {
                    Screen.Focus.route ->
                        WorkspaceTopBar(
                            workspaces          = workspaces,
                            selectedWorkspace   = selectedWorkspace,
                            onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
                            onAddWorkspace      = { showCreateWorkspaceDialog = true },
                            onAddTaskClick      = { workspaceVM.openCreateSheet() }
                        )

                    Screen.Kanban.route ->
                        KanbanTopBar(
                            workspaces          = workspaces,
                            selectedWorkspace   = selectedWorkspace,
                            onWorkspaceSelected = { workspaceVM.selectWorkspace(it) },
                            onAddWorkspace      = { showCreateWorkspaceDialog = true },
                            onAddTaskClick      = { workspaceVM.openCreateSheet() },
                            sortOrder           = (kanbanUiState as? KanbanUiState.Ready)?.sortOrder,
                            onSortOrderChanged  = kanbanVM::setSortOrder
                        )

                    Screen.Statistics.route ->
                        TitleTopBar(title = NavTab.STATISTICS.label)

                    Screen.Profile.route ->
                        TitleTopBar(
                            title = NavTab.PROFILE.label,
                            trailingIcon = {
                                TopBarIconButton(
                                    iconRes            = IconPack.Settings,
                                    contentDescription = "Settings",
                                    onClick            = { navController.navigate(Screen.Settings.route) }
                                )
                            }
                        )

                    Screen.Brief.route ->
                        TitleTopBar(
                            title = (
                                    LocalDate
                                        .now().format(
                                            DateTimeFormatter.ofPattern("EEEE, MMMM d", LocalLocale.current.platformLocale)
                                        )
                                )
                        )

                    Screen.Settings.route ->
                        TitleTopBar(title = "Settings")
                }
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
                        initialTitle         = createDraft.title,
                        initialDescription   = createDraft.description,
                        initialImportance    = createDraft.importance,
                        initialTags          = createDraft.tags,
                        initialDueDateMillis = createDraft.dueDateMillis,
                        onDismiss = { workspaceVM.closeCreateSheet() },
                        onAddTask = { title, desc, importance, tags, dueDate ->
                            val hadTasks = (kanbanUiState as? KanbanUiState.Ready)?.let {
                                it.highTasks.isNotEmpty() || it.mediumTasks.isNotEmpty() || it.lowTasks.isNotEmpty()
                            } ?: false
                            workspaceVM.createTask(title, desc, importance, tags, dueDate)
                            workspaceVM.clearCreateDraft()
                            workspaceVM.closeCreateSheet()
                            if (hadTasks) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        MonoSnackbarVisuals(
                                            message = "Task created",
                                            duration = SnackbarDuration.Short,
                                            leadingIcon = IconPack.Check
                                        )
                                    )
                                }
                            }
                        },
                        onDraftSaved = { title, desc, importance, tags, dueDate ->
                            workspaceVM.saveCreateDraft(CreateSheetDraft(
                                title        = title,
                                description  = desc,
                                importance   = importance,
                                tags         = tags,
                                dueDateMillis = dueDate
                            ))
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
