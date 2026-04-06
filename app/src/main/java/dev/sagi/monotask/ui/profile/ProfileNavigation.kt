package dev.sagi.monotask.ui.profile

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.navigation.ProfileRoute

fun NavGraphBuilder.profileGraph(
    userSessionVM: UserSessionViewModel
) {
    composable<ProfileRoute> {
        val profileVM: ProfileViewModel = hiltViewModel()
        LaunchedEffect(Unit) {
            profileVM.startObserving(userSessionVM.currentUser)
        }
        ProfileScreen(profileVM = profileVM)
    }
}
