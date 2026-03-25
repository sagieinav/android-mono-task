package dev.sagi.monotask.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING


@Composable
fun SettingsScreen(
    settingsVM: SettingsViewModel
) {
    val uiState    by settingsVM.uiState.collectAsStateWithLifecycle()
    val workspaces by settingsVM.workspaces.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SettingsUiState.Loading -> LoadingSpinner()
        is SettingsUiState.Error   -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is SettingsUiState.Ready   -> SettingsContent(
            state             = state,
            workspaces        = workspaces,
            onUpdatePreferences = { hardcoreMode, notifications, dueSoon ->
                settingsVM.updateUserPreferences(hardcoreMode, notifications, dueSoon)
            },
            onUpdateWeights     = { due, importance ->
                settingsVM.updatePriorityWeights(due, importance)
            },
            onUpdateDisplayName = { settingsVM.updateDisplayName(it) },
            onCreateWorkspace   = { settingsVM.createWorkspace(it) },
            onRenameWorkspace   = { workspace, name -> settingsVM.renameWorkspace(workspace, name) },
            onDeleteWorkspace   = { settingsVM.deleteWorkspace(it) },
            onSignOut           = { settingsVM.signOut() }
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(
    state              : SettingsUiState.Ready,
    workspaces         : List<Workspace>,
    onUpdatePreferences: (hardcoreMode: Boolean?, notifications: Boolean?, dueSoon: Int?) -> Unit,
    onUpdateWeights    : (dueDateWeight: Float, importanceWeight: Float) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onCreateWorkspace  : (String) -> Unit,
    onRenameWorkspace  : (Workspace, String) -> Unit,
    onDeleteWorkspace  : (Workspace) -> Unit,
    onSignOut          : () -> Unit
) {
    val scaffoldPadding = LocalScaffoldPadding.current
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "---" }
    }

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = SCREEN_PADDING)
//    ) {
    LazyColumn(
        modifier        = Modifier
            .fillMaxSize(),
        contentPadding  = PaddingValues(
            top    = scaffoldPadding.calculateTopPadding() + SCREEN_PADDING,
            bottom = scaffoldPadding.calculateBottomPadding() + SCREEN_PADDING,
            start = SCREEN_PADDING,
            end = SCREEN_PADDING
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            FocusPreferencesSection(
                hardcoreModeEnabled = state.hardcoreModeEnabled,
                dueDateWeight       = state.dueDateWeight,
                onUpdatePreferences = onUpdatePreferences,
                onUpdateWeights     = onUpdateWeights
            )
        }
        item {
            WorkspacesSection(
                workspaces        = workspaces,
                onCreateWorkspace = onCreateWorkspace,
                onRenameWorkspace = onRenameWorkspace,
                onDeleteWorkspace = onDeleteWorkspace
            )
        }
        item {
            AccountSection(
                displayName         = state.displayName,
                email               = state.email,
                onUpdateDisplayName = onUpdateDisplayName
            )
        }
        item {
            AboutSection()
        }
        item {
            SignOutButton(onSignOut = onSignOut)
        }
        item {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_monotask_raw),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "MonoTask",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = gloock,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
//    }
    } // Box
}
