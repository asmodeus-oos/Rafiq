package com.rafiq.domain.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareManager {

    const val BASE_DOMAIN = "https://rafiq-roan.vercel.app"
    const val SECONDARY_DOMAIN = "https://rafiq.app"

    // ── Canonical URL Generators ──────────────────────────────────────────────

    fun getProfileUrl(userId: String, username: String? = null): String {
        return if (!username.isNullOrBlank()) {
            "$BASE_DOMAIN/u/$username"
        } else {
            "$BASE_DOMAIN/profile/$userId"
        }
    }

    fun getPostUrl(postId: String): String {
        return "$BASE_DOMAIN/post/$postId"
    }

    fun getCommentUrl(postId: String, commentId: String): String {
        return "$BASE_DOMAIN/post/$postId/comment/$commentId"
    }

    fun getCommunityUrl(communityId: String): String {
        return "$BASE_DOMAIN/community/$communityId"
    }

    fun getVoiceRoomUrl(roomId: String): String {
        return "$BASE_DOMAIN/voice/$roomId"
    }

    fun getPrivateVoiceUrl(roomId: String): String {
        return "$BASE_DOMAIN/privatevoice/$roomId"
    }

    fun getEventUrl(eventId: String): String {
        return "$BASE_DOMAIN/event/$eventId"
    }

    fun getStoryUrl(storyId: String): String {
        return "$BASE_DOMAIN/story/$storyId"
    }

    // ── Android Sharing Actions ───────────────────────────────────────────────

    /**
     * Triggers the Android system share chooser with subject/title and link.
     */
    fun shareText(context: Context, title: String, text: String, url: String) {
        val shareBody = "$text\n\n$url"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        val chooser = Intent.createChooser(intent, title)
        context.startActivity(chooser)
    }

    /**
     * Copies a URL/text to the Android Clipboard and displays a Toast feedback.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "RAFIQ Link") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to copy link", Toast.LENGTH_SHORT).show()
        }
    }

    // ── High-Level Entity Share Helpers ───────────────────────────────────────

    fun shareProfile(context: Context, name: String, username: String, userId: String) {
        val url = getProfileUrl(userId, username)
        val title = "Share Profile: $name"
        val text = "Check out $name (@$username) on RAFIQ!"
        shareText(context, title, text, url)
    }

    fun sharePost(context: Context, authorName: String, textContent: String, postId: String) {
        val url = getPostUrl(postId)
        val title = "Share Post by $authorName"
        val snippet = if (textContent.length > 80) textContent.take(80) + "..." else textContent
        val text = "Post by $authorName on RAFIQ:\n\"$snippet\""
        shareText(context, title, text, url)
    }

    fun shareComment(context: Context, postId: String, commentId: String, authorName: String, commentText: String) {
        val url = getCommentUrl(postId, commentId)
        val title = "Share Comment"
        val snippet = if (commentText.length > 60) commentText.take(60) + "..." else commentText
        val text = "$authorName's comment on RAFIQ:\n\"$snippet\""
        shareText(context, title, text, url)
    }

    fun shareVoiceRoom(context: Context, roomTitle: String, roomId: String) {
        val url = getVoiceRoomUrl(roomId)
        val title = "Join Voice Room: $roomTitle"
        val text = "Join me in the voice room \"$roomTitle\" on RAFIQ!"
        shareText(context, title, text, url)
    }

    fun shareCommunity(context: Context, communityName: String, communityId: String) {
        val url = getCommunityUrl(communityId)
        val title = "Join Community: $communityName"
        val text = "Check out the $communityName community on RAFIQ!"
        shareText(context, title, text, url)
    }
}
