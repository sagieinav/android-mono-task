package dev.sagi.monotask.ui.focus

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel
import dev.sagi.monotask.ui.navigation.FocusRoute

fun NavGraphBuilder.focusGraph(
    workspaceVM: WorkspaceViewModel,
    userSessionVM: UserSessionViewModel
) {
    composable<FocusRoute> {
        val focusVM: FocusViewModel = hiltViewModel()
        focusVM.setWorkspaceSource(workspaceVM.selectedWorkspace)
        focusVM.setUserSource(userSessionVM.currentUser)
        FocusScreen(focusVM = focusVM)
    }
}
