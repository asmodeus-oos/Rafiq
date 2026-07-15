package com.rafiq.domain.model

data class Chat(
    val id: String,
    val participantName: String,
    val participantAvatar: String,
    val lastMessage: String,
    val unreadCount: Int,
    val timestamp: Long
)
