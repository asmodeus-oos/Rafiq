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

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rafiq.domain.model.User
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

@Composable
fun RafiqNavGraph(
    navController: NavHostController = rememberNavController(),
    supabaseClient: SupabaseClient,
    deepLinkRoute: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val startDestination = deepLinkRoute ?: Route.Auth.route
    
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
                onNavigateToRoom = { roomId -> /* Navigate to active voice room */ }
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
                androidx.navigation.navDeepLink { uriPattern = "https://rafiq-roan.vercel.app/profile/{userId}" }
            )
        ) {
            com.rafiq.presentation.screen.profile.ModernProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Route.EditProfile.route) },
                onNavigateToPostDetails = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) },
                onNavigateToChat = { targetUserId -> navController.navigate(Route.ChatDetail.createRoute(targetUserId)) }
            )
        }
        composable(
            route = Route.PostDetails.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
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
                onNavigateToPost = { postId -> navController.navigate(Route.PostDetails.createRoute(postId)) },
                onNavigateToProfile = { userId -> navController.navigate(Route.Profile.createRoute(userId)) },
                onNavigateToChat = { userId -> navController.navigate(Route.ChatDetail.createRoute(userId)) }
            )
        }
        composable(Route.ChatList.route) {
            val chatListViewModel: com.rafiq.presentation.screen.chat.ChatListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val chats by chatListViewModel.chats.collectAsState()
            val isLoading by chatListViewModel.isLoading.collectAsState()
            com.rafiq.presentation.screen.chat.ChatListScreen(
                chats = chats,
                isLoading = isLoading,
                onRefresh = { chatListViewModel.refresh() },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { targetUserId -> navController.navigate(Route.ChatDetail.createRoute(targetUserId)) }
            )
        }
        composable(
            route = Route.ChatDetail.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatViewModel: com.rafiq.presentation.screen.chat.ChatViewModel = androidx.hilt.navigation.compose.hiltViewModel()

            // Effect to load messages
            androidx.compose.runtime.LaunchedEffect(otherUserId) {
                chatViewModel.getMessages(otherUserId)
            }

            val messages by chatViewModel.messages.collectAsState()
            val isTyping by chatViewModel.isTyping.collectAsState()
            val targetUser by chatViewModel.targetUser.collectAsState()
            val currentUser by chatViewModel.currentUser.collectAsState()

            // Build a deterministic room ID from sorted user IDs
            val myId = chatViewModel.currentUserId ?: ""
            val chatRoomId = if (myId.isNotEmpty() && otherUserId.isNotEmpty()) {
                listOf(myId, otherUserId).sorted().joinToString("_")
            } else "default_room"

            com.rafiq.presentation.screen.chat.ChatDetailScreen(
                currentUserId = chatViewModel.currentUserId,
                targetUser = targetUser,
                currentUserAvatar = currentUser?.avatar,
                messages = messages,
                isTyping = isTyping,
                onSendMessage = { text, mediaUrl, isVoice, replyToId ->
                    chatViewModel.sendMessage(
                        receiverId = otherUserId,
                        textContent = text,
                        mediaUrl = mediaUrl,
                        isVoice = isVoice,
                        replyToId = replyToId
                    )
                },
                onDeleteMessage = { chatViewModel.deleteMessageForEveryone(it) },
                onTyping = { chatViewModel.sendTypingEvent() },
                onBack = { navController.popBackStack() },
                onCallClick = { navController.navigate(Route.ActiveCall.createRoute(chatRoomId)) },
                onVideoCallClick = { navController.navigate(Route.ActiveCall.createRoute(chatRoomId)) }
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
                navArgument("roomId") {
                    type = NavType.StringType
                    defaultValue = "default_room"
                }
            )
        ) {
            com.rafiq.presentation.screen.call.ActiveCallScreen(
                onCallEnded = { navController.popBackStack(Route.Home.route, false) }
            )
        }
    }
}
