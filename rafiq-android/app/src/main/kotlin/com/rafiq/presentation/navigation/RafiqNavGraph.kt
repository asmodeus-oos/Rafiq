package com.rafiq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rafiq.domain.manager.DeepLinkManager
import com.rafiq.presentation.auth.AuthScreen
import com.rafiq.presentation.screen.call.ActiveCallScreen
import com.rafiq.presentation.screen.call.RandomCallMatchingScreen
import com.rafiq.presentation.screen.chat.ChatDetailScreen
import com.rafiq.presentation.screen.chat.ChatListScreen
import com.rafiq.presentation.screen.chat.ChatViewModel
import com.rafiq.presentation.screen.discovery.DiscoveryScreen
import com.rafiq.presentation.screen.home.HomeScreen
import com.rafiq.presentation.screen.notification.NotificationScreen
import com.rafiq.presentation.screen.post.PostDetailsScreen
import com.rafiq.presentation.screen.profile.ModernProfileScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun RafiqNavGraph(
    supabaseClient: SupabaseClient,
    navController: NavHostController = rememberNavController(),
    deepLinkRoute: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val startDestination = if (supabaseClient.auth.currentSessionOrNull() != null) {
        deepLinkRoute ?: Route.Home.route
    } else {
        Route.Auth.route
    }

    LaunchedEffect(deepLinkRoute) {
        if (deepLinkRoute != null && supabaseClient.auth.currentSessionOrNull() != null) {
            navController.navigate(deepLinkRoute) {
                popUpTo(Route.Home.route) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    val pendingRoute = DeepLinkManager.getAndClearPendingDeepLink(context)
                    val target = pendingRoute ?: Route.Home.route
                    navController.navigate(target) {
                        popUpTo(Route.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Home.route) {
            HomeScreen(
                supabaseClient = supabaseClient,
                onNavigateToDiscovery = { navController.navigate(Route.Discovery.route) },
                onNavigateToRandomCall = { navController.navigate(Route.RandomCallMatching.route) },
                onNavigateToProfile = { userId ->
                    navController.navigate(Route.Profile.createRoute(userId))
                },
                onNavigateToPostDetails = { postId ->
                    navController.navigate(Route.PostDetails.createRoute(postId))
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
                onNavigateToNotifications = { navController.navigate(Route.Notifications.route) },
                onNavigateToChat = { targetUserId -> navController.navigate(Route.ChatDetail.createRoute(targetUserId)) },
                onNavigateToRoom = { roomId -> navController.navigate(Route.ActiveCall.createRoute(roomId)) }
            )
        }
        composable(
            route = Route.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/profile/{userId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/u/{userId}" },
                navDeepLink { uriPattern = "https://rafiq.app/profile/{userId}" },
                navDeepLink { uriPattern = "https://rafiq.app/u/{userId}" },
                navDeepLink { uriPattern = "rafiq://profile/{userId}" },
                navDeepLink { uriPattern = "rafiq://u/{userId}" }
            )
        ) {
            ModernProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Route.EditProfile.route) },
                onNavigateToPostDetails = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) },
                onNavigateToChat = { targetUserId ->
                    if (targetUserId.isNotBlank()) {
                        navController.navigate(Route.ChatDetail.createRoute(targetUserId))
                    }
                }
            )
        }
        composable(
            route = Route.PostDetails.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("commentId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/post/{postId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/post/{postId}?commentId={commentId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/post/{postId}/comment/{commentId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/comment/{commentId}" },
                navDeepLink { uriPattern = "https://rafiq.app/post/{postId}" },
                navDeepLink { uriPattern = "https://rafiq.app/post/{postId}/comment/{commentId}" },
                navDeepLink { uriPattern = "https://rafiq.app/comment/{commentId}" },
                navDeepLink { uriPattern = "rafiq://post/{postId}" },
                navDeepLink { uriPattern = "rafiq://post/{postId}/comment/{commentId}" },
                navDeepLink { uriPattern = "rafiq://comment/{commentId}" }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val commentId = backStackEntry.arguments?.getString("commentId")
            PostDetailsScreen(
                postId = postId,
                highlightCommentId = commentId,
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
                onNavigateToPost = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) },
                onNavigateToProfile = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Route.Profile.createRoute(userId))
                    }
                },
                onNavigateToChat = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Route.ChatDetail.createRoute(userId))
                    }
                }
            )
        }
        composable(Route.ChatList.route) {
            ChatListScreen(
                onNavigateToChat = { targetUserId ->
                    navController.navigate(Route.ChatDetail.createRoute(targetUserId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.ChatDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/chat/{userId}" },
                navDeepLink { uriPattern = "https://rafiq.app/chat/{userId}" },
                navDeepLink { uriPattern = "rafiq://chat/{userId}" }
            )
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel: ChatViewModel = hiltViewModel()

            LaunchedEffect(targetUserId) {
                viewModel.getMessages(targetUserId)
            }

            val messages by viewModel.messages.collectAsState()
            val targetUser by viewModel.targetUser.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            ChatDetailScreen(
                currentUserId = viewModel.currentUserId,
                targetUser = targetUser,
                currentUserAvatar = currentUser?.avatar,
                messages = messages,
                onSendMessage = { text, mediaUrl, isVoice, replyToId ->
                    viewModel.sendMessage(targetUserId, text, mediaUrl, isVoice, replyToId)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Route.Profile.createRoute(userId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.RandomCallMatching.route) {
            RandomCallMatchingScreen(
                onCancel = { navController.popBackStack() },
                onCallConnected = { roomId ->
                    navController.navigate(Route.ActiveCall.createRoute(roomId)) {
                        popUpTo(Route.RandomCallMatching.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Route.ActiveCall.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("isVideo") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/call/{roomId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/voice/{roomId}" },
                navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/privatevoice/{roomId}" },
                navDeepLink { uriPattern = "https://rafiq.app/call/{roomId}" },
                navDeepLink { uriPattern = "https://rafiq.app/voice/{roomId}" },
                navDeepLink { uriPattern = "https://rafiq.app/privatevoice/{roomId}" },
                navDeepLink { uriPattern = "rafiq://call/{roomId}" },
                navDeepLink { uriPattern = "rafiq://voice/{roomId}" },
                navDeepLink { uriPattern = "rafiq://privatevoice/{roomId}" }
            )
        ) {
            ActiveCallScreen(
                onCallEnded = { navController.popBackStack() }
            )
        }
    }
}
