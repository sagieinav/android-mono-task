package dev.sagi.monotask.ui.navigation

internal val TAB_ORDER = TopLevelDestination.entries.map { it.routeQualifiedName }

/** Returns true if navigating from [from] to [to] moves forward in the tab order. */
internal fun isForward(from: String?, to: String?): Boolean {
    val fromIndex = TAB_ORDER.indexOf(from)
    val toIndex   = TAB_ORDER.indexOf(to)
    if (fromIndex == -1 || toIndex == -1) return true
    return toIndex > fromIndex
}
