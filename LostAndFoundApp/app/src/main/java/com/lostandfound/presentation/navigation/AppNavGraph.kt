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
import com.lostandfound.presentation.claim.ContactInfoScreen
import com.lostandfound.presentation.claim.OtpInputScreen
import com.lostandfound.presentation.home.HomeScreen
import com.lostandfound.presentation.items.ItemsListScreen
import com.lostandfound.presentation.items.ReportItemScreen
import com.lostandfound.presentation.items.ItemDetailScreen
import com.lostandfound.presentation.matching.MatchingScreen
import com.lostandfound.presentation.matches.MatchResultsScreen

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
                onReportLost = { navController.navigate("report") },
                onReportFound = { navController.navigate("report") },
                onBrowseLost = { navController.navigate("items/${ItemType.LOST.name}") },
                onBrowseFound = { navController.navigate("items/${ItemType.FOUND.name}") },
                onNavigateToMatching = { navController.navigate("matching") },
                onNavigateToItemsReady = { navController.navigate("items/${ItemType.LOST.name}") },
                onNavigateToItemsInReview = { navController.navigate("items/${ItemType.LOST.name}") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("report") {
            ReportItemScreen(
                type = ItemType.LOST,  // Default type, will be selected in Step 1
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

        composable("matching") {
            MatchingScreen(
                onBack = { navController.popBackStack() },
                onFindMatches = { itemId ->
                    navController.navigate("match_results/$itemId")
                }
            )
        }

        composable(
            route = "match_results/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            MatchResultsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onViewDetails = { matchedItemId ->
                    navController.navigate("item_detail/$matchedItemId")
                },
                navController = navController
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

        composable(
            route = "otp_input/{matchId}/{lostItemId}/{foundItemId}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.StringType },
                navArgument("lostItemId") { type = NavType.StringType },
                navArgument("foundItemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            val lostItemId = backStackEntry.arguments?.getString("lostItemId") ?: ""
            val foundItemId = backStackEntry.arguments?.getString("foundItemId") ?: ""
            OtpInputScreen(
                matchId = matchId,
                lostItemId = lostItemId,
                foundItemId = foundItemId,
                navController = navController
            )
        }

        composable(
            route = "contact_info/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            ContactInfoScreen(
                matchId = matchId,
                navController = navController
            )
        }
    }
}

