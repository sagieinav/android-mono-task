package dev.sagi.monotask.ui.kanban

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel
import dev.sagi.monotask.ui.navigation.KanbanRoute

fun NavGraphBuilder.kanbanGraph(
    navController: NavHostController,
    kanbanVM: KanbanViewModel,
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel,
    hyperfocusMode: Boolean
) {
    composable<KanbanRoute> {
        kanbanVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
        kanbanVM.setUserSource(userSessionVM.currentUser)
        kanbanVM.setLocked(hyperfocusMode)
        KanbanScreen(
            navController = navController,
            kanbanVM = kanbanVM
        )
    }
}
