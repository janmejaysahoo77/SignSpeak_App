package com.example.signspeak

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {

    private var isBlinking = false

    override fun onReceive(context: Context, intent: Intent?) {

        Toast.makeText(context, "Alarm is ringing!", Toast.LENGTH_LONG).show()

        // Play alarm sound
        val ringtone = RingtoneManager.getRingtone(
            context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone.play()

        // Start flashlight blink
        startFlashBlink(context)
    }

    private fun startFlashBlink(context: Context) {
        if (isBlinking) return
        isBlinking = true

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]   // usually back flashlight

        val handler = Handler(Looper.getMainLooper())

        val blinkRunnable = object : Runnable {
            var flashOn = false

            override fun run() {
                flashOn = !flashOn
                try {
                    cameraManager.setTorchMode(cameraId, flashOn)
                } catch (_: Exception) { }

                // Continue blinking until stopped manually
                handler.postDelayed(this, 500)   // flash every 0.5 sec
            }
        }

        handler.post(blinkRunnable)
    }
}
