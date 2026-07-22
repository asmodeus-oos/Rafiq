package com.rafiq.presentation.screen.call

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.*
import javax.inject.Inject

data class CallState(
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
    val isMuted: Boolean = false,
    val isEnded: Boolean = false,
    val status: String = "Connecting..."
)

@HiltViewModel
class ActiveCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private var eglBase: EglBase? = null
    val eglBaseContext: org.webrtc.EglBase.Context?
        get() = eglBase?.eglBaseContext

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var socket: Socket? = null

    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    // Dynamically read from navigation — unique per call pair
    private val roomId: String
        get() = savedStateHandle.get<String>("roomId") ?: "default_room"

    fun initializeWebrtc() {
        if (eglBase != null) return
        eglBase = EglBase.create()
        
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        startLocalMediaCapture()
        connectSignalingServer()
    }

    private fun startLocalMediaCapture() {
        val factory = peerConnectionFactory ?: return
        
        // Audio
        val audioConstraints = MediaConstraints()
        localAudioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", localAudioSource)

        // Video
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
            // Pointing to the Node.js signaling server hosted on Vercel/Render
            socket = IO.socket("https://rafiq-signaling.onrender.com")
            
            socket?.on(Socket.EVENT_CONNECT) {
                socket?.emit("join-room", roomId)
                _callState.value = _callState.value.copy(status = "Connected to Server")
            }

            socket?.on("user-connected") {
                // Initiator
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
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate) {
                val json = JSONObject().apply {
                    put("type", "candidate")
                    put("label", candidate.sdpMLineIndex)
                    put("id", candidate.sdpMid)
                    put("candidate", candidate.sdp)
                }
                socket?.emit("ice-candidate", roomId, json)
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream) {
                if (stream.videoTracks.isNotEmpty()) {
                    _callState.value = _callState.value.copy(
                        remoteVideoTrack = stream.videoTracks.first(),
                        status = "In Call"
                    )
                }
            }
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {}
        })

        // Add local stream
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
                socket?.emit("offer", roomId, json)
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
                socket?.emit("answer", roomId, json)
            }
        }, MediaConstraints())
    }

    fun toggleMute() {
        val isMuted = !_callState.value.isMuted
        localAudioTrack?.setEnabled(!isMuted)
        _callState.value = _callState.value.copy(isMuted = isMuted)
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun endCall() {
        _callState.value = _callState.value.copy(isEnded = true)
        socket?.disconnect()
        videoCapturer?.stopCapture()
        peerConnection?.close()
        peerConnectionFactory?.dispose()
        eglBase?.release()
        surfaceTextureHelper?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        endCall()
    }

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String) {}
        override fun onSetFailure(s: String) {}
    }
}
