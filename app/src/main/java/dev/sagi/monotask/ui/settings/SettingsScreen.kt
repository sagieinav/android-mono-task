package dev.sagi.monotask.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.GlassConfirmDialog
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorderPremium
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.ui.theme.penaltyRed
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
            state               = state,
            workspaces          = workspaces,
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


// ==================================
// Content
// ==================================

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

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(
            top    = scaffoldPadding.calculateTopPadding() + SCREEN_PADDING,
            bottom = scaffoldPadding.calculateBottomPadding() + SCREEN_PADDING,
            start  = SCREEN_PADDING,
            end    = SCREEN_PADDING
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Focus Preferences Section:
        item {
            SettingsFocusPrefsSection(
                hardcoreModeEnabled = state.hardcoreModeEnabled,
                dueDateWeight       = state.dueDateWeight,
                onUpdatePreferences = onUpdatePreferences,
                onUpdateWeights     = onUpdateWeights
            )
        }

        // Workspace Management Section:
        item {
            WorkspacesSection(
                workspaces        = workspaces,
                onCreateWorkspace = onCreateWorkspace,
                onRenameWorkspace = onRenameWorkspace,
                onDeleteWorkspace = onDeleteWorkspace
            )
        }

        // Account Section:
        item {
            SettingsAccountSection(
                displayName         = state.displayName,
                email               = state.email,
                onUpdateDisplayName = onUpdateDisplayName
            )
        }

        // About Section:
        item {
            SettingsAboutSection()
        }

        // Sign Out Button:
        item {
            SignOutButton(onSignOut = onSignOut)
        }

        // App Branding:
        item {
            AppBranding()
        }
    }
}


// ==================================
// Sign Out
// ==================================
@Composable
private fun SignOutButton(onSignOut: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    val color = penaltyRed

    ActionButton(
        onClick  = { showConfirm = true },
        color    = color,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sign_out_alt),
            contentDescription = "Sign Out Button",
            tint = color,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(18.dp)
        )
        Text(
            text       = "Sign Out",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (showConfirm) {
        GlassConfirmDialog(
            onDismissRequest = { showConfirm = false },
            title            = "Sign Out",
            message          = "Are you sure you want to sign out?",
            confirmLabel     = "Sign Out",
            dismissLabel     = "Cancel",
            confirmColor     = penaltyRed,
            onConfirm        = onSignOut
        )
    }
}

@Preview
@Composable
private fun AppBranding() {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "---" }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter            = painterResource(id = R.drawable.logo_monotask_raw),
            contentDescription = "App Logo",
            modifier           = Modifier.width(70.dp)
        )
        Text(
            text       = "MonoTask",
            style      = MaterialTheme.typography.labelMedium,
            fontFamily = gloock,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign  = TextAlign.Center,
            modifier   = Modifier
                .fillMaxWidth()
//                .padding(top = 6.dp)
        )
        Text(
            text      = "Version $versionName",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )
    }
}


@Preview
@Composable
private fun SignOutButtonPreview() {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        SignOutButton({})

    }
}