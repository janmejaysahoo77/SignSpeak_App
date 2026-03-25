package com.example.signspeak

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object SOSLongPressManager {

    private var mediaPlayer: MediaPlayer? = null
    private var isAlertActive = false

    fun triggerSOS(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.VIBRATE
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Toast.makeText(activity, "Permissions needed. Please grant and try again.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), 102)
            return
        }

        if (isAlertActive) return // Prevent multiple triggers

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(activity, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch Emergency Number
                val document = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val emergencyNumber = document.getString("emergency1")
                if (emergencyNumber.isNullOrEmpty()) {
                    Toast.makeText(activity, "No emergency number found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Start Alert
                startAlert(activity)

                // Get Location
                val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
                var locationLink: String? = null
                
                try {
                    val location: Location? = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        locationLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    }
                } catch (e: SecurityException) {
                }

                val message = if (locationLink != null) {
                    "HELP! I am in danger. My location: $locationLink"
                } else {
                    "HELP! I am in danger. Location unavailable."
                }

                // Send SOS SMS
                try {
                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        activity.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    if (smsManager != null) {
                        smsManager.sendTextMessage(emergencyNumber, null, message, null, null)
                    } else {
                        Toast.makeText(activity, "Failed to initialize SMS", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(activity, "SMS fails", Toast.LENGTH_SHORT).show()
                }

                // Start Emergency Call
                try {
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$emergencyNumber")
                    }
                    activity.startActivity(callIntent)
                } catch (e: SecurityException) {
                } catch (e: Exception) {
                }

            } catch (e: Exception) {
                Toast.makeText(activity, "Firebase fetch fails", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startAlert(context: Context) {
        isAlertActive = true
        mediaPlayer = MediaPlayer.create(context, R.raw.sos_sound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 300, 500, 300)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(60_000)
            stopAlert(vibrator)
        }
    }

    private fun stopAlert(vibrator: Vibrator) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator.cancel()
        isAlertActive = false
    }
}
