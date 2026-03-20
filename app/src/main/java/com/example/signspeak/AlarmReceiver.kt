package com.example.signspeak

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        Toast.makeText(context, "Alarm is ringing!", Toast.LENGTH_LONG).show()

        // Play alarm sound
        val ringtone = RingtoneManager.getRingtone(
            context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone.play()

        // Start flashlight blink
        startFlashBlink(context, ringtone)
    }

    private fun startFlashBlink(context: Context, ringtone: Ringtone) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]   // usually back flashlight

        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis()
        val alarmDurationMs = 30_000L // Auto-stop after 30 seconds

        val blinkRunnable = object : Runnable {
            var flashOn = false

            override fun run() {
                // Auto-stop after duration
                if (System.currentTimeMillis() - startTime >= alarmDurationMs) {
                    try {
                        cameraManager.setTorchMode(cameraId, false)
                    } catch (_: Exception) { }
                    ringtone.stop()
                    return
                }

                flashOn = !flashOn
                try {
                    cameraManager.setTorchMode(cameraId, flashOn)
                } catch (_: Exception) { }

                handler.postDelayed(this, 500)   // flash every 0.5 sec
            }
        }

        handler.post(blinkRunnable)
    }
}
