package com.example.signspeak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Official Imports from Documentation
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.ChannelMediaOptions

class VideoCallActivity : AppCompatActivity() {

    private val TAG = "AGORA_OFFICIAL"

    // ✅ YOUR APP ID
    private val appId = "3d5c76e3883047d99b7d961c3192eb74"

    // ✅ Testing Mode: Token is null
    private val token: String? = null

    private val channelName = "doctorchannel"

    private var agoraEngine: RtcEngine? = null

    // Permission Request Id
    private val PERMISSION_REQ_ID = 22

    // Official Documentation Permissions List
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        // 1. Check Permissions first
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel()
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeAndJoinChannel()
            } else {
                showToast("Permissions NOT granted. App will not work.")
            }
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            // --- Step 1: Initialize the RtcEngine ---
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler

            // Create Engine
            agoraEngine = RtcEngine.create(config)

            // Enable Video Module
            agoraEngine?.enableVideo()
            agoraEngine?.startPreview()

            // --- Step 2: Setup Local Video ---
            val container = findViewById<FrameLayout>(R.id.localVideoContainer)
            val surfaceView = SurfaceView(baseContext)

            // ✅ FIX: This line keeps your small local video ON TOP of the remote video
            surfaceView.setZOrderMediaOverlay(true)

            container.addView(surfaceView)

            // RENDER_MODE_HIDDEN means it fills the view without stretching
            agoraEngine?.setupLocalVideo(
                VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            )

            // --- Step 3: Join the Channel with Options ---
            val options = ChannelMediaOptions()
            // Set Broadcaster role (needed to send video)
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Set Channel Profile to Communication (for 1-on-1 calls)
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Auto-publish video/audio
            options.publishMicrophoneTrack = true
            options.publishCameraTrack = true

            agoraEngine?.joinChannel(token, channelName, 0, options)

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            showToast("Init Error: ${e.message}")
        }
    }

    // --- Event Handler ---
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onError(err: Int) {
            Log.e(TAG, "Agora Error Code: $err")
            // Error 101 means INVALID APP ID
            if (err == 101) {
                runOnUiThread { showToast("Error 101: Check App ID or Network!") }
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                showToast("User joined: $uid")
                setupRemoteVideo(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread { showToast("Joined Channel: $channel") }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                val container = findViewById<FrameLayout>(R.id.remoteVideoContainer)
                container.removeAllViews()
            }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remoteVideoContainer)
        if (container.childCount >= 1) {
            return
        }

        val surfaceView = SurfaceView(baseContext)
        // Note: We DO NOT set ZOrderMediaOverlay for remote video,
        // because we want it to stay in the background.
        container.addView(surfaceView)

        agoraEngine?.setupRemoteVideo(
            VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        )
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        agoraEngine=null
        }
}