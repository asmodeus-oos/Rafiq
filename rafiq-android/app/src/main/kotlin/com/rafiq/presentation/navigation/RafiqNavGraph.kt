package com.rafiq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafiq.presentation.auth.AuthScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import com.rafiq.presentation.screen.home.HomeScreen
import com.rafiq.presentation.screen.discovery.DiscoveryScreen
import com.rafiq.presentation.screen.call.RandomCallMatchingScreen
import com.rafiq.presentation.screen.notification.NotificationScreen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RafiqNavGraph(
    navController: NavHostController = rememberNavController(),
    supabaseClient: SupabaseClient
) {
    val coroutineScope = rememberCoroutineScope()
    val startDestination = if (supabaseClient.auth.currentUserOrNull() != null) Route.Home.route else Route.Auth.route
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) }
    ) {

        composable(Route.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToDiscovery = { navController.navigate(Route.Discovery.route) },
                onNavigateToRandomCall = { navController.navigate(Route.RandomCallMatching.route) },
                onNavigateToProfile = { userId -> 
                    navController.navigate(Route.Profile.createRoute(userId)) 
                },
                onSignOut = {
                    coroutineScope.launch {
                        try {
                            supabaseClient.auth.signOut()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        navController.navigate(Route.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToNotifications = { navController.navigate(Route.Notifications.route) }
            )
        }
        composable(
            route = Route.Profile.route,
            arguments = listOf(
                androidx.navigation.navArgument("userId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                androidx.navigation.navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/profile/{userId}" }
            )
        ) {
            com.rafiq.presentation.screen.profile.ModernProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Route.EditProfile.route) },
                onNavigateToPostDetails = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) }
            )
        }
        composable(
            route = Route.PostDetails.route,
            arguments = listOf(
                androidx.navigation.navArgument("postId") {
                    type = androidx.navigation.NavType.StringType
                }
            ),
            deepLinks = listOf(
                androidx.navigation.navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/post/{postId}" }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            com.rafiq.presentation.screen.post.PostDetailsScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId -> navController.navigate(Route.Profile.createRoute(userId)) }
            )
        }
        composable(Route.EditProfile.route) {
            com.rafiq.presentation.screen.profile.EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Discovery.route) {
            DiscoveryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPost = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) }
            )
        }
        composable(Route.RandomCallMatching.route) {
            RandomCallMatchingScreen(
                onCancel = { navController.popBackStack() },
                onCallConnected = { navController.navigate(Route.ActiveCall.route) }
            )
        }
        composable(Route.ActiveCall.route) {
            // Placeholder for active call
        }
    }
}
