package dev.sagi.monotask.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.LoadingSpinner
import dev.sagi.monotask.ui.theme.basicMonoTask
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.SignedIn -> {
                if (state.requiresOnboarding) onNavigateToOnboarding()
                else onNavigateToMain()
            }
            else -> {}
        }
    }

    if (uiState is AuthUiState.Loading) {
        LoadingSpinner()
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            // Apply system bars padding, cause this is outside of MainScaffold
            .systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        // App logo, pinned to top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter            = painterResource(id = R.drawable.ic_monotask),
                contentDescription = "App Logo",
                modifier           = Modifier.width(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = "MonoTask",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text       = "One Task. No Noise.",
                style      = MaterialTheme.typography.headlineMedium,
                fontSize   = 18.sp,
                color      = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
        }

        // Sign-in button, pinned to bottom
        Column(
            modifier            = Modifier.padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Image(
                painter            = painterResource(id = R.drawable.btn_sign_in_google),
                contentDescription = "Sign in with Google",
                modifier           = Modifier
                    .basicMonoTask(MaterialTheme.shapes.extraLarge)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        scope.launch {
                            val idToken = authViewModel.launchGoogleSignIn(context)
                            idToken?. let {
                                authViewModel.onGoogleSignInResult(idToken)
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}
