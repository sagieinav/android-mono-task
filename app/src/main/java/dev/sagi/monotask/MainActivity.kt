package dev.sagi.monotask

import dev.sagi.monotask.ui.navigation.MainScaffold
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.sagi.monotask.ui.auth.AuthViewModel
import dev.sagi.monotask.ui.settings.SettingsViewModel
import dev.sagi.monotask.ui.common.UserSessionViewModel
import dev.sagi.monotask.ui.common.WorkspaceViewModel
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sagi.monotask.ui.kanban.KanbanViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Compose state for invite sheet. Recomposition is triggered automatically when set
    var pendingInviteUid by mutableStateOf<String?>(null)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        // Extract UID from cold-start deep link (if any)
        pendingInviteUid = intent?.data
            ?.takeIf { it.scheme == "monotask" && it.host == "invite" }
            ?.getQueryParameter("uid")
        setContent {
            MonoTaskTheme {
                val navController = rememberNavController()

                // Activity-scoped ViewModels: shared across multiple screens
                // Inside setContent {}, LocalViewModelStoreOwner.current points to the Activity itself
                val authVM: AuthViewModel = hiltViewModel()
                val settingsVM: SettingsViewModel = hiltViewModel()
                val workspaceVM: WorkspaceViewModel = hiltViewModel()
                val userSessionVM: UserSessionViewModel = hiltViewModel()

                val kanbanVM: KanbanViewModel = hiltViewModel()
                // Screen-scoped VMs (focusVM, kanbanVM, profileVM) are created
                // inside their composable() blocks in NavGraph

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(
                        navController      = navController,
                        authVM             = authVM,
                        settingsVM         = settingsVM,
                        workspaceVM        = workspaceVM,
                        userSessionVM      = userSessionVM,
                        kanbanVM           = kanbanVM,
                        pendingInviteUid   = pendingInviteUid,
                        onInviteDismissed  = { pendingInviteUid = null }
                    )
                }
            }
        }
    }

    // Warm-start: app already running, new intent arrives (singleTop launch mode)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingInviteUid = intent.data
            ?.takeIf { it.scheme == "monotask" && it.host == "invite" }
            ?.getQueryParameter("uid")
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
