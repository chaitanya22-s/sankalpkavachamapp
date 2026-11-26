package com.sentinel.protection.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sentinel.protection.ui.screens.assistant.AssistantScreen
import com.sentinel.protection.ui.screens.dashboard.DashboardScreen
import com.sentinel.protection.ui.screens.dialer.CallsScreen
import com.sentinel.protection.ui.screens.dialer.DialerScreen
import com.sentinel.protection.ui.screens.dialer.NumberCheckScreen
import com.sentinel.protection.ui.screens.honeytrap.HoneyTrapScreen
import com.sentinel.protection.ui.screens.media.MediaCheckerScreen
import com.sentinel.protection.ui.screens.privacy.CyberHelpScreen
import com.sentinel.protection.ui.screens.privacy.PrivacyScreen
import com.sentinel.protection.ui.screens.profile.ProfileScreen
import com.sentinel.protection.ui.screens.profile.SettingsScreen
import com.sentinel.protection.ui.screens.spam.MessageScanScreen
import com.sentinel.protection.ui.screens.spam.SpamBoxScreen
import com.sentinel.protection.ui.theme.PrimaryGreen

@Composable
fun KavachamNavGraph(
    isTablet: Boolean = false,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (isTablet) {
        // Tablet: Navigation Rail on the side
        androidx.compose.foundation.layout.Row {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryGreen
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationRailItem(
                        selected = selected,
                        onClick = { navigateTo(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            KavachamNavHost(navController = navController, modifier = Modifier.weight(1f))
        }
    } else {
        // Phone: Bottom Navigation
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTo(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                indicatorColor = Color(0xFFDCFCE7)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            KavachamNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun KavachamNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dialer.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigate = { navController.navigate(it) }
            )
        }
        composable(Screen.Dialer.route) {
            DialerScreen(
                onNavigateTo = { navController.navigate(it) },
                onNumberCheck = { number ->
                    navController.navigate(Screen.NumberCheck.createRoute(number))
                }
            )
        }
        composable(Screen.Calls.route) {
            CallsScreen(
                onNumberCheck = { number ->
                    navController.navigate(Screen.NumberCheck.createRoute(number))
                }
            )
        }
        composable(Screen.NumberCheck.route) { backStackEntry ->
            val number = backStackEntry.arguments?.getString("number") ?: ""
            NumberCheckScreen(
                phoneNumber = number,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SpamBox.route) {
            SpamBoxScreen(
                onNavigateTo = { navController.navigate(it) }
            )
        }
        composable(Screen.MessageScan.route) {
            MessageScanScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Assistant.route) {
            AssistantScreen()
        }
        composable(Screen.HoneyTrap.route) {
            HoneyTrapScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MediaChecker.route) {
            MediaCheckerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CyberHelp.route) {
            CyberHelpScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateTo = { navController.navigate(it) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

