package dev.sagi.monotask.ui.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.sagi.monotask.ui.navigation.AuthRoute
import dev.sagi.monotask.ui.navigation.LoginRoute
import dev.sagi.monotask.ui.navigation.MainRoute
import dev.sagi.monotask.ui.navigation.OnboardingRoute
import dev.sagi.monotask.ui.onboarding.OnboardingScreen

fun NavGraphBuilder.authGraph(
    navController: NavController,
    authVM: AuthViewModel
) {
    navigation<AuthRoute>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            AuthScreen(
                authViewModel = authVM,
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(MainRoute) {
                        popUpTo<AuthRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<OnboardingRoute> {
            OnboardingScreen(
                authViewModel = authVM,
                onFinish = {
                    navController.navigate(MainRoute) {
                        popUpTo<AuthRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
