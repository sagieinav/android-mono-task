package dev.sagi.monotask.ui.brief

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.navigation.BriefRoute

fun NavGraphBuilder.briefGraph(
    userSessionVM: UserSessionViewModel
) {
    composable<BriefRoute> {
        val briefVM: BriefViewModel = hiltViewModel()
        briefVM.setUserSource(userSessionVM.currentUser)
        BriefScreen(briefVM = briefVM)
    }
}
