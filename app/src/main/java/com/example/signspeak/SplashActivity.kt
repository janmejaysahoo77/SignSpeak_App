package com.example.signspeak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the splash screen truly fullscreen (no status/navigation bar)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        val logoLayout = findViewById<View>(R.id.logoLayout)

        // Initial state for animation
        logoLayout.translationY = 100f
        logoLayout.scaleX = 0.8f
        logoLayout.scaleY = 0.8f

        // Cool modern animation
        logoLayout.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1200)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .withEndAction {
                // Wait slightly after animation before navigating
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    navigateToSignup()
                }, 800)
            }
            .start()
    }

    private fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }
}
