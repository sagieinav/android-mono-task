package dev.sagi.monotask.ui.settings

import dev.sagi.monotask.designsystem.theme.IconPack
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.designsystem.components.ActionButton
import dev.sagi.monotask.designsystem.components.MonoConfirmDialog
import dev.sagi.monotask.designsystem.components.MonoLoadingIndicator
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.theme.LocalSnackbarHostState
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.gloock
import dev.sagi.monotask.designsystem.theme.penaltyRed
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.ui.settings.components.SettingsAboutSection
import dev.sagi.monotask.ui.settings.components.SettingsAccountSection
import dev.sagi.monotask.ui.settings.components.SettingsFocusPrefsSection
import dev.sagi.monotask.ui.settings.components.WorkspacesSection
import kotlinx.coroutines.flow.collectLatest


@Composable
fun SettingsScreen(
    settingsVM: SettingsViewModel
) {
    val uiState by settingsVM.uiState.collectAsStateWithLifecycle()
    val workspaces by settingsVM.workspaces.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        settingsVM.effect.collectLatest { effect ->
            when (effect) {
                is SettingsUiEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    when (val state = uiState) {
        is SettingsUiState.Loading -> MonoLoadingIndicator()
        is SettingsUiState.Error   -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is SettingsUiState.Ready -> SettingsContent(
            state = state,
            workspaces = workspaces,
            onEvent = settingsVM::onEvent
        )
    }
}


// ==================================
// Content
// ==================================

@Composable
private fun SettingsContent(
    state: SettingsUiState.Ready,
    workspaces: List<Workspace>,
    onEvent: (SettingsEvent) -> Unit
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
                hyperfocusModeEnabled  = state.hyperfocusModeEnabled,
                dueDateWeight          = state.dueDateWeight,
                onUpdateHyperfocusMode = { onEvent(SettingsEvent.UpdateHyperfocusMode(it)) },
                onUpdateWeights        = { onEvent(SettingsEvent.UpdatePriorityWeights(it)) }
            )
        }

        // Workspace Management Section:
        item {
            WorkspacesSection(
                workspaces = workspaces,
                onCreateWorkspace = { onEvent(SettingsEvent.CreateWorkspace(it)) },
                onRenameWorkspace = { ws, name -> onEvent(SettingsEvent.RenameWorkspace(ws, name)) },
                onDeleteWorkspace = { onEvent(SettingsEvent.DeleteWorkspace(it)) }
            )
        }

        // Account Section:
        item {
            SettingsAccountSection(
                displayName         = state.displayName,
                email               = state.email,
                onUpdateDisplayName = { onEvent(SettingsEvent.UpdateDisplayName(it)) }
            )
        }

        // About Section:
        item {
            SettingsAboutSection()
        }

        // Sign Out Button:
        item {
            Spacer(Modifier.height(30.dp)) // compensate for lack of section title
            SignOutButton(onSignOut = { onEvent(SettingsEvent.SignOut) })
        }

        // App Branding:
        item {
            Spacer(Modifier.height(16.dp)) // compensate for lack of section title
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
        shape    = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Icon(
            painter            = painterResource(IconPack.SignOutAlt),
            contentDescription = "Sign Out Button",
            tint               = color,
            modifier           = Modifier
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
        MonoConfirmDialog(
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


// ==================================
// App Branding
// ==================================

@Composable
private fun AppBranding() {
    val context = LocalContext.current
    // TODO move this calc to VM:
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "---" }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 10.dp)
                .padding(top = 6.dp, bottom = 2.dp)
        ) {
            Text(
                text = "MonoTask",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = gloock,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}


// ==================================
// Previews
// ==================================

@Preview(name = "App Branding")
@Composable
private fun AppBrandingPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppBranding()
        }
    }
}

@Preview(name = "Sign Out Button")
@Composable
private fun SignOutButtonPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(16.dp)) {
                SignOutButton({})
            }
        }
    }
}
