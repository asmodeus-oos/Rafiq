package com.rafiq.domain.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.rafiq.presentation.navigation.Route

sealed class DeepLinkTarget {
    data class Profile(val userId: String? = null, val username: String? = null) : DeepLinkTarget()
    data class Post(val postId: String, val highlightCommentId: String? = null) : DeepLinkTarget()
    data class Comment(val commentId: String) : DeepLinkTarget()
    data class Community(val communityId: String) : DeepLinkTarget()
    data class VoiceRoom(val roomId: String) : DeepLinkTarget()
    data class PrivateVoice(val roomId: String) : DeepLinkTarget()
    data class Event(val eventId: String) : DeepLinkTarget()
    data class Story(val storyId: String) : DeepLinkTarget()
    data class Chat(val userId: String) : DeepLinkTarget()

    fun toRouteString(): String = when (this) {
        is Profile -> {
            if (!userId.isNullOrBlank()) Route.Profile.createRoute(userId)
            else Route.Profile.createRoute()
        }
        is Post -> {
            if (!highlightCommentId.isNullOrBlank()) {
                Route.PostDetails.createRouteWithComment(postId, highlightCommentId)
            } else {
                Route.PostDetails.createRoute(postId)
            }
        }
        is Comment -> Route.PostDetails.createRouteForComment(commentId)
        is Community -> Route.Discovery.route // Or community route
        is VoiceRoom -> Route.ActiveCall.createRoute(roomId, isVideo = false)
        is PrivateVoice -> Route.ActiveCall.createRoute(roomId, isVideo = false)
        is Event -> Route.Home.route
        is Story -> Route.Home.route
        is Chat -> Route.ChatDetail.createRoute(userId)
    }
}

object DeepLinkManager {

    private const val TAG = "DeepLinkManager"
    private const val PREFS_NAME = "rafiq_deeplink_prefs"
    private const val KEY_PENDING_ROUTE = "pending_deeplink_route"

    /**
     * Parses an Android [Intent] to extract a [DeepLinkTarget].
     */
    fun parseIntent(intent: Intent?): DeepLinkTarget? {
        if (intent == null) return null

        // 1. Check explicit extras (Push Notifications)
        val targetPostId = intent.getStringExtra("POST_ID")
        val targetUserId = intent.getStringExtra("USER_ID")
        val targetCommentId = intent.getStringExtra("COMMENT_ID")
        val targetRoomId = intent.getStringExtra("ROOM_ID")
        val isChat = intent.getBooleanExtra("IS_CHAT", false)

        if (!targetPostId.isNullOrBlank()) {
            return DeepLinkTarget.Post(postId = targetPostId, highlightCommentId = targetCommentId)
        }
        if (!targetUserId.isNullOrBlank()) {
            return if (isChat) DeepLinkTarget.Chat(targetUserId) else DeepLinkTarget.Profile(userId = targetUserId)
        }
        if (!targetCommentId.isNullOrBlank()) {
            return DeepLinkTarget.Comment(targetCommentId)
        }
        if (!targetRoomId.isNullOrBlank()) {
            return DeepLinkTarget.VoiceRoom(targetRoomId)
        }

        // 2. Check Intent Data Uri (Deep Links & App Links)
        val data: Uri? = intent.data
        if (data != null) {
            return parseUri(data)
        }

        return null
    }

    /**
     * Parses a [Uri] into a [DeepLinkTarget].
     *
     * Supported URL structures:
     * - rafiq://profile/{id} or rafiq://u/{username}
     * - rafiq://post/{postId}
     * - rafiq://post/{postId}/comment/{commentId} or rafiq://comment/{commentId}
     * - rafiq://community/{id}
     * - rafiq://voice/{id}
     * - rafiq://privatevoice/{id}
     * - rafiq://event/{id}
     * - rafiq://story/{id}
     * - rafiq://chat/{userId}
     * - https://rafiq-roan.vercel.app/profile/{id} or /u/{username}
     * - https://rafiq-roan.vercel.app/post/{postId}
     * - https://rafiq-roan.vercel.app/post/{postId}/comment/{commentId}
     * - https://rafiq-roan.vercel.app/comment/{commentId}
     * - https://rafiq-roan.vercel.app/community/{id}
     * - https://rafiq-roan.vercel.app/voice/{id}
     * - https://rafiq-roan.vercel.app/privatevoice/{id}
     * - https://rafiq.app/...
     */
    fun parseUri(uri: Uri): DeepLinkTarget? {
        Log.d(TAG, "Parsing Uri: $uri (scheme: ${uri.scheme}, host: ${uri.host}, path: ${uri.path})")
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: ""
        val pathSegments = uri.pathSegments ?: emptyList()

        if (scheme == "rafiq") {
            return parsePathSegments(host, pathSegments)
        } else if (scheme == "http" || scheme == "https") {
            if (host == "rafiq-roan.vercel.app" || host == "rafiq.app" || host.endsWith(".vercel.app")) {
                if (pathSegments.isEmpty()) return null
                val firstSeg = pathSegments[0].lowercase()
                val remaining = pathSegments.drop(1)
                return parsePathSegments(firstSeg, remaining)
            }
        }
        return null
    }

    private fun parsePathSegments(root: String, segments: List<String>): DeepLinkTarget? {
        val rootPath = root.lowercase()
        return when (rootPath) {
            "u", "username" -> {
                val username = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Profile(username = username)
            }
            "profile" -> {
                val idOrUsername = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Profile(userId = idOrUsername)
            }
            "post" -> {
                val postId = segments.getOrNull(0) ?: return null
                if (segments.getOrNull(1)?.lowercase() == "comment") {
                    val commentId = segments.getOrNull(2)
                    DeepLinkTarget.Post(postId = postId, highlightCommentId = commentId)
                } else {
                    DeepLinkTarget.Post(postId = postId)
                }
            }
            "comment" -> {
                val commentId = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Comment(commentId = commentId)
            }
            "community" -> {
                val id = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Community(communityId = id)
            }
            "voice", "voiceroom", "room" -> {
                val id = segments.getOrNull(0) ?: return null
                DeepLinkTarget.VoiceRoom(roomId = id)
            }
            "privatevoice", "call" -> {
                val id = segments.getOrNull(0) ?: return null
                DeepLinkTarget.PrivateVoice(roomId = id)
            }
            "event" -> {
                val id = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Event(eventId = id)
            }
            "story" -> {
                val id = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Story(storyId = id)
            }
            "chat" -> {
                val userId = segments.getOrNull(0) ?: return null
                DeepLinkTarget.Chat(userId = userId)
            }
            else -> null
        }
    }

    // ── Pending Deep Link Storage (for Unauthenticated Users) ───────────────────

    fun savePendingDeepLink(context: Context, routeString: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PENDING_ROUTE, routeString).apply()
        Log.d(TAG, "Saved pending deep link route: $routeString")
    }

    fun getAndClearPendingDeepLink(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val route = prefs.getString(KEY_PENDING_ROUTE, null)
        if (route != null) {
            prefs.edit().remove(KEY_PENDING_ROUTE).apply()
            Log.d(TAG, "Retrieved & cleared pending deep link route: $route")
        }
        return route
    }
}
