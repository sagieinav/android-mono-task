package dev.sagi.monotask.ui.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.sagi.monotask.ui.navigation.SettingsRoute

fun NavGraphBuilder.settingsGraph(
    settingsVM: SettingsViewModel
) {
    composable<SettingsRoute> {
        SettingsScreen(settingsVM = settingsVM)
    }
}
