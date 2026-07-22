package com.rafiq.presentation.navigation

sealed class Route(val route: String) {
    object Splash : Route("splash")
    
    // Auth Flow
    object Auth : Route("auth")
    
    // Main App Flow
    object Home : Route("home")
    object Discovery : Route("discovery")
    object ChatList : Route("chat_list")
    object ChatDetail : Route("chat_detail/{userId}") {
        fun createRoute(userId: String): String {
            return "chat_detail/$userId"
        }
    }
    object EditProfile : Route("edit_profile")
    object Notifications : Route("notifications")
    object Profile : Route("profile?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return if (userId != null) "profile?userId=$userId" else "profile"
        }
    }
    
    object PostDetails : Route("post_details/{postId}") {
        fun createRoute(postId: String): String {
            return "post_details/$postId"
        }
    }
    
    // Call Flow
    object RandomCallMatching : Route("random_call_matching")
    object ActiveCall : Route("active_call/{roomId}?isVideo={isVideo}") {
        fun createRoute(roomId: String, isVideo: Boolean = false): String = "active_call/$roomId?isVideo=$isVideo"
    }
}
