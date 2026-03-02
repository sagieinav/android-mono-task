@file:OptIn(ExperimentalHazeMaterialsApi::class)
package dev.sagi.monotask.ui.navigation

import BottomNavBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState

val LocalScaffoldPadding = compositionLocalOf<PaddingValues> { PaddingValues(0.dp) }

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel
) {
    val hazeState = rememberHazeState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(
        Screen.Focus.route,
        Screen.Kanban.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in mainScreens

    val selectedTab = when (currentRoute) {
        Screen.Kanban.route -> NavTab.BOARD
        Screen.Profile.route -> NavTab.PROFILE
        else -> NavTab.FOCUS
    }

    CompositionLocalProvider(LocalHazeState provides hazeState) {

        Scaffold(
            containerColor = Color.Transparent, // Let app background shine through
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            val route = when (tab) {
                                NavTab.BOARD -> Screen.Kanban.route
                                NavTab.FOCUS -> Screen.Focus.route
                                NavTab.PROFILE -> Screen.Profile.route
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->

            // BROADCAST THE PADDING GLOBALLY
            CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {

                // ========== THE BACKGROUND SIBLING (hazeSource) ==========
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) {
                    NavGraph(navController, authVM, settingsVM)
                }
            }
        }
    }
}