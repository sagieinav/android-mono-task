package dev.sagi.monotask.ui.navigation

class Routes {
}

sealed class Screen(val route: String) {
    object Auth : Screen("auth")           // Auth flow parent
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")

    object Main : Screen("main")           // Main app parent
    object Focus : Screen("focus_hub")
    object Kanban : Screen("kanban")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    object FriendProfile : Screen("friend_profile/{userId}") {
        fun createRoute(userId: String) = "friend_profile/$userId"
    }
}