package com.rafiq.presentation.screen.call

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rafiq.domain.manager.CallEngineState
import com.rafiq.domain.manager.CallManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import javax.inject.Inject

@HiltViewModel
class ActiveCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    val callState: StateFlow<CallEngineState> = CallManager.callState

    val eglBaseContext: EglBase.Context?
        get() = CallManager.eglBaseContext

    private val roomId: String
        get() = savedStateHandle.get<String>("roomId") ?: "default_room"

    val isVideoCall: Boolean
        get() = savedStateHandle.get<Boolean>("isVideo") ?: false

    init {
        CallManager.initialize(context, supabaseClient)
        CallManager.startOrJoinCall(roomId, isVideoCall)
    }

    fun initializeWebrtc() {
        CallManager.startOrJoinCall(roomId, isVideoCall)
    }

    fun toggleMute() {
        CallManager.toggleMute()
    }

    fun toggleSpeaker() {
        CallManager.toggleSpeaker()
    }

    fun toggleCamera() {
        CallManager.toggleCamera()
    }

    fun endCall() {
        CallManager.endCall()
    }
}
