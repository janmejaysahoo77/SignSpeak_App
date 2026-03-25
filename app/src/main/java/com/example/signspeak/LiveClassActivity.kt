package com.example.signspeak

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.signspeak.databinding.ActivityLiveClassBinding
import com.example.signspeak.utils.AppLogger
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment

class LiveClassActivity : AppCompatActivity() {

    private val TAG = "LiveClass"
    private lateinit var binding: ActivityLiveClassBinding

    companion object {
        private const val APP_ID: Long = 1834260662L

        /** 
         * FALLBACK ACTIVATED: Zego SDK 3.x crashes with newInstanceWithToken.
         * Paste your 64-character AppSign from ZEGOCLOUD Console here.
         * Dashboard: https://console.zegocloud.com/
         */
        private const val APP_SIGN = "504705563973dfe7942af66a6b011c8371372b04b3529328ab3e227254d89363"

        /** Minimum plausible Zego token length — real tokens are 200+ chars */
        private const val MIN_TOKEN_LENGTH = 50

        /** Zego only allows alphanumeric + underscore + hyphen in IDs */
        private val SAFE_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.section(TAG, "onCreate")

        binding = ActivityLiveClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Read raw intent extras ──────────────────────────────────────────
        val rawRoomId   = intent.getStringExtra("ROOM_ID")
        val rawUserId   = intent.getStringExtra("USER_ID")
        val rawUserName = intent.getStringExtra("USER_NAME")
        val rawToken    = intent.getStringExtra("TOKEN")

        AppLogger.d(TAG, "Raw intent extras →" +
                " ROOM_ID='$rawRoomId'" +
                " USER_ID='$rawUserId'" +
                " USER_NAME='$rawUserName'" +
                " TOKEN_LEN=${rawToken?.length}")

        // ── Validate & sanitize ─────────────────────────────────────────────
        val params = buildSafeParams(rawRoomId, rawUserId, rawUserName, rawToken)
            ?: return   // buildSafeParams already called finish()

        // ── Launch ──────────────────────────────────────────────────────────
        startLiveStream(params)
    }

    // ── Data class to carry only validated values ────────────────────────────
    private data class ZegoParams(
        val roomId: String,
        val userId: String,
        val userName: String,
        val token: String
    )

    /**
     * Validates every value Zego receives.
     * Returns null + calls finish() when a critical field is invalid.
     */
    private fun buildSafeParams(
        rawRoomId: String?,
        rawUserId: String?,
        rawUserName: String?,
        rawToken: String?
    ): ZegoParams? {

        // roomId — required, alphanumeric safe
        val roomId = rawRoomId?.trim().orEmpty()
        if (roomId.isEmpty()) {
            abortWithError("Room ID is missing. Go back and try again.", "roomId null/empty")
            return null
        }
        if (!SAFE_ID_REGEX.matches(roomId)) {
            abortWithError("Room ID has invalid characters.", "roomId='$roomId' failed SAFE_ID_REGEX")
            return null
        }
        AppLogger.d(TAG, "roomId ✔  value='$roomId'")

        // userId — required, sanitize unsafe chars
        val userId = run {
            val raw = rawUserId?.trim().orEmpty()
            if (raw.isEmpty()) {
                val fallback = "uid_${System.currentTimeMillis()}"
                AppLogger.w(TAG, "userId empty — fallback='$fallback'")
                fallback
            } else {
                raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").also {
                    if (it != raw) AppLogger.w(TAG, "userId sanitized: '$raw' → '$it'")
                    else AppLogger.d(TAG, "userId ✔  value='$it'")
                }
            }
        }

        // userName — optional, fallback ok
        val userName = run {
            val raw = rawUserName?.trim().orEmpty()
            if (raw.isEmpty()) {
                val fallback = "Student_${System.currentTimeMillis()}"
                AppLogger.w(TAG, "userName empty — fallback='$fallback'")
                fallback
            } else {
                raw.take(64).also { AppLogger.d(TAG, "userName ✔  value='$it'") }
            }
        }

        // token — required, minimum length check
        val token = rawToken?.trim().orEmpty()
        if (token.isEmpty()) {
            abortWithError("Auth token missing. Please try joining again.", "token null/empty")
            return null
        }
        if (token.length < MIN_TOKEN_LENGTH) {
            abortWithError("Auth token appears invalid. Please try again.", "token too short: ${token.length} < $MIN_TOKEN_LENGTH")
            return null
        }
        AppLogger.d(TAG, "token ✔  length=${token.length}")

        return ZegoParams(roomId, userId, userName, token)
    }

    private fun startLiveStream(params: ZegoParams) {
        AppLogger.section(TAG, "startLiveStream")
        AppLogger.i(TAG, "roomId='${params.roomId}'  userId='${params.userId}'  userName='${params.userName}'  tokenLen=${params.token.length}")

        try {
            // Validate AppSign is present to prevent late NPEs
            if (APP_SIGN == "YOUR_APP_SIGN_HERE" || APP_SIGN.isEmpty()) {
                abortWithError("AppSign is missing. Please check developer console.", "APP_SIGN not configured")
                return
            }

            // Student joins strictly as audience (cannot broadcast)
            val config = ZegoUIKitPrebuiltLiveStreamingConfig.audience().apply {
                // Hardcode checks to guarantee no role escalation (Check 2)
                turnOnCameraWhenJoining = false
                turnOnMicrophoneWhenJoining = false
            }
            AppLogger.d(TAG, "audience() config explicitly restricted (cam/mic off)")

            /*
             * FALLBACK APPLIED: Using newInstance(APP_SIGN) instead of token.
             * The 3.x Zego SDK drops the Android process entirely due to native exceptions 
             * in ReportUtil when AppSign is null. 
             */
            val fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                APP_ID,
                APP_SIGN,        // Using strict AppSign instead of Token
                params.userId,
                params.userName,
                params.roomId,
                config
            )
            AppLogger.d(TAG, "newInstance() with AppSign → fragment created")

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            AppLogger.i(TAG, "Fragment committed — live stream active ✓")

        } catch (e: Exception) {
            AppLogger.e(TAG, "Zego fragment crash!", e)
            Toast.makeText(this, "Could not start live stream: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun abortWithError(userMessage: String, logDetail: String) {
        AppLogger.e(TAG, "Validation failed → $logDetail")
        Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
        finish()
    }

    // ── Lifecycle logging to catch background drops ──────────────────────────
    
    override fun onResume() {
        super.onResume()
        AppLogger.v(TAG, "onResume() — stream foregrounded")
    }

    override fun onPause() {
        super.onPause()
        AppLogger.w(TAG, "onPause() — activity backgrounded (stream may disconnect depending on OS)")
    }
    
    override fun onStop() {
        super.onStop()
        AppLogger.w(TAG, "onStop() — activity stopped")
    }

    override fun onDestroy() {
        AppLogger.w(TAG, "onDestroy() — LiveClassActivity destroying, stream disconnecting")
        super.onDestroy()
    }
}
