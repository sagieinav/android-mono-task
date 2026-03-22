package dev.sagi.monotask

import dev.sagi.monotask.ui.navigation.MainScaffold
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MonoTaskTheme {
                val navController = rememberNavController()

                // Activity-scoped ViewModels: shared across multiple screens
                // Inside setContent {}, LocalViewModelStoreOwner.current points to the Activity itself
                val owner = LocalViewModelStoreOwner.current!!
                val authVM: AuthViewModel = hiltViewModel(owner)
                val settingsVM: SettingsViewModel = hiltViewModel(owner)
                val workspaceVM: WorkspaceViewModel = hiltViewModel(owner)
                val userSessionVM: UserSessionViewModel = hiltViewModel(owner)
                // Screen-scoped VMs (focusVM, kanbanVM, profileVM) are created
                // inside their composable() blocks in NavGraph

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(
                        navController = navController,
                        authVM        = authVM,
                        settingsVM    = settingsVM,
                        workspaceVM   = workspaceVM,
                        userSessionVM = userSessionVM
                    )
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // App is going to background. collectAsStateWithLifecycle() automatically pauses
        // all StateFlow collection in Compose, so no manual suspension is needed
        Log.d("MainActivity", "onPause: app backgrounded")
    }

    override fun onResume() {
        super.onResume()
        // App is returning to foreground. collectAsStateWithLifecycle() resumes flow collection
        // and Firestore listeners deliver any changes that occurred while backgrounded
        Log.d("MainActivity", "onResume: app foregrounded")
    }
}
