package com.example.testcircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.permissionx.guolindev.PermissionX

import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testcircle.databinding.ActivityMainBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.RELAY_STATE_CONNECTING
import io.agora.rtc2.Constants.RELAY_STATE_FAILURE
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.ChannelMediaInfo
import io.agora.rtc2.video.ChannelMediaRelayConfiguration
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var handler = Handler(Looper.getMainLooper())

    //SurfaceView to render local video in a Container.
    private var localSurfaceView: SurfaceView? = null

    //SurfaceView to render Remote video in a Container.
    private var remoteSurfaceView: SurfaceView? = null

    //user1
    //1342 //1736927882
//    private var myUid = 1342
//    private var remoteUid = 543
//    private var myChannel = "1736876456"
//    private var remoteChannel = "1737203993"
//    private var myChannelToken = "00642197c8f82814db2affd4a8480dca9eeIAD7jFQ+W3vaNvDDWg+eq2DLt5w3q7rvlRTIJpU46O24ONW0m44h39v0IgDzwwAALNGNZwQAAQAcl4xnAwAcl4xnAgAcl4xnBAAcl4xn"
//    private var remoteChannelToken = "00642197c8f82814db2affd4a8480dca9eeIADE6kEQwSrPRtfVEvL6NtrwcDFQzEz6MtZyhpncAAEXr/WYjtiIbBvZIgB2qgAAqNiNZwQAAQCYnoxnAwCYnoxnAgCYnoxnBACYnoxn"

    //user2
    //543 //1737203993
     private var myUid = 543
     private var remoteUid = 1342
     private var myChannel = "1737203993"
     private var remoteChannel = "1736876456"
     private var myChannelToken = "00642197c8f82814db2affd4a8480dca9eeIADhhIw6Y2X6qaAhyUMHbwI4KR1PBQ+ZkCc0mzuaSUlBvPWYjtiIbBvZIgD6GwEAu+mNZwQAAQCrr4xnAwCrr4xnAgCrr4xnBACrr4xn"
     private var remoteChannelToken = "00642197c8f82814db2affd4a8480dca9eeIADE6kEQwSrPRtfVEvL6NtrwcDFQzEz6MtZyhpncAAEXr/WYjtiIbBvZIgB2qgAAqNiNZwQAAQCYnoxnAwCYnoxnAgCYnoxnBACYnoxn"


    val AppID = "42197c8f82814db2affd4a8480dca9ee"
    var isJoined = false

    // A toggle switch to change the User role.
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )


    private val agoraConfig: RtcEngineConfig by lazy(LazyThreadSafetyMode.NONE) {
        RtcEngineConfig().apply {
            mContext = baseContext
            mAppId = AppID
            mEventHandler = mRtcEventHandler
        }
    }

    val configuration = VideoEncoderConfiguration(
        VideoEncoderConfiguration.VD_480x360, // resolution
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, // frame rate
        VideoEncoderConfiguration.STANDARD_BITRATE, // bitrate
        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
    )

    private val agoraEngine: RtcEngine by lazy(LazyThreadSafetyMode.NONE) {
        RtcEngine.create(agoraConfig).apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            setVideoEncoderConfiguration(configuration)
            enableVideo()
            enableAudio()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askForPermission()

        binding.btnJoin.setOnClickListener {
            if(checkSelfPermission()) {
//                myChannel = binding.myChannel.text.toString()
                if (myChannel.isNotEmpty()) {
                    joinChannel()
                } else {
                    showToast("my channel is empty")
                }
            }
        }
        binding.btnRelay.setOnClickListener {
//            remoteChannel = binding.remoteChannel.text.toString()
            if (remoteChannel.isNotEmpty()) {
                startRelayPkHostChannel()
            } else {
                showToast("Relay channel is empty")
            }
        }
        binding.btnExit.setOnClickListener {
            agoraEngine.stopChannelMediaRelay()
            agoraEngine.stopPreview()
            agoraEngine.leaveChannel()
        }
        binding.btnStopRelay.setOnClickListener {
            agoraEngine.stopChannelMediaRelay()
        }
    }

    //join agora rtc channel ========================================================
    private fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            // For Live Streaming, set the channel profile as LIVE_BROADCASTING.
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.

            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.

            setupLocalVideo()

            agoraEngine.setLocalRenderMode(
                Constants.RENDER_MODE_HIDDEN,
                Constants.VIDEO_MIRROR_MODE_ENABLED
            )

            agoraEngine.startPreview()

            agoraEngine.joinChannel(myChannelToken, myChannel, myUid, options)

        } else {
            showToast("You have to give necessary user permission")
            askForPermission()
        }
    }

    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(baseContext)
        localSurfaceView?.setZOrderMediaOverlay(true)

        binding.layoutLocalVideo.removeAllViews()
        binding.layoutLocalVideo.addView(localSurfaceView)

        agoraEngine.setupLocalVideo(
            VideoCanvas(
                localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, myUid
            )
        )

        localSurfaceView?.visibility = View.VISIBLE
    }

    // get user permission if has not================================================
    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            application, REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            application, REQUESTED_PERMISSIONS[1]
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            application, REQUESTED_PERMISSIONS[2]
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun askForPermission() {
        PermissionX.init(this).permissions(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
        ).request { allGranted, _, deniedList ->
            if (allGranted) {
                // Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
            } else {
                deniedList.forEach { item ->
//                        showToast("These permissions are denied: ${deniedList.size}")
                    val str = item.split(".")

                    showToast("You have to give ${str.get(2)} permission")
                }

                // goto app info settings
                val intent = Intent()
                val packageName = packageName
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    //join pk agora rtc channel ========================================================
    private fun startRelayPkHostChannel() {

        val config = ChannelMediaRelayConfiguration()

        // Configures the source channel information. For channelName, use the source channel name set by the user. For myUid, set it as 0.
        // Note that sourceChannelToken is different from the token used for joining the source channel.
        // You need to generate sourceChannelToken with the source channel name and a uid of 0.
        val srcChannelInfo = ChannelMediaInfo(myChannel, null, 0)

        config.setSrcChannelInfo(srcChannelInfo)

        // Configures the destination channel information. Set destChannelName as the channel name set by the user.
        // myUid is the user ID that the user uses in the destination channel.
        val destChannelInfo = ChannelMediaInfo(remoteChannel, null, 0)
        config.setDestChannelInfo(remoteChannel, destChannelInfo)

        //agoraEngine!!.setDualStreamMode(Constants.SimulcastStreamMode) //setDualStreamMode()
        agoraEngine.enableDualStreamMode(true)
        agoraEngine.startOrUpdateChannelMediaRelay(config)
    }

    //agora rtc handler==============================================================
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.

        override fun onChannelMediaRelayStateChanged(state: Int, code: Int) {
            super.onChannelMediaRelayStateChanged(state, code)

            Log.d("agora_tag", "channel media Relay: state: $state code: $code")

            when (state) {
                RELAY_STATE_CONNECTING -> {
                    showToast("channel media Relay connected.")
                    Log.d("agora_tag", "channel media Relay connected.")
                }

                RELAY_STATE_FAILURE -> {
                    showToast("channel media Relay failed at error code: $code")
                    Log.d("agora_tag", "channel media Relay failed at error code: $code")
                }

                0 -> {
                    showToast("channel media Relay failed at error code: $code")
                    Log.d("agora_tag", "channel media Relay failed at error code: $code")
                }

                else -> {
                    Log.d("agora_tag", "channel media Relay: state: $state")
                }
            }
        }

        override fun onVideoStopped() {
            super.onVideoStopped()
            Log.d("agora_tag", "onVideoStopped: called")
        }

        override fun onUserJoined(uId: Int, elapsed: Int) {
            Log.d("agora_tag", "onUserJoined:  uId: $uId")

            remoteSurfaceView = SurfaceView(baseContext)
            remoteSurfaceView?.setZOrderMediaOverlay(true)

            binding.layoutRemoteVideo.removeAllViews()
            binding.layoutRemoteVideo.addView(remoteSurfaceView)

            agoraEngine.setupRemoteVideo(
                VideoCanvas(
                    remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uId
                )
            )

            remoteSurfaceView?.visibility = View.VISIBLE
        }

        override fun onJoinChannelSuccess(channel: String, uId: Int, elapsed: Int) {
            isJoined = true
            Log.d("agora_tag", "onJoinChannelSuccess: channel: $channel uid: $uId")
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
            Log.d("agora_tag", "onLeaveChannel: channel:  ${stats?.users} ") //1720414064
        }

        override fun onUserOffline(uId: Int, reason: Int) {
            Log.d("agora_tag", "onUserOffline: $uId")
        }

        override fun onRemoteAudioStateChanged(uId: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uId, state, reason, elapsed)
            Log.d("agora_tag", "onRemoteAudioStateChanged: called: state: $state")
        }

        override fun onRtcStats(stats: RtcStats?) {
            super.onRtcStats(stats)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
    }
}