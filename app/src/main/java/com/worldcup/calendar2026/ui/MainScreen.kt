package com.worldcup.calendar2026.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worldcup.calendar2026.R
import com.worldcup.calendar2026.ui.screens.CalendarScreen
import com.worldcup.calendar2026.ui.screens.LiveScreen
import com.worldcup.calendar2026.ui.screens.MatchDetailScreen
import com.worldcup.calendar2026.ui.screens.NextDayScreen
import com.worldcup.calendar2026.ui.screens.SettingsScreen
import com.worldcup.calendar2026.ui.screens.StandingsScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Calendar("calendar", "Calendar", Icons.Filled.CalendarMonth),
    Live("live", "Live", Icons.Filled.PlayCircle),
    Standings("standings", "Table", Icons.Filled.EmojiEvents),
    NextDay("nextday", "Tomorrow", Icons.Filled.Today),
    Settings("settings", "Settings", Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = Tab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    } ?: Tab.Calendar

    val showBottomBar = Tab.entries.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(title = { Text("${stringResource(R.string.app_name)} · ${currentTab.label}") })
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.Calendar.route) {
                CalendarScreen(onMatchClick = { matchId -> navController.navigate("match/$matchId") })
            }
            composable(Tab.Live.route) {
                LiveScreen(onMatchClick = { matchId -> navController.navigate("match/$matchId") })
            }
            composable(Tab.Standings.route) { StandingsScreen() }
            composable(Tab.NextDay.route) {
                NextDayScreen(onMatchClick = { matchId -> navController.navigate("match/$matchId") })
            }
            composable(Tab.Settings.route) { SettingsScreen() }
            composable(
                route = "match/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.IntType })
            ) {
                MatchDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
