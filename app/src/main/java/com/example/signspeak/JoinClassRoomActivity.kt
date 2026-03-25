package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.signspeak.api.ZegoTokenApi
import com.example.signspeak.databinding.ActivityJoinClassroomBinding
import com.example.signspeak.utils.AppLogger
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class JoinClassRoomActivity : AppCompatActivity() {

    private val TAG = "JoinClass"
    private lateinit var binding: ActivityJoinClassroomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.section(TAG, "onCreate")

        binding = ActivityJoinClassroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill user name from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        AppLogger.d(TAG, "Current Firebase user: uid=${currentUser?.uid}, name=${currentUser?.displayName}")

        currentUser?.displayName?.let { name ->
            if (name.isNotEmpty()) {
                binding.etUserName.setText(name)
                AppLogger.d(TAG, "Pre-filled userName = $name")
            }
        }

        binding.btnBack.setOnClickListener {
            AppLogger.d(TAG, "Back button pressed")
            finish()
        }

        binding.btnJoinClass.setOnClickListener {
            AppLogger.d(TAG, "Join button pressed")
            joinClass()
        }
    }

    private fun joinClass() {
        AppLogger.section(TAG, "joinClass()")

        val roomId = binding.etRoomId.text.toString().trim()
        val userName = binding.etUserName.text.toString().trim().ifEmpty {
            "Student_${(1000..9999).random()}"
        }

        AppLogger.d(TAG, "roomId='$roomId'  userName='$userName'")

        if (roomId.isEmpty()) {
            AppLogger.w(TAG, "Room ID is empty — aborting")
            binding.etRoomId.error = "Room ID is required"
            binding.etRoomId.requestFocus()
            return
        }

        // Generate a unique user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: UUID.randomUUID().toString().take(8)
        AppLogger.d(TAG, "userId='$userId' (from Firebase=${currentUser != null})")

        setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            AppLogger.i(TAG, "Calling ZegoTokenApi.fetchToken ...")

            val result = try {
                ZegoTokenApi.fetchToken(roomId, userId, userName)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Unexpected exception during fetchToken", e)
                setLoading(false)
                showError("Unexpected error: ${e.message}")
                return@launch
            }

            setLoading(false)

            if (result.error != null) {
                AppLogger.e(TAG, "Token fetch failed: ${result.error}")
                showError("Error: ${result.error}")
                return@launch
            }

            if (result.token.isEmpty()) {
                AppLogger.e(TAG, "Token is empty even though no error was returned!")
                showError("Received an empty token. Please try again.")
                return@launch
            }

            AppLogger.i(TAG, "Token received successfully (length=${result.token.length})")
            AppLogger.d(TAG, "Navigating to LiveClassActivity → roomId=$roomId  userId=$userId  userName=$userName")

            // Navigate to LiveClassActivity
            val intent = Intent(this@JoinClassRoomActivity, LiveClassActivity::class.java).apply {
                putExtra("ROOM_ID", roomId)
                putExtra("USER_ID", userId)
                putExtra("USER_NAME", userName)
                putExtra("TOKEN", result.token)
            }
            startActivity(intent)
        }
    }

    private fun showError(msg: String) {
        AppLogger.w(TAG, "Showing error to user: $msg")
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(loading: Boolean) {
        AppLogger.d(TAG, "setLoading($loading)")
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnJoinClass.isEnabled = !loading
        binding.btnJoinClass.alpha = if (loading) 0.5f else 1f
        binding.etRoomId.isEnabled = !loading
        binding.etUserName.isEnabled = !loading
    }
}
