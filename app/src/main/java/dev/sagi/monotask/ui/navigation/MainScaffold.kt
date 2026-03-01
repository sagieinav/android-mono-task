@file:OptIn(ExperimentalHazeMaterialsApi::class)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.navigation.NavGraph
import dev.sagi.monotask.ui.navigation.Screen
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.theme.LocalHazeState

@Composable
fun MainScaffold(
    navController: NavHostController,
    authVM: AuthViewModel,
    settingsVM: SettingsViewModel
    ) {
    // ONE global HazeState for the whole app
//    val hazeState = remember { HazeState() }
    val hazeState = rememberHazeState()

    // Observe the current route the user is on
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens get the bottom bar
    val mainScreens = listOf(
        Screen.Focus.route,
        Screen.Kanban.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in mainScreens

    // Determine the selected tab based on the route
    val selectedTab = when (currentRoute) {
        Screen.Kanban.route -> NavTab.BOARD
        Screen.Profile.route -> NavTab.PROFILE
        else -> NavTab.FOCUS // Default to Focus
    }

    // Provide the Haze State to the whole UI tree
    CompositionLocalProvider(LocalHazeState provides hazeState) {

        // Root box (No Haze here!)
        Box(modifier = Modifier.fillMaxSize()) {
            // ========== THE BACKGROUND SIBLING (hazeSource) ==========
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                NavGraph(navController, authVM, settingsVM)
            }

            // ========== THE FOREGROUND SIBLING ==========
            // Because this is outside the hazeSource box, it floats on top and applies the effect
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}