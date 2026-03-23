package dev.sagi.monotask.ui.navigation

class Routes {
}

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")

    object Main : Screen("main")
    object Focus : Screen("focus_hub")
    object Kanban : Screen("kanban")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    object Statistics : Screen("statistics")
    object Brief : Screen("brief")

    object FriendProfile : Screen("friend_profile/{userId}") {
        fun createRoute(userId: String) = "friend_profile/$userId"
    }
}