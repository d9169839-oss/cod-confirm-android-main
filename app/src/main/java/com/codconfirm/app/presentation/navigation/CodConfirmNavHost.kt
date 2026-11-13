package com.codconfirm.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codconfirm.app.presentation.orders.OrdersScreen
import com.codconfirm.app.presentation.settings.SettingsScreen
import com.codconfirm.app.presentation.stats.StatsScreen
import com.codconfirm.app.presentation.templates.TemplatesScreen

@Composable
fun CodConfirmNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.bottomNavItems.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(text = androidx.compose.ui.res.stringResource(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Orders.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Orders.route) { OrdersScreen() }
            composable(AppDestination.Stats.route) { StatsScreen() }
            composable(AppDestination.Templates.route) { TemplatesScreen() }
            composable(AppDestination.Settings.route) { SettingsScreen() }
        }
    }
}
