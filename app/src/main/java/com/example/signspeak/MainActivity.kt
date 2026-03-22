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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var alarmJob: Job? = null

    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { applyLocale(it) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.VIBRATE),
            101
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)

        val drawerProfileHeader = findViewById<LinearLayout>(R.id.drawerProfileHeader)
        val drawerItemSmartBand = findViewById<LinearLayout>(R.id.drawerItemSmartBand)
        val drawerItemMedical = findViewById<LinearLayout>(R.id.drawerItemMedical)
        val drawerItemLogout = findViewById<LinearLayout>(R.id.drawerItemLogout)

        loadDrawerUserData()

        drawerProfileHeader.setOnClickListener {
            drawerLayout.closeDrawers()
            startActivity(Intent(this, UserProfileViewActivity::class.java))
        }

        drawerItemSmartBand.setOnClickListener {
            drawerLayout.closeDrawers()
            startActivity(Intent(this, DeviceActivity::class.java))
        }

        drawerItemMedical.setOnClickListener {
            drawerLayout.closeDrawers()
            val intent = Intent(this, MedicalActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        drawerItemLogout.setOnClickListener {
            drawerLayout.closeDrawers()
            auth.signOut()
            getSharedPreferences("signspeak_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnNavDrawer = findViewById<android.widget.ImageButton>(R.id.btnNavDrawer)
        btnNavDrawer.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        val sosButton = findViewById<ImageView>(R.id.btnSOS)
        sosButton.setOnClickListener {
            startSOSAlert()
            callEmergencyNumber()
        }

        val settingsButton = findViewById<android.view.View>(R.id.btnSettings)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        val fabGlow = findViewById<android.view.View>(R.id.fabGlow)
        val pulse = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse_scale)
        fabGlow.startAnimation(pulse)
    }

    override fun onResume() {
        super.onResume()
        loadDrawerUserData()
    }

    private fun loadDrawerUserData() {
        val userId = auth.currentUser?.uid ?: return
        val drawerUserName = findViewById<TextView>(R.id.drawerUserName)
        val drawerUserEmail = findViewById<TextView>(R.id.drawerUserEmail)
        val drawerProfileImage = findViewById<ImageView>(R.id.drawerProfileImage)

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    drawerUserName.text = document.getString("name") ?: "User"
                    drawerUserEmail.text = auth.currentUser?.email ?: ""

                    val imageUrl = document.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_placeholder)
                            .into(drawerProfileImage)
                        drawerProfileImage.setPadding(0, 0, 0, 0)
                    }
                }
            }
    }

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
