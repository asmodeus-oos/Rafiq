package com.rafiq.domain.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalCallState {
    var isCallActive by mutableStateOf(false)
    var isMinimized by mutableStateOf(false)
    var roomId by mutableStateOf("default_room")
    var isVideoCall by mutableStateOf(false)
    var partnerId by mutableStateOf("")
    var partnerName by mutableStateOf("Partner")
    var partnerAvatar by mutableStateOf("")
    var isSpeakerOn by mutableStateOf(false)
    var isMuted by mutableStateOf(false)

    fun startCall(room: String, isVideo: Boolean, partnerUser: User?) {
        roomId = room
        isVideoCall = isVideo
        partnerId = partnerUser?.id ?: ""
        partnerName = partnerUser?.name?.takeIf { it.isNotBlank() } ?: "Partner"
        partnerAvatar = partnerUser?.avatar ?: ""
        isCallActive = true
        isMinimized = false
        isSpeakerOn = false
        isMuted = false
    }

    fun minimize() {
        if (isCallActive) {
            isMinimized = true
        }
    }

    fun restore() {
        isMinimized = false
    }

    fun endCall() {
        isCallActive = false
        isMinimized = false
        roomId = "default_room"
        partnerAvatar = ""
        partnerName = "Partner"
    }
}
