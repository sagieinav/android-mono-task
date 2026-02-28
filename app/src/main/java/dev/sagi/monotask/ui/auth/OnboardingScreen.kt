package dev.sagi.monotask.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// PLACEHOLDER:
@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel,
    onFinish: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Onboarding Placeholder", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = {
                // Update Firestore
                authViewModel.completeOnboarding()
                // Trigger the navigation lambda func
                onFinish()
            }) {
                Text("Finish & Go to Focus")
            }
        }
    }
}