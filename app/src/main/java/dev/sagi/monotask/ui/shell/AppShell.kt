package dev.sagi.monotask.ui.shell

import dev.sagi.monotask.designsystem.theme.IconPack
import dev.sagi.monotask.ui.navigation.MonoTaskAppState
import dev.sagi.monotask.ui.navigation.MonoTaskNavHost
import dev.sagi.monotask.ui.navigation.SettingsRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.sagi.monotask.ui.navigation.TopLevelDestination
import dev.sagi.monotask.ui.navigation.navAnimationDuration
import dev.sagi.monotask.ui.navigation.tabSlideIn
import dev.sagi.monotask.ui.navigation.tabSlideOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthUiState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.common.CreateTaskSheet
import dev.sagi.monotask.ui.common.CreateWorkspaceDialog
import dev.sagi.monotask.ui.common.CreateSheetDraft
import dev.sagi.monotask.ui.profile.components.InviteSheet
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel
import dev.sagi.monotask.designsystem.theme.LocalHazeState
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.components.MonoSnackbarDismissible
import dev.sagi.monotask.designsystem.components.MonoSnackbarVisuals
import dev.sagi.monotask.ui.kanban.KanbanUiState
import dev.sagi.monotask.ui.kanban.KanbanViewModel
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.designsystem.theme.LocalSnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun AppShell(
    appState: MonoTaskAppState,
    pendingInviteUid: String? = null,
    onInviteDismissed: () -> Unit = {}
) {
    val authVM: AuthViewModel = hiltViewModel()
    val settingsVM: SettingsViewModel = hiltViewModel()
    val workspaceVM: WorkspaceViewModel = hiltViewModel()
    val userSessionVM: UserSessionViewModel = hiltViewModel()
    val kanbanVM: KanbanViewModel = hiltViewModel()

    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentTopLevel = appState.currentTopLevelDestination
    val authState by authVM.uiState.collectAsStateWithLifecycle()
    val showCreateSheet by workspaceVM.createSheetVisible.collectAsStateWithLifecycle()
    val createDraft by workspaceVM.createDraft.collectAsStateWithLifecycle()
    val kanbanUiState by kanbanVM.uiState.collectAsStateWithLifecycle()
    val showCreateWorkspaceDialog by workspaceVM.createWorkspaceDialogVisible.collectAsStateWithLifecycle()

    var lastNavTime by remember { mutableLongStateOf(0L) }

    // Eagerly initialize KanbanViewModel so uiState is Ready before the user navigates to Kanban.
    LaunchedEffect(Unit) {
        kanbanVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
        kanbanVM.setUserSource(userSessionVM.currentUser)
    }

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalSnackbarHostState provides snackbarHostState,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { MonoSnackbarDismissible(it) }
            },
            topBar = {
                AnimatedContent(
                    targetState = currentTopLevel,
                    transitionSpec = {
                        val isSettingsTransition = initialState == null || targetState == null
                        if (isSettingsTransition)
                            fadeIn(tween(navAnimationDuration)) togetherWith fadeOut(tween(navAnimationDuration))
                        else
                            tabSlideIn(initialState?.routeQualifiedName, targetState?.routeQualifiedName) togetherWith
                            tabSlideOut(initialState?.routeQualifiedName, targetState?.routeQualifiedName)
                    },
                    label = "TopBarTransition"
                ) { dest ->
                    when (dest) {
                        TopLevelDestination.FOCUS -> FocusTopBar()
                        TopLevelDestination.BOARD -> KanbanTopBar()
                        TopLevelDestination.STATISTICS -> StatisticsTopBar()
                        TopLevelDestination.PROFILE -> ProfileTopBar(
                            onSettingsClick = { appState.navController.navigate(SettingsRoute) }
                        )
                        TopLevelDestination.BRIEF -> BriefTopBar()
                        null -> if (appState.currentDestination?.hasRoute<SettingsRoute>() == true) {
                            TitleTopBar(title = "Settings")
                        }
                    }
                }
            },
            bottomBar = {
                currentTopLevel?.let {
                    BottomNavBar(
                        selectedDestination = it,
                        onDestinationSelected = { dest ->
                            val now = System.currentTimeMillis()
                            if (dest == currentTopLevel || now - lastNavTime < navAnimationDuration) return@BottomNavBar
                            lastNavTime = now
                            appState.navigateTo(dest)
                        },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
                Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
                    MonoTaskNavHost(
                        navController = appState.navController,
                        authVM = authVM,
                        settingsVM = settingsVM,
                        workspaceVM = workspaceVM,
                        userSessionVM = userSessionVM,
                        kanbanVM = kanbanVM
                    )
                }

                if (showCreateSheet) {
                    CreateTaskSheet(
                        initialTitle = createDraft.title,
                        initialDescription = createDraft.description,
                        initialImportance = createDraft.importance,
                        initialTags = createDraft.tags,
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
                                title = title,
                                description = desc,
                                importance = importance,
                                tags = tags,
                                dueDateMillis = dueDate
                            ))
                        }
                    )
                }

                if (showCreateWorkspaceDialog) {
                    CreateWorkspaceDialog(
                        onConfirm = { name ->
                            workspaceVM.createWorkspace(name)
                            workspaceVM.closeCreateWorkspaceDialog()
                        },
                        onDismiss = { workspaceVM.closeCreateWorkspaceDialog() }
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
