package com.codconfirm.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.codconfirm.app.R

sealed class AppDestination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    data object Orders : AppDestination("orders", R.string.nav_orders, Icons.Outlined.ShoppingBag)
    data object Stats : AppDestination("stats", R.string.nav_stats, Icons.Outlined.Assessment)
    data object Templates : AppDestination("templates", R.string.nav_templates, Icons.Outlined.ChatBubbleOutline)
    data object Settings : AppDestination("settings", R.string.nav_settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems = listOf(Orders, Stats, Templates, Settings)
    }
}
