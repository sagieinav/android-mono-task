package dev.sagi.monotask.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sagi.monotask.designsystem.components.MonoLoadingIndicator
import dev.sagi.monotask.designsystem.theme.BackgroundGradientBottom
import dev.sagi.monotask.designsystem.theme.BackgroundGradientTop
import dev.sagi.monotask.designsystem.theme.IconPack
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.monoShadow
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // User is signed in:
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.SignedIn -> {
                if (state.requiresOnboarding)
                    onNavigateToOnboarding()
                else
                    onNavigateToMain()
            }
            else -> {}
        }
    }

    // User is NOT signed in:
    AuthScreenContent(
        uiState = uiState,
        onSignInClick = {
            scope.launch {
                val idToken = authViewModel.launchGoogleSignIn(context)
                idToken?.let {
                    authViewModel.onGoogleSignInResult(idToken)
                }
            }
        }
    )
}


@Composable
fun AuthScreenContent(
    uiState: AuthUiState,
    onSignInClick: () -> Unit
) {
    if (uiState is AuthUiState.Loading) {
        MonoLoadingIndicator()
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = Color.Transparent // inherit gradient bg from AppShell
    ) {
        // App Branding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = IconPack.LogoRaw),
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

        // Sign-in button, pinned to bottom
        Column(
            modifier = Modifier.padding(vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val buttonShape = CircleShape
            Image(
                painter = painterResource(IconPack.BtnSignInGoogle),
                contentDescription = "Sign in with Google",
                modifier = Modifier // Fixed a typo here (missing '=')
                    .glassBorder(buttonShape)
                    .border(shape = buttonShape, color = MaterialTheme.customColors.aceDim, width = 0.5.dp)
                    .monoShadow(buttonShape, alpha = 1f)
                    .clip(buttonShape)
                    .clickable { onSignInClick() }, // Moved logic to lambda
                contentScale = ContentScale.Fit
            )
        }
    }
}



@Preview(showBackground = true, name = "Auth Screen - Signed Out")
@Composable
fun AuthScreenContentPreview() {
    MonoTaskTheme {
        AuthScreenContent(
            uiState = AuthUiState.SignedOut,
            onSignInClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Auth Screen - Loading")
@Composable
fun AuthScreenContentLoadingPreview() {
    MonoTaskTheme {
        AuthScreenContent(
            uiState = AuthUiState.Loading,
            onSignInClick = {}
        )
    }
}