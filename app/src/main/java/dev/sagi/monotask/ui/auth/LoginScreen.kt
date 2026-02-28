package dev.sagi.monotask.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.GlassCard
import dev.sagi.monotask.ui.component.LoadingSpinner
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.AceGold

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    // ========== Observe the UI state ==========
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Get the intent
    val intent = authViewModel.getGoogleSignInIntent(context)

    // ========== Navigation Guard (LaunchedEffect) ==========
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.SignedIn -> {
                if (state.requiresOnboarding) {
                    onNavigateToOnboarding()
                } else {
                    onNavigateToMain()
                }
            }
            is AuthUiState.Error -> {
                // TODO display error message in a SnackBar
            }
            else -> {
                // Loading or SignedOut, stay on this screen
            }
        }
    }

    // ========== Google Sign-In Launcher ==========
    // This handles the pop-up dialog for Google Accounts
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                authViewModel.onGoogleSignInResult(account)
            } catch (e: ApiException) {
                // TODO Handle error (show SnackBar)
            }
        }
    }

    // ========== UI Rendering ==========
    // If the state is Loading, show the spinner and STOP drawing the rest of the screen
    if (uiState is AuthUiState.Loading) {
        LoadingSpinner()
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        // ========== Branding (arranged to top) ==========
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_monotask),
                contentDescription = "App Logo",
                modifier = Modifier.width(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MonoTask",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "One Task. No Noise.",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
        }

        // ========== Login Options (arranged to bottom) ==========
        Column(
            modifier = Modifier.padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Use the official google sign-in button from guideline doc
            Image(
                painter = painterResource(id = R.drawable.btn_sign_in_google),
                contentDescription = "Sign in with Google",
                modifier = Modifier
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .clip(shape = MaterialTheme.shapes.extraLarge)
                    .clickable { googleSignInLauncher.launch(intent) },
                contentScale = ContentScale.Fit
            )
        }
    }
}