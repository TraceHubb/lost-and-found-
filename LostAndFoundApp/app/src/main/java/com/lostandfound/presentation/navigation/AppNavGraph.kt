package com.lostandfound.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lostandfound.data.models.ItemType
import com.lostandfound.presentation.auth.LoginScreen
import com.lostandfound.presentation.auth.RegisterScreen
import com.lostandfound.presentation.home.HomeScreen
import com.lostandfound.presentation.items.ItemsListScreen
import com.lostandfound.presentation.items.ReportItemScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onReportLost = { navController.navigate("report/${ItemType.LOST.name}") },
                onReportFound = { navController.navigate("report/${ItemType.FOUND.name}") },
                onBrowseLost = { navController.navigate("items/${ItemType.LOST.name}") },
                onBrowseFound = { navController.navigate("items/${ItemType.FOUND.name}") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "report/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: ItemType.LOST.name
            val type = runCatching { ItemType.valueOf(typeStr) }.getOrDefault(ItemType.LOST)
            ReportItemScreen(
                type = type,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = "items/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: ItemType.LOST.name
            val type = runCatching { ItemType.valueOf(typeStr) }.getOrDefault(ItemType.LOST)
            ItemsListScreen(
                type = type,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

