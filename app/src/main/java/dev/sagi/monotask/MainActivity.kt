package dev.sagi.monotask

import dev.sagi.monotask.ui.navigation.MainScaffold
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.shared.UserSessionViewModel
import dev.sagi.monotask.ui.shared.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.MonoTaskTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MonoTaskTheme {
                val navController = rememberNavController()

                // Activity-scoped ViewModels — shared across multiple screens
                val authVM: AuthViewModel = viewModel()
                val settingsVM: SettingsViewModel = viewModel()
                val workspaceVM: WorkspaceViewModel = viewModel()
                val userSessionVM: UserSessionViewModel = viewModel()
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
}
