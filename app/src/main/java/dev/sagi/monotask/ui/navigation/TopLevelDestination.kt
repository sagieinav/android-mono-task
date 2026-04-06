package dev.sagi.monotask.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.sagi.monotask.designsystem.theme.IconPack

/**
 * Represents every destination reachable via the bottom nav bar.
 * Single source of truth for tab order, route, icon, and label.
 */
enum class TopLevelDestination(
    val route: Any,
    val iconRes: Int,
    val label: String
) {
    BOARD(KanbanRoute, IconPack.StackTick, "Board"),
    BRIEF(BriefRoute, IconPack.FlagBolt, "Brief"),
    FOCUS(FocusRoute, IconPack.Focus, "Focus"),
    STATISTICS(StatisticsRoute, IconPack.Statistics, "Statistics"),
    PROFILE(ProfileRoute, IconPack.UserCircle, "MonoTask");

    /** Qualified class name of the route. Used by animation helpers to compute slide direction. */
    val routeQualifiedName: String get() = route::class.qualifiedName!!

    fun hasRoute(destination: NavDestination): Boolean =
        destination.hasRoute(route::class)
}
