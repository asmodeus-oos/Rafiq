package com.rafiq.domain.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.util.Log
import com.rafiq.domain.model.GlobalCallState
import com.rafiq.domain.model.User
import com.rafiq.service.CallForegroundService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule

data class CallEngineState(
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isVideoCall: Boolean = false,
    val isIncomingCall: Boolean = false,
    val callerName: String = "Rafiq Partner",
    val callerAvatar: String = "",
    val myAvatar: String = "",
    val isEnded: Boolean = false,
    val status: String = "Connecting..."
)

object CallManager {
    private const val TAG = "CallManager"
    private val _callState = MutableStateFlow(CallEngineState())
    val callState: StateFlow<CallEngineState> = _callState.asStateFlow()

    private var applicationContext: Context? = null
    private var supabaseClient: SupabaseClient? = null

    private var eglBase: EglBase? = null
    val eglBaseContext: EglBase.Context?
        get() = eglBase?.eglBaseContext

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var socket: Socket? = null

    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var currentRoomId: String = ""
    private var headsetReceiver: BroadcastReceiver? = null

    fun initialize(context: Context, client: SupabaseClient) {
        applicationContext = context.applicationContext
        supabaseClient = client
    }

    fun startOrJoinCall(roomId: String, isVideo: Boolean) {
        if (_callState.value.status == "In Call" || _callState.value.status == "In Voice Call") {
            if (currentRoomId == roomId) {
                GlobalCallState.restore()
                return
            }
        }

        currentRoomId = roomId
        _callState.value = CallEngineState(
            isVideoCall = isVideo,
            status = "Connecting...",
            isSpeakerOn = false
        )
        GlobalCallState.isSpeakerOn = false

        val context = applicationContext ?: return
        val client = supabaseClient ?: return

        // 1. Delegate Audio Routing to Sovereign AudioRoutingManager
        AudioRoutingManager.startCallAudio(context)

        // 2. Register Headset Broadcast Listener
        registerHeadsetReceiver(context)

        // 3. Start Call Foreground Service for persistent audio in background/bubble
        CallForegroundService.startService(context, _callState.value.callerName)

        // 4. Start ringing via service (NOT via Compose MediaPlayer - that causes audio routing battles)
        CallForegroundService.startRinging(context)
        Log.d(TAG, "startOrJoinCall: ringtone started via service")

        // Query user and partner details reliably
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val parts = roomId.split("_")
                val myId = client.auth.currentUserOrNull()?.id ?: ""

                // Fetch my avatar
                if (myId.isNotBlank()) {
                    val myUser = client.postgrest["users"]
                        .select { filter { eq("id", myId) } }
                        .decodeSingleOrNull<User>()
                    if (myUser != null && myUser.avatar.isNotBlank()) {
                        _callState.value = _callState.value.copy(myAvatar = myUser.avatar)
                    }
                }

                var targetId = parts.firstOrNull { it != myId && it.isNotBlank() && it != "call" && it != "room" }
                if (targetId.isNullOrBlank()) {
                    targetId = parts.lastOrNull { it.isNotBlank() }
                }
                if (!targetId.isNullOrBlank()) {
                    val partnerUser = client.postgrest["users"]
                        .select { filter { eq("id", targetId) } }
                        .decodeSingleOrNull<User>()
                    if (partnerUser != null) {
                        val displayName = partnerUser.name.ifBlank { partnerUser.username.ifBlank { "Partner" } }
                        val avatarUrl = partnerUser.avatar
                        _callState.value = _callState.value.copy(
                            callerName = displayName,
                            callerAvatar = avatarUrl
                        )
                        GlobalCallState.startCall(roomId, isVideo, partnerUser)
                        CallForegroundService.startService(context, displayName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        initializeWebrtc(context)
    }

    fun toggleSpeaker() {
        val context = applicationContext ?: return
        val current = AudioRoutingManager.currentRoute.value
        val newRoute = if (current == AudioRoute.SPEAKER) AudioRoute.EARPIECE else AudioRoute.SPEAKER
        AudioRoutingManager.setUserRoute(context, newRoute)
        
        val isSpeaker = (newRoute == AudioRoute.SPEAKER)
        _callState.value = _callState.value.copy(isSpeakerOn = isSpeaker)
        GlobalCallState.isSpeakerOn = isSpeaker
    }

    fun toggleMute() {
        val isMuted = !_callState.value.isMuted
        localAudioTrack?.setEnabled(!isMuted)
        val context = applicationContext
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.isMicrophoneMute = isMuted
        _callState.value = _callState.value.copy(isMuted = isMuted)
        GlobalCallState.isMuted = isMuted
    }

    private fun registerHeadsetReceiver(context: Context) {
        if (headsetReceiver != null) return
        headsetReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val ctx = applicationContext ?: return
                when (intent?.action) {
                    Intent.ACTION_HEADSET_PLUG -> {
                        val state = intent.getIntExtra("state", -1)
                        AudioRoutingManager.handleHardwareHeadsetEvent(ctx, isConnected = (state == 1))
                    }
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                        AudioRoutingManager.handleHardwareHeadsetEvent(ctx, isConnected = false)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        try {
            context.registerReceiver(headsetReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterHeadsetReceiver(context: Context) {
        try {
            headsetReceiver?.let { context.unregisterReceiver(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        headsetReceiver = null
    }

    private fun initializeWebrtc(context: Context) {
        if (eglBase != null) return
        eglBase = EglBase.create()

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        // Custom JavaAudioDeviceModule to suppress native WebRTC C++ audio route overrides
        val audioDeviceModule: AudioDeviceModule = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .createAudioDeviceModule()

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        startLocalAudioCapture(context)
        connectSignalingServer()
    }

    private fun startLocalAudioCapture(context: Context) {
        val factory = peerConnectionFactory ?: return
        val audioConstraints = MediaConstraints()
        localAudioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", localAudioSource)
        localAudioTrack?.setEnabled(true)
    }

    fun toggleCamera() {
        val newCameraState = !_callState.value.isCameraEnabled
        _callState.value = _callState.value.copy(isCameraEnabled = newCameraState)
        if (newCameraState && localVideoSource == null && _callState.value.isVideoCall) {
            enableVideoCapture()
        } else {
            _callState.value.localVideoTrack?.setEnabled(newCameraState)
        }
    }

    private fun enableVideoCapture() {
        val context = applicationContext ?: return
        val factory = peerConnectionFactory ?: return
        if (videoCapturer != null) return

        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        val frontCamera = deviceNames.firstOrNull { enumerator.isFrontFacing(it) } ?: deviceNames.firstOrNull()

        if (frontCamera != null) {
            videoCapturer = enumerator.createCapturer(frontCamera, null)
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
            localVideoSource = factory.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource!!.capturerObserver)
            videoCapturer?.startCapture(1024, 720, 30)

            val localVideoTrack = factory.createVideoTrack("ARDAMSv0", localVideoSource)
            _callState.value = _callState.value.copy(localVideoTrack = localVideoTrack)
        }
    }

    private fun connectSignalingServer() {
        try {
            socket = IO.socket("https://rafiq-signaling.onrender.com")

            socket?.on(Socket.EVENT_CONNECT) {
                socket?.emit("join-room", currentRoomId)
                _callState.value = _callState.value.copy(status = "Connected to Server")
            }

            socket?.on("user-connected") {
                createPeerConnection()
                createOffer()
            }

            socket?.on("offer") { args ->
                val data = args[0] as JSONObject
                val sdp = SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp"))
                createPeerConnection()
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
                createAnswer()
            }

            socket?.on("answer") { args ->
                val data = args[0] as JSONObject
                val sdp = SessionDescription(SessionDescription.Type.ANSWER, data.getString("sdp"))
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
            }

            socket?.on("ice-candidate") { args ->
                val data = args[0] as JSONObject
                val candidate = IceCandidate(
                    data.getString("sdpMid"),
                    data.getInt("sdpMLineIndex"),
                    data.getString("candidate")
                )
                peerConnection?.addIceCandidate(candidate)
            }

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createPeerConnection() {
        if (peerConnection != null) return
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        )
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                Log.d(TAG, "ICE signaling state: $p0")
            }
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE connection state changed: $state")
                val ctx = applicationContext ?: return
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED -> {
                        // CRITICAL: Stop ringtone immediately when peer is connected.
                        // The transition from ringtone AudioFocus -> voice AudioFocus is the
                        // exact moment Android resets audio routing. We stop ringing here
                        // and immediately re-assert EARPIECE to prevent speaker auto-switch.
                        Log.d(TAG, "ICE CONNECTED: stopping ringtone, re-asserting EARPIECE route")
                        CallForegroundService.stopRinging(ctx)
                        // Only re-assert earpiece if user hasn't manually turned on speaker
                        if (!_callState.value.isSpeakerOn) {
                            AudioRoutingManager.setUserRoute(ctx, AudioRoute.EARPIECE)
                            Log.d(TAG, "ICE CONNECTED: EARPIECE re-asserted")
                        }
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED,
                    PeerConnection.IceConnectionState.FAILED,
                    PeerConnection.IceConnectionState.CLOSED -> {
                        Log.d(TAG, "ICE LOST ($state) — call may end")
                    }
                    else -> {}
                }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "ICE gathering state: $p0")
            }
            override fun onIceCandidate(candidate: IceCandidate) {
                val json = JSONObject().apply {
                    put("type", "candidate")
                    put("label", candidate.sdpMLineIndex)
                    put("id", candidate.sdpMid)
                    put("candidate", candidate.sdp)
                }
                socket?.emit("ice-candidate", currentRoomId, json)
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream) {
                val ctx = applicationContext
                // Stop ringing also on stream arrival (belt-and-suspenders)
                if (ctx != null) CallForegroundService.stopRinging(ctx)
                if (stream.videoTracks.isNotEmpty()) {
                    _callState.value = _callState.value.copy(
                        remoteVideoTrack = stream.videoTracks.first(),
                        status = "In Call"
                    )
                } else {
                    _callState.value = _callState.value.copy(status = "In Voice Call")
                }
            }
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {}
        })

        val factory = peerConnectionFactory ?: return
        val mediaStream = factory.createLocalMediaStream("ARDAMS")
        localAudioTrack?.let { mediaStream.addTrack(it) }
        _callState.value.localVideoTrack?.let { mediaStream.addTrack(it) }
        peerConnection?.addStream(mediaStream)
    }

    private fun createOffer() {
        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                val json = JSONObject().apply {
                    put("type", "offer")
                    put("sdp", desc.description)
                }
                socket?.emit("offer", currentRoomId, json)
            }
        }, MediaConstraints())
    }

    private fun createAnswer() {
        peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                val json = JSONObject().apply {
                    put("type", "answer")
                    put("sdp", desc.description)
                }
                socket?.emit("answer", currentRoomId, json)
            }
        }, MediaConstraints())
    }

    fun endCall() {
        Log.d(TAG, "endCall() called")
        _callState.value = _callState.value.copy(isEnded = true, status = "Call Ended")
        GlobalCallState.endCall()

        val context = applicationContext
        if (context != null) {
            CallForegroundService.stopRinging(context) // Ensure ringtone stops on hang-up
            unregisterHeadsetReceiver(context)
            CallForegroundService.stopService(context)
            AudioRoutingManager.stopCallAudio(context)
        }

        try { socket?.disconnect() } catch (e: Exception) { e.printStackTrace() }
        try { videoCapturer?.stopCapture() } catch (e: Exception) { e.printStackTrace() }
        try { peerConnection?.close() } catch (e: Exception) { e.printStackTrace() }
        try { peerConnectionFactory?.dispose() } catch (e: Exception) { e.printStackTrace() }
        try { eglBase?.release() } catch (e: Exception) { e.printStackTrace() }
        try { surfaceTextureHelper?.dispose() } catch (e: Exception) { e.printStackTrace() }

        eglBase = null
        peerConnectionFactory = null
        peerConnection = null
        socket = null
        localVideoSource = null
        localAudioSource = null
        localAudioTrack = null
        videoCapturer = null
        surfaceTextureHelper = null
        currentRoomId = ""
    }

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String) {}
        override fun onSetFailure(s: String) {}
    }
}
