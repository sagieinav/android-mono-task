package dev.sagi.monotask.ui.navigation

import kotlinx.serialization.Serializable

// ===== Auth flow =====
@Serializable data object AuthRoute
@Serializable data object LoginRoute
@Serializable data object OnboardingRoute

// ===== Main flow =====
@Serializable data object MainRoute
@Serializable data object FocusRoute
@Serializable data object KanbanRoute
@Serializable data object BriefRoute
@Serializable data object StatisticsRoute
@Serializable data object ProfileRoute
@Serializable data object SettingsRoute

// ===== Parameterised =====
@Serializable data class FriendProfileRoute(val userId: String)
