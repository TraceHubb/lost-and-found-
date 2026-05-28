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
import com.lostandfound.presentation.claim.SimpleClaimScreen
import com.lostandfound.presentation.claim.SimpleLostClaimScreen
import com.lostandfound.presentation.claim.ClaimSuccessScreen
import com.lostandfound.presentation.home.HomeScreen
import com.lostandfound.presentation.history.HistoryScreen
import com.lostandfound.presentation.items.ItemDetailScreen
import com.lostandfound.presentation.items.ItemsListScreen
import com.lostandfound.presentation.items.ReportFoundItemScreen
import com.lostandfound.presentation.items.ReportLostItemScreen
import com.lostandfound.presentation.items.SimpleItemsListScreen
import com.lostandfound.presentation.items.SimpleLostItemsListScreen
import com.lostandfound.presentation.suggestions.SuggestionsScreen

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
                onReportLost = { navController.navigate("report_lost") },
                onReportFound = { navController.navigate("report_found") },
                onBrowseLost = { navController.navigate("simple_lost_items") }, // Use simple lost items list
                onBrowseFound = { navController.navigate("simple_items") }, // Use simple found items list
                onNavigateToMatching = { navController.navigate("suggestions") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToItemsReady = { navController.navigate("simple_lost_items") },
                onNavigateToItemsInReview = { navController.navigate("simple_lost_items") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("suggestions") {
            SuggestionsScreen(
                onBack = { navController.popBackStack() },
                onClaimFoundItem = { foundItemId ->
                    navController.navigate("simple_claim/$foundItemId")
                }
            )
        }

        composable("report_lost") {
            ReportLostItemScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { navController.popBackStack() }
            )
        }

        composable("report_found") {
            ReportFoundItemScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { navController.popBackStack() }
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

        composable(
            route = "item_detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }

        // Simple Items System Routes
        composable("simple_items") {
            SimpleItemsListScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate("simple_claim/$itemId")
                }
            )
        }

        // Simple Lost Items System Routes
        composable("simple_lost_items") {
            SimpleLostItemsListScreen(
                onBack = { navController.popBackStack() },
                onItemClick = {
                    // Browsing a lost item should go straight to reporting it as found.
                    navController.navigate("report_found")
                }
            )
        }

        composable(
            route = "simple_claim/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            SimpleClaimScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onClaimSuccess = { contactEmail, contactPhone ->
                    navController.navigate("claim_success/$contactEmail/$contactPhone") {
                        popUpTo("simple_claim/$itemId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "simple_lost_claim/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            SimpleLostClaimScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onClaimSuccess = { contactEmail, contactPhone ->
                    navController.navigate("claim_success/$contactEmail/$contactPhone") {
                        popUpTo("simple_lost_claim/$itemId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "claim_success/{contactEmail}/{contactPhone}",
            arguments = listOf(
                navArgument("contactEmail") { type = NavType.StringType },
                navArgument("contactPhone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contactEmail = backStackEntry.arguments?.getString("contactEmail") ?: ""
            val contactPhone = backStackEntry.arguments?.getString("contactPhone") ?: ""
            ClaimSuccessScreen(
                contactEmail = contactEmail,
                contactPhone = contactPhone,
                onBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

