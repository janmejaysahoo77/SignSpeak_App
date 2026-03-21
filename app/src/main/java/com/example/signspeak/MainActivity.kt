package com.example.signspeak

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var alarmJob: Job? = null

    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { applyLocale(it) }) // 🔥 language applied before UI loads
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Request vibration permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.VIBRATE),
            101
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Bottom set to 0 to let BottomNavigationView handle the navigation bar inset natively
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 🔥 Get user from Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Navigation setup
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // SOS button logic
        val sosButton = findViewById<ImageView>(R.id.btnSOS)
        sosButton.setOnClickListener {
            startSOSAlert()
            callEmergencyNumber()
        }

        // 🔥 Settings Button → Open SettingsActivity
        val settingsButton = findViewById<android.view.View>(R.id.btnSettings)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        // ✨ Start FAB Pulse
        val fabGlow = findViewById<android.view.View>(R.id.fabGlow)
        val pulse = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse_scale)
        fabGlow.startAnimation(pulse)
    }

    // 🔥 Removed loadUserName here (now in fragment)

    private fun startSOSAlert() {
        playSOSTone()
        vibrateDevice()
    }

    private fun playSOSTone() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sos_sound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        alarmJob = CoroutineScope(Dispatchers.Main).launch {
            delay(60_000)
            stopSOSTone()
        }
    }

    private fun stopSOSTone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 300, 500, 300)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(pattern, 0)
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(60_000)
            vibrator.cancel()
        }
    }

    private fun callEmergencyNumber() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val emergencyNumber = prefs.getString("emergency_number", "112") ?: "112"
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$emergencyNumber")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSOSTone()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
        alarmJob?.cancel()
    }

    // 🔥 Locale Handling Helper
    private fun applyLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en")!!
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
