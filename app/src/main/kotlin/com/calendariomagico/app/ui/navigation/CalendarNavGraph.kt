package com.calendariomagico.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calendariomagico.app.data.repository.CalendarRepository
import com.calendariomagico.app.ui.home.HomeScreen
import com.calendariomagico.app.ui.onboarding.OnboardingScreen
import com.calendariomagico.app.ui.settings.SettingsScreen

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun CalendarApp(repository: CalendarRepository) {
    val activeCalendar by repository.activeCalendar.collectAsStateWithLifecycle(initialValue = null)
    val navController = rememberNavController()

    LaunchedEffect(activeCalendar) {
        val target = if (activeCalendar != null) ROUTE_HOME else ROUTE_ONBOARDING
        val current = navController.currentDestination?.route ?: navController.currentBackStackEntry?.destination?.route
        if (current != target && current != ROUTE_SETTINGS) {
            navController.navigate(target) { popUpTo(0) { inclusive = true } }
        } else if (current == ROUTE_SETTINGS && target == ROUTE_ONBOARDING) {
            navController.navigate(target) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_ONBOARDING) {
        composable(ROUTE_ONBOARDING) { OnboardingScreen(repository) }
        composable(ROUTE_HOME) {
            HomeScreen(repository, onOpenSettings = { navController.navigate(ROUTE_SETTINGS) })
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onLeft = {
                    navController.navigate(ROUTE_ONBOARDING) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
